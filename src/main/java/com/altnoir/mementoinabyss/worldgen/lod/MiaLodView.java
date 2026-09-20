package com.altnoir.mementoinabyss.worldgen.lod;

import java.util.List;

/** Immutable camera-relative side planes plus screen scale. Independent of Minecraft/world access. */
public record MiaLodView(double x, double y, double z, double pixelScale, List<Plane> planes) {
    public static final double TARGET_CELL_PIXELS = 12;
    public static final double GUARD_BLOCKS = 48;

    public MiaLodView {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(z)
                || Math.abs(x) > 30_000_000
                || Math.abs(y) > 30_000_000
                || Math.abs(z) > 30_000_000
                || !Double.isFinite(pixelScale)
                || pixelScale < 1
                || pixelScale > 16384
                || planes == null
                || planes.size() != 4) throw new IllegalArgumentException("Invalid LOD camera");
        planes = List.copyOf(planes);
    }

    public record Plane(float x, float y, float z, float w) {
        public Plane {
            double length = x * (double) x + y * (double) y + z * (double) z;
            if (!Double.isFinite(length)
                    || length < .99
                    || length > 1.01
                    || !Float.isFinite(w)
                    || Math.abs(w) > 65536) {
                throw new IllegalArgumentException("Invalid LOD camera plane");
            }
        }
    }

    public double distanceSquared(
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double dx = Math.max(Math.max(minX - x, 0), x - maxX);
        double dy = Math.max(Math.max(minY - y, 0), y - maxY);
        double dz = Math.max(Math.max(minZ - z, 0), z - maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean visible(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            double margin) {
        for (Plane p : planes) {
            double px = (p.x >= 0 ? maxX : minX) - x;
            double py = (p.y >= 0 ? maxY : minY) - y;
            double pz = (p.z >= 0 ? maxZ : minZ) - z;
            if (p.x * px + p.y * py + p.z * pz + p.w < -margin) return false;
        }
        return true;
    }

    public int band(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (visible(minX, minY, minZ, maxX, maxY, maxZ, 0)) return 0;
        return visible(minX, minY, minZ, maxX, maxY, maxZ, GUARD_BLOCKS) ? 1 : 2;
    }

    /** Coarsest acceptable cell size; hysteresis prevents alternating levels near a boundary. */
    public int cellSize(double distanceSquared, int previous) {
        double scale = pixelScale / Math.max(1, Math.sqrt(distanceSquared));
        int desired =
                16 * scale <= TARGET_CELL_PIXELS ? 16 : 4 * scale <= TARGET_CELL_PIXELS ? 4 : 1;
        if (previous != 1 && previous != 4 && previous != 16) return desired;
        if (desired < previous && previous * scale <= TARGET_CELL_PIXELS * 1.15) return previous;
        if (desired > previous && desired * scale > TARGET_CELL_PIXELS * .85) return previous;
        return desired;
    }

    public double priority(
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return band(minX, minY, minZ, maxX, maxY, maxZ) * 1e12
                + distanceSquared(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean materiallyDifferent(MiaLodView other) {
        if (other == null
                || squared(x - other.x, y - other.y, z - other.z) >= 4
                || Math.abs(pixelScale - other.pixelScale) > pixelScale * .03) return true;
        for (int i = 0; i < 4; i++) {
            Plane a = planes.get(i), b = other.planes.get(i);
            if (squared(a.x - b.x, a.y - b.y, a.z - b.z) > .0004 || Math.abs(a.w - b.w) > 1)
                return true;
        }
        return false;
    }

    public boolean near(double anchorX, double anchorY, double anchorZ, double radius) {
        return squared(x - anchorX, y - anchorY, z - anchorZ) <= radius * radius;
    }

    private static double squared(double x, double y, double z) {
        return x * x + y * y + z * z;
    }
}
