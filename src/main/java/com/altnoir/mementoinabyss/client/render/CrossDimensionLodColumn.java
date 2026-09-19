package com.altnoir.mementoinabyss.client.render;

/** Column assembled from independently revisioned physical sections for a mesh worker snapshot. */
final class CrossDimensionLodColumn {
    private final String linkId;
    private final int displayYOffset, radius, chunkX, chunkZ, cellSize, minY, yCells;
    private final int[] palette;
    private final short[] voxels;
    private volatile CrossDimensionLodMesher.HeightField heightField;
    private volatile CrossDimensionLodMesher.QuadBuffer interior;

    private static final int MAX_RADIUS = 16_384;
    private static final int MAX_HEIGHT_CELLS = 1_024;
    private static final int MAX_PALETTE_ENTRIES = 4_096;
    private static final int MAX_VOXELS = 262_144;

    CrossDimensionLodColumn(String linkId, int displayYOffset, int radius, int chunkX, int chunkZ,
                            int cellSize, int minY, int yCells, int[] palette, short[] voxels) {
        if (linkId == null || linkId.isBlank() || linkId.length() > 256
                || radius <= 0 || radius > MAX_RADIUS
                || Math.abs(displayYOffset) > 65_536
                || cellSize <= 0 || cellSize > 16 || 16 % cellSize != 0
                || minY < -65_536 || minY > 65_536
                || yCells <= 0 || yCells > MAX_HEIGHT_CELLS
                || palette == null || palette.length == 0 || palette.length > MAX_PALETTE_ENTRIES
                || voxels == null) {
            throw new IllegalArgumentException("Invalid cross-dimension voxel column");
        }
        int horizontalCells = 16 / cellSize;
        int voxelCount;
        try {
            voxelCount = Math.multiplyExact(Math.multiplyExact(horizontalCells, horizontalCells), yCells);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Invalid cross-dimension voxel count", exception);
        }
        if (voxelCount > MAX_VOXELS || voxels.length != voxelCount) {
            throw new IllegalArgumentException("Invalid cross-dimension voxel count");
        }
        for (int stateId : palette) {
            if (stateId < 0) throw new IllegalArgumentException("Invalid cross-dimension block state");
        }
        for (short voxel : voxels) {
            if (Short.toUnsignedInt(voxel) >= palette.length) {
                throw new IllegalArgumentException("Invalid cross-dimension palette index");
            }
        }
        this.linkId = linkId; this.displayYOffset = displayYOffset; this.radius = radius;
        this.chunkX = chunkX; this.chunkZ = chunkZ; this.cellSize = cellSize;
        this.minY = minY; this.yCells = yCells; this.palette = palette; this.voxels = voxels;
    }

    String linkId() { return linkId; }
    int displayYOffset() { return displayYOffset; }
    int radius() { return radius; }
    int chunkX() { return chunkX; }
    int chunkZ() { return chunkZ; }
    int cellSize() { return cellSize; }
    int minY() { return minY; }
    int yCells() { return yCells; }
    int[] palette() { return palette; }
    short[] voxels() { return voxels; }

    CrossDimensionLodMesher.HeightField heightField() {
        if (cellSize < 4) return null;
        var result = heightField;
        if (result != null) return result;
        synchronized (this) {
            if (heightField == null) heightField = CrossDimensionLodMesher.buildHeightField(this);
            return heightField;
        }
    }

    /** Model-independent CPU geometry only. Owned by this immutable snapshot, never an atlas/GPU cache. */
    synchronized CrossDimensionLodMesher.QuadBuffer interiorCopy() {
        var result = interior;
        if (result == null) {
            result = CrossDimensionLodMesher.buildInterior(this);
            // No global map retaining old columns. Memoization costs at most the voxel body, capped at 64 KiB.
            if ((long) result.size * 32 <= Math.min(64 * 1024, voxels.length * 2)) {
                result.compact();
                interior = result;
            } else return result; // Uncached result can be consumed directly, without another huge allocation.
        }
        return result.copy();
    }
}
