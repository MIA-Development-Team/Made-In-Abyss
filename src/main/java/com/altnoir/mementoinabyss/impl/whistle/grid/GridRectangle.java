package com.altnoir.mementoinabyss.impl.whistle.grid;

public record GridRectangle(int x, int y, int width, int height) {
    public GridRectangle {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid rectangle dimensions must be positive");
        }
    }

    public int area() {
        return width * height;
    }
}
