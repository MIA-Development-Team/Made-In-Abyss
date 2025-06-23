package com.altnoir.mia.worldgen.biome;

import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MiaBiomeDefaultFeatures {
    public static void addAbyssCarvers(BiomeGenerationSettings.Builder builder) {
//        builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
//        builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
//        builder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
    }

    public static void addDefaultSprings(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_WATER);
        builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, AbyssBrinkPlacements.SPRING_WATER);
    }
}
