package com.altnoir.mementoinabyss.worldgen.lod;

/** Reversible chunk-coordinate key with a well-distributed {@link Long#hashCode(long)}. */
public final class CrossDimensionLodKey {
    private static final int Z_MULTIPLIER = 0x9E3779B1;
    private static final int Z_INVERSE = 0x0E8B2F51;

    public static long pack(int chunkX, int chunkZ) {
        int mixedZ = chunkZ * Z_MULTIPLIER;
        return (long) mixedZ << 32 | chunkX & 0xFFFFFFFFL;
    }

    public static int x(long key) {
        return (int) key;
    }

    public static int z(long key) {
        return (int) (key >>> 32) * Z_INVERSE;
    }

    private CrossDimensionLodKey() {}
}
