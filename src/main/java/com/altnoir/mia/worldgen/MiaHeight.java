package com.altnoir.mia.worldgen;

public record MiaHeight(int minY, int allY, int maxY) {
    public static final MiaHeight THE_ABYSS = create(-256, 640);
    public static final MiaHeight GREAT_FAULT = create(-512, 1024);
    public static final MiaHeight GOBLETS_OF_GIANTS = create(0, 320);
    public static final MiaHeight SEA_OF_CORPSES = create(-128, 512);
    public static final MiaHeight CAPITAL_OF_THE_UNRETURNED = create(0, 320);
    public static final MiaHeight FINAL_MAELSTROM = create(0, 320);

    public static MiaHeight create(int minY, int allY) {
        return new MiaHeight(minY, allY, minY + allY);
    }

    public int midY() {
        return 5;
    }
}
