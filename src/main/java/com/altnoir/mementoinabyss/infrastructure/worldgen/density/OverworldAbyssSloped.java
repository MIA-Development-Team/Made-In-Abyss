package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Objects;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Height-normalized terrain input. Cave nodes consume it before their usual vanilla processing. */
public final class OverworldAbyssSloped implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssSloped> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(OverworldAbyssSloped::input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("base_noise")
                                                            .forGetter(
                                                                    OverworldAbyssSloped
                                                                            ::baseNoise),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("reference_height")
                                                            .forGetter(
                                                                    OverworldAbyssSloped
                                                                            ::referenceHeight),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(OverworldAbyssSloped::shape),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .optionalFieldOf(
                                                                    "factor",
                                                                    DensityFunctions.constant(5))
                                                            .forGetter(
                                                                    OverworldAbyssSloped::factor))
                                            .apply(i, OverworldAbyssSloped::new)));
    private final DensityFunction input, baseNoise, factor, referenceHeight;
    private final OverworldAbyssShape shape;
    private final int hash;
    private final ThreadLocal<Columns> columns = ThreadLocal.withInitial(Columns::new);

    public OverworldAbyssSloped(
            DensityFunction input,
            DensityFunction baseNoise,
            DensityFunction referenceHeight,
            OverworldAbyssShape shape,
            DensityFunction factor) {
        this.input = input;
        this.baseNoise = baseNoise;
        this.shape = shape;
        this.factor = factor;
        this.referenceHeight = referenceHeight;
        this.hash = Objects.hash(input, baseNoise, shape, factor, referenceHeight);
    }

    public DensityFunction input() {
        return input;
    }

    public DensityFunction baseNoise() {
        return baseNoise;
    }

    public OverworldAbyssShape shape() {
        return shape;
    }

    public DensityFunction factor() {
        return factor;
    }

    public DensityFunction referenceHeight() {
        return referenceHeight;
    }

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX(), z = context.blockZ();
        double distance = shape.distance(x, z),
                weight = shape.outerWeight(context.blockX(), context.blockZ());
        if (weight == 0) return input.compute(context);
        if (distance <= shape.radius()) return (distance - shape.radius() - 2) / 16.0;
        Columns cache = columns.get();
        if (cache.blender != context.getBlender()) {
            cache.values.clear();
            cache.blender = context.getBlender();
        }
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        Column column = cache.values.get(key);
        if (column == null) {
            double macro = shape.innerHeight(x, z);
            // Sample blended rock noise on the surface, once per column. The resulting terrain
            // gradient is monotone in Y, so it cannot create an isolated second cap above 256.
            double target =
                    macro
                            + 12
                                    * Math.tanh(
                                            baseNoise.compute(
                                                    OverworldAbyssInterpolated.context(
                                                            x,
                                                            (int) Math.round(macro),
                                                            z,
                                                            context.getBlender())));
            double slope = 1.0 / 24;
            if (weight < 1) {
                // Vanilla depth falls by 1/128 per block. Its quarter-negative stage and x4
                // cancel above the surface. Use the existing smooth 2D factor, not a noisy
                // numerical derivative which can approach zero and amplify tiny rock detail.
                slope = Math.clamp(Math.abs(factor.compute(context)) / 128.0, 1.0 / 512, 4.0);
            }
            double height =
                    weight == 1
                            ? target
                            : OverworldAbyssShape.lerp(
                                    weight, referenceHeight.compute(context), target);
            column = new Column(height, slope);
            if (cache.values.size() >= 256)
                cache.values.remove(cache.values.keySet().iterator().next());
            cache.values.put(key, column);
        }
        double local = (column.height - context.blockY());
        // A height field has one surface crossing. Do not blend the original 3D density through
        // the whole descent: extra zero crossings can collapse into a vertical outer wall.
        // Only the final two percent of radial weight restores the exact original volume;
        // by then the imposed height lift is a few blocks rather than an entire mountain.
        double edge = OverworldAbyssShape.smooth(weight / 0.02);
        double equivalent =
                edge == 1
                        ? local
                        : OverworldAbyssShape.lerp(
                                edge, input.compute(context) / column.slope, local);
        double density = equivalent * OverworldAbyssShape.lerp(edge, column.slope, 1.0 / 24);
        // Do not cap this input by radial distance: vanilla uses its magnitude to choose
        // entrances versus deep caves. The final hole wrapper applies the shaft boundary.
        return density;
    }

    @Override
    public void fillArray(double[] out, ContextProvider provider) {
        provider.fillAllDirectly(out, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssSloped(
                        input.mapAll(visitor),
                        baseNoise.mapAll(visitor),
                        referenceHeight.mapAll(visitor),
                        shape,
                        factor.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssSloped> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OverworldAbyssSloped that
                && input.equals(that.input)
                && baseNoise.equals(that.baseNoise)
                && shape.equals(that.shape)
                && factor.equals(that.factor)
                && referenceHeight.equals(that.referenceHeight);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private record Column(double height, double slope) {}

    private static final class Columns {
        Blender blender;
        final LinkedHashMap<Long, Column> values = new LinkedHashMap<>();
    }
}
