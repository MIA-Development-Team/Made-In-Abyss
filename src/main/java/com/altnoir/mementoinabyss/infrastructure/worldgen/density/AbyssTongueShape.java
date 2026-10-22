package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** World-local curves; column samples are bounded and never shared between generation workers. */
public final class AbyssTongueShape {
    public static final int MIN_Y = -128;
    public static final int MAX_Y = 32;
    private static final int CACHE_SIZE = 1024;
    private static final int SECTORS = 32;
    private static final Tip UNFILTERED_TIP =
            new Tip(0, 0, 0, 0, Double.NEGATIVE_INFINITY, new long[0]);
    private final double radius;
    private final List<Curve>[] bands;
    private final List<Curve>[][] sectors;
    private final NormalNoise detail;
    private final Map<Curve, Tip> tips;
    private final ThreadLocal<Column[]> columns =
            ThreadLocal.withInitial(() -> new Column[CACHE_SIZE]);

    private record Curve(
            double angle,
            double root,
            double tip,
            double bend,
            double swerve,
            double top,
            double descent,
            double arch,
            double width,
            double thickness) {}

    private record Ribbon(
            double outside, double top, double bottom, double end, double inward, Tip tip) {}

    private record Tip(int minX, int minZ, int sizeX, int sizeZ, double radius, long[] connected) {
        boolean allows(int x, int y, int z, double distance) {
            if (distance > radius) return true;
            int dx = x - minX, dz = z - minZ;
            if (dx < 0 || dx >= sizeX || dz < 0 || dz >= sizeZ) return false;
            int index = ((y - MIN_Y) * sizeZ + dz) * sizeX + dx;
            int word = index >>> 6;
            return word < connected.length && (connected[word] & (1L << (index & 63))) != 0;
        }
    }

    private record Attachment(double horizontal, double top) {}

    private record Column(
            int x,
            int z,
            double distance,
            Ribbon[] solids,
            Ribbon[] ribbons,
            Attachment[] attachments) {}

    @SuppressWarnings("unchecked")
    public AbyssTongueShape(long seed, double radius, DensityFunction support) {
        this.radius = radius;
        bands = new List[3];
        sectors = new List[3][SECTORS];
        RandomSource random =
                new XoroshiroRandomSource(seed)
                        .forkPositional()
                        .fromHashOf(MementoInAbyss.asResource("radial_abyss_tongues"));
        RandomSource profiles =
                new XoroshiroRandomSource(seed)
                        .forkPositional()
                        .fromHashOf(MementoInAbyss.asResource("radial_abyss_tongue_profiles"));
        detail =
                NormalNoise.create(
                        new XoroshiroRandomSource(seed)
                                .forkPositional()
                                .fromHashOf(
                                        MementoInAbyss.asResource("radial_abyss_tongue_surface")),
                        -4,
                        1,
                        .5);
        for (int band = 0; band < bands.length; band++) {
            List<Curve> curves = new ArrayList<>();
            int count = 7 + random.nextInt(3);
            int rotation = profiles.nextInt(4);
            double phase = random.nextDouble() * Math.TAU;
            for (int i = 0; i < count; i++) {
                double angle = phase + Math.TAU * (i + .7 * (random.nextDouble() - .5)) / count;
                double top = 8 - 48 * band + 12 * (random.nextDouble() - .5);
                double root = findRoot(support, angle, top);
                double bend = .28 + .42 * random.nextDouble();
                if (random.nextBoolean()) bend = -bend;
                double swerve = .12 * (random.nextDouble() - .5);
                double tip = radius * (.04 + .02 * random.nextDouble());
                double descent = 6 + 10 * random.nextDouble();
                double width = (16 + 12 * random.nextDouble()) * radius / 320;
                double thickness = 16 + 16 * random.nextDouble();
                int profile = (i + rotation + band) % 4;
                double arch = 0;
                if (profile == 0)
                    descent = -Math.min(16 + 12 * profiles.nextDouble(), MAX_Y - 6 - top);
                if (profile == 1)
                    descent = Math.min(24 + 16 * profiles.nextDouble(), top - MIN_Y - 8);
                if (profile == 2) {
                    descent = 0;
                    arch = Math.min(24 + 16 * profiles.nextDouble(), MAX_Y - 6 - top);
                }
                if (profile == 3) {
                    descent = 0;
                    arch = -Math.min(20 + 12 * profiles.nextDouble(), top - MIN_Y - 8);
                }
                if (Double.isFinite(root))
                    curves.add(
                            new Curve(
                                    angle, root, tip, bend, swerve, top, descent, arch, width,
                                    thickness));
            }
            bands[band] = List.copyOf(curves);
            for (int sector = 0; sector < SECTORS; sector++) {
                double sectorAngle = Math.TAU * (sector + .5) / SECTORS;
                List<Curve> candidates = new ArrayList<>();
                for (Curve c : curves) {
                    double middle = c.angle + c.bend / 2;
                    double reach =
                            Math.abs(c.bend) / 2
                                    + Math.abs(c.swerve)
                                    + (.5 * c.width + radius * .12) / c.tip
                                    + Math.PI / SECTORS;
                    if (Math.abs(Math.IEEEremainder(sectorAngle - middle, Math.TAU)) <= reach)
                        candidates.add(c);
                }
                sectors[band][sector] = List.copyOf(candidates);
            }
        }
        Map<Curve, Tip> connectedTips = new IdentityHashMap<>();
        for (List<Curve> band : bands)
            for (Curve curve : band) connectedTips.put(curve, connectTip(curve));
        tips = Collections.unmodifiableMap(connectedTips);
    }

    private double findRoot(DensityFunction support, double angle, double top) {
        double step = Math.max(8, radius / 40);
        for (int pass = 0; pass < 2; pass++) {
            double start = radius * (pass == 0 ? .60 : .46);
            double end = radius * (pass == 0 ? .86 : .60);
            for (double r = start; r <= end; r += step) {
                boolean attached = true;
                // Two buried cross-sections remain outside the groove taper at the root.
                for (double along : new double[] {8, 16}) {
                    for (double side : new double[] {-4, 0, 4}) {
                        int x =
                                (int)
                                        Math.round(
                                                (r + along) * Math.cos(angle)
                                                        - side * Math.sin(angle));
                        int z =
                                (int)
                                        Math.round(
                                                (r + along) * Math.sin(angle)
                                                        + side * Math.cos(angle));
                        for (int depth : new int[] {8, 16}) {
                            if (support.compute(
                                            new DensityFunction.SinglePointContext(
                                                    x, (int) top - depth, z))
                                    <= .03) attached = false;
                        }
                    }
                }
                if (attached) return r;
            }
        }
        return Double.NaN;
    }

    public boolean affects(int x, int y, int z) {
        if (y <= MIN_Y || y >= MAX_Y) return false;
        double squared = (double) x * x + (double) z * z;
        return squared < radius * radius * .9025;
    }

    private Column column(int x, int z) {
        int index = (x & 31) | ((z & 31) << 5);
        Column[] cache = columns.get();
        Column column = cache[index];
        if (column != null && column.x == x && column.z == z) return column;
        double distance = Math.hypot(x, z), angle = Math.atan2(z, x);
        int sector =
                (int) Math.floor((angle < 0 ? angle + Math.TAU : angle) * SECTORS / Math.TAU)
                        % SECTORS;
        List<Ribbon> ribbons = new ArrayList<>();
        List<Ribbon> solids = new ArrayList<>();
        List<Attachment> attachments = new ArrayList<>();
        double relief = relief(x, z);
        for (int band = 0; band < bands.length; band++) {
            for (Curve c : sectors[band][sector]) {
                double rootSide =
                        Math.abs(Math.IEEEremainder(angle - c.angle, Math.TAU) * distance);
                double rootDistance =
                        Math.min(
                                Math.min(distance - c.root + 8, c.root + 32 - distance),
                                .5 * c.width + 8 - rootSide);
                if (rootDistance > 0)
                    attachments.add(new Attachment(smooth(rootDistance / 8), c.top));
                Ribbon ribbon = ribbon(c, distance, angle, relief, tips.get(c));
                if (ribbon != null) {
                    ribbons.add(ribbon);
                    if (ribbon.outside < 0 && ribbon.end > 0) solids.add(ribbon);
                }
            }
        }
        column =
                new Column(
                        x,
                        z,
                        distance,
                        solids.toArray(Ribbon[]::new),
                        ribbons.toArray(Ribbon[]::new),
                        attachments.toArray(Attachment[]::new));
        cache[index] = column;
        return column;
    }

    public double solid(DensityFunction.FunctionContext context) {
        if (!affects(context.blockX(), context.blockY(), context.blockZ())) return -8;
        Column c = column(context.blockX(), context.blockZ());
        double density = -8;
        for (Ribbon r : c.solids) {
            double signed = signed(r, context.blockY());
            if (signed > 0 && !r.tip.allows(c.x, context.blockY(), c.z, c.distance)) continue;
            density = Math.max(density, signed / 8);
        }
        return Math.clamp(density, -8, 8);
    }

    private Ribbon ribbon(Curve c, double distance, double angle, double relief, Tip tip) {
        double u = (c.root - distance) / (c.root - c.tip);
        if (u < -.3 || u > 1.15) return null;
        double progress = Math.clamp(u, 0, 1);
        double turn = turn(c, progress);
        double side = Math.abs(Math.IEEEremainder(angle - turn, Math.TAU) * distance);
        double taper = taper(progress);
        double tipCap = Math.max(0, distance - c.tip);
        double width = Math.min(Math.max(.5 * c.width * taper, 1.25), tipCap);
        double crown = width > 0 ? 1.5 * Math.max(0, 1 - side * side / (width * width)) : 0;
        double hump = 16 * progress * progress * (1 - progress) * (1 - progress);
        double top = c.top - c.descent * progress * progress + c.arch * hump + crown + relief;
        double thickness = Math.min(Math.max(c.thickness * taper, 2.5), 2 * tipCap);
        // Fit a deep bend above the lower boundary without shaving it on a horizontal plane.
        double available = Math.max(.01, top - MIN_Y - 2);
        thickness /= Math.pow(1 + Math.pow(thickness / available, 8), .125);
        double end = Math.min(distance - c.tip, c.root + radius * .075 - distance);
        double inward = smooth((c.root - radius * .05 - distance) / (radius * .18));
        return new Ribbon(side - width, top, top - thickness, end, inward, tip);
    }

    private static double signed(Ribbon r, int y) {
        return Math.min(Math.min(-r.outside, r.end), Math.min(r.top - y, y - r.bottom));
    }

    private double relief(int x, int z) {
        return 2 * Math.clamp(detail.getValue(x, 0, z), -1, 1);
    }

    private static double turn(Curve c, double progress) {
        return c.angle
                + c.bend * smooth(progress)
                + c.swerve * Math.pow(Math.sin(Math.PI * progress), 2);
    }

    private static double taper(double progress) {
        return (1 - .65 * smooth(progress)) * Math.sqrt(1 - smooth((progress - .65) / .35));
    }

    private Tip connectTip(Curve c) {
        double low = 0, high = 1;
        for (int i = 0; i < 32; i++) {
            double middle = (low + high) / 2;
            if (.5 * c.width * taper(middle) > 2.5) low = middle;
            else high = middle;
        }
        double capRadius = c.root - high * (c.root - c.tip);
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (double r = c.tip; r <= capRadius + 5; r += .5) {
            double progress = Math.clamp((c.root - r) / (c.root - c.tip), 0, 1);
            double angle = turn(c, progress);
            double width = Math.max(.5 * c.width * taper(progress), 1.25) + 2;
            double x = r * Math.cos(angle), z = r * Math.sin(angle);
            minX = Math.min(minX, (int) Math.floor(x - width));
            minZ = Math.min(minZ, (int) Math.floor(z - width));
            maxX = Math.max(maxX, (int) Math.ceil(x + width));
            maxZ = Math.max(maxZ, (int) Math.ceil(z + width));
        }
        int sizeX = maxX - minX + 1, sizeZ = maxZ - minZ + 1;
        int plane = sizeX * sizeZ, volume = plane * (MAX_Y - MIN_Y);
        BitSet solid = new BitSet(volume), connected = new BitSet(volume);
        int[] queue = new int[volume];
        int head = 0, tail = 0;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                double distance = Math.hypot(x, z);
                if (distance > capRadius + 4) continue;
                Ribbon r = ribbon(c, distance, Math.atan2(z, x), relief(x, z), UNFILTERED_TIP);
                if (r == null) continue;
                for (int y = Math.max(MIN_Y + 1, (int) Math.floor(r.bottom));
                        y < Math.min(MAX_Y, Math.ceil(r.top));
                        y++) {
                    if (signed(r, y) <= 0) continue;
                    int index = ((y - MIN_Y) * sizeZ + z - minZ) * sizeX + x - minX;
                    solid.set(index);
                    if (distance >= capRadius + 2) {
                        connected.set(index);
                        queue[tail++] = index;
                    }
                }
            }
        }
        // Only keep terminal voxels joined by faces to the thicker stem. This is seeded once,
        // in global coordinates, so chunk order and parallel workers cannot change the result.
        while (head < tail) {
            int index = queue[head++];
            int x = index % sizeX, z = index / sizeX % sizeZ;
            if (x > 0) tail = visit(index - 1, solid, connected, queue, tail);
            if (x + 1 < sizeX) tail = visit(index + 1, solid, connected, queue, tail);
            if (z > 0) tail = visit(index - sizeX, solid, connected, queue, tail);
            if (z + 1 < sizeZ) tail = visit(index + sizeX, solid, connected, queue, tail);
            if (index >= plane) tail = visit(index - plane, solid, connected, queue, tail);
            if (index + plane < volume) tail = visit(index + plane, solid, connected, queue, tail);
        }
        return new Tip(minX, minZ, sizeX, sizeZ, capRadius, connected.toLongArray());
    }

    private static int visit(int index, BitSet solid, BitSet connected, int[] queue, int tail) {
        if (solid.get(index) && !connected.get(index)) {
            connected.set(index);
            queue[tail++] = index;
        }
        return tail;
    }

    public double carve(double original, DensityFunction.FunctionContext context) {
        if (!affects(context.blockX(), context.blockY(), context.blockZ())) return original;
        Column c = column(context.blockX(), context.blockZ());
        double result = original;
        for (Ribbon r : c.ribbons) {
            if (r == null || r.inward == 0) continue;
            double expansion = Math.sqrt(r.inward) * verticalFade(context.blockY());
            double depth = (64 + 24 * r.inward) * expansion;
            // Clear the space beneath the ribbon as well as its flanks. The separately
            // processed solid branch restores its thick body, rather than leaving pillars.
            double signed =
                    Math.min(r.top + 32 - context.blockY(), context.blockY() - r.top - 32 + depth);
            signed = Math.min(signed, radius * .11 * expansion - r.outside);
            signed = Math.min(signed, c.distance - radius * .225);
            double limited = Math.min(original, Math.clamp(-signed / 8, -8, 8));
            result = Math.min(result, limited);
        }
        double protection = 0;
        for (Attachment a : c.attachments) {
            protection =
                    Math.max(
                            protection,
                            a.horizontal
                                    * smooth((a.top + 8 - context.blockY()) / 8)
                                    * smooth((context.blockY() - a.top + 32) / 8));
        }
        return result + protection * (original - result);
    }

    private static double verticalFade(double y) {
        return smooth((y - MIN_Y) / 16) * smooth((MAX_Y - y) / 16);
    }

    private static double smooth(double value) {
        double t = Math.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }
}
