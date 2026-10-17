package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Keep a continuous surface skin while retaining the world's cave graph deeper in the mountain. */
public record OverworldAbyssCaveGuard(
        DensityFunction input,
        DensityFunction solid,
        DensityFunction depth,
        OverworldAbyssShape shape)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssCaveGuard> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(
                                                                    OverworldAbyssCaveGuard::input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("solid")
                                                            .forGetter(
                                                                    OverworldAbyssCaveGuard::solid),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("depth")
                                                            .forGetter(
                                                                    OverworldAbyssCaveGuard::depth),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(
                                                                    OverworldAbyssCaveGuard::shape))
                                            .apply(i, OverworldAbyssCaveGuard::new)));

    @Override
    public double compute(FunctionContext context) {
        double weight = shape.outerWeight(context.blockX(), context.blockZ());
        if (weight == 0) return input.compute(context);
        double depthBlocks = depth.compute(context) * 24;
        double skin =
                (1 - OverworldAbyssShape.smooth((depthBlocks - 8) / 16))
                        * OverworldAbyssShape.smooth(weight / 0.05);
        if (skin == 0) return input.compute(context);
        if (skin == 1) return solid.compute(context);
        return OverworldAbyssShape.lerp(skin, input.compute(context), solid.compute(context));
    }

    @Override
    public void fillArray(double[] out, ContextProvider p) {
        double[] solidValues = new double[out.length], depthValues = new double[out.length];
        input.fillArray(out, p);
        solid.fillArray(solidValues, p);
        depth.fillArray(depthValues, p);
        for (int i = 0; i < out.length; i++) {
            var context = p.forIndex(i);
            double weight = shape.outerWeight(context.blockX(), context.blockZ());
            double skin =
                    (1 - OverworldAbyssShape.smooth((depthValues[i] * 24 - 8) / 16))
                            * OverworldAbyssShape.smooth(weight / 0.05);
            out[i] = OverworldAbyssShape.lerp(skin, out[i], solidValues[i]);
        }
    }

    @Override
    public DensityFunction mapAll(Visitor v) {
        return v.apply(
                new OverworldAbyssCaveGuard(
                        input.mapAll(v), solid.mapAll(v), depth.mapAll(v), shape));
    }

    @Override
    public double minValue() {
        return Math.min(input.minValue(), solid.minValue());
    }

    @Override
    public double maxValue() {
        return Math.max(input.maxValue(), solid.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssCaveGuard> codec() {
        return CODEC;
    }
}
