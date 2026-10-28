package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public final class MiaAbyssBiomeDefinitions {
    public static TheAbyssBiomeSource create(HolderGetter<Biome> biomes) {
        var layer1 =
                new Climate.ParameterList<Holder<Biome>>(
                        List.of(
                                biomePair(
                                        -1.0F,
                                        1.0F,
                                        -1.0F,
                                        1.5F,
                                        -0.2F,
                                        0.2F,
                                        biomes.getOrThrow(MiaBiomes.ABYSS_PLAINS)),
                                biomePair(
                                        -0.5F,
                                        0.5F,
                                        0.0F,
                                        0.0F,
                                        0.3F,
                                        0.5F,
                                        biomes.getOrThrow(MiaBiomes.SKYFOG_FOREST)),
                                biomePair(
                                        -0.5F,
                                        0.5F,
                                        0.0F,
                                        0.0F,
                                        0.5F,
                                        0.8F,
                                        biomes.getOrThrow(MiaBiomes.DENSE_SKYFOG_FOREST)),
                                biomePair(
                                        -1.0F,
                                        0.0F,
                                        0.0F,
                                        0.0F,
                                        -0.6F,
                                        -0.4F,
                                        biomes.getOrThrow(MiaBiomes.FOSSILIZED_FOREST)),
                                biomePair(
                                        -1.0F,
                                        0.0F,
                                        0.0F,
                                        0.0F,
                                        -0.9F,
                                        -0.6F,
                                        biomes.getOrThrow(MiaBiomes.RICH_FOSSILIZED_FOREST)),
                                biomePair(
                                        -0.5F,
                                        0.5F,
                                        0.2F,
                                        1.5F,
                                        -0.6F,
                                        -0.4F,
                                        biomes.getOrThrow(MiaBiomes.UNDER_FOSSILIZED_FOREST)),
                                biomePair(
                                        -0.5F,
                                        0.5F,
                                        0.1F,
                                        0.7F,
                                        0.15F,
                                        0.2F,
                                        biomes.getOrThrow(MiaBiomes.ABYSS_LUSH_CAVES)),
                                biomePair(
                                        -0.5F,
                                        0.0F,
                                        0.4F,
                                        1.0F,
                                        0.2F,
                                        0.3F,
                                        biomes.getOrThrow(MiaBiomes.ABYSS_DRIPSTONE_CAVES)),
                                biomePair(
                                        -0.3F,
                                        0.5F,
                                        0.7F,
                                        1.5F,
                                        0.3F,
                                        0.45F,
                                        biomes.getOrThrow(MiaBiomes.PRASIOLITE_CAVES))));
        return new TheAbyssBiomeSource(
                new TheAbyssBiomeSource.Definition(
                        2.0F,
                        2.5F,
                        272,
                        biomes.getOrThrow(MiaBiomes.THE_ABYSS),
                        layer1,
                        biomes.getOrThrow(MiaBiomes.ABYSS_RIVER),
                        biomes.getOrThrow(MiaBiomes.INVERTED_FOREST)));
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

    private MiaAbyssBiomeDefinitions() {}
}
