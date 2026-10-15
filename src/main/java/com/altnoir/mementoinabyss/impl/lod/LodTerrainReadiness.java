package com.altnoir.mementoinabyss.impl.lod;

/** Shared nonblocking coverage policy. It never requests chunks to make itself ready. */
public final class LodTerrainReadiness {
    @FunctionalInterface
    public interface Loaded {
        boolean test(int x, int z);
    }

    public record Coverage(int expected, int loaded, boolean centerReady) {
        public boolean mostlyReady() {
            return centerReady && expected > 0 && loaded * 10L >= expected * 9L;
        }
    }

    public static Coverage sample(int centerX, int centerZ, int distance, Loaded loaded) {
        int radius = Math.clamp(distance, 2, 32), expected = 0, ready = 0;
        boolean center = true;
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                expected++;
                boolean present = loaded.test(centerX + dx, centerZ + dz);
                if (present) ready++;
                else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) center = false;
            }
        return new Coverage(expected, ready, center);
    }

    public static final class Gate {
        private long readySince = Long.MIN_VALUE;

        public boolean update(long tick, boolean mostlyReady) {
            if (!mostlyReady) {
                reset();
                return false;
            }
            if (readySince == Long.MIN_VALUE || tick < readySince) readySince = tick;
            return tick - readySince >= 10;
        }

        public void reset() {
            readySince = Long.MIN_VALUE;
        }
    }

    private LodTerrainReadiness() {}
}
