package com.altnoir.mia.worldgen.biome.abyss_edge;

import com.altnoir.mia.init.MiaSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class AbyssEdgeBiomes {
    public static Biome abyssEdge(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);
        AbyssEdgeUtils.abyssGeneration(generationBuilder);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.TREES_SKYFOG_AND_AZALEA)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.PATCH_SUNFLOWER);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }

    public static Biome skyfogForest(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        AbyssEdgeUtils.globalAbyssGeneration(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssEdgePlacements.RAW_IRON);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.TREES_SKYFOG_AND_AZALEA)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }

    public static Biome abyssPlains(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        AbyssEdgeUtils.globalAbyssGeneration(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssEdgePlacements.RAW_IRON);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.PATCH_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }

    public static Biome prasioliteCaves(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        AbyssEdgeUtils.globalAbyssGenerationNotGeode(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssEdgePlacements.PRASIOLITE_CLUSTER);
        generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, AbyssEdgePlacements.BIG_PRASIOLITE_CLUSTER);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.TREES_SKYFOG);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.PATCH_LARGE_FERN);
        BiomeDefaultFeatures.addDefaultMushrooms(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }

    public static Biome abyssLushCaves(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        spawnBuilder.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        AbyssEdgeUtils.globalAbyssGeneration(generationBuilder);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.TREES_SKYFOG);
        BiomeDefaultFeatures.addLushCavesVegetationFeatures(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }

    public static Biome abyssDripstoneCaves(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        AbyssEdgeUtils.farmAnimals(spawnBuilder);
        AbyssEdgeUtils.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        AbyssEdgeUtils.globalAbyssGeneration(generationBuilder);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        AbyssEdgeUtils.addMeadowVegetation(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AbyssEdgePlacements.TREES_SKYFOG);
        AbyssEdgeUtils.addDripstone(generationBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generationBuilder);

        return baseAbyssEdge(generationBuilder, spawnBuilder).build();
    }


    public static Biome.BiomeBuilder baseAbyssEdge(BiomeGenerationSettings.Builder generationBuilder, MobSpawnSettings.Builder spawnBuilder) {
        return new Biome.BiomeBuilder().hasPrecipitation(true).downfall(0.8F).temperature(0.8F)
                .generationSettings(generationBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .specialEffects((new BiomeSpecialEffects.Builder())
                        .waterColor(6141935)
                        .waterFogColor(6141935)
                        .skyColor(8888490)
                        .fogColor(8888490)
                        .grassColorOverride(11335504)
                        .ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(MiaSounds.MUSIC_ABYSS_EDGE_DIM))
                        .build());
    }


    private static BiomeGenerationSettings.Builder createGenerationBuilder(BootstrapContext<Biome> context) {
        return new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
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
