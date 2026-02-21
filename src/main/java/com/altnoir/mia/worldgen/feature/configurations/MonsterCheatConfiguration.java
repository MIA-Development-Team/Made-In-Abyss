package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public class MonsterCheatConfiguration implements FeatureConfiguration {
    public static final Codec<MonsterCheatConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockStateProvider.CODEC.fieldOf("core_state_provider").forGetter(config -> config.coreStateProvider),
                    BlockStateProvider.CODEC.fieldOf("outer_state_provider").forGetter(config -> config.outerStateProvider)
            ).apply(instance, MonsterCheatConfiguration::new)
    );

    public final BlockStateProvider coreStateProvider;
    public final BlockStateProvider outerStateProvider;

    public MonsterCheatConfiguration(BlockStateProvider coreStateProvider, BlockStateProvider outerStateProvider) {
        this.coreStateProvider = coreStateProvider;
        this.outerStateProvider = outerStateProvider;
    }
}