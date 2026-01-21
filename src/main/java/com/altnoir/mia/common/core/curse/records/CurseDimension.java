package com.altnoir.mia.common.core.curse.records;

import net.minecraft.resources.ResourceLocation;

public record CurseDimension(ResourceLocation dimension, CurseEffect[] curseEffects, int level) {
}
