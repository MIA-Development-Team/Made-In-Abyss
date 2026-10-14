package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Guard coverage for the switch and for every level that must not receive an abyss.
 *
 * <p>These run without a server: {@link OverworldAbyss#decide} is pure, so a foreign dimension, a
 * disabled feature, a non-noise generator and a second install of the same random state are all decided
 * before any world data is touched. The install branch itself needs a mixin-transformed
 * {@link RandomState} and is covered by the generation tests instead.
 */
class OverworldAbyssGuardTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    /**
     * The proposed spawn has to stand on terrain the carve never touched. The slope band is
     * configurable, so the candidate radius is derived from {@code outerLimit()} and this test fails if
     * anyone writes it back as a fixed multiple of the radius.
     */
    @Test
    void suggestedSpawnIsOutsideTheCarvedSlope() {
        for (double band : new double[] {1.0, 1.25, 2.0}) {
            var shape = new OverworldAbyssShape(160, -64, 320, 12345L, 1.0, band);
            var chunk = OverworldAbyssSpawn.suggested(shape);
            double distance = shape.distance(chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
            assertTrue(
                    distance > shape.outerLimit(),
                    "the proposed chunk is carved terrain at band " + band + ": " + distance);
            // Well clear of the boundary, not a single block past it.
            assertTrue(
                    distance >= shape.outerLimit() + 0.25 * shape.radius(),
                    "the proposed chunk is too close to the boundary at band " + band);
        }
    }

    @Test
    void abyssIsOnlyBuiltForTheOverworld() {
        // The nether and the end reuse the overworld noise settings in some modpacks, so the
        // dimension is
        // checked before anything else.
        assertEquals(
                OverworldAbyss.Decision.NOT_OVERWORLD,
                OverworldAbyss.decide(Level.NETHER, new PlainGenerator(), true, false));
        assertEquals(
                OverworldAbyss.Decision.NOT_OVERWORLD,
                OverworldAbyss.decide(Level.END, new PlainGenerator(), true, false));
    }

    @Test
    void switchedOffAbyssNeverTouchesALevel() {
        assertEquals(
                OverworldAbyss.Decision.DISABLED,
                OverworldAbyss.decide(Level.OVERWORLD, new PlainGenerator(), false, false));
    }

    @Test
    void anAlreadyInstalledStateIsNotWrappedTwice() {
        // Wrapping twice would nest two funnels over the same column and double the carve, so a
        // second
        // call for the same random state has to be refused even with everything else in order.
        assertEquals(
                OverworldAbyss.Decision.ALREADY_INSTALLED,
                OverworldAbyss.decide(Level.OVERWORLD, new PlainGenerator(), true, true));
    }

    @Test
    void foreignGeneratorIsLeftAlone() {
        assertEquals(
                OverworldAbyss.Decision.NOT_NOISE_BASED,
                OverworldAbyss.decide(Level.OVERWORLD, new PlainGenerator(), true, false));
    }

    @Test
    void guardsAreCheckedInCheapToExpensiveOrder() {
        // A disabled feature in the nether must report the dimension, not the switch: the dimension
        // test
        // is what protects other levels, and it runs first.
        assertEquals(
                OverworldAbyss.Decision.NOT_OVERWORLD,
                OverworldAbyss.decide(Level.NETHER, new PlainGenerator(), false, true));
    }

    @Test
    void uninstalledRandomStateHasNoShape() {
        // The blind spot the old instanceof check had: a level this mod never touched must report
        // "no
        // abyss" instead of letting the aquifer and the surface rules assume one.
        assertNull(OverworldAbyss.shapeOf(null));
        assertNull(OverworldAbyss.holeOf(null));
    }

    @Test
    void theStubIsDeliberatelyNotNoiseBased() {
        // Compile-time safe form of the instanceof the guard performs on the real generator.
        assertFalse(NoiseBasedChunkGenerator.class.isAssignableFrom(PlainGenerator.class));
    }

    /** Minimal concrete generator; only its type matters to the guard. */
    private static class PlainGenerator extends ChunkGenerator {
        @SuppressWarnings("unchecked")
        PlainGenerator() {
            super(null, (java.util.function.Function<Holder<Biome>, BiomeGenerationSettings>) null);
        }

        @Override
        protected MapCodec<? extends ChunkGenerator> codec() {
            return null;
        }

        @Override
        public void applyCarvers(
                WorldGenRegion region,
                long seed,
                RandomState state,
                BiomeManager manager,
                StructureManager structures,
                ChunkAccess chunk) {}

        @Override
        public void buildSurface(
                WorldGenRegion region,
                StructureManager structures,
                RandomState state,
                ChunkAccess chunk) {}

        @Override
        public void spawnOriginalMobs(WorldGenRegion region) {}

        @Override
        public int getGenDepth() {
            return 0;
        }

        @Override
        public CompletableFuture<ChunkAccess> fillFromNoise(
                Blender blender,
                RandomState state,
                StructureManager structures,
                ChunkAccess chunk) {
            return CompletableFuture.completedFuture(chunk);
        }

        @Override
        public int getSeaLevel() {
            return 0;
        }

        @Override
        public int getMinY() {
            return 0;
        }

        @Override
        public int getBaseHeight(
                int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState state) {
            return 0;
        }

        @Override
        public NoiseColumn getBaseColumn(
                int x, int z, LevelHeightAccessor level, RandomState state) {
            return null;
        }

        @Override
        public void addDebugScreenInfo(List<String> lines, RandomState state, BlockPos pos) {}
    }
}
