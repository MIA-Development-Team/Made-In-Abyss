package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record VerdantTreeConfiguration(
        BlockState stemState,
        BlockState leavesState,
        BlockPredicate canPlaceOn,
        IntProvider height,
        IntProvider crownRadius)
        implements FeatureConfiguration {
    public static final Codec<VerdantTreeConfiguration> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            BlockState.CODEC
                                                    .fieldOf("stem_state")
                                                    .forGetter(VerdantTreeConfiguration::stemState),
                                            BlockState.CODEC
                                                    .fieldOf("leaves_state")
                                                    .forGetter(
                                                            VerdantTreeConfiguration::leavesState),
                                            BlockPredicate.CODEC
                                                    .fieldOf("can_place_on")
                                                    .forGetter(
                                                            VerdantTreeConfiguration::canPlaceOn),
                                            IntProviders.codec(5, 32)
                                                    .fieldOf("height")
                                                    .forGetter(VerdantTreeConfiguration::height),
                                            IntProviders.codec(3, 9)
                                                    .fieldOf("crown_radius")
                                                    .forGetter(
                                                            VerdantTreeConfiguration::crownRadius))
                                    .apply(instance, VerdantTreeConfiguration::new));
}
