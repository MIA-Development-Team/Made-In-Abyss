package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;

/** The immutable, seed-bound wall shared by the two dimension ends. */
public final class SharedAbyssWall implements DensityFunction.SimpleFunction {
    public static final double RELIEF = 24;
    public static final KeyDispatchDataCodec<SharedAbyssWall> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    Codec.DOUBLE
                                                            .fieldOf("layout_radius")
                                                            .forGetter(
                                                                    SharedAbyssWall::layoutRadius),
                                                    Codec.LONG
                                                            .optionalFieldOf("seed", 0L)
                                                            .forGetter(SharedAbyssWall::seed),
                                                    Codec.INT
                                                            .optionalFieldOf("phase_offset", 0)
                                                            .forGetter(
                                                                    SharedAbyssWall::phaseOffset),
                                                    Codec.BOOL
                                                            .optionalFieldOf("bound", false)
                                                            .forGetter(SharedAbyssWall::bound))
                                            .apply(i, SharedAbyssWall::new)));
    private final double layoutRadius;
    private final long seed;
    private final int phaseOffset;
    private final boolean bound;
    private final DensityFunction noise;

    private SharedAbyssWall(double layoutRadius, long seed, int phaseOffset, boolean bound) {
        this.layoutRadius = layoutRadius;
        this.seed = seed;
        this.phaseOffset = phaseOffset;
        this.bound = bound;
        this.noise =
                new OverworldAbyssInterpolated(
                        BlendedNoise.createUnseeded(.25, .25, 160, 160, 8)
                                .withNewRandom(
                                        new XoroshiroRandomSource(seed)
                                                .forkPositional()
                                                .fromHashOf(
                                                        MementoInAbyss.asResource(
                                                                "shared_abyss_wall"))),
                        4,
                        4);
    }

    public static SharedAbyssWall forOverworld(double layoutRadius, long seed, int phaseOffset) {
        return new SharedAbyssWall(layoutRadius, seed, phaseOffset, true);
    }

    public static SharedAbyssWall forAbyss(double layoutRadius, long seed) {
        return forOverworld(layoutRadius, seed, 0);
    }

    public static SharedAbyssWall unbound(double layoutRadius) {
        return new SharedAbyssWall(layoutRadius, 0, 0, false);
    }

    public SharedAbyssWall bind(long seed) {
        return bound && this.seed == seed
                ? this
                : new SharedAbyssWall(layoutRadius, seed, phaseOffset, true);
    }

    public SharedAbyssWall withSeed(long seed) {
        return bind(seed);
    }

    public double layoutRadius() {
        return layoutRadius;
    }

    public double baseRadius() {
        return layoutRadius + RELIEF;
    }

    public long seed() {
        return seed;
    }

    public int phaseOffset() {
        return phaseOffset;
    }

    public boolean bound() {
        return bound;
    }

    public double boundaryRadius(FunctionContext context) {
        // Sample a fixed cylinder: each horizontal ray has exactly one boundary.
        double distance = Math.hypot(context.blockX(), context.blockZ());
        double scale = distance == 0 ? 0 : baseRadius() / distance;
        double sample =
                noise.compute(
                        new SinglePointContext(
                                (int) Math.round(context.blockX() * scale),
                                context.blockY() + phaseOffset,
                                (int) Math.round(context.blockZ() * scale)));
        return baseRadius() + RELIEF * Math.tanh(3 * sample);
    }

    @Override
    public double compute(FunctionContext context) {
        return Math.clamp(
                (Math.hypot(context.blockX(), context.blockZ()) - boundaryRadius(context)) / 16,
                -5,
                6.25);
    }

    @Override
    public double minValue() {
        return -5;
    }

    @Override
    public double maxValue() {
        return 6.25;
    }

    @Override
    public KeyDispatchDataCodec<SharedAbyssWall> codec() {
        return CODEC;
    }
}
