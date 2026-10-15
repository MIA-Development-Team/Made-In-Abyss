package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssCliffDensityTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(320, -64, 320, 12345, 1, 1.25);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static DensityFunction.FunctionContext at(int x, int y) {
        return new DensityFunction.SinglePointContext(x, y, 0);
    }

    @Test
    void lowerWallVariesWithHeightAndFadesWithoutChangingUpperOrOuterTerrain() {
        var cliff =
                new OverworldAbyssCliff(
                        DensityFunctions.constant(10),
                        DensityFunctions.yClampedGradient(-64, 112, -0.6, 0.6),
                        SHAPE);
        assertTrue(cliff.compute(at(334, -64)) > 0);
        assertTrue(cliff.compute(at(334, 112)) < 0);
        for (int y : new int[] {-64, 0, 112, 127, 128, 129, 143}) {
            assertTrue(cliff.compute(at(322, y)) <= 0);
            assertTrue(cliff.compute(at(347, y)) > 0);
        }
        for (int y : new int[] {144, 145, 256}) assertEquals(10, cliff.compute(at(334, y)));
        for (int x : new int[] {352, 400, 640, 1040}) assertEquals(10, cliff.compute(at(x, 0)));
        assertTrue(Math.abs(cliff.compute(at(334, 127)) - cliff.compute(at(334, 129))) < 0.2);
        // A pure carve cannot create rock where the cave field already reports air.
        var air = new OverworldAbyssCliff(DensityFunctions.constant(-2), cliff.baseNoise(), SHAPE);
        assertEquals(-2, air.compute(at(334, 0)));
    }

    @Test
    void codecRoundTripAndVisitorKeepHeightDependentDetail() {
        var noise = DensityFunctions.yClampedGradient(-64, 112, -0.6, 0.6);
        var cliff = new OverworldAbyssCliff(DensityFunctions.constant(10), noise, SHAPE);
        var codec = DensityFunction.DIRECT_CODEC;
        var json = codec.encodeStart(JsonOps.INSTANCE, cliff).getOrThrow();
        var decoded = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        var mapped =
                cliff.mapAll(
                        function -> function == noise ? DensityFunctions.constant(0) : function);
        for (int y = -64; y < 160; y++)
            assertEquals(cliff.compute(at(334, y)), decoded.compute(at(334, y)));
        assertNotEquals(cliff.compute(at(334, -64)), mapped.compute(at(334, -64)));
    }

    @Test
    void roughWallFlagSurvivesCodecAndVisitorsWhileOldGraphsKeepTheirRadialGuard() {
        var rough =
                new OverworldAbyssHole(
                        DensityFunctions.zero(),
                        DensityFunctions.constant(1),
                        DensityFunctions.constant(70),
                        DensityFunctions.zero(),
                        SHAPE,
                        false,
                        true,
                        DensityFunctions.constant(1),
                        false,
                        true);
        var codec = OverworldAbyssHole.CODEC.codec().codec();
        var json = codec.encodeStart(JsonOps.INSTANCE, rough).getOrThrow();
        var decoded = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertTrue(decoded.roughWall());
        assertEquals(1, decoded.compute(at(321, 0)));
        assertEquals(1, rough.mapAll(function -> function).compute(at(321, 0)));
        assertEquals(-1, decoded.compute(at(320, 0)));
        json.getAsJsonObject().remove("rough_wall");
        var old = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertFalse(old.roughWall());
        assertTrue(old.compute(at(321, 0)) < 0);
    }

    @Test
    void bulkEvaluationUsesChildBulkContractAndAgreesWithPointSamples() {
        var child =
                new DensityFunctions.BeardifierOrMarker() {
                    @Override
                    public double compute(FunctionContext context) {
                        throw new AssertionError("Child must be filled in bulk");
                    }

                    @Override
                    public void fillArray(double[] output, ContextProvider provider) {
                        java.util.Arrays.fill(output, 10);
                    }

                    @Override
                    public double minValue() {
                        return 10;
                    }

                    @Override
                    public double maxValue() {
                        return 10;
                    }
                };
        var cliff = new OverworldAbyssCliff(child, DensityFunctions.constant(0.5), SHAPE);
        var point =
                new OverworldAbyssCliff(DensityFunctions.constant(10), cliff.baseNoise(), SHAPE);
        var provider =
                new DensityFunction.ContextProvider() {
                    @Override
                    public DensityFunction.FunctionContext forIndex(int i) {
                        return at(324 + i % 8 * 4, -64 + i / 8 * 8);
                    }

                    @Override
                    public void fillAllDirectly(double[] output, DensityFunction function) {
                        for (int i = 0; i < output.length; i++)
                            output[i] = function.compute(forIndex(i));
                    }
                };
        double[] values = new double[240];
        cliff.fillArray(values, provider);
        for (int i = 0; i < values.length; i++)
            assertEquals(point.compute(provider.forIndex(i)), values[i]);
    }
}
