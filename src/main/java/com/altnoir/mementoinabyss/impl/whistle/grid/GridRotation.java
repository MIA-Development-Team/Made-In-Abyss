package com.altnoir.mementoinabyss.impl.whistle.grid;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GridRotation implements StringRepresentable {
    NONE("none"),
    CLOCKWISE_90("clockwise_90"),
    CLOCKWISE_180("clockwise_180"),
    COUNTERCLOCKWISE_90("counterclockwise_90");

    public static final Codec<GridRotation> CODEC =
            StringRepresentable.fromEnum(GridRotation::values);

    private final String serializedName;

    GridRotation(String serializedName) {
        this.serializedName = serializedName;
    }

    public GridRotation next() {
        return values()[(ordinal() + 1) % values().length];
    }

    GridCell rotate(GridCell cell) {
        return switch (this) {
            case NONE -> cell;
            case CLOCKWISE_90 -> new GridCell(-cell.y(), cell.x());
            case CLOCKWISE_180 -> new GridCell(-cell.x(), -cell.y());
            case COUNTERCLOCKWISE_90 -> new GridCell(cell.y(), -cell.x());
        };
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
