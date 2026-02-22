package com.altnoir.mia.worldgen.biome.great_fault;

import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssPlacements;
import com.altnoir.mia.worldgen.biome.BiomesUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GreatFaultBiomes {
    public static Biome theGreatFault(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomesUtils.farmAnimals(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GreatFaultPlacements.ABYSS_LIGHT);

        return baseBiome(generationBuilder, spawnBuilder).build();
    }

    public static Biome greatFault(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        BiomesUtils.farmAnimals(spawnBuilder);

        BiomeGenerationSettings.Builder generationBuilder = createGenerationBuilder(context);

        generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TheAbyssPlacements.PATCH_LARGE_FERN);

        return baseBiome(generationBuilder, spawnBuilder).build();
    }

    public static Biome.BiomeBuilder baseBiome(BiomeGenerationSettings.Builder generationBuilder, MobSpawnSettings.Builder spawnBuilder) {
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
                        .backgroundMusic(Musics.createGameMusic(MiaSounds.MUSIC_THE_ABYSS_DIM))
                        .build());
    }

    private static BiomeGenerationSettings.Builder createGenerationBuilder(BootstrapContext<Biome> context) {
        return new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
    }
}
