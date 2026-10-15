package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Regression coverage for transition cliffs, supported rock, and the underground volume. */
class OverworldAbyssTransitionTest {
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);
    private static final long[] SEEDS = {0, 12345, 987654321};
    private static HolderLookup.Provider registries;
    private static NoiseBasedChunkGenerator generator;
    private static net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomes;
    private static net.minecraft.world.level.chunk.PalettedContainerFactory palettes;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                registries
                                        .lookupOrThrow(Registries.BIOME)
                                        .getOrThrow(Biomes.PLAINS)),
                        registries
                                .lookupOrThrow(Registries.NOISE_SETTINGS)
                                .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var mutable =
                new net.minecraft.core.MappedRegistry<net.minecraft.world.level.biome.Biome>(
                        Registries.BIOME, com.mojang.serialization.Lifecycle.stable());
        registries
                .lookupOrThrow(Registries.BIOME)
                .listElements()
                .forEach(
                        holder ->
                                mutable.register(
                                        holder.key(),
                                        holder.value(),
                                        net.minecraft.core.RegistrationInfo.BUILT_IN));
        biomes = mutable.freeze();
        palettes =
                net.minecraft.world.level.chunk.PalettedContainerFactory.create(
                        new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(
                                List.of(biomes)));
    }

    private static RandomState state(long seed, double slope) {
        var state =
                RandomState.create(
                        generator.generatorSettings().value(),
                        registries.lookupOrThrow(Registries.NOISE),
                        seed);
        if (slope > 0)
            ((RandomStateAccessor) (Object) state)
                    .mia$setRouter(
                            OverworldAbyss.compose(
                                    state,
                                    state.router(),
                                    new OverworldAbyssShape(320, -64, 320, seed, slope, 1.25)));
        return state;
    }

    private static int topSolid(int x, int z, RandomState state) {
        var column = generator.getBaseColumn(x, z, HEIGHT, state);
        for (int y = 319; y >= -64; y--) if (column.getBlock(y).blocksMotion()) return y;
        return -65;
    }

    @Test
    void sampledSurfaceAgreesWithActualOuterColumns() {
        for (long seed : new long[] {0, 12345})
            for (double slope : new double[] {1, 2}) {
                var modified = state(seed, slope);
                var hole = OverworldAbyss.holeOf(modified);
                var reference = new java.util.concurrent.atomic.AtomicReference<
                        com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCaveGuard>();
                hole.surface().mapAll(function -> {
                    if (function instanceof com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCaveGuard found)
                        reference.set(found);
                    return function;
                });
                var guard = reference.get();
                assertNotNull(guard);
                var solid = OverworldAbyssPipeline.sampleGraph(guard.solid(), 4, 8);
                for (int[] point :
                        new int[][] {
                            {0, 868}, {0, 872}, {0, 972}, {0, 976}, {852, 0}, {856, 0}, {964, 0},
                            {968, 0}, {972, 0}
                        }) {
                    int x = point[0], z = point[1];
                    int actual = topSolid(x, z, modified);
                    int sampled =
                            (int)
                                    hole.compute(
                                            new net.minecraft.world.level.levelgen.DensityFunction
                                                    .SinglePointContext(x, 0, z));
                    int solidTop = -65;
                    for (int y = 319; y >= -64; y--)
                        if (solid.compute(
                                        new net.minecraft.world.level.levelgen.DensityFunction
                                                .SinglePointContext(x, y, z))
                                > 0) {
                            solidTop = y;
                            break;
                        }
                    assertEquals(sampled, actual, "Actual lattice and off-loop surface must agree");
                    assertTrue(
                            Math.abs(solidTop - actual) <= 2,
                            "Cave masks must not cut through the surface skin: seed="
                                    + seed
                                    + ", slope="
                                    + slope
                                    + ", x="
                                    + x
                                    + ", z="
                                    + z
                                    + ", solid="
                                    + solidTop
                                    + ", actual="
                                    + actual);
                }
            }
    }

    @Test
    void retainedNoodleBranchActuallyExcavatesInnerRock() {
        int removed = 0;
        for (long seed : SEEDS) {
            var normal = state(seed, 1);
            var without =
                    RandomState.create(
                            generator.generatorSettings().value(),
                            registries.lookupOrThrow(Registries.NOISE),
                            seed);
            var original = without.router();
            var root = original.finalDensity();
            while (root
                    instanceof net.minecraft.world.level.levelgen.DensityFunctions.HolderHolder h)
                root = h.function().value();
            var pair =
                    assertInstanceOf(
                            com.altnoir.mementoinabyss.mixin.worldgen.DensityPairAccessor.class,
                            root);
            var noNoodle =
                    new net.minecraft.world.level.levelgen.NoiseRouter(
                            original.barrierNoise(),
                            original.fluidLevelFloodednessNoise(),
                            original.fluidLevelSpreadNoise(),
                            original.lavaNoise(),
                            original.temperature(),
                            original.vegetation(),
                            original.continents(),
                            original.erosion(),
                            original.depth(),
                            original.ridges(),
                            original.preliminarySurfaceLevel(),
                            pair.mia$first(),
                            original.veinToggle(),
                            original.veinRidged(),
                            original.veinGap());
            ((RandomStateAccessor) (Object) without)
                    .mia$setRouter(
                            OverworldAbyss.compose(
                                    without,
                                    noNoodle,
                                    new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25)));
            for (int x = 352; x <= 608 && removed == 0; x += 16)
                for (int z = 0; z <= 64 && removed == 0; z += 16) {
                    var a = generator.getBaseColumn(x, z, HEIGHT, normal);
                    var b = generator.getBaseColumn(x, z, HEIGHT, without);
                    for (int y = 8; y <= 200; y++)
                        if (a.getBlock(y).isAir() && b.getBlock(y).blocksMotion()) removed++;
                }
            if (removed > 0) break;
        }
        assertTrue(
                removed > 0, "Retained overworld/caves/noodle must remove actual generated rock");
    }

    @Test
    void actualOuterColumnsDoNotAmplifySmallVanillaChangesIntoCliffs() throws Exception {
        var failures = new ArrayList<String>();
        var csv =
                new StringBuilder(
                        "seed,slope,direction,distance,x,z,vanillaHeight,modifiedHeight,excessStep,naturalAllowance\n");
        for (long seed : SEEDS) {
            var vanilla = state(seed, 0);
            Map<Long, Integer> vanillaHeights = new HashMap<>();
            for (double slope : new double[] {OverworldAbyssShape.DEFAULT_OUTER_STEEPNESS, 2, 3}) {
                var modified = state(seed, slope);
                var shape = OverworldAbyss.holeOf(modified).shape();
                for (int direction = 0; direction < 4; direction++) {
                    int previousVanilla = 0, previousModified = 0;
                    for (int distance = 640; distance <= 1044; distance += 4) {
                        double angle = direction * Math.PI / 4;
                        int x = (int) Math.round(distance * Math.cos(angle));
                        int z = (int) Math.round(distance * Math.sin(angle));
                        long key = ((long) x << 32) ^ (z & 0xffffffffL);
                        int original =
                                vanillaHeights.computeIfAbsent(
                                        key, ignored -> topSolid(x, z, vanilla));
                        int actual = topSolid(x, z, modified);
                        int excess =
                                distance == 640
                                        ? 0
                                        : Math.abs(actual - previousModified)
                                                - Math.abs(original - previousVanilla);
                        int naturalAllowance = Math.abs(original - previousVanilla);
                        // Four blocks can traverse the planned descent and bounded surface detail.
                        // Where the imposed lift is below eight blocks, an existing cave mouth can
                        // move slightly as the exact original volume returns. Admit only a nearby
                        // vanilla step within eight blocks, never an arbitrary new mountain wall.
                        double nominalLift =
                                shape.outerWeight(shape.distance(x, z))
                                        * Math.abs(shape.ridgeHeight(x, z) - original);
                        if (excess > 8 && nominalLift < 8) {
                            int previous = 0;
                            for (int d = distance - 8; d <= distance + 8; d += 4) {
                                int nx = (int) Math.round(d * Math.cos(angle)),
                                        nz = (int) Math.round(d * Math.sin(angle));
                                long neighborKey = ((long) nx << 32) ^ (nz & 0xffffffffL);
                                int neighbor =
                                        vanillaHeights.computeIfAbsent(
                                                neighborKey, ignored -> topSolid(nx, nz, vanilla));
                                if (d > distance - 8)
                                    naturalAllowance =
                                            Math.max(
                                                    naturalAllowance,
                                                    Math.abs(neighbor - previous));
                                previous = neighbor;
                            }
                        }
                        if (distance > 640
                                && Math.abs(actual - previousModified) > naturalAllowance + 8)
                            failures.add(
                                    "seed="
                                            + seed
                                            + ", slope="
                                            + slope
                                            + ", direction="
                                            + direction * 45
                                            + ", distance="
                                            + distance
                                            + ", excess="
                                            + excess);
                        if (Math.hypot(x, z) >= 1040 && actual != original)
                            failures.add("Outside column changed at " + x + "," + z);
                        csv.append(seed)
                                .append(',')
                                .append(slope)
                                .append(',')
                                .append(direction * 45)
                                .append(',')
                                .append(distance)
                                .append(',')
                                .append(x)
                                .append(',')
                                .append(z)
                                .append(',')
                                .append(original)
                                .append(',')
                                .append(actual)
                                .append(',')
                                .append(excess)
                                .append(',')
                                .append(naturalAllowance)
                                .append('\n');
                        previousVanilla = original;
                        previousModified = actual;
                    }
                }
            }
        }
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("transition-columns.csv"), csv);
        assertTrue(
                failures.isEmpty(),
                () -> String.join("\n", failures.subList(0, Math.min(20, failures.size()))));
    }

    @Test
    void aboveCrestRockHasThreeDimensionalSupportRatherThanFloatingComponents() throws Exception {
        var csv = new StringBuilder("seed,centerX,solidAbove256,unsupportedAbove256\n");
        var failures = new ArrayList<String>();
        // Contiguous voxel patches, not a vertical-column gap test: overhangs may connect sideways.
        for (long seed : SEEDS) {
            var modified = state(seed, OverworldAbyssShape.DEFAULT_OUTER_STEEPNESS);
            for (int center : new int[] {640, 688, 736}) {
                final int nx = 17, nz = 17, ny = 64, bottom = 240;
                boolean[] solid = new boolean[nx * nz * ny];
                boolean[] supported = new boolean[solid.length];
                var queue = new ArrayDeque<Integer>();
                int above = 0;
                for (int x = 0; x < nx; x++)
                    for (int z = 0; z < nz; z++) {
                        var column =
                                generator.getBaseColumn(
                                        center + x - nx / 2, z - nz / 2, HEIGHT, modified);
                        for (int y = 0; y < ny; y++) {
                            int index = (x * nz + z) * ny + y;
                            solid[index] = column.getBlock(bottom + y).blocksMotion();
                            if (!solid[index]) continue;
                            if (bottom + y > 256) above++;
                            // A component leaving the patch may be grounded outside it. Treat it as
                            // supported conservatively; only enclosed, disconnected pieces are
                            // failures.
                            if (bottom + y <= 256
                                    || x == 0
                                    || x == nx - 1
                                    || z == 0
                                    || z == nz - 1
                                    || y == ny - 1) {
                                supported[index] = true;
                                queue.add(index);
                            }
                        }
                    }
                while (!queue.isEmpty()) {
                    int index = queue.removeFirst();
                    int y = index % ny, z = (index / ny) % nz, x = index / (ny * nz);
                    for (int[] offset : NEIGHBORS) {
                        int xx = x + offset[0], yy = y + offset[1], zz = z + offset[2];
                        if (xx < 0 || xx >= nx || yy < 0 || yy >= ny || zz < 0 || zz >= nz)
                            continue;
                        int neighbor = (xx * nz + zz) * ny + yy;
                        if (solid[neighbor] && !supported[neighbor]) {
                            supported[neighbor] = true;
                            queue.add(neighbor);
                        }
                    }
                }
                int unsupported = 0;
                for (int i = 0; i < solid.length; i++)
                    if (solid[i] && !supported[i] && bottom + i % ny > 256) unsupported++;
                csv.append(seed)
                        .append(',')
                        .append(center)
                        .append(',')
                        .append(above)
                        .append(',')
                        .append(unsupported)
                        .append('\n');
                if (unsupported > 0)
                    failures.add(
                            "seed="
                                    + seed
                                    + ", center="
                                    + center
                                    + ", floating voxels="
                                    + unsupported);
            }
        }
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("ridge-support.csv"), csv);
        assertEquals(List.of(), failures);
    }

    @Test
    void noiseChunkKeepsUndergroundRockAtTheWorldFloorOutsideTheOpenShaft() {
        var modified = state(12345, OverworldAbyssShape.DEFAULT_OUTER_STEEPNESS);
        var position = new net.minecraft.world.level.ChunkPos(25, 0);
        var abyssChunk = fillNoiseOnly(position, modified);
        boolean hasBottomRockColumn = false;
        for (int x = position.getMinBlockX(); x <= position.getMaxBlockX(); x++)
            for (int z = position.getMinBlockZ(); z <= position.getMaxBlockZ(); z++) {
                boolean solidColumn = true;
                for (int y = -64; y <= -60; y++)
                    solidColumn &=
                            abyssChunk
                                    .getBlockState(new net.minecraft.core.BlockPos(x, y, z))
                                    .blocksMotion();
                hasBottomRockColumn |= solidColumn;
            }
        assertTrue(
                hasBottomRockColumn,
                "The real NoiseChunk must retain rock at the world floor outside the open shaft");
    }

    @Test
    void fullSurfaceConvertsBedrockThroughTwoRadiiAndKeepsTheShaftOpen() {
        var bedrock = net.minecraft.world.level.block.Blocks.BEDROCK;
        var deepslate = net.minecraft.world.level.block.Blocks.DEEPSLATE;
        long seed = 12345;
        var vanilla = state(seed, 0);
        var modified = state(seed, OverworldAbyssShape.DEFAULT_OUTER_STEEPNESS);
        var shape = OverworldAbyss.shapeOf(modified);
        var boundaryChunks =
                new net.minecraft.world.level.ChunkPos[] {
                    new net.minecraft.world.level.ChunkPos(40, 0),
                    new net.minecraft.world.level.ChunkPos(-40, 0),
                    new net.minecraft.world.level.ChunkPos(28, 28),
                    new net.minecraft.world.level.ChunkPos(-29, -29)
                };
        var originalChunks = new HashMap<Long, net.minecraft.world.level.chunk.ProtoChunk>();
        var abyssChunks = new HashMap<Long, net.minecraft.world.level.chunk.ProtoChunk>();
        int converted = 0, preserved = 0;
        for (var position : boundaryChunks) {
            var original = filledSurface(position, vanilla, seed);
            var abyss = filledSurface(position, modified, seed);
            long key = chunkKey(position);
            originalChunks.put(key, original);
            abyssChunks.put(key, abyss);
            for (int x = position.getMinBlockX(); x <= position.getMaxBlockX(); x++)
                for (int z = position.getMinBlockZ(); z <= position.getMaxBlockZ(); z++)
                    for (int y = -64; y < 320; y++) {
                        var blockPos = new net.minecraft.core.BlockPos(x, y, z);
                        boolean inside = shape.distance(x, z) <= 640.0;
                        var originalState = original.getBlockState(blockPos);
                        var actualState = abyss.getBlockState(blockPos);
                        if (originalState.is(bedrock)) {
                            if (inside) {
                                assertTrue(
                                        actualState.is(deepslate),
                                        "Inner bedrock was not replaced: pos=" + blockPos);
                                converted++;
                            } else {
                                assertTrue(
                                        actualState.is(bedrock),
                                        "Outer bedrock changed: pos=" + blockPos);
                                preserved++;
                            }
                        }
                        if (inside)
                            assertFalse(
                                    actualState.is(bedrock),
                                    "Bedrock remains within two radii: pos=" + blockPos);
                    }
        }
        assertTrue(converted > 0, "Fixture must exercise original bedrock inside the boundary");
        assertTrue(preserved > 0, "Fixture must exercise original bedrock outside the boundary");

        // These coordinates straddle the circular limit on the axes and at positive and negative
        // diagonal boundaries. The exact axis sample at r=640 is included above.
        for (int[] point :
                new int[][] {
                    {640, 0},
                    {641, 0},
                    {-640, 0},
                    {452, 452},
                    {453, 453},
                    {-452, -452},
                    {-453, -453}
                }) {
            var position = new net.minecraft.world.level.ChunkPos(point[0] >> 4, point[1] >> 4);
            var original = originalChunks.get(chunkKey(position));
            var abyss = abyssChunks.get(chunkKey(position));
            var floor = new net.minecraft.core.BlockPos(point[0], -64, point[1]);
            assertTrue(original.getBlockState(floor).is(bedrock), "Floor fixture missing bedrock");
            var expected = shape.distance(point[0], point[1]) <= 640.0 ? deepslate : bedrock;
            assertTrue(
                    abyss.getBlockState(floor).is(expected),
                    "Wrong bedrock treatment at boundary point " + point[0] + "," + point[1]);
        }

        var shaftPosition = new net.minecraft.world.level.ChunkPos(20, 0);
        var shaft = filledSurface(shaftPosition, modified, seed);
        for (int y = -64; y < 320; y++)
            assertTrue(
                    shaft.getBlockState(new net.minecraft.core.BlockPos(320, y, 0)).isAir(),
                    "The r=R opening must stay clear through the world: y=" + y);
    }

    @Test
    void bulkNoiseChunkFillMatchesIndependentColumnsAcrossInterpolationCells() {
        // These columns cross the Y=128 density transition and the outer terrain join. Sampling
        // several X/Z interpolation cells catches stale cache_once values in custom fillArray
        // paths.
        for (long[] fixture : new long[][] {{0, 2, 61}, {12345, 1, 54}}) {
            long seed = fixture[0];
            var modified = state(seed, fixture[1]);
            int chunkZ = (int) fixture[2];
            var chunk = fillNoiseOnly(new net.minecraft.world.level.ChunkPos(0, chunkZ), modified);
            for (int x : new int[] {0, 3, 8, 15})
                for (int localZ : new int[] {0, 3, 8, 15}) {
                    int z = chunkZ * 16 + localZ;
                    var column = generator.getBaseColumn(x, z, HEIGHT, modified);
                    for (int y = 0; y < 320; y++) {
                        assertEquals(
                                column.getBlock(y),
                                chunk.getBlockState(new net.minecraft.core.BlockPos(x, y, z)),
                                "Bulk/column mismatch: seed="
                                        + seed
                                        + ", slope="
                                        + fixture[1]
                                        + ", x="
                                        + x
                                        + ", y="
                                        + y
                                        + ", z="
                                        + z);
                    }
                }
        }
    }

    private static net.minecraft.world.level.chunk.ProtoChunk fillNoiseOnly(
            net.minecraft.world.level.ChunkPos position, RandomState state) {
        var chunk =
                new net.minecraft.world.level.chunk.ProtoChunk(
                        position,
                        net.minecraft.world.level.chunk.UpgradeData.EMPTY,
                        HEIGHT,
                        palettes,
                        null);
        var beardifier =
                new net.minecraft.world.level.levelgen.DensityFunctions.BeardifierOrMarker() {
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
                        net.minecraft.world.level.levelgen.NoiseChunk.forChunk(
                                c,
                                state,
                                beardifier,
                                generator.generatorSettings().value(),
                                (x, y, z) ->
                                        new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(
                                                y < -54 ? -54 : 63,
                                                (y < -54
                                                                ? net.minecraft.world.level.block
                                                                        .Blocks.LAVA
                                                                : net.minecraft.world.level.block
                                                                        .Blocks.WATER)
                                                        .defaultBlockState()),
                                net.minecraft.world.level.levelgen.blending.Blender.empty()));
        generator
                .fillFromNoise(
                        net.minecraft.world.level.levelgen.blending.Blender.empty(),
                        state,
                        null,
                        chunk)
                .join();
        return chunk;
    }

    private static long chunkKey(net.minecraft.world.level.ChunkPos position) {
        return ((long) position.x() << 32) ^ (position.z() & 0xffffffffL);
    }

    private static net.minecraft.world.level.chunk.ProtoChunk filledSurface(
            net.minecraft.world.level.ChunkPos position, RandomState state, long seed) {
        var chunk =
                new net.minecraft.world.level.chunk.ProtoChunk(
                        position,
                        net.minecraft.world.level.chunk.UpgradeData.EMPTY,
                        HEIGHT,
                        palettes,
                        null);
        var settings = generator.generatorSettings().value();
        var beardifier =
                new net.minecraft.world.level.levelgen.DensityFunctions.BeardifierOrMarker() {
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
                        net.minecraft.world.level.levelgen.NoiseChunk.forChunk(
                                c,
                                state,
                                beardifier,
                                settings,
                                (x, y, z) ->
                                        new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(
                                                y < -54 ? -54 : 63,
                                                (y < -54
                                                                ? net.minecraft.world.level.block
                                                                        .Blocks.LAVA
                                                                : net.minecraft.world.level.block
                                                                        .Blocks.WATER)
                                                        .defaultBlockState()),
                                net.minecraft.world.level.levelgen.blending.Blender.empty()));
        generator
                .fillFromNoise(
                        net.minecraft.world.level.levelgen.blending.Blender.empty(),
                        state,
                        null,
                        chunk)
                .join();
        var plains = biomes.getOrThrow(Biomes.PLAINS);
        generator.buildSurface(
                chunk,
                new net.minecraft.world.level.levelgen.WorldGenerationContext(generator, HEIGHT),
                state,
                null,
                new net.minecraft.world.level.biome.BiomeManager((x, y, z) -> plains, seed),
                biomes,
                net.minecraft.world.level.levelgen.blending.Blender.empty());
        return chunk;
    }

    private static final int[][] NEIGHBORS = {
        {-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}
    };
}
