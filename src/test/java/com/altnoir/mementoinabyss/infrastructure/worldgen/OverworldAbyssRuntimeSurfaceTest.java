package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Exercises the packaged overworld JSON through a real chunk fill and badlands surface pass. */
class OverworldAbyssRuntimeSurfaceTest {
    private static final long SEED = -3641258292459704473L;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;
    private int regionSize = 48;
    private static final int HEIGHT = MAX_Y - MIN_Y;
    private static final int SKIN_ALLOWANCE = 4;
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private static HolderLookup.Provider registries;
    private static NoiseGeneratorSettings settings;
    private static NoiseBasedChunkGenerator generator;
    private static Registry<Biome> biomes;
    private static PalettedContainerFactory palettes;
    private static Holder<Biome> badlands;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        settings = packagedOverworldSettings();
        badlands = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.ERODED_BADLANDS);
        generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(badlands), Holder.direct(settings));

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

        // Ensure the test runs with the same transformed surface hooks as generated chunks.
        Class.forName("net.minecraft.server.level.ChunkMap");
        Class.forName("net.minecraft.server.MinecraftServer");
        Class.forName("net.minecraft.world.level.levelgen.SurfaceSystem");
    }

    @Test
    void packagedOverworldHasSupportedOuterSlopeAfterRealBadlandsSurfaceGeneration() {
        verifyRegion(560, 640, "player");
        verifyRegion(512, 576, "slope");
        verifyRegion(400, 464, "ridge");
    }

    @Test
    void roughInnerWallSurvivesRealSurfaceGenerationWithoutUnsupportedRaisedRock() {
        verifyRegion(288, -48, "wall");
    }

    private void verifyRegion(int regionMinX, int regionMinZ, String name) {
        int regionChunks = name.equals("wall") ? 7 : 3;
        regionSize = regionChunks * 16;
        var shape = new OverworldAbyssShape(320, MIN_Y, MAX_Y, SEED, 1.0, 1.25);
        var vanillaState = state(false, shape);
        var abyssState = state(true, shape);
        var original = vanillaState.router().finalDensity();
        var pipeline =
                OverworldAbyssPipeline.build(
                        original,
                        shape,
                        settings.noiseSettings().getCellWidth(),
                        settings.noiseSettings().getCellHeight());
        assertEquals(
                "before_vanilla_interpolation",
                pipeline.path(),
                "The actual packaged JSON must take the height-normalized terrain path");

        OverworldAbyssHole hole = OverworldAbyss.holeOf(abyssState);
        assertNotNull(hole);
        var biomeManager = new BiomeManager((x, y, z) -> badlands, SEED);
        Map<Long, GeneratedChunk> vanilla = new HashMap<>();
        Map<Long, GeneratedChunk> abyss = new HashMap<>();
        for (int chunkX = regionMinX >> 4; chunkX < (regionMinX >> 4) + regionChunks; chunkX++) {
            for (int chunkZ = regionMinZ >> 4;
                    chunkZ < (regionMinZ >> 4) + regionChunks;
                    chunkZ++) {
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                vanilla.put(key(pos), generate(pos, vanillaState, biomeManager, false));
                abyss.put(key(pos), generate(pos, abyssState, biomeManager, true));
            }
        }

        int regionVolume = HEIGHT * regionSize * regionSize;
        BlockState[] abyssBlocks = new BlockState[regionVolume];
        byte[] vanillaSolid = new byte[regionVolume];
        int[] abyssBeforeSurfaceTop = new int[regionSize * regionSize];
        int[] abyssAfterSurfaceTop = new int[regionSize * regionSize];
        int[] vanillaAfterSurfaceTop = new int[regionSize * regionSize];
        int[] fieldTop = new int[regionSize * regionSize];
        int extensionAirToSolid = 0;
        int modifiedVoxelCount = 0;

        for (int regionZ = 0; regionZ < regionSize; regionZ++) {
            int z = regionMinZ + regionZ;
            for (int regionX = 0; regionX < regionSize; regionX++) {
                int x = regionMinX + regionX;
                int column = columnIndex(regionX, regionZ);
                fieldTop[column] =
                        (int) hole.compute(new DensityFunction.SinglePointContext(x, 0, z));
                GeneratedChunk vanillaChunk = chunkAt(vanilla, x, z);
                GeneratedChunk abyssChunk = chunkAt(abyss, x, z);
                vanillaAfterSurfaceTop[column] =
                        vanillaChunk.afterSurfaceTop[localColumnIndex(x, z)];
                abyssBeforeSurfaceTop[column] = abyssChunk.beforeSurfaceTop[localColumnIndex(x, z)];
                abyssAfterSurfaceTop[column] = abyssChunk.afterSurfaceTop[localColumnIndex(x, z)];

                for (int y = MIN_Y; y < MAX_Y; y++) {
                    int index = volumeIndex(regionX, regionZ, y);
                    BlockState before = abyssChunk.beforeSurface[localVolumeIndex(x, z, y)];
                    BlockState after =
                            abyssChunk.chunk.getBlockState(
                                    new net.minecraft.core.BlockPos(x, y, z));
                    BlockState vanillaAfter =
                            vanillaChunk.chunk.getBlockState(
                                    new net.minecraft.core.BlockPos(x, y, z));
                    abyssBlocks[index] = after;
                    vanillaSolid[index] = (byte) (solid(vanillaAfter) ? 1 : 0);
                    modifiedVoxelCount += before.equals(after) ? 0 : 1;
                    if (!solid(before) && solid(after)) extensionAirToSolid++;
                }
            }
        }

        assertTrue(modifiedVoxelCount > 0, "The full surface pass should leave a voxel diff");

        List<String> surfaceOutliers = new ArrayList<>();
        int maxSurfaceDelta = Integer.MIN_VALUE;
        String largestDelta = "none";
        int maxUnexpectedStep = 0;
        String largestStep = "none";
        for (int regionZ = 0; regionZ < regionSize; regionZ++) {
            int z = regionMinZ + regionZ;
            for (int regionX = 0; regionX < regionSize; regionX++) {
                int x = regionMinX + regionX;
                int column = columnIndex(regionX, regionZ);
                int before = abyssBeforeSurfaceTop[column];
                int after = abyssAfterSurfaceTop[column];
                int field = fieldTop[column];
                int baseline = vanillaAfterSurfaceTop[column];
                int delta = after - before;
                if (delta > maxSurfaceDelta) {
                    maxSurfaceDelta = delta;
                    largestDelta = "x=" + x + ",z=" + z + ",before=" + before + ",after=" + after;
                }

                int expectedTop = Math.max(field, baseline);
                if (after > expectedTop + SKIN_ALLOWANCE || after < field - SKIN_ALLOWANCE) {
                    if (surfaceOutliers.size() < 20) {
                        surfaceOutliers.add(
                                "x="
                                        + x
                                        + ",z="
                                        + z
                                        + ",before="
                                        + before
                                        + ",after="
                                        + after
                                        + ",field="
                                        + field
                                        + ",vanilla="
                                        + baseline);
                    }
                }

                for (int[] direction : DIRECTIONS) {
                    int nextX = regionX + direction[0];
                    int nextZ = regionZ + direction[1];
                    if (nextX < 0 || nextX >= regionSize || nextZ < 0 || nextZ >= regionSize)
                        continue;
                    int next = columnIndex(nextX, nextZ);
                    int unexpectedStep =
                            Math.abs(after - abyssAfterSurfaceTop[next])
                                    - Math.abs(baseline - vanillaAfterSurfaceTop[next]);
                    if (unexpectedStep > maxUnexpectedStep) {
                        maxUnexpectedStep = unexpectedStep;
                        largestStep =
                                "x="
                                        + x
                                        + ",z="
                                        + z
                                        + ",neighborX="
                                        + (x + direction[0])
                                        + ",neighborZ="
                                        + (z + direction[1])
                                        + ",modified="
                                        + Math.abs(after - abyssAfterSurfaceTop[next])
                                        + ",field="
                                        + Math.abs(field - fieldTop[next])
                                        + ",vanilla="
                                        + Math.abs(baseline - vanillaAfterSurfaceTop[next]);
                    }
                }
            }
        }

        assertTrue(
                surfaceOutliers.isEmpty(),
                "Post-surface height must follow the density field, with vanilla badlands tops"
                        + " allowed: "
                        + surfaceOutliers);
        // The shaft intentionally has a near-vertical inner cliff. The outer slope must not.
        if (!name.equals("wall"))
            assertTrue(
                    maxUnexpectedStep <= SKIN_ALLOWANCE * 2,
                    "Badlands must not add a new large wall above the field and vanilla relief: "
                            + largestStep);

        byte[] supportedFromFloor = floodSolidFromBottom(abyssBlocks);
        int addedHighBlocks = 0;
        int unsupportedRaisedRock = 0;
        int unsupportedAbove160 = 0;
        List<String> unsupportedExamples = new ArrayList<>();
        int centerMinX = regionMinX + (regionChunks / 2) * 16;
        int centerMinZ = regionMinZ + (regionChunks / 2) * 16;
        for (int z = centerMinZ; z < centerMinZ + 16; z++) {
            for (int x = centerMinX; x < centerMinX + 16; x++) {
                int regionX = x - regionMinX;
                int regionZ = z - regionMinZ;
                int originalTop = vanillaAfterSurfaceTop[columnIndex(regionX, regionZ)];
                for (int y = originalTop + 1; y < MAX_Y; y++) {
                    int index = volumeIndex(regionX, regionZ, y);
                    if (vanillaSolid[index] != 0 || !solid(abyssBlocks[index])) continue;
                    addedHighBlocks++;
                    if (supportedFromFloor[index] == 0) {
                        unsupportedRaisedRock++;
                        if (y >= 160) unsupportedAbove160++;
                        if (unsupportedExamples.size() < 12)
                            unsupportedExamples.add("x=" + x + ",y=" + y + ",z=" + z);
                    }
                }
            }
        }

        assertTrue(addedHighBlocks > 0, "Fixture must contain newly raised high rock");
        assertEquals(
                0,
                unsupportedRaisedRock,
                "New high rock in the center chunk must connect by solid faces to the region floor;"
                        + " edge contact alone is not a support: added="
                        + addedHighBlocks
                        + ", above160="
                        + unsupportedAbove160
                        + ", examples="
                        + unsupportedExamples);
        assertEquals(0, unsupportedAbove160);
        int wallSolidSamples = 0;
        int wallUnsupportedSamples = 0;
        if (name.equals("wall")) {
            // Also cover old underground rock exposed by the carve, not only newly raised rock.
            for (int z = centerMinZ; z < centerMinZ + 16; z++)
                for (int x = centerMinX; x < centerMinX + 16; x++)
                    for (int y = MIN_Y; y < MAX_Y; y++) {
                        int index = volumeIndex(x - regionMinX, z - regionMinZ, y);
                        if (!solid(abyssBlocks[index])) continue;
                        wallSolidSamples++;
                        if (supportedFromFloor[index] == 0) wallUnsupportedSamples++;
                    }
            assertTrue(wallSolidSamples > 0);
            assertEquals(0, wallUnsupportedSamples, "Exposed underground wall rock lost support");
        }
        try {
            var directory =
                    java.nio.file.Path.of(
                            System.getProperty(
                                    "abyss.reportDirectory", "build/reports/overworld-abyss"));
            java.nio.file.Files.createDirectories(directory);
            String report =
                    "{\n  \"seed\": "
                            + SEED
                            + ",\n  \"chunksPerMode\": "
                            + regionChunks * regionChunks
                            + ",\n"
                            + "  \"regionMinX\": "
                            + regionMinX
                            + ",\n  \"regionMinZ\": "
                            + regionMinZ
                            + ",\n  \"regionWidth\": "
                            + regionSize
                            + ",\n"
                            + "  \"surfaceChangedBlocks\": "
                            + modifiedVoxelCount
                            + ",\n"
                            + "  \"surfaceAddedSolidBlocks\": "
                            + extensionAirToSolid
                            + ",\n"
                            + "  \"maxSurfaceHeightChange\": "
                            + maxSurfaceDelta
                            + ",\n"
                            + "  \"maxStepBeyondVanilla\": "
                            + maxUnexpectedStep
                            + ",\n"
                            + "  \"newHighBlocksChecked\": "
                            + addedHighBlocks
                            + ",\n"
                            + "  \"newHighBlocksWithoutSupport\": "
                            + unsupportedRaisedRock
                            + ",\n  \"wallSolidSamples\": "
                            + wallSolidSamples
                            + ",\n  \"wallUnsupportedSolidSamples\": "
                            + wallUnsupportedSamples
                            + "\n}\n";
            java.nio.file.Files.writeString(
                    directory.resolve("reported-runtime-surface-" + name + ".json"), report);
            if (name.equals("player"))
                java.nio.file.Files.writeString(
                        directory.resolve("reported-runtime-surface.json"), report);
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static NoiseGeneratorSettings packagedOverworldSettings() throws Exception {
        var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        try (var stream =
                OverworldAbyssRuntimeSurfaceTest.class
                        .getClassLoader()
                        .getResourceAsStream(
                                "data/minecraft/worldgen/noise_settings/overworld.json")) {
            assertNotNull(
                    stream, "Packaged overworld noise_settings JSON must be on the classpath");
            var json =
                    JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return NoiseGeneratorSettings.DIRECT_CODEC.parse(ops, json).getOrThrow();
        }
    }

    private static RandomState state(boolean compose, OverworldAbyssShape shape) {
        RandomState state =
                RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), SEED);
        if (compose) {
            var original = state.router();
            var router =
                    OverworldAbyss.compose(
                            state,
                            original,
                            shape,
                            DensityFunctions.cache2d(original.preliminarySurfaceLevel()),
                            settings.noiseSettings().getCellWidth(),
                            settings.noiseSettings().getCellHeight());
            ((RandomStateAccessor) (Object) state).mia$setRouter(router);
        }
        return state;
    }

    private static GeneratedChunk generate(
            ChunkPos position,
            RandomState state,
            BiomeManager biomeManager,
            boolean keepBeforeSurface) {
        var chunk = new ProtoChunk(position, UpgradeData.EMPTY, HEIGHT_ACCESSOR, palettes, null);
        var noiseSettings = generator.generatorSettings().value();
        var beardifier =
                new DensityFunctions.BeardifierOrMarker() {
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
                };
        chunk.getOrCreateNoiseChunk(
                c ->
                        NoiseChunk.forChunk(
                                c,
                                state,
                                beardifier,
                                noiseSettings,
                                (x, y, z) ->
                                        new Aquifer.FluidStatus(
                                                y < -54 ? -54 : 63,
                                                (y < -54 ? Blocks.LAVA : Blocks.WATER)
                                                        .defaultBlockState()),
                                Blender.empty()));
        generator.fillFromNoise(Blender.empty(), state, null, chunk).join();

        BlockState[] beforeSurface = null;
        int[] beforeTop = null;
        if (keepBeforeSurface) {
            beforeSurface = new BlockState[16 * 16 * HEIGHT];
            beforeTop = new int[16 * 16];
            for (int localX = 0; localX < 16; localX++)
                for (int localZ = 0; localZ < 16; localZ++) {
                    int x = position.getMinBlockX() + localX;
                    int z = position.getMinBlockZ() + localZ;
                    beforeTop[localX * 16 + localZ] = topSolid(chunk, x, z);
                    for (int y = MIN_Y; y < MAX_Y; y++) {
                        BlockState stateAt =
                                chunk.getBlockState(new net.minecraft.core.BlockPos(x, y, z));
                        beforeSurface[localVolumeIndex(x, z, y)] = stateAt;
                    }
                }
        }

        generator.buildSurface(
                chunk,
                new WorldGenerationContext(generator, HEIGHT_ACCESSOR),
                state,
                null,
                biomeManager,
                biomes,
                Blender.empty());

        int[] afterTop = new int[16 * 16];
        for (int localX = 0; localX < 16; localX++)
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = position.getMinBlockX() + localX;
                int z = position.getMinBlockZ() + localZ;
                afterTop[localX * 16 + localZ] = topSolid(chunk, x, z);
            }
        return new GeneratedChunk(chunk, beforeSurface, beforeTop, afterTop);
    }

    private static final LevelHeightAccessor HEIGHT_ACCESSOR =
            LevelHeightAccessor.create(MIN_Y, HEIGHT);

    private static int topSolid(ProtoChunk chunk, int x, int z) {
        for (int y = MAX_Y - 1; y >= MIN_Y; y--)
            if (solid(chunk.getBlockState(new net.minecraft.core.BlockPos(x, y, z)))) return y;
        return MIN_Y - 1;
    }

    private static boolean solid(BlockState state) {
        return state.blocksMotion() && state.getFluidState().isEmpty();
    }

    private byte[] floodSolidFromBottom(BlockState[] blocks) {
        byte[] reached = new byte[blocks.length];
        int[] queue = new int[blocks.length];
        int tail = 0;
        for (int z = 0; z < regionSize; z++)
            for (int x = 0; x < regionSize; x++) {
                int index = volumeIndex(x, z, MIN_Y);
                if (solid(blocks[index])) {
                    reached[index] = 1;
                    queue[tail++] = index;
                }
            }

        int head = 0;
        int plane = regionSize * regionSize;
        while (head < tail) {
            int index = queue[head++];
            int yIndex = index / plane;
            int remainder = index % plane;
            int z = remainder / regionSize;
            int x = remainder % regionSize;
            if (x > 0) tail = enqueue(index - 1, blocks, reached, queue, tail);
            if (x + 1 < regionSize) tail = enqueue(index + 1, blocks, reached, queue, tail);
            if (z > 0) tail = enqueue(index - regionSize, blocks, reached, queue, tail);
            if (z + 1 < regionSize)
                tail = enqueue(index + regionSize, blocks, reached, queue, tail);
            if (yIndex > 0) tail = enqueue(index - plane, blocks, reached, queue, tail);
            if (yIndex + 1 < HEIGHT) tail = enqueue(index + plane, blocks, reached, queue, tail);
        }
        return reached;
    }

    private static int enqueue(
            int index, BlockState[] blocks, byte[] reached, int[] queue, int tail) {
        if (reached[index] == 0 && solid(blocks[index])) {
            reached[index] = 1;
            queue[tail++] = index;
        }
        return tail;
    }

    private static GeneratedChunk chunkAt(Map<Long, GeneratedChunk> chunks, int x, int z) {
        GeneratedChunk chunk =
                chunks.get(key(new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(z, 16))));
        assertNotNull(chunk, "Missing generated chunk at " + x + "," + z);
        return chunk;
    }

    private static long key(ChunkPos pos) {
        return ((long) pos.x() << 32) ^ (pos.z() & 0xffffffffL);
    }

    private static int localColumnIndex(int x, int z) {
        return Math.floorMod(x, 16) * 16 + Math.floorMod(z, 16);
    }

    private static int localVolumeIndex(int x, int z, int y) {
        return (y - MIN_Y) * 16 * 16 + localColumnIndex(x, z);
    }

    private int columnIndex(int x, int z) {
        return z * regionSize + x;
    }

    private int volumeIndex(int x, int z, int y) {
        return ((y - MIN_Y) * regionSize + z) * regionSize + x;
    }

    private record GeneratedChunk(
            ProtoChunk chunk,
            BlockState[] beforeSurface,
            int[] beforeSurfaceTop,
            int[] afterSurfaceTop) {}
}
