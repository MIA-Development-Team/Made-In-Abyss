package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** A rough shaft wall applied after cave selection, before blending and interpolation. */
public record OverworldAbyssCliff(
        DensityFunction input,
        DensityFunction baseNoise,
        OverworldAbyssShape shape,
        Optional<SharedAbyssWall> sharedWall)
        implements DensityFunction {
    public static final int TRANSITION_BOTTOM = 112;
    public static final int TRANSITION_TOP = 144;
    public static final int RELIEF = 24;
    public static final int BAND = 32;
    public static final KeyDispatchDataCodec<OverworldAbyssCliff> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(OverworldAbyssCliff::input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("base_noise")
                                                            .forGetter(
                                                                    OverworldAbyssCliff::baseNoise),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(OverworldAbyssCliff::shape),
                                                    SharedAbyssWall.CODEC
                                                            .codec()
                                                            .codec()
                                                            .optionalFieldOf("shared_wall")
                                                            .forGetter(
                                                                    OverworldAbyssCliff
                                                                            ::sharedWall))
                                            .apply(i, OverworldAbyssCliff::new)));

    /** The original three-field codec remains readable with its original relief. */
    public OverworldAbyssCliff(
            DensityFunction input, DensityFunction baseNoise, OverworldAbyssShape shape) {
        this(input, baseNoise, shape, Optional.empty());
    }

    @Override
    public double compute(FunctionContext context) {
        return constrain(input.compute(context), context);
    }

    private double constrain(double original, FunctionContext context) {
        double distance = shape.distance(context.blockX(), context.blockZ());
        if (sharedWall.isPresent()) {
            if (distance >= shape.radius() + 64 || context.blockY() >= TRANSITION_TOP)
                return original;
            double fade =
                    1
                            - OverworldAbyssShape.smooth(
                                    (context.blockY() - TRANSITION_BOTTOM)
                                            / (double) (TRANSITION_TOP - TRANSITION_BOTTOM));
            double boundary =
                    OverworldAbyssShape.lerp(
                            fade, shape.wallRadius(), sharedWall.get().boundaryRadius(context));
            double raw = Math.clamp((distance - boundary) / 16, -5, 6.25);
            // This branch is already after vanilla processing: preserve that density scale.
            double d = Math.clamp(raw * 0.64, -1, 1);
            double processed = d / 2 - d * d * d / 24;
            return Math.min(original, processed);
        }
        if (distance <= shape.radius()
                || distance >= shape.radius() + BAND
                || context.blockY() >= TRANSITION_TOP) return original;
        double fade =
                1
                        - OverworldAbyssShape.smooth(
                                (context.blockY() - TRANSITION_BOTTOM)
                                        / (double) (TRANSITION_TOP - TRANSITION_BOTTOM));
        // Project onto one cylinder so noise is constant along a horizontal ray. Each height
        // then has a single rock boundary rather than alternating floating radial shells.
        double scale = (shape.radius() + RELIEF / 2.0) / distance;
        int x = (int) Math.round(context.blockX() * scale);
        int z = (int) Math.round(context.blockZ() * scale);
        double noise =
                baseNoise.compute(
                        OverworldAbyssInterpolated.context(
                                x, context.blockY(), z, context.getBlender()));
        double offset = fade * RELIEF * (0.5 + 0.5 * Math.tanh(3 * noise));
        double wall = Math.clamp(distance - shape.radius() - 2 - offset, -80, 100) / 16;
        return Math.min(original, wall);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        // Evaluate the child in bulk to preserve NoiseChunk CacheOnce's array semantics.
        input.fillArray(output, provider);
        for (int i = 0; i < output.length; i++)
            output[i] = constrain(output[i], provider.forIndex(i));
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssCliff(
                        input.mapAll(visitor),
                        baseNoise.mapAll(visitor),
                        shape,
                        sharedWall.map(wall -> (SharedAbyssWall) wall.mapAll(visitor))));
    }

    @Override
    public double minValue() {
        return Math.min(input.minValue(), -5);
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssCliff> codec() {
        return CODEC;
    }
}
