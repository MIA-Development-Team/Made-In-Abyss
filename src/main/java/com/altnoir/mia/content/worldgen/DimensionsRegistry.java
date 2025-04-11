package com.altnoir.mia.content.worldgen;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class DimensionsRegistry {
    public static final ResourceKey<Level> ABYSS_BRINK_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(MIA.MODID, "abyss_brink"));
    public static final ResourceKey<LevelStem> ABYSS_BRINK_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(MIA.MODID, "abyss_brink"));
    public static final ResourceKey<DimensionType> ABYSS_BRINK_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(MIA.MODID, "abyss_brink_type"));
}
