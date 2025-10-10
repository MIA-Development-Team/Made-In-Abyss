package com.altnoir.mia.worldgen.dimension;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.biome.biomesource.AbyssNoiseBiomeSource;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;

public class MiaDimensions {
    public static final ResourceKey<LevelStem> ABYSS_BRINK = ResourceKey.create(Registries.LEVEL_STEM,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink"));
    public static final ResourceKey<Level> ABYSS_BRINK_LEVEL = ResourceKey.create(Registries.DIMENSION,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink"));

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                AbyssNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                Pair.of(
                                        Climate.parameters(0.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                                        biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                ),
                                Pair.of(
                                        Climate.parameters(0.8F, 0.0F, 0.0F, 0.0F, 0.2F, 0.0F, 0.0F),
                                        biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                ),
                                Pair.of(
                                        Climate.parameters(0.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.0F),
                                        biomeRegistry.getOrThrow(MiaBiomes.ABYSS_PLAINS)
                                ),
                                Pair.of(
                                        Climate.parameters(0.8F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                                        biomeRegistry.getOrThrow(MiaBiomes.ABYSS_PLAINS)
                                ),
                                Pair.of(
                                        Climate.parameters(0.8F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F),
                                        biomeRegistry.getOrThrow(Biomes.CHERRY_GROVE)
                                ),
                                Pair.of(
                                        Climate.parameters(0.8F, -0.3F, 0.0F, 0.0F, 0.2F, 0.0F, 0.5F),
                                        biomeRegistry.getOrThrow(MiaBiomes.PRASIOLITE_CAVE)
                                )
                        )),
                        biomeRegistry.getOrThrow(MiaBiomes.ABYSS_BRINK)
                ),
                noiseGenSettings.getOrThrow(MiaNoiseGeneratorSettings.ABYSS_BRINK)
        );

        LevelStem stem = new LevelStem(dimensionTypes.getOrThrow(MiaDimensionTypes.ABYSS_TYPE), generator);

        context.register(ABYSS_BRINK, stem);
    }
}
