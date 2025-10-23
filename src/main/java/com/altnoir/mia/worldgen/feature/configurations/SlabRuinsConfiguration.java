package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class SlabRuinsConfiguration implements FeatureConfiguration {
    public static final Codec<SlabRuinsConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockStateProvider.CODEC.fieldOf("slab_state_provider").forGetter(config -> config.slabStateProvider),
                    BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter(config -> config.blockStateProvider)
            ).apply(instance, SlabRuinsConfiguration::new)
    );

    public final BlockStateProvider slabStateProvider;
    public final BlockStateProvider blockStateProvider;

    public SlabRuinsConfiguration(BlockStateProvider coreStateProvider, BlockStateProvider outerStateProvider) {
        this.slabStateProvider = coreStateProvider;
        this.blockStateProvider = outerStateProvider;
    }
}
