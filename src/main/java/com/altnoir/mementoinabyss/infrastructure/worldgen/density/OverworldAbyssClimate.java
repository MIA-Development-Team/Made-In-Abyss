package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Local climate correction; the original biome parameter map remains in charge of selection. */
public record OverworldAbyssClimate(
        DensityFunction input, OverworldAbyssShape shape, Component component)
        implements DensityFunction.SimpleFunction {
    public enum Component {
        CONTINENTS,
        EROSION,
        DEPTH,
        RIDGES
    }

    public static final KeyDispatchDataCodec<OverworldAbyssClimate> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(
                                                                    OverworldAbyssClimate::input),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(
                                                                    OverworldAbyssClimate::shape),
                                                    Codec.STRING
                                                            .xmap(
                                                                    Component::valueOf,
                                                                    Component::name)
                                                            .fieldOf("component")
                                                            .forGetter(
                                                                    OverworldAbyssClimate
                                                                            ::component))
                                            .apply(instance, OverworldAbyssClimate::new)));

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX(), z = context.blockZ();
        double distance = shape.distance(x, z);
        double weight = shape.outerWeight(distance);
        double original = input.compute(context);
        if (weight <= 0) return original;
        double height = shape.innerHeight(x, z);
        double mountain = OverworldAbyssShape.smooth((height - 144.0) / 80.0);
        double target =
                switch (component) {
                    case CONTINENTS -> Math.max(original, 0.1 + mountain * 0.4);
                    case EROSION -> OverworldAbyssShape.lerp(mountain, original, -0.85);
                    case RIDGES ->
                            OverworldAbyssShape.lerp(
                                    mountain, original, Math.copySign(0.65, original));
                    case DEPTH -> (height - context.blockY()) / 128.0;
                };
        return OverworldAbyssShape.lerp(weight, original, target);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new OverworldAbyssClimate(input.mapAll(visitor), shape, component));
    }

    @Override
    public double minValue() {
        return Math.min(input.minValue(), -256);
    }

    @Override
    public double maxValue() {
        return Math.max(input.maxValue(), 256);
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssClimate> codec() {
        return CODEC;
    }
}
