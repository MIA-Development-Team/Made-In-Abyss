package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

/** Shared by visual views and real boundary crossings; max heights are exclusive boundaries. */
public final class VerticalDimensionCoordinates {
    public static int displayOffset(int targetBoundary, int sourceBoundary) {
        return Math.subtractExact(targetBoundary, sourceBoundary);
    }

    public static double sourceY(double displayedY, int displayOffset) {
        return displayedY - displayOffset;
    }

    private VerticalDimensionCoordinates() {}
}
