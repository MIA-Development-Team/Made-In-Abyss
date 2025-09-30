package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ClusterConfiguration(
        BlockState state,
        BlockState crystalState,
        IntProvider size,
        IntProvider height,
        float crystalChance
) implements FeatureConfiguration {

    public static final Codec<ClusterConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockState.CODEC.fieldOf("state").forGetter(ClusterConfiguration::state),
                    BlockState.CODEC.fieldOf("crystal_state").forGetter(ClusterConfiguration::crystalState),
                    IntProvider.codec(1, 16).fieldOf("size").forGetter(ClusterConfiguration::size),
                    IntProvider.codec(1, 32).fieldOf("height").forGetter(ClusterConfiguration::height),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("crystal_chance").forGetter(ClusterConfiguration::crystalChance)
            ).apply(instance, ClusterConfiguration::new)
    );
}