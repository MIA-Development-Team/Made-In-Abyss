package com.altnoir.mementoinabyss.worldgen.lod;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Declarative mapping from one dimension's stored chunks into another dimension's render space. */
public record CrossDimensionLodLink(
        Identifier id, ResourceKey<Level> source, ResourceKey<Level> target,
        int centerX, int centerZ, int displayYOffset, int outsidePlaneY) {
}
