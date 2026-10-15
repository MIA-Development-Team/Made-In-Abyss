package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssGenerationTest {
    private static HolderLookup.Provider registries;
    private static NoiseBasedChunkGenerator generator;
    private static Registry<Biome> biomes;
    private static PalettedContainerFactory palettes;
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        var mutableBiomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        registries
                .lookupOrThrow(Registries.BIOME)
                .listElements()
                .forEach(
                        holder ->
                                mutableBiomes.register(
                                        holder.key(), holder.value(), RegistrationInfo.BUILT_IN));
        biomes = mutableBiomes.freeze();
        palettes =
                PalettedContainerFactory.create(
                        new RegistryAccess.ImmutableRegistryAccess(List.of(biomes)));
        generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                registries
                                        .lookupOrThrow(Registries.BIOME)
                                        .getOrThrow(Biomes.PLAINS)),
                        registries
                                .lookupOrThrow(Registries.NOISE_SETTINGS)
                                .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        // Force transformation of every integration target, not just the density functions.
        Class.forName("net.minecraft.server.level.ChunkMap");
        Class.forName("net.minecraft.server.MinecraftServer");
        Class.forName("net.minecraft.world.level.levelgen.SurfaceSystem");
    }

    private static RandomState state(long seed, boolean modified) {
        var state =
                RandomState.create(
                        generator.generatorSettings().value(),
                        registries.lookupOrThrow(Registries.NOISE),
                        seed);
        if (modified)
            ((RandomStateAccessor) (Object) state)
                    .mia$setRouter(
                            OverworldAbyss.compose(
                                    state,
                                    state.router(),
                                    new OverworldAbyssShape(160, -64, 320, seed, 2.0, 1.25)));
        return state;
    }

    @Test
    void realNoiseChunkHasDryOpenCenterAndSolidRim() {
        for (long seed : new long[] {0, 12345, -85217}) {
            var state = state(seed, true);
            for (int x : new int[] {-160, -80, 0, 80, 160}) {
                var column = generator.getBaseColumn(x, 0, HEIGHT, state);
                for (int y = -64; y < 320; y++)
                    assertTrue(
                            column.getBlock(y).isAir(), "seed=" + seed + ", x=" + x + ", y=" + y);
            }
            for (int x : new int[] {-320, 320}) {
                int y =
                        generator.getBaseHeight(
                                x, 0, Heightmap.Types.OCEAN_FLOOR_WG, HEIGHT, state);
                assertTrue(y >= 212 && y < 320, "noisy rim height=" + y);
                // The report names the block the rock stops at: the block it reports is solid in
                // the
                // generator's own column and the block above it is not.
                var shape = new OverworldAbyssShape(160, -64, 320, seed, 2.0, 1.25);
                double preliminary =
                        state.router()
                                .preliminarySurfaceLevel()
                                .compute(new DensityFunction.SinglePointContext(x, 0, 0));
                assertEquals(y - 1, preliminary, "actual noisy surface, seed=" + seed + ", x=" + x);
                assertTrue(Math.abs(preliminary - shape.innerHeight(x, 0)) <= 13);
                var rim = generator.getBaseColumn(x, 0, HEIGHT, state);
                int reported = (int) preliminary;
                assertFalse(
                        rim.getBlock(reported).isAir(),
                        "rock at the report, seed=" + seed + ", x=" + x);
                assertTrue(
                        rim.getBlock(reported + 1).isAir(),
                        "air above the report, seed=" + seed + ", x=" + x);
            }
        }
    }

    @Test
    void realColumnsOutsideBoundaryAreUnchanged() {
        var original = state(12345, false);
        var modified = state(12345, true);
        assertSame(
                original.router().temperature().codec(), modified.router().temperature().codec());
        // The first two sit one block outside the influence radius, where the carve is switched off
        // entirely; the last two are well outside it.
        for (int[] point : new int[][] {{521, 0}, {0, -521}, {640, 0}, {2048, -1024}}) {
            var before = generator.getBaseColumn(point[0], point[1], HEIGHT, original);
            var after = generator.getBaseColumn(point[0], point[1], HEIGHT, modified);
            for (int y = -64; y < 320; y++)
                assertEquals(
                        before.getBlock(y),
                        after.getBlock(y),
                        "x=" + point[0] + ", z=" + point[1] + ", y=" + y);
        }
    }

    @Test
    void composingRouterPreservesClimateOutsideAndAquiferOreInputsAndOriginalGraph() {
        var host = state(12345, false);
        var original = host.router();
        var composed =
                OverworldAbyss.compose(
                        host, original, new OverworldAbyssShape(160, -64, 320, 12345, 2.0, 1.25));
        assertSame(original.temperature(), composed.temperature());
        assertSame(original.vegetation(), composed.vegetation());
        var outside = new DensityFunction.SinglePointContext(1000, 200, -1000);
        assertEquals(
                original.continents().compute(outside), composed.continents().compute(outside));
        assertEquals(original.erosion().compute(outside), composed.erosion().compute(outside));
        assertEquals(original.depth().compute(outside), composed.depth().compute(outside));
        assertEquals(original.ridges().compute(outside), composed.ridges().compute(outside));
        assertSame(original.barrierNoise(), composed.barrierNoise());
        assertSame(original.fluidLevelFloodednessNoise(), composed.fluidLevelFloodednessNoise());
        assertSame(original.fluidLevelSpreadNoise(), composed.fluidLevelSpreadNoise());
        assertSame(original.lavaNoise(), composed.lavaNoise());
        assertSame(original.veinToggle(), composed.veinToggle());
        assertSame(original.veinRidged(), composed.veinRidged());
        assertSame(original.veinGap(), composed.veinGap());
    }

    @Test
    void fullChunkFillAndSurfaceDoNotPlugOpeningWithBedrockBadlandsOrIcebergs() {
        int solidSamples = 0;
        for (var biomeKey : List.of(Biomes.PLAINS, Biomes.ERODED_BADLANDS, Biomes.FROZEN_OCEAN)) {
            for (int chunkX : new int[] {0, 9, 19}) {
                var state = state(12345, true);
                var shape = new OverworldAbyssShape(160, -64, 320, 12345, 2.0, 1.25);
                var chunk =
                        new ProtoChunk(
                                new ChunkPos(chunkX, 0), UpgradeData.EMPTY, HEIGHT, palettes, null);
                var settings = generator.generatorSettings().value();
                // Prepopulate the cache so these pure generator stages need no live
                // StructureManager.
                var beardifier =
                        new DensityFunctions.BeardifierOrMarker() {
                            public double compute(FunctionContext context) {
                                return 0.25;
                            }

                            public double minValue() {
                                return 0.25;
                            }

                            public double maxValue() {
                                return 0.25;
                            }
                        };
                chunk.getOrCreateNoiseChunk(
                        c ->
                                NoiseChunk.forChunk(
                                        c,
                                        state,
                                        beardifier,
                                        settings,
                                        (x, y, z) ->
                                                new Aquifer.FluidStatus(
                                                        y < -54 ? -54 : 63,
                                                        (y < -54 ? Blocks.LAVA : Blocks.WATER)
                                                                .defaultBlockState()),
                                        Blender.empty()));
                generator.fillFromNoise(Blender.empty(), state, null, chunk).join();
                var biome = biomes.getOrThrow(biomeKey);
                generator.buildSurface(
                        chunk,
                        new WorldGenerationContext(generator, HEIGHT),
                        state,
                        null,
                        new BiomeManager((x, y, z) -> biome, 12345),
                        biomes,
                        Blender.empty());
                OverworldAbyssHole hole = OverworldAbyss.holeOf(state);
                for (int x = chunk.getPos().getMinBlockX();
                        x <= chunk.getPos().getMaxBlockX();
                        x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = -64; y < 320; y++) {
                            var context = new DensityFunction.SinglePointContext(x, y, z);
                            if (hole.isOpening(context)) {
                                assertTrue(
                                        chunk.getBlockState(new BlockPos(x, y, z)).isAir(),
                                        "Opening filled at "
                                                + x
                                                + ","
                                                + y
                                                + ","
                                                + z
                                                + " in "
                                                + biomeKey);
                            } else if (shape.distance(x, z) < 2.0 * shape.radius()
                                    && hole.terrainDensity(context) > 0) {
                                assertFalse(
                                        chunk.getBlockState(new BlockPos(x, y, z)).isAir(),
                                        "Positive terrain erased at " + x + "," + y + "," + z);
                                solidSamples++;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(solidSamples > 0, "The full generation pipeline must preserve positive rock");
    }

    @Test
    void noisyCliffStaysWithinItsReliefBandBelowShoulderForMultipleSeeds() {
        for (long seed : new long[] {0, 12345, -85217}) {
            var state = state(seed, true);
            // The original shaft stays open through the world floor, while the wall above it
            // remains near vertical below the shoulder.
            for (int x = 146; x <= 160; x++) {
                var column = generator.getBaseColumn(x, 0, HEIGHT, state);
                assertTrue(column.getBlock(-64).isAir());
                assertTrue(column.getBlock(-16).isAir());
            }
            // The shared wall is centred at layout R+24 with +/-24 relief.
            // Check intact floor outside that band and the seam's radial fade.
            assertTrue(generator.getBaseColumn(224, 0, HEIGHT, state).getBlock(-64).blocksMotion());
            int rockHeights = 0;
            for (int y : new int[] {8, 32, 64, 96, 112, 120}) {
                int firstRock = -1;
                for (int x = 160; x <= 220; x++) {
                    if (!generator.getBaseColumn(x, 0, HEIGHT, state).getBlock(y).isAir()) {
                        firstRock = x;
                        break;
                    }
                }
                // Cave mouths are now intentionally retained in this wall. At heights where
                // rock is present its boundary stays near R with bounded 3D relief.
                if (firstRock == -1) continue;
                rockHeights++;
                assertTrue(
                        firstRock > 160 && firstRock <= 220,
                        "Cliff must stay near R below128; seed="
                                + seed
                                + ", y="
                                + y
                                + ", x="
                                + firstRock);
            }
            assertTrue(rockHeights >= 2, "Wall must retain connected rock outside cave mouths");
            assertFalse(generator.getBaseColumn(200, 0, HEIGHT, state).getBlock(115).isAir());
            assertTrue(generator.getBaseColumn(200, 0, HEIGHT, state).getBlock(150).isAir());
        }
    }

    @Test
    void completeSurfaceColumnsOutsideCircleRemainOriginalNearBoundary() {
        for (long seed : new long[] {0, 12345, -85217}) {
            for (var biomeKey :
                    List.of(Biomes.PLAINS, Biomes.ERODED_BADLANDS, Biomes.FROZEN_OCEAN)) {
                for (var pos :
                        List.of(new ChunkPos(32, 0), new ChunkPos(-33, -1), new ChunkPos(23, 23))) {
                    var original = filledSurface(pos, state(seed, false), biomeKey);
                    var modifiedState = state(seed, true);
                    var modified = filledSurface(pos, modifiedState, biomeKey);
                    var shape = OverworldAbyss.shapeOf(modifiedState);
                    for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                        for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
                            if (shape.affects(x, z)) continue;
                            for (int y = -64; y < 320; y++) {
                                var block = new BlockPos(x, y, z);
                                assertEquals(
                                        original.getBlockState(block),
                                        modified.getBlockState(block),
                                        "Boundary changed: seed="
                                                + seed
                                                + ", biome="
                                                + biomeKey
                                                + ", pos="
                                                + block);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ProtoChunk filledSurface(
            ChunkPos pos, RandomState state, net.minecraft.resources.ResourceKey<Biome> biomeKey) {
        var chunk = new ProtoChunk(pos, UpgradeData.EMPTY, HEIGHT, palettes, null);
        var settings = generator.generatorSettings().value();
        var beardifier =
                new DensityFunctions.BeardifierOrMarker() {
                    public double compute(FunctionContext context) {
                        return 0;
                    }

                    public double minValue() {
                        return 0;
                    }

                    public double maxValue() {
                        return 0;
                    }
                };
        chunk.getOrCreateNoiseChunk(
                c ->
                        NoiseChunk.forChunk(
                                c,
                                state,
                                beardifier,
                                settings,
                                (x, y, z) ->
                                        new Aquifer.FluidStatus(
                                                y < -54 ? -54 : 63,
                                                (y < -54 ? Blocks.LAVA : Blocks.WATER)
                                                        .defaultBlockState()),
                                Blender.empty()));
        generator.fillFromNoise(Blender.empty(), state, null, chunk).join();
        var biome = biomes.getOrThrow(biomeKey);
        generator.buildSurface(
                chunk,
                new WorldGenerationContext(generator, HEIGHT),
                state,
                null,
                new BiomeManager((x, y, z) -> biome, 0),
                biomes,
                Blender.empty());
        return chunk;
    }
}
