package com.altnoir.mementoinabyss.infrastructure.worldgen.river;

import net.minecraft.util.Mth;

/** Large native forest districts separated by river boundaries. */
public final class AbyssLayerRegions {
    private static final int CELL = 512;
    private final long seed;

    public AbyssLayerRegions(long seed) {
        this.seed = seed;
    }

    public Region sample(int x, int z) {
        int cellX = Math.floorDiv(x, CELL), cellZ = Math.floorDiv(z, CELL);
        double nearest = Double.POSITIVE_INFINITY, second = Double.POSITIVE_INFINITY;
        double ax = 0, az = 0, bx = 0, bz = 0;
        int firstKind = 0, secondKind = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = cellX + dx, cz = cellZ + dz;
                long hash =
                        hash(
                                seed
                                        ^ (long) cx * 0x9e3779b97f4a7c15L
                                        ^ (long) cz * 0xc2b2ae3d27d4eb4fL);
                double px = (cx + .25 + ((hash >>> 16) & 65535) / 131070.0) * CELL;
                double pz = (cz + .25 + ((hash >>> 32) & 65535) / 131070.0) * CELL;
                double distance = Mth.square(x - px) + Mth.square(z - pz);
                int pick = (int) hash & 7;
                int kind = pick == 0 ? 1 : pick == 1 ? 2 : 0;
                if (distance < nearest) {
                    second = nearest;
                    bx = ax;
                    bz = az;
                    secondKind = firstKind;
                    nearest = distance;
                    ax = px;
                    az = pz;
                    firstKind = kind;
                } else if (distance < second) {
                    second = distance;
                    bx = px;
                    bz = pz;
                    secondKind = kind;
                }
            }
        }
        double edge = (second - nearest) / (2 * Math.hypot(ax - bx, az - bz));
        double river =
                firstKind != secondKind && (firstKind != 0 || secondKind != 0)
                        ? 1 - smooth(edge / 20)
                        : 0;
        return new Region(firstKind, river);
    }

    public double pond(int x, int z) {
        int cellX = Math.floorDiv(x, 32), cellZ = Math.floorDiv(z, 32);
        double weight = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                int cx = cellX + dx, cz = cellZ + dz;
                long value =
                        hash(
                                seed
                                        ^ 0x70a3d284f19b5c6eL
                                        ^ (long) cx * 73428767
                                        ^ (long) cz * 912931);
                double px = cx * 32 + 6 + ((value >>> 16) & 255) / 12.75;
                double pz = cz * 32 + 6 + ((value >>> 32) & 255) / 12.75;
                double radius = 10 + (value & 3);
                weight = Math.max(weight, 1 - smooth(Math.hypot(x - px, z - pz) / radius));
            }
        return weight;
    }

    private static long hash(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private static double smooth(double value) {
        double t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public record Region(int nativeForest, double river) {}
}
