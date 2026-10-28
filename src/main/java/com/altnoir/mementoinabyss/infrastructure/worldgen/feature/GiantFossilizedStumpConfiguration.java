package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record GiantFossilizedStumpConfiguration(
        BlockState wood,
        BlockState mossyWood,
        BlockState topWood,
        BlockState mossyTopWood,
        float mossyChance,
        IntProvider diameter,
        IntProvider extraHeight,
        IntProvider rootDepth,
        Holder<ConfiguredFeature<?, ?>> mossPatch,
        int mossPatches)
        implements FeatureConfiguration {
    public static final Codec<GiantFossilizedStumpConfiguration> CODEC =
            RecordCodecBuilder.create(
                    i ->
                            i.group(
                                            BlockState.CODEC
                                                    .fieldOf("wood")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::wood),
                                            BlockState.CODEC
                                                    .fieldOf("mossy_wood")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::mossyWood),
                                            BlockState.CODEC
                                                    .fieldOf("top_wood")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::topWood),
                                            BlockState.CODEC
                                                    .fieldOf("mossy_top_wood")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::mossyTopWood),
                                            Codec.floatRange(0, 1)
                                                    .fieldOf("mossy_chance")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::mossyChance),
                                            IntProviders.codec(16, 24)
                                                    .fieldOf("diameter")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::diameter),
                                            IntProviders.codec(8, 32)
                                                    .fieldOf("extra_height")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::extraHeight),
                                            IntProviders.codec(2, 6)
                                                    .fieldOf("root_depth")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::rootDepth),
                                            ConfiguredFeature.CODEC
                                                    .fieldOf("moss_patch")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::mossPatch),
                                            Codec.intRange(0, 8)
                                                    .fieldOf("moss_patches")
                                                    .forGetter(
                                                            GiantFossilizedStumpConfiguration
                                                                    ::mossPatches))
                                    .apply(i, GiantFossilizedStumpConfiguration::new));
}
