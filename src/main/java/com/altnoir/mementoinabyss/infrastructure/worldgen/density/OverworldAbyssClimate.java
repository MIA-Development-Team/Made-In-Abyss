package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Local climate correction; the original biome parameter map remains in charge of selection. */
public record OverworldAbyssClimate(
        DensityFunction input,
        OverworldAbyssShape shape,
        Component component,
        Optional<DensityFunction> surface)
        implements DensityFunction.SimpleFunction {
    public enum Component {
        TEMPERATURE,
        HUMIDITY,
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
                                                                            ::component),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .optionalFieldOf("terrain_surface")
                                                            .forGetter(
                                                                    OverworldAbyssClimate::surface))
                                            .apply(instance, OverworldAbyssClimate::new)));

    public OverworldAbyssClimate(
            DensityFunction input, OverworldAbyssShape shape, Component component) {
        this(input, shape, component, Optional.empty());
    }

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX(), z = context.blockZ();
        double weight = shape.outerWeight(context.blockX(), context.blockZ());
        double original = input.compute(context);
        if (weight <= 0) return original;
        if (component == Component.TEMPERATURE || component == Component.HUMIDITY) {
            double warmed = original + 0.06;
            double target =
                    component == Component.TEMPERATURE
                            ? (warmed <= 0.4 ? warmed : 0.4 + 0.1 * Math.tanh((warmed - 0.4) / 0.1))
                            : 0.88 * original + 0.12;
            return OverworldAbyssShape.lerp(
                    OverworldAbyssShape.smooth(weight / 0.15), original, target);
        }
        double height = shape.innerHeight(x, z);
        if (surface.isPresent()) height = surface.get().compute(context);
        double mountain = OverworldAbyssShape.smooth((height - 144.0) / 80.0);
        double target =
                switch (component) {
                    case CONTINENTS -> Math.max(original, 0.1 + mountain * 0.4);
                    case EROSION -> OverworldAbyssShape.lerp(mountain, original, -0.85);
                    case RIDGES ->
                            OverworldAbyssShape.lerp(
                                    mountain, original, Math.copySign(0.65, original));
                    case DEPTH -> (height - context.blockY()) / 128.0;
                    case TEMPERATURE, HUMIDITY -> throw new AssertionError(component);
                };
        return OverworldAbyssShape.lerp(weight, original, target);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssClimate(
                        input.mapAll(visitor),
                        shape,
                        component,
                        surface.map(function -> function.mapAll(visitor))));
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
