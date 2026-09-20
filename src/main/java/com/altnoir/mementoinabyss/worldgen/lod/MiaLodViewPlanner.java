package com.altnoir.mementoinabyss.worldgen.lod;

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

    static int workLimit(Demand demand, int confirmedLevel, int capacity) {
        return demand.band() == 0 && confirmedLevel == 0 ? capacity : Math.max(1, capacity - 8);
    }

    private MiaLodViewPlanner() {}
}
