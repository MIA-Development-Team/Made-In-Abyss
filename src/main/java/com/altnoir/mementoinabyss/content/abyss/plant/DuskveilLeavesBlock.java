package com.altnoir.mementoinabyss.content.abyss.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IShearable;

public final class DuskveilLeavesBlock extends Block implements SimpleWaterloggedBlock, IShearable {
    public static final int DECAY_DISTANCE = 14;
    public static final IntegerProperty DISTANCE =
            IntegerProperty.create("distance", 1, DECAY_DISTANCE);
    public static final MapCodec<DuskveilLeavesBlock> CODEC = simpleCodec(DuskveilLeavesBlock::new);

    public DuskveilLeavesBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(DISTANCE, DECAY_DISTANCE)
                        .setValue(BlockStateProperties.PERSISTENT, false)
                        .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected MapCodec<DuskveilLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, BlockStateProperties.PERSISTENT, BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(DISTANCE) == DECAY_DISTANCE
                && !state.getValue(BlockStateProperties.PERSISTENT);
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int distance = distanceAt(level, pos);
        if (distance < DECAY_DISTANCE) {
            level.setBlock(pos, state.setValue(DISTANCE, distance), UPDATE_ALL);
        } else {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int distance = distanceAt(level, pos);
        if (distance != state.getValue(DISTANCE)) {
            level.setBlock(pos, state.setValue(DISTANCE, distance), UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighbor,
            RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (Math.min(DECAY_DISTANCE, supportDistance(neighbor) + 1) != state.getValue(DISTANCE)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    private static int distanceAt(BlockGetter level, BlockPos pos) {
        int distance = DECAY_DISTANCE;
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            int candidate =
                    supportDistance(level.getBlockState(neighbor.setWithOffset(pos, direction)))
                            + 1;
            if (candidate == 1) return 1;
            distance = Math.min(distance, candidate);
        }
        return distance;
    }

    private static int supportDistance(BlockState state) {
        if (state.hasProperty(DISTANCE)) return state.getValue(DISTANCE);
        int distance = LeavesBlock.getOptionalDistanceAt(state).orElse(LeavesBlock.DECAY_DISTANCE);
        return distance < LeavesBlock.DECAY_DISTANCE ? distance : DECAY_DISTANCE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(BlockStateProperties.PERSISTENT, true)
                .setValue(
                        BlockStateProperties.WATERLOGGED, level.getFluidState(pos).is(Fluids.WATER))
                .setValue(DISTANCE, distanceAt(level, pos));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 1;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.isRainingAt(pos.above()) || random.nextInt(15) != 1) return;
        BlockPos below = pos.below();
        BlockState floor = level.getBlockState(below);
        if (!floor.canOcclude() || !floor.isFaceSturdy(level, below, Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypes.DRIPPING_WATER);
        }
    }
}
