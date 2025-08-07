package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingPrasioliteBlock extends PrasioliteBlock {
    private static final Direction[] DIRECTIONS = Direction.values();
    public static final int GROWTH_CHANCE = 4;

    public BuddingPrasioliteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) == 0) {
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            Block block = null;
            if (canClusterGrowAtState(blockstate)) {
                block = MiaBlocks.SMALL_PRASIOLITE_BUD.get();
            } else if (blockstate.is(MiaBlocks.SMALL_PRASIOLITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = MiaBlocks.MEDIUM_PRASIOLITE_BUD.get();
            } else if (blockstate.is(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = MiaBlocks.LARGE_PRASIOLITE_BUD.get();
            } else if (blockstate.is(MiaBlocks.LARGE_PRASIOLITE_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = MiaBlocks.PRASIOLITE_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate.getFluidState().getType() == Fluids.WATER));
                level.setBlockAndUpdate(blockpos, blockstate1);
            }
        }
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
