package com.altnoir.mia.worldgen;

public record MiaHeight(int minY, int allY, int maxY) {
    public static final MiaHeight THE_ABYSS = create(-256, 640);
    public static final MiaHeight THE_GREAT_FAULT = create(-512, 512);
    public static final MiaHeight THE_GOBLETS_OF_GIANTS = create(0, 320);
    public static final MiaHeight THE_SEA_OF_CORPSES = create(-128, 512);
    public static final MiaHeight THE_CAPITAL_OF_THE_UNRETURNED = create(0, 320);
    public static final MiaHeight THE_FINAL_MAELSTROM = create(0, 320);

    public static MiaHeight create(int minY, int allY) {
        return new MiaHeight(minY, allY, minY + allY);
    }
}
