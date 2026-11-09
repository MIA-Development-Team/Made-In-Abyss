package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public final class SegmentedVegetationProvider extends BlockStateProvider {
    public static final MapCodec<SegmentedVegetationProvider> CODEC =
            BlockStateProvider.CODEC
                    .fieldOf("source")
                    .xmap(SegmentedVegetationProvider::new, provider -> provider.source);

    private final BlockStateProvider source;

    public SegmentedVegetationProvider(BlockStateProvider source) {
        this.source = source;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return MiaWorldgenFeatures.SEGMENTED_VEGETATION.get();
    }

    @Override
    public BlockState getState(WorldGenLevel level, RandomSource random, BlockPos pos) {
        BlockState state = this.source.getState(level, random, pos);
        if (!(state.getBlock() instanceof SegmentableBlock segmented)
                || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                || !state.hasProperty(segmented.getSegmentAmountProperty())) return state;
        var amounts = segmented.getSegmentAmountProperty().getPossibleValues();
        return state.setValue(
                        BlockStateProperties.HORIZONTAL_FACING,
                        Direction.from2DDataValue(random.nextInt(4)))
                .setValue(
                        segmented.getSegmentAmountProperty(),
                        amounts.get(random.nextInt(amounts.size())));
    }
}
