package com.altnoir.mementoinabyss.impl.lod.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongToIntFunction;

/** Camera-driven column selection. A coarse complete cover precedes expensive visible refinement. */
public final class MiaLodViewPlanner {
    public record Demand(long key, int cellSize, int band, double priority) {}

    public static List<Demand> plan(
            MiaLodView view, int radius, int minY, int maxY, LongToIntFunction confirmedLevel) {
        if (radius <= 0 || radius > 2048 || minY >= maxY)
            throw new IllegalArgumentException("Invalid LOD view window");
        int centerX = (int) Math.floor(view.x() / 16), centerZ = (int) Math.floor(view.z() / 16);
        int chunks =
                (radius + 15) / 16 + 1; // Include columns whose far face just touches the sphere.
        List<Demand> result = new ArrayList<>();
        for (int x = centerX - chunks; x <= centerX + chunks; x++) {
            for (int z = centerZ - chunks; z <= centerZ + chunks; z++) {
                double minX = x * 16.0, minZ = z * 16.0;
                double distance =
                        view.distanceSquared(minX, minY, minZ, minX + 16, maxY, minZ + 16);
                if (distance > (double) radius * radius) continue;
                long key = CrossDimensionLodKey.pack(x, z);
                int previous = confirmedLevel.applyAsInt(key);
                int band = view.band(minX, minY, minZ, minX + 16, maxY, minZ + 16);
                // Merely turning away never replaces a useful fine column with a coarse one.
                int cell =
                        previous == 0
                                ? 16
                                : band == 2 ? previous : view.cellSize(distance, previous);
                // Finish nearby detail without waiting for a whole-radius coarse sweep. Missing
                // cover wins
                // within each 64-block shell; the send ledger still reserves credits for new
                // visible cover.
                double priority =
                        band * 1e12
                                + Math.floor(Math.sqrt(distance) / 64) * 1e8
                                + (previous == 0 ? 0 : 1e6)
                                + distance;
                result.add(new Demand(key, cell, band, priority));
            }
        }
        result.sort(Comparator.comparingDouble(Demand::priority).thenComparingLong(Demand::key));
        return List.copyOf(result);
    }

    static boolean preemptUnsent(int sectionsSent, int cellSize, Demand previous, Demand next) {
        if (sectionsSent != 0) return false;
        return next == null
                || next.cellSize() > cellSize
                || (next.band() == 2 && previous != null && previous.band() < 2);
    }

    /** Refine only already-selected, nearby wall columns when the camera approaches the seam. */
    static Demand refineAbyssSeam(
            Demand demand, MiaLodView view, CrossDimensionLodLink link, int layoutRadius) {
        var abyss =
                com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions
                        .THE_ABYSS_LEVEL;
        var overworld = net.minecraft.world.level.Level.OVERWORLD;
        boolean down = link.source().equals(abyss) && link.target().equals(overworld);
        boolean up = link.source().equals(overworld) && link.target().equals(abyss);
        if ((!down && !up) || demand.band() > 1 || demand.cellSize() == 1) return demand;
        double boundary =
                (down ? link.sourceHeight().maxY() : link.sourceHeight().minY())
                        + link.displayYOffset();
        if (Math.abs(view.y() - boundary) > 64) return demand;
        double minX = CrossDimensionLodKey.x(demand.key()) * 16.0;
        double minZ = CrossDimensionLodKey.z(demand.key()) * 16.0;
        double nearestX = Math.clamp(0, minX, minX + 16), nearestZ = Math.clamp(0, minZ, minZ + 16);
        double farX = Math.max(Math.abs(minX), Math.abs(minX + 16));
        double farZ = Math.max(Math.abs(minZ), Math.abs(minZ + 16));
        if (Math.hypot(nearestX, nearestZ) > layoutRadius + 64
                || Math.hypot(farX, farZ) < layoutRadius - 16) return demand;
        if (view.distanceSquared(minX, boundary, minZ, minX + 16, boundary, minZ + 16) > 64 * 64)
            return demand;
        return new Demand(demand.key(), 1, demand.band(), demand.priority());
    }

    static int workLimit(Demand demand, int confirmedLevel, int capacity) {
        return demand.band() == 0 && confirmedLevel == 0 ? capacity : Math.max(1, capacity - 8);
    }

    private MiaLodViewPlanner() {}
}
