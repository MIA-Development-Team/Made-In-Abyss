package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.Test;

/** Exports real NoiseChunk columns, including a frozen copy of the old production formula. */
class OverworldAbyssProfileTest {
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);

    @Test
    void exportRealTerrainProfilesAndMeasuredGenerationCost() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                registries
                                        .lookupOrThrow(Registries.BIOME)
                                        .getOrThrow(Biomes.PLAINS)),
                        registries
                                .lookupOrThrow(Registries.NOISE_SETTINGS)
                                .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        Path directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        var csv =
                new StringBuilder(
                        "seed,direction,distance,x,z,originalHeight,beforeHeight,afterHeight,originalSolidCount,beforeSolidCount,afterSolidCount\n");
        var section = new StringBuilder("seed,x,z,original,before,after\n");
        long[] nanos = new long[3];
        int[] counts = new int[3];
        int rows = 0;
        for (long seed : new long[] {0, 12345, 987654321}) {
            RandomState[] states = new RandomState[3];
            for (int mode = 0; mode < states.length; mode++) {
                var state =
                        RandomState.create(
                                generator.generatorSettings().value(),
                                registries.lookupOrThrow(Registries.NOISE),
                                seed);
                if (mode == 1) installFrozen(state, seed);
                if (mode == 2)
                    ((RandomStateAccessor) (Object) state)
                            .mia$setRouter(
                                    OverworldAbyss.compose(
                                            state,
                                            state.router(),
                                            new OverworldAbyssShape(
                                                    320, -64, 320, seed, 1.0, 1.25)));
                states[mode] = state;
                // Warm all three graphs before measuring. Counts below exclude warmup.
                for (int x : new int[] {0, 400, 640, 960, 1048})
                    generator.getBaseColumn(x, 0, HEIGHT, state);
            }
            for (int direction = 0; direction < 4; direction++) {
                for (int distance = -1152; distance <= 1152; distance += 8) {
                    // One complete cardinal section; other sections focus on shaft, shoulder,
                    // ridge and both joins at a modest cost in normal test runs.
                    int radius = Math.abs(distance);
                    if (!(seed == 12345 && direction == 0)
                            && !(radius >= 304 && radius <= 352)
                            && !(radius >= 448 && radius <= 512)
                            && !(radius >= 608 && radius <= 680)
                            && !(radius >= 936 && radius <= 976)
                            && !(radius >= 1016 && radius <= 1064)) continue;
                    double angle = direction * Math.PI / 4.0;
                    int x = (int) Math.round(distance * Math.cos(angle));
                    int z = (int) Math.round(distance * Math.sin(angle));
                    int[] heights = new int[3];
                    int[] solids = new int[3];
                    boolean fullSection = seed == 12345 && direction == 0;
                    if (fullSection)
                        section.append(seed).append(',').append(x).append(',').append(z);
                    for (int mode = 0; mode < states.length; mode++) {
                        long start = System.nanoTime();
                        var column = generator.getBaseColumn(x, z, HEIGHT, states[mode]);
                        nanos[mode] += System.nanoTime() - start;
                        counts[mode]++;
                        heights[mode] = -64;
                        if (fullSection) section.append(',');
                        for (int y = -64; y < 320; y++) {
                            if (fullSection)
                                section.append(column.getBlock(y).blocksMotion() ? '1' : '0');
                            if (column.getBlock(y).blocksMotion()) {
                                solids[mode]++;
                                heights[mode] = y + 1;
                            }
                        }
                    }
                    if (fullSection) section.append('\n');
                    if (Math.hypot(x, z) >= 1040) {
                        assertEquals(heights[0], heights[2], "Outside height at " + x + "," + z);
                        assertEquals(solids[0], solids[2], "Outside solid count at " + x + "," + z);
                    }
                    csv.append(seed)
                            .append(',')
                            .append(direction * 45)
                            .append(',')
                            .append(distance)
                            .append(',')
                            .append(x)
                            .append(',')
                            .append(z);
                    for (int height : heights) csv.append(',').append(height);
                    for (int solid : solids) csv.append(',').append(solid);
                    csv.append('\n');
                    rows++;
                }
            }
        }
        Files.writeString(directory.resolve("terrain-profiles.csv"), csv);
        Files.writeString(directory.resolve("terrain-section.csv"), section);
        var json =
                new StringBuilder(
                        "{\n"
                            + "  \"source\": \"NoiseBasedChunkGenerator.getBaseColumn (NoiseChunk),"
                            + " before=frozen HEAD formula at effective R=320; heights are top"
                            + " motion-blocking block + 1; fluids excluded\",\n"
                            + "  \"rows\": "
                                + rows
                                + ",\n  \"warmupColumnsPerMode\": 15,\n  \"timings\": [\n");
        String[] labels = {"vanilla", "before", "after"};
        for (int i = 0; i < 3; i++) {
            json.append(
                    String.format(
                            Locale.ROOT,
                            "    {\"mode\":\"%s\",\"columns\":%d,\"milliseconds\":%.3f,\"relativeToVanilla\":%.4f}%s\n",
                            labels[i],
                            counts[i],
                            nanos[i] / 1e6,
                            (double) nanos[i] / nanos[0],
                            i == 2 ? "" : ","));
        }
        json.append("  ]\n}\n");
        Files.writeString(directory.resolve("terrain-profile-metrics.json"), json);
        assertTrue(rows > 500);
    }

    @SuppressWarnings("unchecked")
    private static void installFrozen(RandomState state, long seed)
            throws ReflectiveOperationException {
        var original = state.router();
        var surface = DensityFunctions.cache2d(original.preliminarySurfaceLevel());
        var shape = new FrozenOverworldAbyssShape(320, -64, 320, seed, 1.0, 1.25);
        var detail = new FrozenOverworldAbyssNoise(seed);
        var estimate =
                new FrozenOverworldAbyssHole(
                        original.finalDensity(), surface, surface, detail, shape, true);
        var terrain =
                new FrozenOverworldAbyssHole(
                        original.finalDensity(), estimate, surface, detail, shape, false);
        // Retain the production aquifer/original-surface hooks for a fair old/new comparison.
        // The graph is frozen, while the integration marker adapts its field for those hooks.
        var installed = OverworldAbyss.class.getDeclaredField("INSTALLED");
        installed.setAccessible(true);
        var markers = (java.util.Map<RandomState, OverworldAbyssHole>) installed.get(null);
        markers.put(
                state,
                new OverworldAbyssHole(
                        original.finalDensity(),
                        terrain,
                        surface,
                        DensityFunctions.zero(),
                        new OverworldAbyssShape(320, -64, 320, seed, 1.0, 1.25),
                        true,
                        true,
                        terrain));
        ((RandomStateAccessor) (Object) state)
                .mia$setRouter(
                        new NoiseRouter(
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
                                estimate,
                                terrain,
                                original.veinToggle(),
                                original.veinRidged(),
                                original.veinGap()));
    }
}
