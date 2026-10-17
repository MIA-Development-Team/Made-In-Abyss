package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;

/** Immutable world-local geometry; radius is the effective overworld radius (twice config). */
public final class OverworldAbyssShape {
    private final int radius, minY, maxY, abyssMaxY;
    private final long seed;
    private final double outerSteepness, slopeBand;
    private final int hash;
    private final OverworldAbyssMountains mountains;

    /**
     * Range of the outer slope's shape exponent. One is the plain smoothstep fall; below one holds the
     * ring drop earlier in the band; above one holds its height further out before descending.
     */
    public static final double MIN_OUTER_STEEPNESS = 0.5;

    public static final double MAX_OUTER_STEEPNESS = 3.0;

    /**
     * Shape exponent of the outer slope; see {@link #MIN_OUTER_STEEPNESS} for what moving it does. It
     * changes the profile of the descent, never its length or its average gradient: those are set by
     * {@link #slopeBand}.
     */
    public static final double DEFAULT_OUTER_STEEPNESS = 1.0;

    /**
     * Narrowest band the slope may descend over, in radii. The band cannot be narrower than the ground
     * the blend has to cover: the weight has to reach zero exactly where the blend lands on the
     * surrounding terrain, so a band that ends before the carve does leaves a cliff at the boundary.
     */
    public static final double MIN_SLOPE_BAND = 1.0;

    public static final double MAX_SLOPE_BAND = 2.0;

    /**
     * Width of the nominal descent, in radii, measured from the mean ridge at {@code 2 * radius}.
     * Local crests meander inside this band; all slopes land at the same outer limit.
     *
     * <p>The final slope also depends on the original density and local 3D rock detail.
     */
    public static final double DEFAULT_SLOPE_BAND = 1.25;

    public static final Codec<OverworldAbyssShape> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            Codec.intRange(64, 20480)
                                                    .fieldOf("radius")
                                                    .forGetter(OverworldAbyssShape::radius),
                                            Codec.INT
                                                    .fieldOf("min_y")
                                                    .forGetter(OverworldAbyssShape::minY),
                                            Codec.INT
                                                    .fieldOf("max_y")
                                                    .forGetter(OverworldAbyssShape::maxY),
                                            Codec.LONG
                                                    .fieldOf("seed")
                                                    .forGetter(OverworldAbyssShape::seed),
                                            Codec.DOUBLE
                                                    .fieldOf("outer_steepness")
                                                    .forGetter(OverworldAbyssShape::outerSteepness),
                                            Codec.DOUBLE
                                                    .fieldOf("slope_band")
                                                    .forGetter(OverworldAbyssShape::slopeBand),
                                            Codec.INT
                                                    .optionalFieldOf("abyss_max_y", 384)
                                                    .forGetter(OverworldAbyssShape::abyssMaxY))
                                    .apply(instance, OverworldAbyssShape::new));

    public OverworldAbyssShape(
            int radius,
            int minY,
            int maxY,
            long seed,
            double outerSteepness,
            double slopeBand,
            int abyssMaxY) {
        if (radius < 64 || radius > 20480 || minY >= maxY) {
            throw new IllegalArgumentException("Invalid overworld abyss geometry");
        }
        this.radius = radius;
        this.minY = minY;
        this.maxY = maxY;
        this.seed = seed;
        this.abyssMaxY = abyssMaxY;
        this.outerSteepness = Math.clamp(outerSteepness, MIN_OUTER_STEEPNESS, MAX_OUTER_STEEPNESS);
        this.slopeBand = Math.clamp(slopeBand, MIN_SLOPE_BAND, MAX_SLOPE_BAND);
        this.hash =
                Objects.hash(
                        radius, minY, maxY, seed, this.outerSteepness, this.slopeBand, abyssMaxY);
        this.mountains = new OverworldAbyssMountains(this);
    }

    public int radius() {
        return radius;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public long seed() {
        return seed;
    }

    public double outerSteepness() {
        return outerSteepness;
    }

    public double slopeBand() {
        return slopeBand;
    }

    public int abyssMaxY() {
        return abyssMaxY;
    }

    public OverworldAbyssMountains.Sample sample(int x, int z) {
        return mountains.sample(x, z);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof OverworldAbyssShape that
                        && radius == that.radius
                        && minY == that.minY
                        && maxY == that.maxY
                        && seed == that.seed
                        && abyssMaxY == that.abyssMaxY
                        && Double.compare(outerSteepness, that.outerSteepness) == 0
                        && Double.compare(slopeBand, that.slopeBand) == 0;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /** Old saved graphs use the original abyss boundary; live worlds supply their actual type. */
    public OverworldAbyssShape(
            int radius, int minY, int maxY, long seed, double outerSteepness, double slopeBand) {
        this(
                radius,
                minY,
                maxY,
                seed,
                outerSteepness,
                slopeBand,
                com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight.THE_ABYSS.maxY());
    }

    /** The shaft moves without moving the ring ridge, outer slope or connection range. */
    public double wallRadius() {
        return radius + 24.0;
    }

    public int wallYOffset() {
        return com.altnoir.mementoinabyss.infrastructure.worldgen.dimension
                .VerticalDimensionCoordinates.displayOffset(abyssMaxY, minY);
    }

    public double distance(int x, int z) {
        return Math.hypot((double) x, (double) z);
    }

    /**
     * Radius the slope fades out over: the ridge plus the whole descent band. Ending the band here is
     * what makes the junction work - the blend reaches the surrounding ground exactly where the weight
     * reaches zero, so there is neither a cliff where the carve stops nor a flat bed in front of it. A
     * band that stopped short of this would leave the last stretch almost level and then drop the
     * remaining height in the final few blocks, which is the seam this shape exists to avoid.
     */
    public double outerLimit() {
        return (2.0 + slopeBand) * radius;
    }

    public boolean affects(int x, int z) {
        double limit = outerLimit();
        return (double) x * x + (double) z * z < limit * limit;
    }

    public double ridgeHeight(int x, int z) {
        return sample(x, z).ridgeHeight();
    }

    /** Target height before the single outer blend with the original terrain. */
    public double innerHeight(int x, int z) {
        return sample(x, z).height();
    }

    static double innerRise(double t) {
        double rise = t * t * t;
        if (t > 0.85) {
            // Hermite matches the cubic's value and derivative before rounding to a horizontal
            // ridge. The outer descent also begins horizontally at the local crest.
            double u = (t - 0.85) / 0.15;
            double start = 0.85 * 0.85 * 0.85;
            double tangent = 3.0 * 0.85 * 0.85 * 0.15;
            rise =
                    (2 * u * u * u - 3 * u * u + 1) * start
                            + (u * u * u - 2 * u * u + u) * tangent
                            + (-2 * u * u * u + 3 * u * u);
        }
        return rise;
    }

    public double outerWeight(int x, int z) {
        double distance = distance(x, z);
        if (distance <= (2 - OverworldAbyssMountains.MAX_RIDGE_SHIFT) * radius) return 1;
        if (distance >= outerLimit()) return 0;
        return sample(x, z).weight();
    }

    public static double smooth(double value) {
        double t = Math.clamp(value, 0.0, 1.0);
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    public static double lerp(double t, double a, double b) {
        // The endpoints are exact: with the a + t * (b - a) form a weight of one can return b minus
        // one ulp, which is enough to report one block of rock where the density has none.
        if (t <= 0.0) return a;
        if (t >= 1.0) return b;
        return a + t * (b - a);
    }
}
