package com.altnoir.mia.core;

public enum MiaColors {
    WHITE(0xeeeeee),
    BLACK(0x221111),

    RED(0xff5d6c),
    GREEN(0x8cba51),
    BLUE(0x5f6caf),

    SLOW(0x22ff22),
    MEDIUM(0x0084ff),
    FAST(0xff55ff),

    INPUT(0x7fcde0),

    OUTPUT(0xddc166),
    ;


    private final int color;

    MiaColors(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
