package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCliff;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
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

/** Actual NoiseChunk columns around the shaft, plus the separate pre-interpolation wall envelope. */
class OverworldAbyssWallProfileTest {
    @Test
    void exportSeededWallsAndCheckOpenCoreHeightVariationAndUnaffectedTerrain() throws Exception {
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
        var generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                registries
                                        .lookupOrThrow(Registries.BIOME)
                                        .getOrThrow(Biomes.PLAINS)),
                        Holder.direct(settings));
        var height = LevelHeightAccessor.create(-64, 384);
        var columns = new StringBuilder("seed,angle,r,x,z,blocks\n");
        var envelopes = new StringBuilder("seed,angle,y,radius\n");
        var metrics = new StringBuilder("seed,maxVerticalRelief,coreAirSamples,columns\n");
        for (long seed : new long[] {-3641258292459704473L, 12345, 987654321}) {
            var shape = new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25);
            var state =
                    RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), seed);
            var router = OverworldAbyss.compose(state, state.router(), shape);
            ((RandomStateAccessor) (Object) state).mia$setRouter(router);
            var hole = OverworldAbyss.holeOf(state);
            assertNotNull(hole);
            assertTrue(hole.roughWall());
            var cliff = new AtomicReference<OverworldAbyssCliff>();
            hole.sampledTerrain()
                    .mapAll(
                            function -> {
                                if (function instanceof OverworldAbyssCliff wall) cliff.set(wall);
                                return function;
                            });
            assertNotNull(cliff.get());
            var envelope = cliff.get().sharedWall().orElseThrow();
            var withoutWall =
                    hole.sampledTerrain()
                            .mapAll(
                                    function ->
                                            function instanceof OverworldAbyssCliff wall
                                                    ? wall.input()
                                                    : function);
            double maxVerticalRelief = 0;
            int coreSamples = 0, sampledColumns = 0;
            for (int angle = 0; angle < 360; angle += 15) {
                double radians = Math.toRadians(angle);
                int sampleX = (int) Math.round(336 * Math.cos(radians));
                int sampleZ = (int) Math.round(336 * Math.sin(radians));
                double minRadius = Double.POSITIVE_INFINITY, maxRadius = 0;
                for (int y = -64; y < 144; y += 2) {
                    var point = new DensityFunction.SinglePointContext(sampleX, y, sampleZ);
                    double fade = 1 - OverworldAbyssShape.smooth((y - 112) / 32.0);
                    double radius =
                            OverworldAbyssShape.lerp(
                                    fade, shape.wallRadius(), envelope.boundaryRadius(point));
                    assertTrue(
                            radius >= 320 && radius <= 368,
                            "Shared wall relief must remain bounded");
                    envelopes
                            .append(seed)
                            .append(',')
                            .append(angle)
                            .append(',')
                            .append(y)
                            .append(',')
                            .append(radius)
                            .append('\n');
                    if (y <= 104) {
                        minRadius = Math.min(minRadius, radius);
                        maxRadius = Math.max(maxRadius, radius);
                    }
                }
                maxVerticalRelief = Math.max(maxVerticalRelief, maxRadius - minRadius);
                for (int r = 312; r <= 392; r += 4) {
                    int x = (int) Math.round(r * Math.cos(radians));
                    int z = (int) Math.round(r * Math.sin(radians));
                    var column = generator.getBaseColumn(x, z, height, state);
                    sampledColumns++;
                    columns.append(seed)
                            .append(',')
                            .append(angle)
                            .append(',')
                            .append(r)
                            .append(',')
                            .append(x)
                            .append(',')
                            .append(z)
                            .append(',');
                    for (int y = -64; y < 320; y++) {
                        var block = column.getBlock(y);
                        columns.append(block.blocksMotion() ? '1' : '0');
                        if (shape.distance(x, z) <= shape.radius()) {
                            assertTrue(block.isAir(), "Guaranteed shaft core was refilled");
                            coreSamples++;
                        }
                    }
                    columns.append('\n');
                }
                for (int y : new int[] {-48, 0, 64, 112, 128, 144, 152, 256}) {
                    for (int r : new int[] {336, 400, 640, 1040}) {
                        if (r == 336 && y < 144) continue;
                        var point =
                                new DensityFunction.SinglePointContext(
                                        (int) Math.round(r * Math.cos(radians)),
                                        y,
                                        (int) Math.round(r * Math.sin(radians)));
                        assertEquals(
                                withoutWall.compute(point),
                                hole.sampledTerrain().compute(point),
                                0,
                                "Cliff detail escaped its local height/radius band");
                    }
                }
            }
            assertTrue(maxVerticalRelief > 2, "Wall detail must actually vary along Y: " + seed);
            assertTrue(coreSamples > 0);
            metrics.append(seed)
                    .append(',')
                    .append(maxVerticalRelief)
                    .append(',')
                    .append(coreSamples)
                    .append(',')
                    .append(sampledColumns)
                    .append('\n');
        }
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("wall-columns.csv"), columns);
        Files.writeString(directory.resolve("wall-envelope.csv"), envelopes);
        Files.writeString(directory.resolve("wall-metrics.csv"), metrics);
    }
}
