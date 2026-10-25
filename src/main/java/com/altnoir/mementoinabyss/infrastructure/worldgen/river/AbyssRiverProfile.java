package com.altnoir.mementoinabyss.infrastructure.worldgen.river;

import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.function.ToDoubleBiFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class AbyssRiverProfile {
    public static final int SEA_LEVEL = -112;
    public static final int MIN_Y = -152;
    public static final int MAX_Y = -96;
    private static final int CACHE_LIMIT = 2048;
    private final DensityFunction support;
    private final DensityFunction bank;
    private final DensityFunction bed;
    private final int supportY;
    private final int clearanceY;
    private final AbyssLayerRegions regions;
    private final double openingRadius;
    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Column>> columns =
            ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    private final ThreadLocal<Long2ByteLinkedOpenHashMap> biomeColumns =
            ThreadLocal.withInitial(Long2ByteLinkedOpenHashMap::new);
    private final ThreadLocal<Long2ObjectLinkedOpenHashMap<CaveColumn>> caveColumns =
            ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);
    private final ThreadLocal<Long2DoubleLinkedOpenHashMap> groundCorners =
            ThreadLocal.withInitial(AbyssRiverProfile::cornerCache);
    private final ThreadLocal<Long2DoubleLinkedOpenHashMap> tributaryCorners =
            ThreadLocal.withInitial(AbyssRiverProfile::cornerCache);
    private ToDoubleBiFunction<Integer, Integer> tributaries;

    public AbyssRiverProfile(
            DensityFunction support,
            DensityFunction bank,
            DensityFunction bed,
            int supportY,
            int clearanceY,
            long seed,
            double openingRadius) {
        this.support = support;
        this.bank = bank;
        this.bed = bed;
        this.supportY = supportY;
        this.clearanceY = clearanceY;
        this.regions = new AbyssLayerRegions(seed);
        this.tributaries = (x, z) -> regions.sample(x, z).river();
        this.openingRadius = openingRadius;
    }

    public void connect(ToDoubleBiFunction<Integer, Integer> tributaries) {
        this.tributaries = tributaries;
        columns.remove();
        biomeColumns.remove();
        caveColumns.remove();
        tributaryCorners.remove();
    }

    public Column column(int x, int z) {
        var cache = columns.get();
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        Column result = cache.getAndMoveToLast(key);
        if (result != null) return result;
        double radial = smooth((Math.hypot(x, z) - openingRadius - 24) / 48);
        double channel = interpolated(x, z, 8, false);
        double ground = radial == 0 || channel == 0 ? 0 : interpolated(x, z, 16, true);
        double wetness = channel * ground * radial;
        var sea = new DensityFunction.SinglePointContext(x, SEA_LEVEL, z);
        var below = new DensityFunction.SinglePointContext(x, SEA_LEVEL - 1, z);
        double value = Mth.lerp(wetness, bank.compute(sea), bed.compute(sea));
        double slope = Mth.lerp(wetness, bank.compute(below), bed.compute(below)) - value;
        double floor = slope > 0 ? SEA_LEVEL + value / slope : SEA_LEVEL;
        result = new Column(floor, wetness);
        if (cache.size() >= CACHE_LIMIT) cache.removeFirst();
        cache.putAndMoveToLast(key, result);
        return result;
    }

    private double interpolated(int x, int z, int size, boolean ground) {
        int x0 = Math.floorDiv(x, size) * size, z0 = Math.floorDiv(z, size) * size;
        double tx = (x - x0) / (double) size, tz = (z - z0) / (double) size;
        var cache = (ground ? groundCorners : tributaryCorners).get();
        return Mth.lerp(
                tz,
                Mth.lerp(tx, corner(x0, z0, ground, cache), corner(x0 + size, z0, ground, cache)),
                Mth.lerp(
                        tx,
                        corner(x0, z0 + size, ground, cache),
                        corner(x0 + size, z0 + size, ground, cache)));
    }

    private double corner(int x, int z, boolean ground, Long2DoubleLinkedOpenHashMap cache) {
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        double value = cache.getAndMoveToLast(key);
        if (!Double.isNaN(value)) return value;
        // Interpolate bounded weights, not the gated cave density: a one-block gate
        // jump would otherwise remove a riverbank beside a full water column.
        value =
                ground
                        ? smooth(
                                (support.compute(
                                                        new DensityFunction.SinglePointContext(
                                                                x, supportY, z))
                                                - .01)
                                        / .12)
                        : Mth.clamp(tributaries.applyAsDouble(x, z), 0, 1);
        if (ground && value > 0) {
            // A river must not sever the root of a tall rock column.
            double pillar =
                    support.compute(new DensityFunction.SinglePointContext(x, clearanceY, z));
            value *= 1 - smooth((pillar - .01) / .12);
        }
        if (cache.size() >= CACHE_LIMIT) cache.removeFirstDouble();
        cache.putAndMoveToLast(key, value);
        return value;
    }

    private static Long2DoubleLinkedOpenHashMap cornerCache() {
        var cache = new Long2DoubleLinkedOpenHashMap();
        cache.defaultReturnValue(Double.NaN);
        return cache;
    }

    public boolean isRiver(int x, int y, int z) {
        if (y < -136 || y >= SEA_LEVEL + 8) return false;
        var column = column(x, z);
        return column.influence() == 1 && column.bed() < SEA_LEVEL && y >= column.bed() - 4;
    }

    public boolean isRiverBiome(int x, int y, int z) {
        if (y < -140 || y >= SEA_LEVEL + 12) return false;
        var cache = biomeColumns.get();
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        byte cached = cache.getAndMoveToLast(key);
        if (cached != 0) return cached == 2;
        // BiomeManager jitters quart samples around the block being queried. Keep its
        // shoreline samples inside the river biome without widening the actual water.
        boolean river = false;
        outer:
        for (int dx = -8; dx <= 8; dx += 8)
            for (int dz = -8; dz <= 8; dz += 8) {
                if (Math.hypot(x + dx, z + dz) > openingRadius + 24
                        && interpolated(x + dx, z + dz, 8, false) > .05) {
                    river = true;
                    break outer;
                }
            }
        if (cache.size() >= CACHE_LIMIT) cache.removeFirstByte();
        cache.putAndMoveToLast(key, (byte) (river ? 2 : 1));
        return river;
    }

    public boolean protectsBed(int x, int y, int z) {
        return y >= MIN_Y && y < SEA_LEVEL && column(x, z).influence() > 0;
    }

    public double caveWeight(
            DensityFunction.FunctionContext context, int surfaceDepth, int riverClearance) {
        int x = context.blockX(), y = context.blockY(), z = context.blockZ();
        var cache = caveColumns.get();
        long key = ((long) x << 32) ^ (z & 0xffffffffL);
        CaveColumn column = cache.getAndMoveToLast(key);
        if (column == null || column.clearance() != riverClearance) {
            boolean allowed = true;
            var corners = tributaryCorners.get();
            int maxX = Math.floorDiv(x + riverClearance + 7, 8) * 8;
            int maxZ = Math.floorDiv(z + riverClearance + 7, 8) * 8;
            outer:
            for (int sx = Math.floorDiv(x - riverClearance, 8) * 8; sx <= maxX; sx += 8)
                for (int sz = Math.floorDiv(z - riverClearance, 8) * 8; sz <= maxZ; sz += 8)
                    if (corner(sx, sz, false, corners) > 0) {
                        allowed = false;
                        break outer;
                    }
            column = new CaveColumn(riverClearance, allowed);
            if (cache.size() >= CACHE_LIMIT) cache.removeFirst();
            cache.putAndMoveToLast(key, column);
        }
        if (!column.allowed()) return 0;
        // Test the rock above the candidate, without traversing the full height of a column.
        double roof =
                support.compute(new DensityFunction.SinglePointContext(x, y + surfaceDepth, z));
        if (roof <= .05) return 0;
        double middle =
                support.compute(new DensityFunction.SinglePointContext(x, y + surfaceDepth / 2, z));
        return smooth((Math.min(roof, middle) - .05) / .10);
    }

    public int nativeForest(int x, int z) {
        if (Math.hypot(x, z) < openingRadius + 64) return 0;
        return regions.sample(x, z).nativeForest();
    }

    public static double smooth(double value) {
        double t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public record Column(double bed, double weight) {
        public double influence() {
            return smooth(weight / .15);
        }
    }

    private record CaveColumn(int clearance, boolean allowed) {}
}
