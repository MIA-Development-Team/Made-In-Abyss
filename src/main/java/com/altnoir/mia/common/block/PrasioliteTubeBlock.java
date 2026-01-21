package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.abs.AbsCrystalTubeBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrasioliteTubeBlock extends AbsCrystalTubeBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 4);
    public static final MapCodec<PrasioliteTubeBlock> CODEC = simpleCodec(PrasioliteTubeBlock::new);

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    public PrasioliteTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 1));

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(LEVEL, 1);
    }

    @Override
    protected IntegerProperty getLevelProperty() {
        return LEVEL;
    }

    @Override
    protected boolean crystalProcessing(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i) {
        if (targetState.getBlock() instanceof AbsCrystalTubeBlock) {
            return propagateSignal(level, pos, state, targetPos, targetState, i);
        } else if (!targetState.isAir()) {
            return growOn(level, pos, state, targetPos);
        }
        return false;
    }

    private boolean growOn(Level level, BlockPos pos, BlockState state, BlockPos targetPos) {
        int size = state.getValue(LEVEL) - 1;
        AtomicBoolean hasGrown = new AtomicBoolean(false);

        BlockPos.betweenClosedStream(
                targetPos.offset(-size, -1, -size),
                targetPos.offset(size, 0, size)
        ).forEach(pos1 -> {
            boolean flag = BoneMealItem.applyBonemeal(ItemStack.EMPTY, level, pos1, null)
                    || BoneMealItem.growWaterPlant(ItemStack.EMPTY, level, pos1, null);
            if (flag) {
                level.levelEvent(1505, pos1, 7);
                hasGrown.set(true);
            }
        });
        if (hasGrown.get()) {
            signalParticles(0.5F, 1.0F, 0.5F, level, pos, targetPos, state);
            return true;
        }
        return false;
    }

    private boolean inWater(Level level, BlockPos targetPos) {
        return level.getBlockState(targetPos.above()).is(Blocks.WATER) && level.getFluidState(targetPos.above()).getAmount() == 8;
    }

    private boolean propagateSignal(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i) {
        playAmethyst(level, targetPos, state);
        signalParticles(0.5F, 1.0F, 0.5F, level, pos, targetPos, state);

        var newState = targetState;
        if (targetState.getBlock() instanceof PrasioliteTubeBlock) {
            newState = newState.setValue(LEVEL, Math.min(state.getValue(LEVEL) + 1, 4));
        }
        Direction direction = state.getValue(FACING);
        newState = newState.cycle(POWERED);
        level.setBlock(targetPos, newState, 2);
        level.updateNeighborsAt(pos.relative(direction, i - 1), state.getBlock());
        return true;
    }
}
