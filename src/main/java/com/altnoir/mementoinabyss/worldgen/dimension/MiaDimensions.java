package com.altnoir.mementoinabyss.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.biome.AbyssNoiseBiomeSource;
import com.altnoir.mementoinabyss.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.worldgen.noise.MiaNoiseGeneratorSettings;
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

public final class MiaDimensions {
    public static final ResourceKey<LevelStem> THE_ABYSS = ResourceKey.create(
            Registries.LEVEL_STEM, MementoInAbyss.asResource("the_abyss"));
    public static final ResourceKey<Level> THE_ABYSS_LEVEL = ResourceKey.create(
            Registries.DIMENSION, MementoInAbyss.asResource("the_abyss"));

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        Climate.ParameterList<Holder<Biome>> outside = new Climate.ParameterList<>(List.of(
                biomePair(-0.5F, 0.5F, 0.0F, 0.0F, 0.3F, 0.5F, biomes.getOrThrow(MiaBiomes.SKYFOG_FOREST)),
                biomePair(-0.5F, 0.5F, 0.0F, 0.0F, 0.5F, 0.8F, biomes.getOrThrow(MiaBiomes.DENSE_SKYFOG_FOREST)),
                biomePair(-1.0F, 0.0F, 0.0F, 0.0F, -0.6F, -0.4F, biomes.getOrThrow(MiaBiomes.FOSSILIZED_FOREST)),
                biomePair(-1.0F, 0.0F, 0.0F, 0.0F, -0.9F, -0.6F, biomes.getOrThrow(MiaBiomes.RICH_FOSSILIZED_FOREST)),
                biomePair(-0.5F, 0.5F, 0.2F, 1.5F, -0.6F, -0.4F, biomes.getOrThrow(MiaBiomes.UNDER_FOSSILIZED_FOREST)),
                biomePair(-0.5F, 0.5F, 0.1F, 0.7F, 0.15F, 0.2F, biomes.getOrThrow(MiaBiomes.ABYSS_LUSH_CAVES)),
                biomePair(-0.5F, 0.0F, 0.4F, 1.0F, 0.2F, 0.3F, biomes.getOrThrow(MiaBiomes.ABYSS_DRIPSTONE_CAVES)),
                biomePair(-0.3F, 0.5F, 0.7F, 1.5F, 0.3F, 0.45F, biomes.getOrThrow(MiaBiomes.PRASIOLITE_CAVES)),
                biomePair(-1.0F, 1.0F, 1.85F, 1.95F, -0.5F, 0.5F, biomes.getOrThrow(MiaBiomes.TEMPTATION_FOREST)),
                biomePair(-1.0F, 1.0F, 1.95F, 2.0F, -0.5F, 0.5F, biomes.getOrThrow(MiaBiomes.INVERTED_FOREST)),
                biomePair(-1.0F, 1.0F, -1.0F, 1.5F, -0.2F, 0.2F, biomes.getOrThrow(MiaBiomes.ABYSS_PLAINS))));

        Climate.ParameterList<Holder<Biome>> inside = new Climate.ParameterList<>(List.of(
                biomePair(-1.0F, 1.0F, 0.0F, 1.9F, -1.0F, 1.0F, biomes.getOrThrow(MiaBiomes.THE_ABYSS)),
                biomePair(-1.0F, 1.0F, 1.9F, 2.0F, -1.0F, 1.0F, biomes.getOrThrow(MiaBiomes.INVERTED_FOREST))));

        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                AbyssNoiseBiomeSource.createFromList(272, outside, inside),
                noiseSettings.getOrThrow(MiaNoiseGeneratorSettings.THE_ABYSS));
        context.register(THE_ABYSS, new LevelStem(dimensionTypes.getOrThrow(MiaDimensionTypes.THE_ABYSS), generator));
    }

    private static Pair<Climate.ParameterPoint, Holder<Biome>> biomePair(
            float humidityMin, float humidityMax, float depthMin, float depthMax,
            float weirdnessMin, float weirdnessMax, Holder<Biome> biome) {
        return Pair.of(Climate.parameters(
                Climate.Parameter.point(0.8F),
                Climate.Parameter.span(humidityMin, humidityMax),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.point(0.0F),
                Climate.Parameter.span(depthMin, depthMax),
                Climate.Parameter.span(weirdnessMin, weirdnessMax),
                0.0F), biome);
    }

    private MiaDimensions() {
    }
}
