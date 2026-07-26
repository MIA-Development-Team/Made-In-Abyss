package com.altnoir.mementoinabyss.impl.whistle.grid;

import java.util.Set;

public record WhistleGrid(int width, int height, Set<GridCell> blockedCells) {
    public static final WhistleGrid RED_WHISTLE = new WhistleGrid(
            3,
            3,
            Set.of()
    );

    public WhistleGrid {
        if (width < 1 || height < 1 || width > 16 || height > 16) {
            throw new IllegalArgumentException("Whistle grids must be between 1x1 and 16x16");
        }
        blockedCells = Set.copyOf(blockedCells);
        if (blockedCells.stream().anyMatch(cell ->
                cell.x() < 0 || cell.y() < 0 || cell.x() >= width || cell.y() >= height)) {
            throw new IllegalArgumentException("Blocked whistle grid cells must be inside the grid");
        }
    }

    public boolean accepts(GridCell cell) {
        return cell.x() >= 0
                && cell.y() >= 0
                && cell.x() < width
                && cell.y() < height
                && !blockedCells.contains(cell);
    }

    public int usableCells() {
        return width * height - blockedCells.size();
    }
}
