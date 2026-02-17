package com.altnoir.mia.worldgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public record BlockTrunkConfiguration(List<BlockTrunkConfiguration.Layer> layers,
                                      BlockTrunkConfiguration.Layer dec, BlockTrunkConfiguration.Layer decFace,
                                      float decChance,
                                      Direction direction,
                                      BlockPredicate allowedPlacement, boolean prioritizeTip)
        implements FeatureConfiguration {
    public static final Codec<BlockTrunkConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BlockTrunkConfiguration.Layer.CODEC.listOf().fieldOf("layers").forGetter(BlockTrunkConfiguration::layers),
                            BlockTrunkConfiguration.Layer.CODEC.fieldOf("dec").forGetter(BlockTrunkConfiguration::dec),
                            BlockTrunkConfiguration.Layer.CODEC.fieldOf("dec_face").forGetter(BlockTrunkConfiguration::decFace),
                            Codec.FLOAT.fieldOf("dec_chance").forGetter(BlockTrunkConfiguration::decChance),
                            Direction.CODEC.fieldOf("direction").forGetter(BlockTrunkConfiguration::direction),
                            BlockPredicate.CODEC.fieldOf("allowed_placement").forGetter(BlockTrunkConfiguration::allowedPlacement),
                            Codec.BOOL.fieldOf("prioritize_tip").forGetter(BlockTrunkConfiguration::prioritizeTip)
                    )
                    .apply(instance, BlockTrunkConfiguration::new)
    );

    public static BlockTrunkConfiguration.Layer layer(BlockStateProvider state) {
        return layer(ConstantInt.of(1), state);
    }

    public static BlockTrunkConfiguration.Layer layer(IntProvider height, BlockStateProvider state) {
        return new BlockTrunkConfiguration.Layer(height, state);
    }

    public static BlockTrunkConfiguration simple(IntProvider height, BlockStateProvider state) {
        return new BlockTrunkConfiguration(List.of(layer(height, state)), layer(height, state), layer(height, state), 0.2F, Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, false);
    }

    public record Layer(IntProvider height, BlockStateProvider state) {
        public static final Codec<BlockTrunkConfiguration.Layer> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(BlockTrunkConfiguration.Layer::height),
                                BlockStateProvider.CODEC.fieldOf("provider").forGetter(BlockTrunkConfiguration.Layer::state)
                        )
                        .apply(instance, BlockTrunkConfiguration.Layer::new)
        );
    }
}
