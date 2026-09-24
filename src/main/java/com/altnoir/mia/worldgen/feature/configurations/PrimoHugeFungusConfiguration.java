package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * 太初巨型菌的配置（移植自 PoopSky 的 {@code PoHugeFungusConfiguration}）。
 */
public record PrimoHugeFungusConfiguration(
        TagKey<Block> validBaseTag,
        BlockState stemState,
        BlockState hatState,
        Optional<BlockState> decorState,
        BlockPredicate replaceableBlocks)
        implements FeatureConfiguration {
    public static final Codec<PrimoHugeFungusConfiguration> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            TagKey.hashedCodec(Registries.BLOCK)
                                                    .fieldOf("valid_base_block")
                                                    .forGetter(
                                                            PrimoHugeFungusConfiguration
                                                                    ::validBaseTag),
                                            BlockState.CODEC
                                                    .fieldOf("stem_state")
                                                    .forGetter(
                                                            PrimoHugeFungusConfiguration
                                                                    ::stemState),
                                            BlockState.CODEC
                                                    .fieldOf("hat_state")
                                                    .forGetter(
                                                            PrimoHugeFungusConfiguration::hatState),
                                            BlockState.CODEC
                                                    .optionalFieldOf("decor_state")
                                                    .forGetter(
                                                            PrimoHugeFungusConfiguration
                                                                    ::decorState),
                                            BlockPredicate.CODEC
                                                    .fieldOf("replaceable_blocks")
                                                    .forGetter(
                                                            PrimoHugeFungusConfiguration
                                                                    ::replaceableBlocks))
                                    .apply(instance, PrimoHugeFungusConfiguration::new));
}
