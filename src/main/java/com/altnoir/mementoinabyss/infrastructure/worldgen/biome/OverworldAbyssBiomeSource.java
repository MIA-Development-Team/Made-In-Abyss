package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Adds the inner habitat while retaining the original source, including modded biome entries.
 */
public final class OverworldAbyssBiomeSource extends BiomeSource {
    public static final MapCodec<OverworldAbyssBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("delegate")
                                                    .forGetter(OverworldAbyssBiomeSource::delegate),
                                            Biome.CODEC
                                                    .fieldOf("inner_biome")
                                                    .forGetter(source -> source.innerBiome),
                                            Biome.CODEC
                                                    .listOf()
                                                    .fieldOf("land_fallbacks")
                                                    .forGetter(source -> source.landFallbacks),
                                            OverworldAbyssShape.CODEC
                                                    .fieldOf("shape")
                                                    .forGetter(source -> source.shape))
                                    .apply(instance, OverworldAbyssBiomeSource::new));

    private final BiomeSource delegate;
    private final Holder<Biome> innerBiome;
    private final List<Holder<Biome>> landFallbacks;
    private final OverworldAbyssShape shape;

    public OverworldAbyssBiomeSource(
            BiomeSource delegate,
            Holder<Biome> innerBiome,
            List<Holder<Biome>> landFallbacks,
            OverworldAbyssShape shape) {
        this.delegate = delegate;
        this.innerBiome = innerBiome;
        this.landFallbacks = List.copyOf(landFallbacks);
        this.shape = shape;
    }

    public BiomeSource delegate() {
        return delegate;
    }

    public static BiomeSource unwrap(BiomeSource source) {
        while (source instanceof OverworldAbyssBiomeSource wrapper) source = wrapper.delegate;
        return source;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                Stream.concat(delegate.possibleBiomes().stream(), Stream.of(innerBiome)),
                landFallbacks.stream());
    }

    @Override
    public Holder<Biome> getNoiseBiome(
            int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int x = QuartPos.toBlock(quartX), z = QuartPos.toBlock(quartZ);
        if (!shape.affects(x, z)) return delegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
        // At 1.75R the cubic inner slope is only ~144, so the low shoulder is the abyss habitat;
        // the upper half of the ascent belongs to the ordinary mountain climate map.
        if (shape.distance(x, z) < 1.75 * shape.radius()) return innerBiome;
        Holder<Biome> selected = delegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
        double height = shape.innerHeight(x, z);
        double weight = shape.outerWeight(shape.distance(x, z));
        // A foreign source may disregard continentalness. Only reject aquatic surface biomes
        // where the imposed ring is still materially elevated; preserve its land/cave biomes.
        if (weight > 0.15
                && (selected.is(BiomeTags.IS_OCEAN)
                || selected.is(BiomeTags.IS_RIVER)
                || selected.is(BiomeTags.IS_BEACH))) {
            float temperature =
                    Climate.unquantizeCoord(sampler.sample(quartX, quartY, quartZ).temperature());
            ResourceKey<Biome> fallback =
                    height >= 192 && weight >= 0.5
                            ? (temperature < -0.45
                            ? Biomes.FROZEN_PEAKS
                            : temperature < 0.2 ? Biomes.JAGGED_PEAKS : Biomes.STONY_PEAKS)
                            : (temperature < -0.45
                            ? Biomes.SNOWY_SLOPES
                            : temperature < 0.2 ? Biomes.GROVE : Biomes.MEADOW);
            return landFallbacks.stream()
                    .filter(biome -> biome.is(fallback))
                    .findFirst()
                    .orElse(selected);
        }
        return selected;
    }
}
