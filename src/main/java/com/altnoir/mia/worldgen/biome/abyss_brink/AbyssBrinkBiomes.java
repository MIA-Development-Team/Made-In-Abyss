package com.altnoir.mia.worldgen.biome.abyss_brink;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class AbyssBrinkBiomes {
    public static Biome abyssBrink(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)
        );
        AbyssBrinkUtils.abyssGeneration(generationBuilder);

        AbyssBrinkUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.TREES_SKYFOG_AND_AZALEA)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.PATCH_SUNFLOWER);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssBrink(generationBuilder, spawnBuilder).build();
    }

    public static Biome skyfogForest(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)
        );

        AbyssBrinkUtils.globalAbyssGeneration(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssBrinkPlacements.RAW_IRON);

        AbyssBrinkUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.TREES_SKYFOG_AND_AZALEA)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssBrink(generationBuilder, spawnBuilder).build();
    }

    public static Biome abyssPlains(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)
        );

        AbyssBrinkUtils.globalAbyssGeneration(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssBrinkPlacements.RAW_IRON);

        AbyssBrinkUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssBrink(generationBuilder, spawnBuilder).build();
    }

    public static Biome prasioliteCave(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)
        );

        AbyssBrinkUtils.globalAbyssGenerationNotGeode(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssBrinkPlacements.PRASIOLITE_CLUSTER);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssBrinkPlacements.BIG_PRASIOLITE_CLUSTER);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        AbyssBrinkUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.TREES_SKYFOG);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.PATCH_LARGE_FERN);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssBrink(generationBuilder, spawnBuilder).build();
    }


    public static Biome.BiomeBuilder baseAbyssBrink(BiomeGenerationSettings.Builder generationBuilder, MobSpawnSettings.Builder spawnBuilder) {
        return new Biome.BiomeBuilder().hasPrecipitation(true).downfall(0.8F).temperature(0.8F)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(6141935)
                        .waterFogColor(6141935)
                        .skyColor(8888490)
                        .fogColor(8888490)
                        .grassColorOverride(11335504)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES)).build());
    }


    // 16进制转10进制
    public static int hexToRgb(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return (r << 16) | (g << 8) | b;
    }
}
