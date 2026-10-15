package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.Test;

/** A before/after profile of the actual JSON loading failure, at the reported seed and bearing. */
class OverworldAbyssJsonProfileTest {
    @Test
    void exportReportedSeedAlongThePlayersBearing() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = VanillaRegistries.createLookup();
        NoiseGeneratorSettings settings;
        try (var stream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "data/minecraft/worldgen/noise_settings/overworld.json")) {
            assertNotNull(stream);
            settings =
                    NoiseGeneratorSettings.DIRECT_CODEC
                            .parse(
                                    RegistryOps.create(JsonOps.INSTANCE, registries),
                                    JsonParser.parseReader(
                                            new InputStreamReader(stream, StandardCharsets.UTF_8)))
                            .getOrThrow();
        }
        long seed = -3641258292459704473L;
        var shape = new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25);
        var generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                registries
                                        .lookupOrThrow(Registries.BIOME)
                                        .getOrThrow(Biomes.ERODED_BADLANDS)),
                        Holder.direct(settings));
        RandomState[] states = new RandomState[3];
        for (int mode = 0; mode < states.length; mode++) {
            var state =
                    RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), seed);
            var router = state.router();
            if (mode == 1) {
                // Adding zero deliberately prevents recognition without changing the original
                // density: this recreates the fallback selected by the pre-fix JSON loader.
                router =
                        new NoiseRouter(
                                router.barrierNoise(),
                                router.fluidLevelFloodednessNoise(),
                                router.fluidLevelSpreadNoise(),
                                router.lavaNoise(),
                                router.temperature(),
                                router.vegetation(),
                                router.continents(),
                                router.erosion(),
                                router.depth(),
                                router.ridges(),
                                router.preliminarySurfaceLevel(),
                                DensityFunctions.add(
                                        router.finalDensity(), DensityFunctions.zero()),
                                router.veinToggle(),
                                router.veinRidged(),
                                router.veinGap());
            }
            if (mode != 0)
                ((RandomStateAccessor) (Object) state)
                        .mia$setRouter(OverworldAbyss.compose(state, router, shape));
            states[mode] = state;
        }
        var height = LevelHeightAccessor.create(-64, 384);
        var csv = new StringBuilder("distance,x,z,original,before,after\n");
        double angle = Math.atan2(660, 577);
        int outside = 0;
        for (int r = 624; r <= 1072; r += 4) {
            int x = (int) Math.round(r * Math.cos(angle)),
                    z = (int) Math.round(r * Math.sin(angle));
            csv.append(r).append(',').append(x).append(',').append(z);
            var original = generator.getBaseColumn(x, z, height, states[0]);
            for (int mode = 0; mode < states.length; mode++) {
                var column =
                        mode == 0 ? original : generator.getBaseColumn(x, z, height, states[mode]);
                csv.append(',');
                for (int y = -64; y < 320; y++) {
                    csv.append(column.getBlock(y).blocksMotion() ? '1' : '0');
                    if (mode == 2 && shape.distance(x, z) >= shape.outerLimit()) {
                        assertEquals(
                                original.getBlock(y), column.getBlock(y), "Outside output changed");
                        outside++;
                    }
                }
            }
            csv.append('\n');
        }
        assertTrue(outside > 0);
        assertTrue(
                generator.getBaseColumn(577, 660, height, states[2]).getBlock(217).isAir(),
                "The reported camera position must not be inside an added high terrain sheet");
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("reported-seed-section.csv"), csv);
    }
}
