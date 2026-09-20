package com.altnoir.mementoinabyss.content.abyss.farmland;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class HopperFarmlandBlock extends FarmlandBlock {
    public HopperFarmlandBlock(Properties properties) {
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
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        if (!ticks.getBlockTicks().hasScheduledTick(pos, this)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(
                state,
                level,
                ticks,
                pos,
                directionToNeighbour,
                neighbourPos,
                neighbourState,
                random);
    }

    private void cropDrop(ServerLevel level, BlockPos pos) {
        var abovePos = pos.above();
        var belowPos = pos.below();
        var aboveState = level.getBlockState(abovePos);
        var belowState = level.getBlockState(belowPos);

        var ageProp =
                (IntegerProperty) aboveState.getBlock().getStateDefinition().getProperty("age");
        if (ageProp == null) return;

        int age = aboveState.getValue(ageProp);
        Collection<Integer> possibleValues = ageProp.getPossibleValues();
        if (possibleValues.isEmpty()) return;

        int minAge = possibleValues.iterator().next();
        int maxAge = minAge == 0 ? possibleValues.size() - 1 : possibleValues.size();

        if (!notFarmland(aboveState)
                && !belowState.isCollisionShapeFullBlock(level, belowPos)
                && age == maxAge) {
            BlockState newState;
            if (aboveState.getBlock() instanceof SweetBerryBushBlock) {
                newState = aboveState.setValue(ageProp, minAge + 1);
            } else {
                newState = aboveState.setValue(ageProp, minAge);
            }
            level.setBlock(abovePos, newState, 2);

            getDrops(aboveState, level, abovePos, null)
                    .forEach(
                            stack -> {
                                var itemEntity =
                                        new ItemEntity(
                                                level,
                                                belowPos.getX() + 0.5,
                                                belowPos.getY() + 0.5,
                                                belowPos.getZ() + 0.5,
                                                stack);
                                itemEntity.setDeltaMovement(0, 0, 0);
                                level.addFreshEntity(itemEntity);
                            });

            level.playSound(null, abovePos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS);
        }
    }

    private boolean notFarmland(BlockState aboveState) {
        return aboveState.getBlock() instanceof TorchflowerCropBlock
                || aboveState.getBlock() instanceof PitcherCropBlock;
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToBlock(null, state, level, pos);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        var state = level.getBlockState(pos);
        for (var blockPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (state.canBeHydrated(level, pos, level.getFluidState(blockPos), blockPos)) {
                return true;
            }
        }
        return net.neoforged.neoforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }

    @Override
    public void fallOn(
            Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        // No trampling — hopper farmland stays tilled.
        entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
    }

    public static void turnToBlock(
            @Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        var newState =
                pushEntitiesUp(
                        state, MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, newState));
    }
}
