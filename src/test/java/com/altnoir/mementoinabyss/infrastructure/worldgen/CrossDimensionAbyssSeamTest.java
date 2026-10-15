package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.*;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.*;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Real packaged routers, RandomState noise binding and NoiseChunk base columns on both ends. */
class CrossDimensionAbyssSeamTest {
    private static final long[] SEEDS = {-3641258292459704473L, 12345L, 987654321L};
    private static final int[] ANGLES = {0, 45, 90, 135, 180, 225, 270, 315};
    static HolderLookup.Provider registries;
    static NoiseGeneratorSettings overworldSettings;
    static NoiseGeneratorSettings abyssSettings;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var vanilla = VanillaRegistries.createLookup();
        registries =
                net.minecraft.data.registries.RegistryPatchGenerator.createLookup(
                                java.util.concurrent.CompletableFuture.completedFuture(vanilla),
                                new RegistrySetBuilder()
                                        .add(Registries.NOISE, MiaNoiseData::bootstrap)
                                        .add(
                                                Registries.DENSITY_FUNCTION,
                                                MiaDensityFunctions::bootstrap))
                        .join()
                        .full();
        overworldSettings = load("minecraft", "overworld");
        abyssSettings = load("mementoinabyss", "the_abyss");
        assertEquals(
                384, abyssSettings.noiseSettings().minY() + abyssSettings.noiseSettings().height());
    }

    private static NoiseGeneratorSettings load(String namespace, String name) throws Exception {
        String resource = "data/" + namespace + "/worldgen/noise_settings/" + name + ".json";
        try (var stream =
                CrossDimensionAbyssSeamTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            return NoiseGeneratorSettings.DIRECT_CODEC
                    .parse(
                            RegistryOps.create(JsonOps.INSTANCE, registries),
                            JsonParser.parseReader(
                                    new InputStreamReader(stream, StandardCharsets.UTF_8)))
                    .getOrThrow();
        }
    }

    @Test
    void physicalCoordinatesShareTheSameFieldWithoutQuantizedRadius() {
        for (long seed : SEEDS) {
            var ow = SharedAbyssWall.forOverworld(320, seed, 448);
            var abyss = SharedAbyssWall.forAbyss(320, seed);
            int fractionalBoundaries = 0;
            for (int angle : ANGLES)
                for (int y = -112; y <= -48; y++) {
                    int x = (int) Math.round(344 * Math.cos(Math.toRadians(angle)));
                    int z = (int) Math.round(344 * Math.sin(Math.toRadians(angle)));
                    var physical = new DensityFunction.SinglePointContext(x, y, z);
                    var mapped = new DensityFunction.SinglePointContext(x, y + 448, z);
                    assertEquals(ow.boundaryRadius(physical), abyss.boundaryRadius(mapped), 1e-9);
                    assertEquals(ow.compute(physical), abyss.compute(mapped), 1e-9);
                    double radius = ow.boundaryRadius(physical);
                    assertEquals((Math.hypot(x, z) - radius) / 16, ow.compute(physical), 1e-9);
                    if (Math.abs(radius / 8 - Math.rint(radius / 8)) > 1e-6) fractionalBoundaries++;
                }
            assertTrue(fractionalBoundaries > 400, "Boundary must not snap to 8/16-block radii");
            // Adjacent seam blocks have different physical heights: compare each to its
            // continuation.
            for (int y : new int[] {-65, -64}) {
                assertEquals(
                        abyss.compute(new DensityFunction.SinglePointContext(-344, y + 448, 0)),
                        ow.compute(new DensityFunction.SinglePointContext(-344, y, 0)),
                        1e-9);
            }
        }
    }

    @Test
    void actualNoiseChunkColumnsHaveOpenCoreAndSolidRoofOutsideSharedWall() throws Exception {
        var csv = new StringBuilder("seed,angle,r,x,z,dimension,min_display_y,blocks\n");
        for (long seed : SEEDS) {
            var ow =
                    RandomState.create(
                            overworldSettings, registries.lookupOrThrow(Registries.NOISE), seed);
            var shape = new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25, 384);
            ((RandomStateAccessor) (Object) ow)
                    .mia$setRouter(OverworldAbyss.compose(ow, ow.router(), shape));
            var abyss =
                    RandomState.create(
                            abyssSettings, registries.lookupOrThrow(Registries.NOISE), seed);
            var owSeam = AbyssSeamRuntime.seamOf(ow);
            var abyssSeam = AbyssSeamRuntime.seamOf(abyss);
            assertNotNull(owSeam, "Installed overworld router must contain shared seam");
            assertNotNull(abyssSeam, "Generated abyss JSON must contain shared seam");
            assertEquals(seed, abyssSeam.wall().seed(), "RandomState must bind actual world seed");
            var owGenerator = generator(overworldSettings);
            var abyssGenerator = generator(abyssSettings);
            var wall = SharedAbyssWall.forAbyss(320, seed);
            int airChecked = 0, rockChecked = 0;
            for (int angle : ANGLES)
                for (int r = 304; r <= 392; r++) {
                    int x = (int) Math.round(r * Math.cos(Math.toRadians(angle)));
                    int z = (int) Math.round(r * Math.sin(Math.toRadians(angle)));
                    var owColumn =
                            owGenerator.getBaseColumn(
                                    x, z, LevelHeightAccessor.create(-64, 384), ow);
                    var abyssColumn =
                            abyssGenerator.getBaseColumn(
                                    x, z, LevelHeightAccessor.create(-256, 640), abyss);
                    csv.append(seed)
                            .append(',')
                            .append(angle)
                            .append(',')
                            .append(r)
                            .append(',')
                            .append(x)
                            .append(',')
                            .append(z)
                            .append(",overworld,-64,");
                    for (int y = -64; y <= 32; y++)
                        csv.append(owColumn.getBlock(y).blocksMotion() ? '1' : '0');
                    csv.append('\n')
                            .append(seed)
                            .append(',')
                            .append(angle)
                            .append(',')
                            .append(r)
                            .append(',')
                            .append(x)
                            .append(',')
                            .append(z)
                            .append(",abyss,-112,");
                    for (int y = 336; y <= 383; y++)
                        csv.append(abyssColumn.getBlock(y).blocksMotion() ? '1' : '0');
                    csv.append('\n');
                    for (int y = 368; y <= 383; y++) {
                        double density =
                                wall.compute(new DensityFunction.SinglePointContext(x, y, z));
                        if (density > .125) {
                            assertTrue(
                                    abyssColumn.getBlock(y).blocksMotion(),
                                    "Roof rock seed=" + seed + " x=" + x + " y=" + y + " z=" + z);
                            rockChecked++;
                        } else if (density < -.125) {
                            assertTrue(
                                    abyssColumn.getBlock(y).isAir(),
                                    "Roof opening seed="
                                            + seed
                                            + " x="
                                            + x
                                            + " y="
                                            + y
                                            + " z="
                                            + z);
                            airChecked++;
                        }
                    }
                    for (int y = -64; y < -48; y++) {
                        var context = new DensityFunction.SinglePointContext(x, y, z);
                        var continuation = new DensityFunction.SinglePointContext(x, y + 448, z);
                        assertEquals(
                                owSeam.wall().compute(context),
                                abyssSeam.wall().compute(continuation),
                                1e-9,
                                "Actual loaded routers must share the same field at the same"
                                        + " physical point");
                        double density = owSeam.wall().compute(context);
                        if (r <= 366 && density > .125)
                            assertTrue(
                                    owColumn.getBlock(y).blocksMotion(),
                                    "OW bottom must continue rock");
                        else if (density < -.125)
                            assertTrue(
                                    owColumn.getBlock(y).isAir(),
                                    "OW bottom must continue opening");
                    }
                }
            assertTrue(airChecked > 1000);
            assertTrue(rockChecked > 1000);
        }
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("cross-dimension-seam.csv"), csv);
    }

    static NoiseBasedChunkGenerator generator(NoiseGeneratorSettings settings) {
        return new NoiseBasedChunkGenerator(
                new FixedBiomeSource(
                        registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),
                Holder.direct(settings));
    }
}
