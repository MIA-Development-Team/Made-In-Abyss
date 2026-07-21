package com.altnoir.mementoinabyss.worldgen.lod;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;

/** Declarative mapping from one dimension's stored chunks into another dimension's render space. */
public record CrossDimensionLodLink(
        Identifier id, ResourceKey<Level> source, ResourceKey<Level> target,
        MiaHeight sourceHeight, int displayYOffset, DetailProfile detailProfile) {
    public enum DetailProfile {
        STANDARD,
        ABYSS_HOLE
    }
}
