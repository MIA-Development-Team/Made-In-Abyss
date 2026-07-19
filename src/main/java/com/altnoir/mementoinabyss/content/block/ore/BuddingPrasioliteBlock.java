package com.altnoir.mementoinabyss.content.block.ore;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingPrasioliteBlock extends Block {
    public BuddingPrasioliteBlock(Properties properties) { super(properties); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState target = level.getBlockState(targetPos);
        Block next = null;
        if (canGrowInto(target)) next = MiaBlocks.SMALL_PRASIOLITE_BUD.get();
        else if (target.is(MiaBlocks.SMALL_PRASIOLITE_BUD.get()) && target.getValue(AmethystClusterBlock.FACING) == direction) next = MiaBlocks.MEDIUM_PRASIOLITE_BUD.get();
        else if (target.is(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get()) && target.getValue(AmethystClusterBlock.FACING) == direction) next = MiaBlocks.LARGE_PRASIOLITE_BUD.get();
        else if (target.is(MiaBlocks.LARGE_PRASIOLITE_BUD.get()) && target.getValue(AmethystClusterBlock.FACING) == direction) next = MiaBlocks.PRASIOLITE_CLUSTER.get();
        if (next != null) level.setBlockAndUpdate(targetPos, next.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, target.getFluidState().is(Fluids.WATER)));
    }
    private static boolean canGrowInto(BlockState state) { return state.isAir() || state.getFluidState().is(Fluids.WATER); }
}
