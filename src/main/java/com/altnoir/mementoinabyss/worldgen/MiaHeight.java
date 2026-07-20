package com.altnoir.mementoinabyss.worldgen;

public record MiaHeight(int minY, int height, int maxY) {
    public static final MiaHeight THE_ABYSS = create(-256, 640);
    public static final MiaHeight GREAT_FAULT = create(-384, 768);
    public static MiaHeight create(int minY, int height) { return new MiaHeight(minY, height, minY + height); }
    public int middleY() { return this == THE_ABYSS ? 5 : (minY + maxY) / 2; }
}
