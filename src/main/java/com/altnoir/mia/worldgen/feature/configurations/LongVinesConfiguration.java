package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record LongVinesConfiguration(IntProvider height) implements FeatureConfiguration {
    public static final Codec<LongVinesConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(LongVinesConfiguration::height)
                    )
                    .apply(instance, LongVinesConfiguration::new)
    );
}
