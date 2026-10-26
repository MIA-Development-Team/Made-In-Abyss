package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.TheAbyssChunkGenerator;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.AbyssNoiseBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaAbyssBiomeDefinitions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.noise.MiaNoiseGeneratorSettings;
import com.mojang.datafixers.util.Pair;
import java.util.List;
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

public final class MiaDimensions {
    public static final ResourceKey<LevelStem> THE_ABYSS =
            ResourceKey.create(Registries.LEVEL_STEM, MementoInAbyss.asResource("the_abyss"));
    public static final ResourceKey<Level> THE_ABYSS_LEVEL =
            ResourceKey.create(Registries.DIMENSION, MementoInAbyss.asResource("the_abyss"));
    public static final ResourceKey<LevelStem> GREAT_FAULT =
            ResourceKey.create(Registries.LEVEL_STEM, MementoInAbyss.asResource("great_fault"));
    public static final ResourceKey<Level> GREAT_FAULT_LEVEL =
            ResourceKey.create(Registries.DIMENSION, MementoInAbyss.asResource("great_fault"));

    public static boolean isMiaDimension(ResourceKey<Level> dimension) {
        return dimension.equals(THE_ABYSS_LEVEL) || dimension.equals(GREAT_FAULT_LEVEL);
    }

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings =
                context.lookup(Registries.NOISE_SETTINGS);

        TheAbyssChunkGenerator generator =
                new TheAbyssChunkGenerator(
                        MiaAbyssBiomeDefinitions.create(biomes),
                        noiseSettings.getOrThrow(MiaNoiseGeneratorSettings.THE_ABYSS));
        context.register(
                THE_ABYSS,
                new LevelStem(dimensionTypes.getOrThrow(MiaDimensionTypes.THE_ABYSS), generator));

        Climate.ParameterList<Holder<Biome>> greatFaultOutside =
                new Climate.ParameterList<>(
                        List.of(
                                biomePair(
                                        -1.0F,
                                        1.0F,
                                        0.0F,
                                        0.0F,
                                        -1.0F,
                                        1.0F,
                                        biomes.getOrThrow(MiaBiomes.GREAT_FAULT))));
        NoiseBasedChunkGenerator greatFaultGenerator =
                new NoiseBasedChunkGenerator(
                        AbyssNoiseBiomeSource.createFromList(
                                112,
                                greatFaultOutside,
                                biomes.getOrThrow(MiaBiomes.THE_GREAT_FAULT)),
                        noiseSettings.getOrThrow(MiaNoiseGeneratorSettings.GREAT_FAULT));
        context.register(
                GREAT_FAULT,
                new LevelStem(
                        dimensionTypes.getOrThrow(MiaDimensionTypes.GREAT_FAULT),
                        greatFaultGenerator));
    }

    private static Pair<Climate.ParameterPoint, Holder<Biome>> biomePair(
            float humidityMin,
            float humidityMax,
            float depthMin,
            float depthMax,
            float weirdnessMin,
            float weirdnessMax,
            Holder<Biome> biome) {
        return Pair.of(
                Climate.parameters(
                        Climate.Parameter.point(0.8F),
                        Climate.Parameter.span(humidityMin, humidityMax),
                        Climate.Parameter.point(0.0F),
                        Climate.Parameter.point(0.0F),
                        Climate.Parameter.span(depthMin, depthMax),
                        Climate.Parameter.span(weirdnessMin, weirdnessMax),
                        0.0F),
                biome);
    }

    private MiaDimensions() {}
}
