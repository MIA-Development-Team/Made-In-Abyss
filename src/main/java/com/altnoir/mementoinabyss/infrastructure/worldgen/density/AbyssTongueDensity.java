package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

public final class AbyssTongueDensity implements DensityFunction {
    public static final KeyDispatchDataCodec<AbyssTongueDensity> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(f -> f.input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("support")
                                                            .forGetter(f -> f.support),
                                                    Codec.doubleRange(1, 65536)
                                                            .fieldOf("radius")
                                                            .forGetter(f -> f.radius),
                                                    Codec.BOOL
                                                            .fieldOf("solid")
                                                            .forGetter(f -> f.solid))
                                            .apply(i, AbyssTongueDensity::new)));

    private final DensityFunction input;
    private final DensityFunction support;
    private final double radius;
    private final boolean solid;
    private final @Nullable AbyssTongueShape shape;
    private final int hash;

    public AbyssTongueDensity(
            DensityFunction input, DensityFunction support, double radius, boolean solid) {
        this(input, support, radius, solid, null);
    }

    private AbyssTongueDensity(
            DensityFunction input,
            DensityFunction support,
            double radius,
            boolean solid,
            @Nullable AbyssTongueShape shape) {
        this.input = input;
        this.support = support;
        this.radius = radius;
        this.solid = solid;
        this.shape = shape;
        this.hash = java.util.Objects.hash(input, radius, solid, System.identityHashCode(shape));
    }

    public AbyssTongueDensity bind(
            long seed, double layoutRadius, Map<Double, AbyssTongueShape> shapes) {
        if (shape != null) return this;
        return new AbyssTongueDensity(
                input,
                support,
                layoutRadius,
                solid,
                shapes.computeIfAbsent(layoutRadius, r -> new AbyssTongueShape(seed, r, support)));
    }

    @Override
    public double compute(FunctionContext context) {
        if (solid) return shape == null ? -8 : shape.solid(context);
        double original = input.compute(context);
        return shape == null ? original : shape.carve(original, context);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        if (solid) {
            provider.fillAllDirectly(output, this);
        } else {
            input.fillArray(output, provider);
            if (shape != null)
                for (int i = 0; i < output.length; i++)
                    output[i] = shape.carve(output[i], provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // Support is used only at world binding. Mapping it again in NoiseChunk would install
        // unused interpolators for the entire original cave graph on every chunk.
        return visitor.apply(
                new AbyssTongueDensity(
                        input.mapAll(visitor),
                        shape == null ? support.mapAll(visitor) : support,
                        radius,
                        solid,
                        shape));
    }

    @Override
    public double minValue() {
        return solid ? -8 : Math.min(-8, input.minValue());
    }

    @Override
    public double maxValue() {
        return solid ? 8 : input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<AbyssTongueDensity> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof AbyssTongueDensity f
                        && radius == f.radius
                        && solid == f.solid
                        && shape == f.shape
                        && input.equals(f.input)
                        && (shape != null || support.equals(f.support));
    }
}
