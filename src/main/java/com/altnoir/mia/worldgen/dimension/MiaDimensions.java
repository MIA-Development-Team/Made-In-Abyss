package com.altnoir.mia.worldgen.dimension;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.biomesource.AbyssNoiseBiomeSource;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;

public class MiaDimensions {
    public static final ResourceKey<LevelStem> ABYSS_EDGE = ResourceKey.create(Registries.LEVEL_STEM,
            MiaUtil.id(MIA.MOD_ID, "abyss_edge"));
    public static final ResourceKey<Level> ABYSS_EDGE_LEVEL = ResourceKey.create(Registries.DIMENSION,
            MiaUtil.id(MIA.MOD_ID, "abyss_edge"));
    public static final ResourceKey<LevelStem> TEMPTATION_FOREST = ResourceKey.create(Registries.LEVEL_STEM,
            MiaUtil.id(MIA.MOD_ID, "temptation_forest"));
    public static final ResourceKey<Level> TEMPTATION_FOREST_LEVEL = ResourceKey.create(Registries.DIMENSION,
            MiaUtil.id(MIA.MOD_ID, "temptation_forest"));

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator abyss_edge = new NoiseBasedChunkGenerator(
                AbyssNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                biomePair(
                                        Climate.Parameter.span(-0.5F, 0.5F),
                                        Climate.Parameter.point(0.0F),
                                        Climate.Parameter.span(0.3F, 0.5F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-0.5F, 0.0F),
                                        Climate.Parameter.span(0.0F, 0.8F),
                                        Climate.Parameter.span(0.3F, 0.5F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-1.0F, 0.5F),
                                        Climate.Parameter.point(0.0F),
                                        Climate.Parameter.point(0.0F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.ABYSS_PLAINS)
                                ),
                                biomePair(
                                        Climate.Parameter.span(0.0F, 0.5F),
                                        Climate.Parameter.span(0.0F, 0.8F),
                                        Climate.Parameter.point(0.0F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.ABYSS_PLAINS)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-0.8F, 0.2F),
                                        Climate.Parameter.point(0.0F),
                                        Climate.Parameter.span(-0.5F, -0.3F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-0.5F, 0.5F),
                                        Climate.Parameter.span(0.1F, 0.6F),
                                        Climate.Parameter.point(0.0F),
                                        0.175F, biomeRegistry.getOrThrow(MiaBiomes.ABYSS_LUSH_CAVES)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-0.5F, 0.0F),
                                        Climate.Parameter.span(0.6F, 1.1F),
                                        Climate.Parameter.point(0.0F),
                                        0.25F, biomeRegistry.getOrThrow(MiaBiomes.ABYSS_DRIPSTONE_CAVES)
                                ),
                                biomePair(
                                        Climate.Parameter.span(-0.3F, 0.5F),
                                        Climate.Parameter.span(1.1F, 1.6F),
                                        Climate.Parameter.point(0.0F),
                                        0.375F, biomeRegistry.getOrThrow(MiaBiomes.PRASIOLITE_CAVES)
                                )
                        )),
                        biomeRegistry.getOrThrow(MiaBiomes.ABYSS_EDGE)
                ),
                noiseGenSettings.getOrThrow(MiaNoiseGeneratorSettings.ABYSS_EDGE)
        );
        NoiseBasedChunkGenerator temptation_forest = new NoiseBasedChunkGenerator(
                AbyssNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                biomePair(
                                        Climate.Parameter.span(-0.5F, 0.5F),
                                        Climate.Parameter.point(0.0F),
                                        Climate.Parameter.span(0.3F, 0.5F),
                                        0.0F, biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                                )
                        )),
                        biomeRegistry.getOrThrow(MiaBiomes.SKYFOG_FOREST)
                ),
                noiseGenSettings.getOrThrow(MiaNoiseGeneratorSettings.TEMPTATION_FOREST)
        );

        LevelStem abyss_edge_stem = new LevelStem(dimensionTypes.getOrThrow(MiaDimensionTypes.ABYSS_EDGE_TYPE), abyss_edge);
        LevelStem temptation_forest_stem = new LevelStem(dimensionTypes.getOrThrow(MiaDimensionTypes.TEMPTATION_FOREST_TYPE), temptation_forest);

        context.register(ABYSS_EDGE, abyss_edge_stem);
        context.register(TEMPTATION_FOREST, temptation_forest_stem);
    }

    private static Pair<Climate.ParameterPoint, Holder<Biome>> biomePair(
            Climate.Parameter humidity, Climate.Parameter depth, Climate.Parameter weirdness, float offset, Holder<Biome> biome) {

        return Pair.of(Climate.parameters(
                Climate.Parameter.point(0.8F),
                humidity,
                Climate.Parameter.point(0.0F),
                Climate.Parameter.point(0.0F),
                depth,
                weirdness,
                offset), biome);
    }
}
