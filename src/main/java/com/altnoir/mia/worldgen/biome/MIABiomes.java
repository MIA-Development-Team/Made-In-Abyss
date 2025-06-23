package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class MIABiomes {
    public static final ResourceKey<Biome> ABYSS_BRINK = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink/abyss_brink"));

    public static void boostrap(BootstrapContext<Biome> context) {
        context.register(ABYSS_BRINK, abyssBrink(context));
    }

    public static void globalAbyssGeneration(BiomeGenerationSettings.Builder builder) {
        //MiaBiomeDefaultFeatures.addAbyssCarvers(builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);

        BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder);

        MiaBiomeDefaultFeatures.addDefaultSprings(builder);
        BiomeDefaultFeatures.addSurfaceFreezing(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
    }

    private static Biome abyssBrink(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CAT, 10, 4, 4));

        BiomeDefaultFeatures.farmAnimals(spawnBuilder);
        BiomeDefaultFeatures.commonSpawns(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)
        );

        globalAbyssGeneration(generationBuilder);
        BiomeDefaultFeatures.addMossyStoneBlock(generationBuilder);
        BiomeDefaultFeatures.addExtraGold(generationBuilder);

        BiomeDefaultFeatures.addPlainGrass(generationBuilder);
        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_CHERRY);
        //generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_CHERRY);
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
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .skyColor(5205851)
                        .fogColor(3165784)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES)).build())
                .build();
    }
}
