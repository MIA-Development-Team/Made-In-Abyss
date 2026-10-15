package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Raise the vanilla sky fade locally, retaining its original value outside the radial transition. */
public record OverworldAbyssSlide(DensityFunction input, OverworldAbyssShape shape)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssSlide> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(OverworldAbyssSlide::input),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(OverworldAbyssSlide::shape))
                                            .apply(i, OverworldAbyssSlide::new)));

    @Override
    public double compute(FunctionContext context) {
        double weight = shape.outerWeight(shape.distance(context.blockX(), context.blockZ()));
        double original = input.compute(context);
        if (weight == 0) return original;
        double raised = 1 - Math.clamp((context.blockY() - (shape.maxY() - 32.0)) / 24.0, 0, 1);
        return OverworldAbyssShape.lerp(weight, original, raised);
    }

    @Override
    public void fillArray(double[] out, ContextProvider provider) {
        provider.fillAllDirectly(out, this);
    }

    @Override
    public DensityFunction mapAll(Visitor v) {
        return v.apply(new OverworldAbyssSlide(input.mapAll(v), shape));
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssSlide> codec() {
        return CODEC;
    }
}
