package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Immutable world-local geometry; radius is the effective overworld radius (1.5 times config). */
public record OverworldAbyssShape(
        int radius, int minY, int maxY, long seed, double outerSteepness, double slopeBand) {
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
     * Width of the descent, in radii, measured from the ridge at {@code 2 * radius}. This is the one
     * lever that sets how steep the junction averages: the whole ridge-to-ground drop is spent across
     * this band, so the nominal gradient is {@code drop / (slopeBand * radius)} and the peak is a fixed
     * multiple of it for a given shape exponent.
     *
     * <p>The final slope also depends on the original density and local 3D rock detail.
     */
    public static final double DEFAULT_SLOPE_BAND = 1.25;

    public static final Codec<OverworldAbyssShape> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            Codec.intRange(64, 15360)
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
                                                    .forGetter(OverworldAbyssShape::slopeBand))
                                    .apply(instance, OverworldAbyssShape::new));

    public OverworldAbyssShape {
        if (radius < 64 || radius > 15360 || minY >= maxY) {
            throw new IllegalArgumentException("Invalid overworld abyss geometry");
        }
        outerSteepness = Math.clamp(outerSteepness, MIN_OUTER_STEEPNESS, MAX_OUTER_STEEPNESS);
        slopeBand = Math.clamp(slopeBand, MIN_SLOPE_BAND, MAX_SLOPE_BAND);
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
        double scale = radius / 3.0;
        double u = x / scale;
        double v = z / scale;
        int ix = (int) Math.floor(u);
        int iz = (int) Math.floor(v);
        double a = lerp(smooth(u - ix), sample(ix, iz), sample(ix + 1, iz));
        double b = lerp(smooth(u - ix), sample(ix, iz + 1), sample(ix + 1, iz + 1));
        double ceiling = Math.min(256.0, maxY - 2.0);
        double floor = Math.max(minY, ceiling - 32.0);
        return lerp(lerp(smooth(v - iz), a, b), floor, ceiling);
    }

    /** Y=128 shoulder, followed by a rise that gets steeper toward the ridge. */
    public double innerHeight(int x, int z) {
        double t = Math.clamp((distance(x, z) - radius) / radius, 0.0, 1.0);
        double shoulder = Math.clamp(128.0, minY, Math.min(256.0, maxY - 2.0));
        return lerp(t * t * t, shoulder, ridgeHeight(x, z));
    }

    public double outerWeight(double distance) {
        // Clamping the position keeps the weight exactly zero from the outer limit on, so a caller
        // can
        // never mistake a rounding crumb of the smoothstep for "the abyss still reaches here" and
        // sample the whole carve graph outside its own boundary.
        double position = Math.clamp((distance - 2.0 * radius) / (slopeBand * radius), 0.0, 1.0);
        return 1.0 - smooth(Math.pow(position, outerSteepness));
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

    private double sample(int x, int z) {
        long hash = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        hash = (hash ^ (hash >>> 30)) * 0xBF58476D1CE4E5B9L;
        hash = (hash ^ (hash >>> 27)) * 0x94D049BB133111EBL;
        hash ^= hash >>> 31;
        return (hash >>> 11) * 0x1.0p-53;
    }
}
