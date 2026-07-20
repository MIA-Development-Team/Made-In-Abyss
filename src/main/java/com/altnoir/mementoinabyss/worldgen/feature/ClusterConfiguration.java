package com.altnoir.mementoinabyss.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ClusterConfiguration(BlockStateProvider base, BlockStateProvider crystalsUp,
                                   BlockStateProvider crystalsDown, float crystalChance,
                                   IntProvider size, IntProvider height) implements FeatureConfiguration {
    public static final Codec<ClusterConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(ClusterConfiguration::base),
            BlockStateProvider.CODEC.fieldOf("crystal_state_provider_up").forGetter(ClusterConfiguration::crystalsUp),
            BlockStateProvider.CODEC.fieldOf("crystal_state_provider_down").forGetter(ClusterConfiguration::crystalsDown),
            Codec.floatRange(0.0F, 1.0F).fieldOf("crystal_chance").forGetter(ClusterConfiguration::crystalChance),
            IntProviders.codec(1, 32).fieldOf("size").forGetter(ClusterConfiguration::size),
            IntProviders.codec(1, 64).fieldOf("height").forGetter(ClusterConfiguration::height)
    ).apply(instance, ClusterConfiguration::new));
}
