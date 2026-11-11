package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public record FallenLogConfiguration(
        BlockStateProvider log, IntProvider length, List<TreeDecorator> decorators)
        implements FeatureConfiguration {
    public static final Codec<FallenLogConfiguration> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            BlockStateProvider.CODEC
                                                    .fieldOf("log")
                                                    .forGetter(FallenLogConfiguration::log),
                                            IntProviders.codec(1, 16)
                                                    .fieldOf("length")
                                                    .forGetter(FallenLogConfiguration::length),
                                            TreeDecorator.CODEC
                                                    .listOf()
                                                    .fieldOf("decorators")
                                                    .forGetter(FallenLogConfiguration::decorators))
                                    .apply(instance, FallenLogConfiguration::new));
}
