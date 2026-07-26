package com.altnoir.mementoinabyss.impl.whistle.grid;

import java.util.HashSet;
import java.util.Set;

public record SkillShape(Set<GridCell> cells) {
    public SkillShape {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("A whistle skill shape cannot be empty");
        }
        cells = Set.copyOf(cells);
    }

    public static SkillShape of(int... coordinates) {
        if (coordinates.length == 0 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Skill shape coordinates must be non-empty x/y pairs");
        }
        Set<GridCell> cells = new HashSet<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            cells.add(new GridCell(coordinates[i], coordinates[i + 1]));
        }
        return new SkillShape(cells).normalized();
    }

    public SkillShape rotate(GridRotation rotation) {
        Set<GridCell> rotated = new HashSet<>();
        for (GridCell cell : cells) {
            rotated.add(rotation.rotate(cell));
        }
        return new SkillShape(rotated).normalized();
    }

    public int width() {
        return cells.stream().mapToInt(GridCell::x).max().orElse(0) + 1;
    }

    public int height() {
        return cells.stream().mapToInt(GridCell::y).max().orElse(0) + 1;
    }

    public GridRectangle largestRectangle() {
        GridRectangle best = null;
        for (int top = 0; top < height(); top++) {
            for (int left = 0; left < width(); left++) {
                for (int bottom = top; bottom < height(); bottom++) {
                    for (int right = left; right < width(); right++) {
                        GridRectangle candidate = new GridRectangle(
                                left,
                                top,
                                right - left + 1,
                                bottom - top + 1
                        );
                        if (isFilled(candidate) && isBetter(candidate, best)) {
                            best = candidate;
                        }
                    }
                }
            }
        }
        if (best == null) {
            throw new IllegalStateException("A non-empty skill shape must contain a rectangle");
        }
        return best;
    }

    private boolean isFilled(GridRectangle rectangle) {
        for (int y = rectangle.y(); y < rectangle.y() + rectangle.height(); y++) {
            for (int x = rectangle.x(); x < rectangle.x() + rectangle.width(); x++) {
                if (!cells.contains(new GridCell(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isBetter(GridRectangle candidate, GridRectangle current) {
        if (current == null || candidate.area() != current.area()) {
            return current == null || candidate.area() > current.area();
        }

        int candidateThickness = Math.min(candidate.width(), candidate.height());
        int currentThickness = Math.min(current.width(), current.height());
        if (candidateThickness != currentThickness) {
            return candidateThickness > currentThickness;
        }
        if (candidate.y() != current.y()) {
            return candidate.y() < current.y();
        }
        return candidate.x() < current.x();
    }

    private SkillShape normalized() {
        int minX = cells.stream().mapToInt(GridCell::x).min().orElse(0);
        int minY = cells.stream().mapToInt(GridCell::y).min().orElse(0);
        if (minX == 0 && minY == 0) {
            return this;
        }
        Set<GridCell> normalized = new HashSet<>();
        for (GridCell cell : cells) {
            normalized.add(new GridCell(cell.x() - minX, cell.y() - minY));
        }
        return new SkillShape(normalized);
    }
}
