package com.altnoir.mementoinabyss.impl.whistle.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GridCell(int x, int y) {
    public static final Codec<GridCell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(-32, 32).fieldOf("x").forGetter(GridCell::x),
            Codec.intRange(-32, 32).fieldOf("y").forGetter(GridCell::y)
    ).apply(instance, GridCell::new));

    public GridCell offset(int offsetX, int offsetY) {
        return new GridCell(x + offsetX, y + offsetY);
    }
}
