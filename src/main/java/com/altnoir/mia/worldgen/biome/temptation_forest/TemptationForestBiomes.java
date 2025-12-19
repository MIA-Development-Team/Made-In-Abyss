package com.altnoir.mia.worldgen.biome.temptation_forest;

import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.worldgen.biome.abyss_edge.AbyssEdgePlacements;
import com.altnoir.mia.worldgen.biome.abyss_edge.AbyssEdgeUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class TemptationForestBiomes {
    public static Biome skyfogForest(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

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

        return baseTemptationForest(generationBuilder, spawnBuilder).build();
    }
    public static Biome.BiomeBuilder baseTemptationForest(BiomeGenerationSettings.Builder generationBuilder, MobSpawnSettings.Builder spawnBuilder) {
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
}
