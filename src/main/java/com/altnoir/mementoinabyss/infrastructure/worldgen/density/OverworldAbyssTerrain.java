package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** A single radial transition, normally evaluated before vanilla interpolation and squeezing. */
public record OverworldAbyssTerrain(
        DensityFunction input,
        DensityFunction baseNoise,
        OverworldAbyssShape shape,
        boolean finalBlend,
        boolean localOnly)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssTerrain> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(
                                                                    OverworldAbyssTerrain::input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("base_noise")
                                                            .forGetter(
                                                                    OverworldAbyssTerrain
                                                                            ::baseNoise),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(
                                                                    OverworldAbyssTerrain::shape),
                                                    Codec.BOOL
                                                            .optionalFieldOf("final_blend", false)
                                                            .forGetter(
                                                                    OverworldAbyssTerrain
                                                                            ::finalBlend),
                                                    Codec.BOOL
                                                            .optionalFieldOf("local_only", false)
                                                            .forGetter(
                                                                    OverworldAbyssTerrain
                                                                            ::localOnly))
                                            .apply(instance, OverworldAbyssTerrain::new)));

    public OverworldAbyssTerrain(
            DensityFunction input,
            DensityFunction baseNoise,
            OverworldAbyssShape shape,
            boolean finalBlend) {
        this(input, baseNoise, shape, finalBlend, false);
    }

    @Override
    public double compute(FunctionContext context) {
        double distance = shape.distance(context.blockX(), context.blockZ());
        double weight = localOnly ? 1.0 : shape.outerWeight(context.blockX(), context.blockZ());
        if (weight == 0) return input.compute(context);
        double target;
        if (finalBlend) {
            // Unknown graphs retain their own processing. Match their positive density range;
            // do not squeeze or interpolate the original final field a second time.
            double maximum = input.maxValue();
            double scale =
                    Double.isFinite(maximum) && maximum > 0
                            ? Math.clamp(maximum / (11.0 / 24.0), 0.25, 16.0)
                            : 1.0;
            target = baseNoise.compute(context) * scale;
        } else {
            // The terrain gradient controls relief; the world's existing blended noise supplies
            // coherent rock variation in the same density units as vanilla sloped cheese.
            double height = shape.innerHeight(context.blockX(), context.blockZ());
            double vertical = (height - context.blockY()) / 24.0;
            double radial = (distance - shape.radius() - 2.0) / 16.0;
            double detail =
                    0.5
                            * Math.tanh(
                                    baseNoise.compute(
                                            OverworldAbyssInterpolated.context(
                                                    context.blockX(),
                                                    (int) Math.round(height),
                                                    context.blockZ(),
                                                    context.getBlender())));
            target = Math.min(radial, vertical + detail);
        }
        return weight == 1
                ? target
                : OverworldAbyssShape.lerp(weight, input.compute(context), target);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        int size = output.length;
        double[] weights = new double[size];
        boolean needsInput = false;
        boolean needsBaseNoise = false;
        for (int i = 0; i < size; i++) {
            FunctionContext context = provider.forIndex(i);
            double weight = localOnly ? 1.0 : shape.outerWeight(context.blockX(), context.blockZ());
            weights[i] = weight;
            needsInput |= weight != 1.0;
            needsBaseNoise |= weight != 0.0;
        }

        // Preserve bulk behavior of wrapped vanilla graph nodes such as CacheOnce.
        if (needsInput) input.fillArray(output, provider);

        double[] baseNoiseValues = needsBaseNoise ? new double[size] : null;
        if (needsBaseNoise) {
            ContextProvider baseNoiseProvider = finalBlend ? provider : atTargetHeight(provider);
            baseNoise.fillArray(baseNoiseValues, baseNoiseProvider);
        }

        double scale = 1.0;
        if (finalBlend) {
            double maximum = input.maxValue();
            scale =
                    Double.isFinite(maximum) && maximum > 0
                            ? Math.clamp(maximum / (11.0 / 24.0), 0.25, 16.0)
                            : 1.0;
        }

        for (int i = 0; i < size; i++) {
            double weight = weights[i];
            if (weight == 0.0) {
                continue;
            }

            FunctionContext context = provider.forIndex(i);
            double target;
            if (finalBlend) {
                target = baseNoiseValues[i] * scale;
            } else {
                double height = shape.innerHeight(context.blockX(), context.blockZ());
                double vertical = (height - context.blockY()) / 24.0;
                double radial =
                        (shape.distance(context.blockX(), context.blockZ()) - shape.radius() - 2.0)
                                / 16.0;
                double detail = 0.5 * Math.tanh(baseNoiseValues[i]);
                target = Math.min(radial, vertical + detail);
            }

            output[i] =
                    weight == 1.0 ? target : OverworldAbyssShape.lerp(weight, output[i], target);
        }
    }

    private ContextProvider atTargetHeight(ContextProvider provider) {
        return new ContextProvider() {
            @Override
            public FunctionContext forIndex(int index) {
                FunctionContext context = provider.forIndex(index);
                int height =
                        (int) Math.round(shape.innerHeight(context.blockX(), context.blockZ()));
                return OverworldAbyssInterpolated.context(
                        context.blockX(), height, context.blockZ(), context.getBlender());
            }

            @Override
            public void fillAllDirectly(double[] output, DensityFunction function) {
                for (int i = 0; i < output.length; i++) output[i] = function.compute(forIndex(i));
            }
        };
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssTerrain(
                        input.mapAll(visitor),
                        baseNoise.mapAll(visitor),
                        shape,
                        finalBlend,
                        localOnly));
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
    public KeyDispatchDataCodec<OverworldAbyssTerrain> codec() {
        return CODEC;
    }
}
