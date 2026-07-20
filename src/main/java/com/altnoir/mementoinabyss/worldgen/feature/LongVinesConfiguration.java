package com.altnoir.mementoinabyss.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record LongVinesConfiguration(IntProvider height) implements FeatureConfiguration {
    public static final Codec<LongVinesConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProviders.codec(0, 256).fieldOf("height").forGetter(LongVinesConfiguration::height)
    ).apply(instance, LongVinesConfiguration::new));
}
