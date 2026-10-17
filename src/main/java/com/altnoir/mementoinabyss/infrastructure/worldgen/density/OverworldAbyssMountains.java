package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.MementoInAbyss;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Seeded horizontal relief, shared by every consumer of the imposed mountain surface. */
public final class OverworldAbyssMountains {
    static final double MAX_RIDGE_SHIFT = 0.18;

    public record Sample(double height, double ridgeRadius, double ridgeHeight, double weight) {}

    private final OverworldAbyssShape shape;
    private final NormalNoise warpX, warpZ, ridge, peaks, valleys, branches, branchDetail;
    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Sample>> columns =
            ThreadLocal.withInitial(() -> new Long2ObjectLinkedOpenHashMap<>(1024));

    OverworldAbyssMountains(OverworldAbyssShape shape) {
        this.shape = shape;
        var random = new XoroshiroRandomSource(shape.seed()).forkPositional();
        warpX = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/warp_x")));
        warpZ = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/warp_z")));
        ridge = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/ridge")));
        peaks = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/peaks")));
        valleys = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/valleys")));
        branches = noise(random.fromHashOf(MementoInAbyss.asResource("mountains/branches")));
        branchDetail =
                noise(random.fromHashOf(MementoInAbyss.asResource("mountains/branch_detail")));
    }

    private static NormalNoise noise(net.minecraft.util.RandomSource random) {
        return NormalNoise.create(random, -1, 1.0, 0.5);
    }

    public Sample sample(int x, int z) {
        var cache = columns.get();
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        Sample cached = cache.get(key);
        if (cached != null) return cached;
        Sample result = compute(x, z);
        if (cache.size() >= 1024) cache.removeFirst();
        cache.put(key, result);
        return result;
    }

    private Sample compute(int x, int z) {
        double distance = shape.distance(x, z);
        double shoulder = Math.clamp(128.0, shape.minY(), shape.maxY() - 2.0);
        double nominalPeak = Math.clamp(256.0, shape.minY(), shape.maxY() - 2.0);
        if (distance <= shape.wallRadius())
            return new Sample(shoulder, 2.0 * shape.radius(), nominalPeak, 1);
        if (distance >= shape.outerLimit())
            return new Sample(shoulder, 2.0 * shape.radius(), nominalPeak, 0);

        double nx = (double) x / shape.radius(), nz = (double) z / shape.radius();
        // Project the crest fields onto a circle without rounding or atan2: each ray has one
        // crest, and the negative-X axis has no angular seam.
        double scale = 2.0 * shape.radius() / distance;
        double cx = nx * scale, cz = nz * scale;
        double cwx = cx + 0.28 * value(warpX, cx * 1.3, cz * 1.3);
        double cwz = cz + 0.28 * value(warpZ, cx * 1.3, cz * 1.3);
        double ridgeRadius =
                shape.radius()
                        * (2
                                + MAX_RIDGE_SHIFT
                                        * Math.tanh(2.4 * value(ridge, cwx * 1.8, cwz * 1.8)));
        double valley = channel(value(valleys, cwx * 3.0, cwz * 3.0));
        double ridgeHeight =
                Math.clamp(
                        256
                                + 32 * Math.tanh(2.4 * value(peaks, cwx * 2.0, cwz * 2.0))
                                - 48 * valley,
                        shape.minY(),
                        shape.maxY() - 2.0);

        double position =
                Math.clamp((distance - ridgeRadius) / (shape.outerLimit() - ridgeRadius), 0, 1);
        double weight = 1 - Math.pow(OverworldAbyssShape.smooth(position), shape.outerSteepness());
        double t =
                Math.clamp(
                        (distance - shape.wallRadius()) / (ridgeRadius - shape.wallRadius()), 0, 1);
        double rise = OverworldAbyssShape.innerRise(t);
        double px = nx + 0.30 * value(warpX, nx * 1.3, nz * 1.3);
        double pz = nz + 0.30 * value(warpZ, nx * 1.3, nz * 1.3);
        double spine = 2 * channel(value(branches, px * 3.0, pz * 3.0)) - 1;
        double detail = 2 * channel(value(branchDetail, px * 6.0, pz * 6.0)) - 1;
        double flank =
                OverworldAbyssShape.smooth(
                        Math.abs(distance - ridgeRadius) / (0.30 * shape.radius()));
        double relief =
                flank
                        * (28 * spine
                                + 12 * detail
                                - 24 * channel(value(valleys, px * 3.0, pz * 3.0)));
        double height = shoulder + rise * (ridgeHeight - shoulder + relief);
        double activation =
                OverworldAbyssShape.smooth(
                        (distance - shape.wallRadius()) / (0.35 * shape.radius()));
        double oldRise =
                OverworldAbyssShape.innerRise(
                        Math.clamp(
                                (distance - shape.wallRadius())
                                        / (2.0 * shape.radius() - shape.wallRadius()),
                                0,
                                1));
        height =
                OverworldAbyssShape.lerp(
                        activation, shoulder + oldRise * (nominalPeak - shoulder), height);
        return new Sample(height, ridgeRadius, ridgeHeight, weight);
    }

    private static double value(NormalNoise noise, double x, double z) {
        return noise.getValue(x, 0, z);
    }

    private static double channel(double value) {
        return 1 - OverworldAbyssShape.smooth(Math.abs(value) / 0.45);
    }
}
