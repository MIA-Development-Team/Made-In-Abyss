package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.ToDoubleFunction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssTerrainTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(64, -64, 320, 12345, 1, 1);
    private static final List<DensityFunction.FunctionContext> POINTS =
            List.of(
                    new DensityFunction.SinglePointContext(96, 5, 0),
                    new DensityFunction.SinglePointContext(160, 17, 0),
                    new DensityFunction.SinglePointContext(192, 30, 0),
                    new DensityFunction.SinglePointContext(200, 0, 0));

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void finalBlendArrayFillDelegatesToBothChildrenAndMatchesScalarEvaluation() {
        ToDoubleFunction<DensityFunction.FunctionContext> inputValue =
                context ->
                        0.2
                                + context.blockX() * 0.0005
                                + context.blockY() * 0.001
                                - context.blockZ() * 0.0003;
        ToDoubleFunction<DensityFunction.FunctionContext> baseValue =
                context ->
                        0.1
                                + context.blockX() * 0.001
                                - context.blockY() * 0.0005
                                + context.blockZ() * 0.0002;
        var input = new BulkOnly(inputValue, 0.0, 11.0 / 24.0);
        var baseNoise = new BulkOnly(baseValue, -1.0, 1.0);
        var terrain = new OverworldAbyssTerrain(input, baseNoise, SHAPE, true);
        double[] actual = new double[POINTS.size()];

        terrain.fillArray(actual, new Points(POINTS));

        assertEquals(1, input.arrayCalls);
        assertEquals(1, baseNoise.arrayCalls);
        assertEquals(0, input.scalarCalls);
        assertEquals(0, baseNoise.scalarCalls);
        var scalarTerrain =
                new OverworldAbyssTerrain(
                        new ScalarField(inputValue, 0.0, 11.0 / 24.0),
                        new ScalarField(baseValue, -1.0, 1.0),
                        SHAPE,
                        true);
        for (int i = 0; i < actual.length; i++)
            assertEquals(scalarTerrain.compute(POINTS.get(i)), actual[i], 1.0E-12);
    }

    @Test
    void rawArrayFillSamplesBaseNoiseAtTargetHeightAndMatchesScalarEvaluation() {
        ToDoubleFunction<DensityFunction.FunctionContext> inputValue =
                context ->
                        0.3
                                + context.blockX() * 0.0004
                                - context.blockY() * 0.001
                                + context.blockZ() * 0.0002;
        ToDoubleFunction<DensityFunction.FunctionContext> baseValue =
                context -> context.blockY() * 0.002 + context.blockX() * 0.0001;
        var input = new BulkOnly(inputValue, -1.0, 1.0);
        var baseNoise = new BulkOnly(baseValue, -1.0, 1.0);
        var terrain = new OverworldAbyssTerrain(input, baseNoise, SHAPE, false);
        double[] actual = new double[POINTS.size()];

        terrain.fillArray(actual, new Points(POINTS));

        assertEquals(1, input.arrayCalls);
        assertEquals(1, baseNoise.arrayCalls);
        assertEquals(0, input.scalarCalls);
        assertEquals(0, baseNoise.scalarCalls);
        var scalarTerrain =
                new OverworldAbyssTerrain(
                        new ScalarField(inputValue, -1.0, 1.0),
                        new ScalarField(baseValue, -1.0, 1.0),
                        SHAPE,
                        false);
        for (int i = 0; i < actual.length; i++)
            assertEquals(scalarTerrain.compute(POINTS.get(i)), actual[i], 1.0E-12);
    }

    private record Points(List<DensityFunction.FunctionContext> points)
            implements DensityFunction.ContextProvider {
        @Override
        public DensityFunction.FunctionContext forIndex(int index) {
            return points.get(index);
        }

        @Override
        public void fillAllDirectly(double[] output, DensityFunction function) {
            for (int i = 0; i < output.length; i++) output[i] = function.compute(forIndex(i));
        }
    }

    private static final class BulkOnly implements DensityFunction {
        private final ToDoubleFunction<DensityFunction.FunctionContext> value;
        private final double min, max;
        private int arrayCalls;
        private int scalarCalls;

        private BulkOnly(
                ToDoubleFunction<DensityFunction.FunctionContext> value, double min, double max) {
            this.value = value;
            this.min = min;
            this.max = max;
        }

        @Override
        public double compute(FunctionContext context) {
            scalarCalls++;
            throw new AssertionError("Bulk fill must not scalar-sample its child");
        }

        @Override
        public void fillArray(double[] output, ContextProvider provider) {
            arrayCalls++;
            for (int i = 0; i < output.length; i++)
                output[i] = value.applyAsDouble(provider.forIndex(i));
        }

        @Override
        public DensityFunction mapAll(Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public double minValue() {
            return min;
        }

        @Override
        public double maxValue() {
            return max;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.zero().codec();
        }
    }

    private record ScalarField(
            ToDoubleFunction<DensityFunction.FunctionContext> value, double min, double max)
            implements DensityFunction.SimpleFunction {
        @Override
        public double compute(FunctionContext context) {
            return value.applyAsDouble(context);
        }

        @Override
        public double minValue() {
            return min;
        }

        @Override
        public double maxValue() {
            return max;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.zero().codec();
        }
    }
}
