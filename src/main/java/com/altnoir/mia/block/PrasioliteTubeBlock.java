package com.altnoir.mia.block;

import com.altnoir.mia.block.abs.ACrystalTubeBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class PrasioliteTubeBlock extends ACrystalTubeBlock {
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
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean flag = state.getValue(POWERED);
        if (flag && !level.hasNeighborSignal(pos)) {
            finalProcessing(level, pos, state);
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 2);
                } else {
                    playAmethyst(level, pos, state);
                    var newState = state;
                    newState = newState.cycle(POWERED);
                    newState = newState.setValue(LEVEL, 1);
                    level.setBlock(pos, newState, 2);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected boolean crystalProcessing(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i) {
        if (targetState.getBlock() instanceof ACrystalTubeBlock) {
            return propagateSignal(level, pos, state, targetPos, targetState, i);
        } else return process(level, pos, state, targetPos);
    }

    private boolean process(Level level, BlockPos pos, BlockState state, BlockPos targetPos) {
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.getBlock() instanceof BonemealableBlock || inWater(level, targetPos)) {
            int size = state.getValue(LEVEL) - 1;
            BlockPos.betweenClosedStream(targetPos.offset(-size, 0, -size), targetPos.offset(size, 1, size))
                    .forEach(pos1 -> {
                        BoneMealItem.applyBonemeal(ItemStack.EMPTY, level, pos1, null);
                        BoneMealItem.growWaterPlant(ItemStack.EMPTY, level, pos1, null);
                        level.levelEvent(1505, pos1, 15);
                    });

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
