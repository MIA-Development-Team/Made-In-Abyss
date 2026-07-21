package com.altnoir.mementoinabyss.worldgen.dimension;

import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Declares two dimensions that touch at their vertical world boundaries. */
public record VerticalDimensionLink(
        Identifier id,
        ResourceKey<Level> upperDimension,
        MiaHeight upperHeight,
        ResourceKey<Level> lowerDimension,
        MiaHeight lowerHeight,
        double entryOffset) {

    public VerticalDimensionLink {
        if (upperDimension.equals(lowerDimension)) {
            throw new IllegalArgumentException("A vertical dimension link must connect two dimensions");
        }
        if (!(entryOffset > 0.0)
                || entryOffset >= upperHeight.height()
                || entryOffset >= lowerHeight.height()) {
            throw new IllegalArgumentException("Invalid vertical dimension entry offset: " + entryOffset);
        }
    }
}
