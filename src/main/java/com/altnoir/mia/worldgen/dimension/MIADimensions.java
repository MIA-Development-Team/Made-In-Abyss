package com.altnoir.mia.worldgen.dimension;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.biome.MIABiomes;
import com.altnoir.mia.worldgen.noisesetting.MIANoiseSettings;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;

public class MIADimensions {
    public static final ResourceKey<LevelStem> ABYSS_BRINK = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink"));
    public static final ResourceKey<Level> ABYSS_BRINK_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink"));

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(Pair.of(
                                Climate.parameters(0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.0f), biomeRegistry.getOrThrow(MIABiomes.ABYSS_BRINK))
                        ))
                ),
                noiseGenSettings.getOrThrow(MIANoiseSettings.ABYSS_BRINK)
        );

        LevelStem stem = new LevelStem(dimensionTypes.getOrThrow(MIADimensionTypes.ABYSS_TYPE), generator);

        context.register(ABYSS_BRINK, stem);
    }
}
