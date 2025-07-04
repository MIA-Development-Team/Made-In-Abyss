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
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );
        AbyssBrinkUtils.abyssGeneration(generationBuilder);
        BiomeDefaultFeatures.addMossyStoneBlock(generationBuilder);

        BiomeDefaultFeatures.addPlainGrass(generationBuilder);
        BiomeDefaultFeatures.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.TREES_SKYFOG_AND_AZALEA);
        BiomeDefaultFeatures.addFerns(generationBuilder);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .downfall(0.8F)
                .temperature(0.8F)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(6141935)
                        .waterFogColor(6141935)
                        .skyColor(8888490)
                        .fogColor(8888490)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES)).build())
                .build();
    }

    public static Biome skyfogForest(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        AbyssBrinkUtils.globalAbyssGeneration(generationBuilder);
        BiomeDefaultFeatures.addMossyStoneBlock(generationBuilder);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        BiomeDefaultFeatures.addPlainGrass(generationBuilder);
        BiomeDefaultFeatures.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssBrinkPlacements.TREES_SKYFOG_AND_AZALEA);
        BiomeDefaultFeatures.addFerns(generationBuilder);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .downfall(0.8F)
                .temperature(0.8F)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(6141935)
                        .waterFogColor(6141935)
                        .skyColor(8888490)
                        .fogColor(8888490)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES)).build())
                .build();
    }
    public static Biome abyssPlains(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssBrinkUtils.farmAnimals(spawnBuilder);
        AbyssBrinkUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        AbyssBrinkUtils.globalAbyssGeneration(generationBuilder);
        BiomeDefaultFeatures.addMossyStoneBlock(generationBuilder);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        BiomeDefaultFeatures.addMeadowVegetation(generationBuilder);
        BiomeDefaultFeatures.addFerns(generationBuilder);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .downfall(0.8F)
                .temperature(0.8F)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(6141935)
                        .waterFogColor(6141935)
                        .skyColor(8888490)
                        .fogColor(8888490)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES)).build())
                .build();
    }
}
