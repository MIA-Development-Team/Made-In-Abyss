package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssCliffTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(160, -64, 320, 12345, 1.0, 1.25);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static DensityFunction.FunctionContext at(int x, int y, int z) {
        return new DensityFunction.SinglePointContext(x, y, z);
    }

    private static OverworldAbyssHole hole(DensityFunction detail) {
        return new OverworldAbyssHole(
                DensityFunctions.constant(0.25),
                DensityFunctions.constant(70),
                DensityFunctions.constant(70),
                detail,
                SHAPE,
                false);
    }

    @Test
    void verticalWallExtendsToLevelShoulderAndCoreRadiusIsGuaranteed() {
        var plain = hole(DensityFunctions.zero());
        var rough = hole(DensityFunctions.constant(1));
        for (int y = -64; y <= 120; y += 8) {
            assertTrue(plain.compute(at(161, y, 0)) > 0, "wall at y=" + y);
            assertEquals(plain.compute(at(161, -64, 0)), plain.compute(at(161, y, 0)));
            assertEquals(-1, rough.compute(at(160, y, 0)));
        }
        assertEquals(128, SHAPE.innerHeight(160, 0));
        assertTrue(SHAPE.innerHeight(161, 0) - 128 < 0.001);
        assertTrue(plain.compute(at(161, 129, 0)) < 0);
    }

    @Test
    void shoulderUsesCubicProgressAndAcceleratesTowardCrest() {
        double previousRise = 0;
        for (int x : new int[] {180, 200, 220, 240, 260, 280, 300, 320}) {
            double t = (x - 160.0) / 160.0;
            double expected = 128 + (SHAPE.ridgeHeight(x, 0) - 128) * t * t * t;
            assertEquals(expected, SHAPE.innerHeight(x, 0), 1.0E-10);
            // Normalize out ridge-height variation to isolate the shoulder's curve.
            double rise = t * t * t - Math.pow(t - 0.125, 3);
            assertTrue(rise > previousRise);
            previousRise = rise;
        }
    }

    @Test
    void detailVariesInAllThreeAxesAndSeedRoundTrips() {
        var noise = new OverworldAbyssNoise(12345);
        var same = new OverworldAbyssNoise(12345);
        var other = new OverworldAbyssNoise(54321);
        var encoded =
                OverworldAbyssNoise.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, noise)
                        .getOrThrow();
        var decoded =
                OverworldAbyssNoise.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, encoded)
                        .getOrThrow();
        assertEquals(12345, decoded.seed());
        boolean xVariation = false, yVariation = false, zVariation = false, seedVariation = false;
        for (int i = 0; i < 20; i++) {
            var point = at(173 + i * 7, -40 + i * 9, 11 + i * 3);
            double value = noise.compute(point);
            assertEquals(value, same.compute(point));
            assertEquals(value, decoded.compute(point));
            assertTrue(value >= -1 && value <= 1);
            xVariation |=
                    value != noise.compute(at(point.blockX() + 7, point.blockY(), point.blockZ()));
            yVariation |=
                    value != noise.compute(at(point.blockX(), point.blockY() + 7, point.blockZ()));
            zVariation |=
                    value != noise.compute(at(point.blockX(), point.blockY(), point.blockZ() + 7));
            seedVariation |= value != other.compute(point);
        }
        assertTrue(xVariation && yVariation && zVariation && seedVariation);
    }

    @Test
    void noisyRockAboveNominalSurfaceSurvivesOpeningAndAquiferGuards() {
        var positive = hole(DensityFunctions.constant(1));
        var outcrop = at(200, (int) Math.ceil(SHAPE.innerHeight(200, 0)) + 4, 0);
        assertTrue(positive.compute(outcrop) > 0);
        assertFalse(positive.isOpening(outcrop));
        Aquifer delegate =
                new Aquifer() {
                    public BlockState computeSubstance(
                            DensityFunction.FunctionContext context, double density) {
                        return density > 0 ? null : Blocks.WATER.defaultBlockState();
                    }

                    public boolean shouldScheduleFluidUpdate() {
                        return true;
                    }
                };
        var aquifer = new AbyssAquifer(delegate, positive);
        assertNull(aquifer.computeSubstance(outcrop, positive.compute(outcrop)));
        assertTrue(aquifer.computeSubstance(at(0, 0, 0), 100).isAir());
        assertFalse(aquifer.shouldScheduleFluidUpdate());
        var empty = at(200, 200, 0);
        assertTrue(positive.isOpening(empty));
        assertTrue(aquifer.computeSubstance(empty, positive.compute(empty)).isAir());
        assertFalse(aquifer.shouldScheduleFluidUpdate());
        assertTrue(aquifer.computeSubstance(at(700, 0, 0), -1).is(Blocks.WATER));
        assertTrue(aquifer.shouldScheduleFluidUpdate());
    }

    @Test
    void noiseCannotRaiseInnerRidgeAboveCapOrAffectOuterColumns() {
        var positive = hole(DensityFunctions.constant(1));
        for (int x = 161; x <= 320; x += 7) {
            for (int y = 256; y < 320; y++) assertTrue(positive.compute(at(x, y, 0)) <= 0);
        }
        for (int x : new int[] {-700, -520, 520, 700}) {
            for (int y : new int[] {-64, 70, 256, 319})
                assertEquals(0.25, positive.compute(at(x, y, 0)));
        }
    }

    @Test
    void visitorMapsDetailAndMissingSerializedDetailDefaultsToZero() {
        var detail = DensityFunctions.constant(0.5);
        var original = hole(detail);
        var mapped =
                original.mapAll(
                        function -> function == detail ? DensityFunctions.constant(1) : function);
        assertTrue(mapped.compute(at(200, 130, 0)) > original.compute(at(200, 130, 0)));
        var encoded =
                OverworldAbyssHole.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, original)
                        .getOrThrow();
        var decoded =
                OverworldAbyssHole.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, encoded)
                        .getOrThrow();
        assertEquals(original.compute(at(200, 130, 0)), decoded.compute(at(200, 130, 0)));
        encoded.getAsJsonObject().remove("detail");
        var legacy =
                OverworldAbyssHole.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, encoded)
                        .getOrThrow();
        assertEquals(
                hole(DensityFunctions.zero()).compute(at(200, 130, 0)),
                legacy.compute(at(200, 130, 0)));
    }
}
