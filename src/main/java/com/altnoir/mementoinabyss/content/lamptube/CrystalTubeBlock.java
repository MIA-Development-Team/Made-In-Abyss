package com.altnoir.mementoinabyss.content.lamptube;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class CrystalTubeBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape Y_AXIS_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape Z_AXIS_AABB = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0);

    protected CrystalTubeBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.UP)
                        .setValue(POWERED, false)
                        .setValue(WATERLOGGED, false)
                        .setValue(levelProperty(), 1));
    }

    protected abstract IntegerProperty levelProperty();

    protected abstract int maxLevel();

    protected abstract int beamColor();

    protected abstract boolean affect(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockPos targetPos,
            BlockState targetState,
            int distance);

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> X_AXIS_AABB;
            case Z -> Z_AXIS_AABB;
            case Y -> Y_AXIS_AABB;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, POWERED, levelProperty());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction looking = context.getNearestLookingDirection().getOpposite();
        var player = context.getPlayer();
        Direction facing =
                player != null && player.isShiftKeyDown() ? looking.getOpposite() : looking;
        boolean water =
                context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, water)
                .setValue(POWERED, powered);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(
                state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            fire(level, pos, state);
            discharge(level, pos, state);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            @Nullable Orientation orientation,
            boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = state.getValue(POWERED);
        if (powered == level.hasNeighborSignal(pos)) {
            return;
        }
        if (powered) {
            level.scheduleTick(pos, this, 2);
        } else {
            playResonate(level, pos, state);
            discharge(level, pos, state);
        }
    }

    protected void fire(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        for (int distance = 1; distance <= 12; distance++) {
            BlockPos targetPos = pos.relative(direction, distance);
            if (affect(level, pos, state, targetPos, level.getBlockState(targetPos), distance)) {
                return;
            }
        }
    }

    protected boolean relay(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockPos targetPos,
            BlockState targetState,
            int distance) {
        if (!targetState.hasProperty(POWERED)) {
            return false;
        }
        playResonate(level, targetPos, state);
        beam(level, pos, targetPos, state);
        BlockState updated = targetState;
        if (targetState.is(this)) {
            int levelValue = Math.min(state.getValue(levelProperty()) + 1, maxLevel());
            updated = updated.setValue(levelProperty(), levelValue);
        }
        level.setBlock(targetPos, updated.cycle(POWERED), Block.UPDATE_CLIENTS);
        level.updateNeighborsAt(
                pos.relative(state.getValue(FACING), distance - 1), state.getBlock());
        return true;
    }

    protected void beam(Level level, BlockPos pos, BlockPos targetPos, BlockState state) {
        double startX = pos.getX() + 0.5;
        double startY = pos.getY() + 0.5;
        double startZ = pos.getZ() + 0.5;
        double endX = targetPos.getX() + 0.5;
        double endY = targetPos.getY() + 0.5;
        double endZ = targetPos.getZ() + 0.5;
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= 1.0) {
            return;
        }
        Direction facing = state.getValue(FACING);
        double dxFactor = facing.getAxis() == Direction.Axis.X ? 0.21 : 0.01;
        double dyFactor = facing.getAxis() == Direction.Axis.Y ? 0.21 : 0.01;
        double dzFactor = facing.getAxis() == Direction.Axis.Z ? 0.21 : 0.01;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new DustParticleOptions(beamColor(), 0.5F),
                    (startX + endX) / 2,
                    (startY + endY) / 2,
                    (startZ + endZ) / 2,
                    (int) Math.abs(dx + dy + dz) / 2 * 50,
                    dx * dxFactor,
                    dy * dyFactor,
                    dz * dzFactor,
                    0);
        }
    }

    protected void playResonate(Level level, BlockPos pos, BlockState state) {
        level.playSound(
                null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.5F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(state));
    }

    private void discharge(Level level, BlockPos pos, BlockState state) {
        level.setBlock(
                pos, state.cycle(POWERED).setValue(levelProperty(), 1), Block.UPDATE_CLIENTS);
    }
}
