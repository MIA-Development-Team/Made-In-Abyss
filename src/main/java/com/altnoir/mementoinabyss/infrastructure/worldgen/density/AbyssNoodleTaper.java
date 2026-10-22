package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Erode the solid noodle field toward the centre, without cutting a cylindrical boundary. */
public final class AbyssNoodleTaper implements DensityFunction {
    public static final KeyDispatchDataCodec<AbyssNoodleTaper> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(f -> f.input),
                                                    Codec.doubleRange(1, 65536)
                                                            .fieldOf("radius")
                                                            .forGetter(f -> f.radius),
                                                    Codec.LONG
                                                            .optionalFieldOf("seed", 0L)
                                                            .forGetter(f -> f.seed),
                                                    Codec.BOOL
                                                            .optionalFieldOf("bound", false)
                                                            .forGetter(f -> f.bound))
                                            .apply(i, AbyssNoodleTaper::new)));
    private final DensityFunction input;
    private final double radius;
    private final long seed;
    private final boolean bound;
    private final NormalNoise noise;
    private final double erosion;
    private final int hash;

    public AbyssNoodleTaper(DensityFunction input, double radius) {
        this(input, radius, 0, false);
    }

    private AbyssNoodleTaper(DensityFunction input, double radius, long seed, boolean bound) {
        this(
                input,
                radius,
                seed,
                bound,
                NormalNoise.create(
                        new XoroshiroRandomSource(seed)
                                .forkPositional()
                                .fromHashOf(MementoInAbyss.asResource("abyss_noodle_core")),
                        -4,
                        1,
                        .5));
    }

    private AbyssNoodleTaper(
            DensityFunction input, double radius, long seed, boolean bound, NormalNoise noise) {
        this.input = input;
        this.radius = radius;
        this.seed = seed;
        this.bound = bound;
        this.noise = noise;
        erosion = Math.clamp(input.maxValue() + .05, .4, .75);
        hash = java.util.Objects.hash(input, radius, seed, bound);
    }

    public AbyssNoodleTaper bind(long seed, double radius) {
        return bound && this.seed == seed && this.radius == radius
                ? this
                : new AbyssNoodleTaper(input, radius, seed, true);
    }

    private double taper(double original, FunctionContext c) {
        // The old field activated at 0.05. Moving that threshold to its zero crossing
        // preserves the distant solid shape while allowing continuous erosion near the core.
        original -= .05;
        if (original <= -.75) return original;
        double distance = Math.hypot(c.blockX(), c.blockZ());
        // The noise has a bounded amplitude; outside 0.3125R there is no core erosion.
        if (distance >= radius * .3125) return original;
        double offset =
                radius
                        * .0125
                        * Math.clamp(
                                noise.getValue(c.blockX(), c.blockY() * .5, c.blockZ()), -1, 1);
        double t = Math.clamp((distance - radius * .16 - offset) / (radius * .14), 0, 1);
        double smooth = t * t * (3 - 2 * t);
        double activity = Math.clamp((original + .75) / .5, 0, 1);
        activity = activity * activity * (3 - 2 * activity);
        return original - erosion * (1 - smooth) * activity;
    }

    @Override
    public double compute(FunctionContext c) {
        return taper(input.compute(c), c);
    }

    @Override
    public void fillArray(double[] out, ContextProvider provider) {
        input.fillArray(out, provider);
        for (int i = 0; i < out.length; i++) out[i] = taper(out[i], provider.forIndex(i));
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new AbyssNoodleTaper(input.mapAll(visitor), radius, seed, bound, noise));
    }

    @Override
    public double minValue() {
        return input.minValue() - .05 - erosion;
    }

    @Override
    public double maxValue() {
        return input.maxValue() - .05;
    }

    @Override
    public KeyDispatchDataCodec<AbyssNoodleTaper> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof AbyssNoodleTaper f
                        && radius == f.radius
                        && seed == f.seed
                        && bound == f.bound
                        && input.equals(f.input);
    }
}
