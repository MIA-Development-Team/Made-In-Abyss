package com.altnoir.mia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class HopperFarmBlock extends FarmBlock {
    public HopperFarmBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int i = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (i > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToDirt(null, state, level, pos);
            }
        } else if (i < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, Integer.valueOf(7)), 2);
        }
        checkCropGrowth(level, pos);
    }

    private void checkCropGrowth(ServerLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockPos belowPos = pos.below();
        BlockState aboveState = level.getBlockState(abovePos);
        BlockState belowState = level.getBlockState(belowPos);

        IntegerProperty ageProp = (IntegerProperty) aboveState.getBlock().getStateDefinition().getProperty("age");
        if (ageProp == null) return;

        int age = aboveState.getValue(ageProp);
        int maxAge = ageProp.getPossibleValues().size() - 1;

        if (age == maxAge && !belowState.isSolid()) {
            BlockState resetState = aboveState.setValue(ageProp, 0);
            level.setBlock(abovePos, resetState, 2);

            getDrops(aboveState, level, abovePos, null, null, ItemStack.EMPTY)
                    .forEach(stack -> Block.popResource(level, belowPos, stack));

            level.playSound(null, abovePos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS);
        }

    }

    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-3, 0, -3), pos.offset(3, 1, 3))) {
            if (state.canBeHydrated(level, pos, level.getFluidState(blockpos), blockpos)) {
                return true;
            }
        }

        return net.neoforged.neoforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}
