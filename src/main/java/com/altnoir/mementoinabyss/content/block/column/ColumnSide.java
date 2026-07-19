package com.altnoir.mementoinabyss.content.block.column;

import net.minecraft.util.StringRepresentable;

public enum ColumnSide implements StringRepresentable {
    NONE("none"),
    BOTTOM("bottom"),
    MIDDLE("middle"),
    TOP("top");

    private final String name;

    ColumnSide(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
