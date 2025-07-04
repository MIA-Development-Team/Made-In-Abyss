package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

public class MiaWoodBlock extends RotatedPillarBlock {
    public MiaWoodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (context.getItemInHand().getItem() instanceof AxeItem) {
            if (state.is(MiaBlocks.SKYFOG_LOG)) {
                return MiaBlocks.STRIPPED_SKYFOG_LOG.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
            }
            if (state.is(MiaBlocks.SKYFOG_WOOD)) {
                return MiaBlocks.STRIPPED_SKYFOG_WOOD.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
            }

        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
