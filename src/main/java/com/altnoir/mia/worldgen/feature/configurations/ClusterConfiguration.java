package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ClusterConfiguration implements FeatureConfiguration {
    public static final Codec<ClusterConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(config -> config.stateProvider),
                    BlockStateProvider.CODEC.fieldOf("crystal_state_provider_up").forGetter(config -> config.crystalStateProviderUp),
                    BlockStateProvider.CODEC.fieldOf("crystal_state_provider_down").forGetter(config -> config.crystalStateProviderDown),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("crystal_chance").forGetter(config -> config.crystalChance),
                    IntProvider.codec(1, 32).fieldOf("size").forGetter(config -> config.size),
                    IntProvider.codec(1, 64).fieldOf("height").forGetter(config -> config.height)
            ).apply(instance, ClusterConfiguration::new)
    );

    public final BlockStateProvider stateProvider;
    public final BlockStateProvider crystalStateProviderUp;
    public final BlockStateProvider crystalStateProviderDown;
    public final float crystalChance;
    public final IntProvider size;
    public final IntProvider height;

    public ClusterConfiguration(
            BlockStateProvider stateProvider,
            BlockStateProvider crystalStateProviderUp,
            BlockStateProvider crystalStateProviderDown,
            float crystalChance,
            IntProvider size,
            IntProvider height) {
        this.stateProvider = stateProvider;
        this.crystalStateProviderUp = crystalStateProviderUp;
        this.crystalStateProviderDown = crystalStateProviderDown;
        this.crystalChance = crystalChance;
        this.size = size;
        this.height = height;
    }
}