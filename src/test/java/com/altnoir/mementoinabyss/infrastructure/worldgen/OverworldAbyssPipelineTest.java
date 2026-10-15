package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCaveGuard;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssClimate;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssInterpolated;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSlide;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSloped;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSurface;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssTerrain;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssPipelineTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(320, -64, 320, 12345, 1, 1.25);
    private static HolderLookup.Provider registries;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
    }

    @Test
    void realVanillaGraphAdaptsBeforeProcessingAndReusesItsSeededBase() {
        var settings =
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                        .value();
        var state =
                RandomState.create(
                        settings, registries.lookupOrThrow(Registries.NOISE), SHAPE.seed());
        DensityFunction original = state.router().finalDensity();
        Set<BlendedNoise> bases = Collections.newSetFromMap(new IdentityHashMap<>());
        original.mapAll(
                function -> {
                    if (function instanceof BlendedNoise base) bases.add(base);
                    return function;
                });
        assertEquals(1, bases.size());
        var result =
                OverworldAbyssPipeline.build(
                        original,
                        SHAPE,
                        settings.noiseSettings().getCellWidth(),
                        settings.noiseSettings().getCellHeight());
        assertEquals("before_vanilla_interpolation", result.path());
        AtomicInteger adapted = new AtomicInteger();
        result.terrain()
                .mapAll(
                        function -> {
                            if (function instanceof OverworldAbyssSloped terrain) {
                                adapted.incrementAndGet();
                                assertSame(bases.iterator().next(), terrain.baseNoise());
                            }
                            return function;
                        });
        assertTrue(adapted.get() >= 1, "Every reused sloped-cheese occurrence must adapt");
        // Replacing the completed cave field used to erase these branches. They must still be
        // traversable after changing only the terrain input consumed by vanilla's cave logic.
        var caveKeys = new java.util.HashSet<>();
        result.terrain()
                .mapAll(
                        new DensityFunction.Visitor() {
                            @Override
                            public DensityFunction apply(DensityFunction function) {
                                return function;
                            }

                            @Override
                            public DensityFunction.NoiseHolder visitNoise(
                                    DensityFunction.NoiseHolder noise) {
                                noise.noiseData().unwrapKey().ifPresent(caveKeys::add);
                                return noise;
                            }
                        });
        assertTrue(caveKeys.contains(Noises.NOODLE), "Noodle branch must survive");
        assertTrue(caveKeys.contains(Noises.CAVE_CHEESE), "Cheese cave branch must survive");
        assertTrue(caveKeys.contains(Noises.SPAGHETTI_2D), "Spaghetti cave branch must survive");
    }

    @Test
    void vanillaRouterWithoutC2meKeepsItsOriginalDensityGraph() {
        var settings =
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                        .value();
        var state =
                RandomState.create(
                        settings, registries.lookupOrThrow(Registries.NOISE), SHAPE.seed());

        assertSame(
                state.router().finalDensity(),
                C2meDensityFunctionCompat.originalFinalDensity(state.router()));
    }

    @Test
    void packagedOverworldJsonUsesTheSamePipelineAsProgrammaticVanilla() throws Exception {
        var ops = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, registries);
        for (String name : new String[] {"overworld", "large_biomes", "amplified"})
            try (var stream =
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream(
                                    "data/minecraft/worldgen/noise_settings/" + name + ".json")) {
                assertNotNull(stream);
                var json =
                        com.google.gson.JsonParser.parseReader(
                                new java.io.InputStreamReader(
                                        stream, java.nio.charset.StandardCharsets.UTF_8));
                var settings = NoiseGeneratorSettings.DIRECT_CODEC.parse(ops, json).getOrThrow();
                var wrappedStage = new java.util.concurrent.atomic.AtomicBoolean();
                settings.noiseRouter()
                        .finalDensity()
                        .mapAll(
                                function -> {
                                    if (function instanceof DensityFunctions.MarkerOrMarked marker
                                            && marker.wrapped()
                                                    instanceof DensityFunctions.HolderHolder holder
                                            && holder.function().value()
                                                    instanceof
                                                    com.altnoir.mementoinabyss.mixin.worldgen
                                                            .BlendDensityAccessor)
                                        wrappedStage.set(true);
                                    return function;
                                });
                assertTrue(
                        wrappedStage.get(),
                        "Fixture must retain the datapack's wrapped blend stage");
                var state =
                        RandomState.create(
                                settings,
                                registries.lookupOrThrow(Registries.NOISE),
                                -3641258292459704473L);
                var result =
                        OverworldAbyssPipeline.build(
                                state.router().finalDensity(),
                                SHAPE,
                                settings.noiseSettings().getCellWidth(),
                                settings.noiseSettings().getCellHeight());
                assertEquals(
                        "before_vanilla_interpolation",
                        result.path(),
                        "The graph loaded from the actual vanilla datapack must not use the"
                                + " fallback: "
                                + name);
            }
    }

    @Test
    void customMultiplierAndMalformedProcessingUseConservativeFallback() {
        BlendedNoise base =
                BlendedNoise.createUnseeded(0.25, 0.125, 80, 160, 8)
                        .withNewRandom(RandomSource.create(SHAPE.seed()));
        DensityFunction stage = DensityFunctions.interpolated(DensityFunctions.blendDensity(base));
        DensityFunction vanilla =
                DensityFunctions.mul(stage, DensityFunctions.constant(0.64)).squeeze();
        // A familiar post-process alone is insufficient: this fixture has no sloped-cheese node.
        for (DensityFunction custom :
                new DensityFunction[] {
                    vanilla,
                    DensityFunctions.mul(stage, DensityFunctions.constant(7)),
                    DensityFunctions.mul(stage, DensityFunctions.constant(0.5)).squeeze(),
                    DensityFunctions.mul(stage, DensityFunctions.constant(0.64)),
                    vanilla.squeeze(),
                    DensityFunctions.add(vanilla, stage),
                    DensityFunctions.min(vanilla, vanilla)
                }) {
            var result = OverworldAbyssPipeline.build(custom, SHAPE, 4, 8);
            assertEquals("independent_processed_branch", result.path());
            var seam = assertInstanceOf(
                    com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity.class,
                    result.terrain());
            var cliff = assertInstanceOf(
                    com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCliff.class,
                    seam.input());
            var finalBlend = assertInstanceOf(OverworldAbyssTerrain.class, cliff.input());
            assertSame(custom, finalBlend.input());
            assertTrue(finalBlend.finalBlend());
            for (int[] point : new int[][] {{1040, 63, 0}, {-1200, 80, 23}, {37, -40, 1200}}) {
                var context = new DensityFunction.SinglePointContext(point[0], point[1], point[2]);
                assertEquals(custom.compute(context), result.terrain().compute(context));
            }
        }
    }

    @Test
    void densityFunctionWithoutCodecFallsBackInsteadOfCrashing() {
        DensityFunction noCodec =
                new DensityFunction.SimpleFunction() {
                    @Override
                    public double compute(FunctionContext context) {
                        return 0;
                    }

                    @Override
                    public double minValue() {
                        return 0;
                    }

                    @Override
                    public double maxValue() {
                        return 0;
                    }

                    @Override
                    public net.minecraft.util.KeyDispatchDataCodec<? extends DensityFunction>
                            codec() {
                        throw new UnsupportedOperationException();
                    }
                };

        var result = OverworldAbyssPipeline.build(noCodec, SHAPE, 4, 8);

        assertEquals("independent_processed_branch", result.path());
        assertEquals(
                0,
                result.terrain().compute(new DensityFunction.SinglePointContext(1200, 64, 0)),
                1.0E-10);
    }

    @Test
    void latticeMatchesAnalyticTrilinearFieldAtPositiveAndNegativeCoordinates() {
        var grid = new OverworldAbyssInterpolated(new Analytic(), 4, 8);
        for (int x : new int[] {-9, -4, -1, 0, 1, 4, 9})
            for (int y : new int[] {-17, -8, -1, 0, 3, 8, 17})
                for (int z : new int[] {-5, -1, 0, 2, 7}) {
                    var context = new DensityFunction.SinglePointContext(x, y, z);
                    assertEquals(analytic(x, y, z), grid.compute(context), 1.0E-10);
                }
    }

    @Test
    void consecutiveVerticalCellsReuseFacesWithoutMovingTheField() {
        AtomicInteger samples = new AtomicInteger();
        var grid =
                new OverworldAbyssInterpolated(
                        new Analytic() {
                            @Override
                            public double compute(FunctionContext context) {
                                samples.incrementAndGet();
                                return super.compute(context);
                            }
                        },
                        4,
                        8);
        int visited = 0;
        for (int y : new int[] {17, 9, 1, -7, -15, -7, 1, 9}) {
            assertEquals(
                    analytic(-3, y, 2),
                    grid.compute(new Context(-3, y, 2, Blender.empty())),
                    1e-10);
            assertEquals(8 + 4 * visited++, samples.get());
        }
    }

    @Test
    void newDensityCodecsRoundTripAndLegacyDefaultsStillDecode() {
        var terrain =
                new OverworldAbyssTerrain(
                        DensityFunctions.constant(0.2),
                        DensityFunctions.constant(0.1),
                        SHAPE,
                        false);
        var json =
                OverworldAbyssTerrain.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, terrain)
                        .getOrThrow();
        json.getAsJsonObject().remove("local_only");
        json.getAsJsonObject().remove("final_blend");
        var decoded =
                OverworldAbyssTerrain.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, json)
                        .getOrThrow();
        for (int x : new int[] {0, 336, 640, 984, 1040})
            assertEquals(
                    terrain.compute(new Context(x, 192, 0, Blender.empty())),
                    decoded.compute(new Context(x, 192, 0, Blender.empty())));
        var grid =
                new OverworldAbyssInterpolated(
                        DensityFunctions.yClampedGradient(-64, 320, 1, -1), 4, 8);
        var gridJson =
                OverworldAbyssInterpolated.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, grid)
                        .getOrThrow();
        var gridDecoded =
                OverworldAbyssInterpolated.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, gridJson)
                        .getOrThrow();
        assertEquals(
                grid.compute(new Context(-3, 137, 2, Blender.empty())),
                gridDecoded.compute(new Context(-3, 137, 2, Blender.empty())));
        var surface = new OverworldAbyssSurface(DensityFunctions.constant(264));
        var surfaceJson =
                OverworldAbyssSurface.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, surface)
                        .getOrThrow();
        var surfaceDecoded =
                OverworldAbyssSurface.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, surfaceJson)
                        .getOrThrow();
        assertEquals(264, surfaceDecoded.compute(new Context(640, 0, 0, Blender.empty())));
    }

    @Test
    void slopedSlideAndCaveGuardCodecsRoundTripConstantSubgraphs() {
        var sloped =
                new OverworldAbyssSloped(
                        DensityFunctions.constant(0.25),
                        DensityFunctions.constant(0.5),
                        DensityFunctions.constant(264),
                        SHAPE,
                        DensityFunctions.constant(5));
        var slopedJson =
                OverworldAbyssSloped.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, sloped)
                        .getOrThrow();
        var slopedDecoded =
                OverworldAbyssSloped.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, slopedJson)
                        .getOrThrow();
        assertEquals(
                sloped.compute(new Context(640, 200, 0, Blender.empty())),
                slopedDecoded.compute(new Context(640, 200, 0, Blender.empty())));

        var slide = new OverworldAbyssSlide(DensityFunctions.constant(0.2), SHAPE);
        var slideJson =
                OverworldAbyssSlide.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, slide)
                        .getOrThrow();
        var slideDecoded =
                OverworldAbyssSlide.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, slideJson)
                        .getOrThrow();
        assertEquals(
                slide.compute(new Context(400, 300, 0, Blender.empty())),
                slideDecoded.compute(new Context(400, 300, 0, Blender.empty())));

        var guard =
                new OverworldAbyssCaveGuard(
                        DensityFunctions.constant(-0.25),
                        DensityFunctions.constant(0.75),
                        DensityFunctions.constant(0),
                        SHAPE);
        var guardJson =
                OverworldAbyssCaveGuard.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, guard)
                        .getOrThrow();
        var guardDecoded =
                OverworldAbyssCaveGuard.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, guardJson)
                        .getOrThrow();
        assertEquals(
                guard.compute(new Context(400, 200, 0, Blender.empty())),
                guardDecoded.compute(new Context(400, 200, 0, Blender.empty())));
        assertEquals(
                guard.compute(new Context(4000, 200, 0, Blender.empty())),
                guardDecoded.compute(new Context(4000, 200, 0, Blender.empty())));
    }

    @Test
    void aquiferRouterUnwrapsOnlyMatchingClimateRootsWithoutSamplingTerrainSurface() {
        var vanillaErosion = DensityFunctions.constant(0.4);
        var vanillaDepth = DensityFunctions.constant(-0.2);
        AtomicInteger surfaceSamples = new AtomicInteger();
        DensityFunction terrainSurface =
                new Analytic() {
                    @Override
                    public double compute(FunctionContext context) {
                        surfaceSamples.incrementAndGet();
                        return 256;
                    }
                };
        var erosion =
                new OverworldAbyssClimate(
                        vanillaErosion, SHAPE, OverworldAbyssClimate.Component.EROSION);
        var depth =
                new OverworldAbyssClimate(
                        vanillaDepth,
                        SHAPE,
                        OverworldAbyssClimate.Component.DEPTH,
                        Optional.of(terrainSurface));
        NoiseRouter router = routerWith(erosion, depth);

        NoiseRouter aquiferRouter = OverworldAbyssAquiferRouter.forAquifer(router);

        assertSame(vanillaErosion, aquiferRouter.erosion());
        assertSame(vanillaDepth, aquiferRouter.depth());
        var context = new Context(400, 200, 0, Blender.empty());
        assertEquals(vanillaErosion.compute(context), aquiferRouter.erosion().compute(context));
        assertEquals(vanillaDepth.compute(context), aquiferRouter.depth().compute(context));
        assertEquals(0, surfaceSamples.get(), "Aquifer climate must not scan the composed surface");
        assertSame(router.barrierNoise(), aquiferRouter.barrierNoise());
        assertSame(router.fluidLevelFloodednessNoise(), aquiferRouter.fluidLevelFloodednessNoise());
        assertSame(router.fluidLevelSpreadNoise(), aquiferRouter.fluidLevelSpreadNoise());
        assertSame(router.lavaNoise(), aquiferRouter.lavaNoise());
        assertSame(router.temperature(), aquiferRouter.temperature());
        assertSame(router.vegetation(), aquiferRouter.vegetation());
        assertSame(router.continents(), aquiferRouter.continents());
        assertSame(router.ridges(), aquiferRouter.ridges());
        assertSame(router.preliminarySurfaceLevel(), aquiferRouter.preliminarySurfaceLevel());
        assertSame(router.finalDensity(), aquiferRouter.finalDensity());
        assertSame(router.veinToggle(), aquiferRouter.veinToggle());
        assertSame(router.veinRidged(), aquiferRouter.veinRidged());
        assertSame(router.veinGap(), aquiferRouter.veinGap());

        DensityFunction otherErosion =
                new OverworldAbyssClimate(
                        vanillaErosion, SHAPE, OverworldAbyssClimate.Component.DEPTH);
        DensityFunction otherDepth =
                new OverworldAbyssClimate(
                        vanillaDepth, SHAPE, OverworldAbyssClimate.Component.EROSION);
        NoiseRouter unknownRouter =
                OverworldAbyssAquiferRouter.forAquifer(routerWith(otherErosion, otherDepth));
        assertSame(otherErosion, unknownRouter.erosion());
        assertSame(otherDepth, unknownRouter.depth());

        DensityFunction erosionClimate =
                new OverworldAbyssClimate(
                        vanillaErosion, SHAPE, OverworldAbyssClimate.Component.EROSION);
        DensityFunction depthClimate =
                new OverworldAbyssClimate(
                        vanillaDepth,
                        SHAPE,
                        OverworldAbyssClimate.Component.DEPTH,
                        Optional.of(terrainSurface));
        DensityFunction wrappedErosion =
                DensityFunctions.add(erosionClimate, DensityFunctions.constant(0.1));
        DensityFunction wrappedDepth =
                DensityFunctions.add(depthClimate, DensityFunctions.constant(0.1));
        NoiseRouter wrappedRouter =
                OverworldAbyssAquiferRouter.forAquifer(routerWith(wrappedErosion, wrappedDepth));
        assertSame(wrappedErosion, wrappedRouter.erosion());
        assertSame(wrappedDepth, wrappedRouter.depth());
        assertEquals(0, surfaceSamples.get(), "Unwrapping must not evaluate terrain surface");
    }

    private static NoiseRouter routerWith(DensityFunction erosion, DensityFunction depth) {
        var zero = DensityFunctions.constant(0);
        return new NoiseRouter(
                zero, zero, zero, zero, zero, zero, zero, erosion, depth, zero, zero, zero, zero,
                zero, zero);
    }

    @Test
    void latticePropagatesBlenderAndInvalidatesSameCellCacheWhenItChanges() throws Exception {
        var constructor =
                Blender.class.getDeclaredConstructor(
                        Long2ObjectOpenHashMap.class, Long2ObjectOpenHashMap.class);
        constructor.setAccessible(true);
        Blender other =
                constructor.newInstance(
                        new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>());
        AtomicInteger samples = new AtomicInteger();
        DensityFunction input =
                new Analytic() {
                    @Override
                    public double compute(FunctionContext context) {
                        samples.incrementAndGet();
                        return context.getBlender() == other ? 20 : 10;
                    }
                };
        var grid = new OverworldAbyssInterpolated(input, 4, 8);
        assertEquals(10, grid.compute(new Context(1, 2, 3, Blender.empty())));
        assertEquals(10, grid.compute(new Context(2, 3, 2, Blender.empty())));
        assertEquals(8, samples.get());
        assertEquals(20, grid.compute(new Context(1, 2, 3, other)));
        assertEquals(16, samples.get());
        assertEquals(20, grid.withBlender(other).compute(new Context(1, 2, 3, Blender.empty())));
        assertEquals(24, samples.get());
    }

    private static double analytic(int x, int y, int z) {
        return 2 * x
                + 3 * y
                - 5 * z
                + 0.25 * x * y
                + 0.5 * y * z
                - 0.75 * x * z
                + 0.125 * x * y * z;
    }

    private static class Analytic implements DensityFunction.SimpleFunction {
        @Override
        public double compute(FunctionContext context) {
            return analytic(context.blockX(), context.blockY(), context.blockZ());
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.zero().codec();
        }
    }

    private record Context(int blockX, int blockY, int blockZ, Blender blender)
            implements DensityFunction.FunctionContext {
        @Override
        public Blender getBlender() {
            return blender;
        }
    }
}
