package com.altnoir.mementoinabyss.infrastructure.worldgen.layer2;

import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeSelection;
import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.api.worldgen.TheAbyssGeneratorSetupEvent;
import com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime;
import com.altnoir.mementoinabyss.infrastructure.worldgen.C2meDensityFunctionCompat;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.Layer2BiomeSampler;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.TheAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.Layer2DensityFunctions;
import com.altnoir.mementoinabyss.mixin.worldgen.ChunkGeneratorAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public final class Layer2Runtime {
    private static final Map<RandomState, State> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void install(ServerLevel level, ChunkGenerator generator, RandomState random) {
        if (!level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL) || STATES.containsKey(random))
            return;
        Layer2Layout[] found = new Layer2Layout[1];
        var router = C2meDensityFunctionCompat.originalRouter(random.router());
        router.finalDensity()
                .mapAll(
                        f -> {
                            if (f instanceof Layer2Layout l) found[0] = l;
                            return f;
                        });
        if (found[0] == null) return;
        if (!found[0].bound()) {
            // C2ME retains a source router from before the normal world-seed binding visitor.
            router = AbyssSeamRuntime.bindRouter(router, level.getSeed());
            ((RandomStateAccessor) (Object) random).mia$setRouter(router);
            router.finalDensity()
                    .mapAll(
                            f -> {
                                if (f instanceof Layer2Layout l) found[0] = l;
                                return f;
                            });
        }
        var layout = found[0];
        if (!layout.bound()) throw new IllegalStateException("Unbound layer 2 layout");
        if (generator.getBiomeSource() instanceof TheAbyssBiomeSource configured)
            setBiomeSource(
                    generator,
                    configured.bind(
                            layout,
                            random.sampler(),
                            router,
                            level.registryAccess().lookupOrThrow(Layer2Biomes.REGISTRY_KEY)));
        var functions = level.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION);
        var binder =
                new DensityFunction.Visitor() {
                    @Override
                    public DensityFunction apply(DensityFunction f) {
                        return f instanceof Layer2Layout ? layout : f;
                    }

                    @Override
                    public DensityFunction.NoiseHolder visitNoise(
                            DensityFunction.NoiseHolder noise) {
                        return new DensityFunction.NoiseHolder(
                                noise.noiseData(),
                                random.getOrCreateNoise(
                                        noise.noiseData().unwrapKey().orElseThrow()));
                    }
                };
        STATES.put(
                random,
                new State(
                        layout,
                        generator.getBiomeSource() instanceof TheAbyssBiomeSource source
                                ? source.layer2Sampler()
                                : null,
                        functions
                                .getOrThrow(Layer2DensityFunctions.SURFACE_BASE)
                                .value()
                                .mapAll(binder),
                        functions
                                .getOrThrow(Layer2DensityFunctions.ROOF_BASE)
                                .value()
                                .mapAll(binder),
                        functions
                                .getOrThrow(Layer2DensityFunctions.SUPPORT)
                                .value()
                                .mapAll(binder)));
        NeoForge.EVENT_BUS.post(
                new TheAbyssGeneratorSetupEvent(level, generator, random, STATES.get(random)));
    }

    public static void setBiomeSource(ChunkGenerator generator, BiomeSource source) {
        var access = (ChunkGeneratorAccessor) (Object) generator;
        access.mia$setBiomeSource(source);
        var settings = access.mia$generationSettingsGetter();
        access.mia$setFeaturesPerStep(
                Lazy.of(
                        () ->
                                FeatureSorter.buildFeaturesPerStep(
                                        List.copyOf(source.possibleBiomes()),
                                        biome -> settings.apply(biome).features(),
                                        true)));
    }

    public static @Nullable State state(RandomState random) {
        return STATES.get(random);
    }

    public static final class State {
        private static final DensityFunction AQUIFER_Y =
                DensityFunctions.yClampedGradient(
                        DimensionType.MIN_Y * 2,
                        DimensionType.MAX_Y * 2,
                        DimensionType.MIN_Y * 2,
                        DimensionType.MAX_Y * 2);
        private final Layer2Layout layout;
        private final @Nullable Layer2BiomeSampler biomes;
        private final DensityFunction surfaceBase, roofBase, support;
        private final Aquifer.FluidStatus water, empty;
        private final double openingSquared, waterOpeningSquared;
        private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Column>> columns =
                ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);

        private State(
                Layer2Layout layout,
                @Nullable Layer2BiomeSampler biomes,
                DensityFunction surfaceBase,
                DensityFunction roofBase,
                DensityFunction support) {
            this.layout = layout;
            this.biomes = biomes;
            this.surfaceBase = surfaceBase;
            this.roofBase = roofBase;
            this.support = support;
            this.water =
                    new Aquifer.FluidStatus(layout.waterLevel(), Blocks.WATER.defaultBlockState());
            // Aquifer pressure sums integer levels before converting to double.
            this.empty =
                    new Aquifer.FluidStatus(
                            DimensionType.WAY_BELOW_MIN_Y, Blocks.AIR.defaultBlockState());
            double waterOpening = layout.openingRadius() + 64;
            this.waterOpeningSquared = waterOpening * waterOpening;
            this.openingSquared = layout.openingRadius() * layout.openingRadius();
        }

        public Layer2Layout layout() {
            return layout;
        }

        public Layer2BiomeSelection biomeSelection(int x, int z) {
            if (biomes == null)
                throw new IllegalStateException("Layer2 biome sampler is not installed");
            return biomes.query(x, z);
        }

        public Column column(int x, int z) {
            var values = columns.get();
            long key = ((long) x << 32) ^ (z & 0xffffffffL);
            var cached = values.getAndMoveToLast(key);
            if (cached != null) return cached;
            var point = new DensityFunction.SinglePointContext(x, 0, z);
            double floor = surfaceBase.compute(point);
            double ceiling = roofBase.compute(point);
            var result = new Column(floor, ceiling, support.compute(point) > 1);
            if (values.size() >= 2048) values.removeFirst();
            values.putAndMoveToLast(key, result);
            return result;
        }

        public double surfaceHeight(int x, int z) {
            return column(x, z).height();
        }

        public double roofHeight(int x, int z) {
            return column(x, z).roof();
        }

        public boolean pillarCore(int x, int z) {
            return column(x, z).pillar();
        }

        public record Column(double height, double roof, boolean pillar) {}

        public boolean dry(int x, int y, int z) {
            return y < 0 && (y < layout.minWaterY() || y >= layout.waterLevel() || opening(x, z));
        }

        public boolean opening(int x, int z) {
            return (double) x * x + (double) z * z <= openingSquared;
        }

        public NoiseRouter aquiferRouter(NoiseRouter router) {
            // Layer 2 Depth describes biome layers, not vanilla's deep-dark water exclusion.
            // Include neighbouring aquifer cell centres while leaving biome sampling unchanged.
            var depth =
                    DensityFunctions.rangeChoice(
                            AQUIFER_Y,
                            layout.minWaterY() - 48,
                            0,
                            DensityFunctions.zero(),
                            router.depth());
            return new NoiseRouter(
                    router.barrierNoise(),
                    router.fluidLevelFloodednessNoise(),
                    router.fluidLevelSpreadNoise(),
                    router.lavaNoise(),
                    router.temperature(),
                    router.vegetation(),
                    router.continents(),
                    router.erosion(),
                    depth,
                    router.ridges(),
                    router.preliminarySurfaceLevel(),
                    router.finalDensity(),
                    router.veinToggle(),
                    router.veinRidged(),
                    router.veinGap());
        }

        public Aquifer.FluidPicker picker(Aquifer.FluidPicker original) {
            // Vanilla selects lava below a negative sea level before its lava density is queried.
            return (x, y, z) ->
                    y >= 0
                            ? original.computeFluid(x, y, z)
                            : y < layout.minWaterY()
                                            || (double) x * x + (double) z * z
                                                    <= waterOpeningSquared
                                    ? empty
                                    : water;
        }
    }

    private Layer2Runtime() {}
}
