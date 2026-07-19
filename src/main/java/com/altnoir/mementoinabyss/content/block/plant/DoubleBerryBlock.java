package com.altnoir.mementoinabyss.content.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class DoubleBerryBlock extends BushBlock implements BonemealableBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;

    protected DoubleBerryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, 0));
    }

    @Override protected boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < MAX_AGE; }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;
        int age = state.getValue(AGE);
        if (age < MAX_AGE && canOccupyUpper(level, pos) && level.getRawBrightness(pos.above(), 0) >= 9 && random.nextInt(5) == 0) {
            setAge(level, pos, state, age + 1);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        var half = state.getValue(HALF);
        if (state.getValue(AGE) > 1 && direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighbor.is(this) && neighbor.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    protected void setAge(ServerLevel level, BlockPos pos, BlockState state, int age) {
        var lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        var lower = level.getBlockState(lowerPos);
        if (!lower.is(this) || age > 1 && !canOccupyUpper(level, lowerPos)) return;
        level.setBlock(lowerPos, lower.setValue(AGE, age), Block.UPDATE_CLIENTS);
        var upperPos = lowerPos.above();
        if (age > 1) {
            var upper = defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, age);
            level.setBlock(upperPos, upper, Block.UPDATE_CLIENTS);
            level.gameEvent(GameEvent.BLOCK_CHANGE, upperPos, GameEvent.Context.of(upper));
        } else if (level.getBlockState(upperPos).is(this)) level.removeBlock(upperPos, false);
    }

    private boolean canOccupyUpper(LevelReader level, BlockPos lowerPos) {
        var upper = level.getBlockState(lowerPos.above());
        return upper.isAir() || upper.is(this);
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        var lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        return state.getValue(AGE) < MAX_AGE && canOccupyUpper(level, lowerPos);
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { setAge(level, pos, state, Math.min(MAX_AGE, state.getValue(AGE) + 1)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HALF, AGE); }
}
