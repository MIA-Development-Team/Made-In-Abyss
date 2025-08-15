package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;

public class HopperFarmBlock extends FarmBlock {
    public HopperFarmBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState()
                : super.getStateForPlacement(context);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToBlock(null, state, level, pos);
        }
        cropDrop(level, pos);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!level.isClientSide() && !level.getBlockTicks().hasScheduledTick(currentPos, this)) {
            level.scheduleTick(currentPos, this, 1);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    private void cropDrop(ServerLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockPos belowPos = pos.below();
        BlockState aboveState = level.getBlockState(abovePos);
        BlockState belowState = level.getBlockState(belowPos);

        IntegerProperty ageProp = (IntegerProperty) aboveState.getBlock().getStateDefinition().getProperty("age");
        if (ageProp == null) return;

        int age = aboveState.getValue(ageProp);
        Collection<Integer> possibleValues = ageProp.getPossibleValues();
        if (possibleValues.isEmpty()) return;
        // IntegerProperty的possibleValues保证按min→max顺序存储，直接取首元素
        int minAge = possibleValues.iterator().next();
        int maxAge = minAge == 0 ? possibleValues.size() - 1 : possibleValues.size();

        if (!notFarmland(aboveState) && !belowState.isCollisionShapeFullBlock(level, belowPos) && age == maxAge) {
            BlockState newState;
            if (aboveState.getBlock() instanceof SweetBerryBushBlock) {
                newState = aboveState.setValue(ageProp, minAge + 1);
            } else {
                newState = aboveState.setValue(ageProp, minAge);
            }
            level.setBlock(abovePos, newState, 2);

            getDrops(aboveState, level, abovePos, null, null, ItemStack.EMPTY)
                    .forEach(stack -> Block.popResource(level, belowPos, stack));

            level.playSound(null, abovePos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS);
        }
    }

    private boolean notFarmland(BlockState aboveState) {
        return aboveState.getBlock() instanceof TorchflowerCropBlock || aboveState.getBlock() instanceof PitcherCropBlock;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int i = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (i > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToBlock(null, state, level, pos);
            }
        } else if (i < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, Integer.valueOf(7)), 2);
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

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide) {
            entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
        }
    }

    public static void turnToBlock(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState blockstate = pushEntitiesUp(state, MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, blockstate);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, blockstate));
    }
}
