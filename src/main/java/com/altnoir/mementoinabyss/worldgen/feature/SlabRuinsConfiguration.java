package com.altnoir.mementoinabyss.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record SlabRuinsConfiguration(
        BlockStateProvider slabStateProvider,
        BlockStateProvider blockStateProvider) implements FeatureConfiguration {
    public static final Codec<SlabRuinsConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockStateProvider.CODEC.fieldOf("slab_state_provider")
                            .forGetter(SlabRuinsConfiguration::slabStateProvider),
                    BlockStateProvider.CODEC.fieldOf("block_state_provider")
                            .forGetter(SlabRuinsConfiguration::blockStateProvider)
            ).apply(instance, SlabRuinsConfiguration::new));
}
