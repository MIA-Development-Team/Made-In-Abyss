package com.altnoir.mia.block.abs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.joml.Vector3f;

import java.util.Objects;

public abstract class ACrystalTubeBlock extends ATubeBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected ACrystalTubeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var hasNeighbor = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(POWERED, Boolean.valueOf(hasNeighbor));
    }

    protected abstract IntegerProperty getLevelProperty();

    protected void update(Level level, BlockPos pos, BlockState state) {
        var newState = state.cycle(POWERED).setValue(getLevelProperty(), 1);
        level.setBlock(pos, newState, 2);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            finalProcessing(level, pos, state);
            update(level, pos, state);
        }
        super.tick(state, level, pos, random);
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
                    update(level, pos, state);
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    protected void finalProcessing(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        for (int i = 1; i <= 12; i++) {
            var targetPos = pos.relative(direction, i);
            var targetState = level.getBlockState(targetPos);
            if (crystalProcessing(level, pos, state, targetPos, targetState, i)) {
                return;
            }
        }
    }

    protected abstract boolean crystalProcessing(Level level, BlockPos pos, BlockState state, BlockPos targetPos, BlockState targetState, int i);

    protected void signalParticles(float red, float green, float blue, Level level, BlockPos pos, BlockPos targetPos, BlockState state) {
        double startX = pos.getX() + 0.5, startY = pos.getY() + 0.5, startZ = pos.getZ() + 0.5;
        double endX = targetPos.getX() + 0.5, endY = targetPos.getY() + 0.5, endZ = targetPos.getZ() + 0.5;

        double midX = (startX + endX) / 2, midY = (startY + endY) / 2, midZ = (startZ + endZ) / 2;

        double dx = endX - startX, dy = endY - startY, dz = endZ - startZ;

        int particleCount = (int) Math.abs(dx + dy + dz) / 2 * 50;

        Direction facing = state.getValue(FACING);

        double dxFactor = (facing == Direction.WEST || facing == Direction.EAST) ? 0.21 : 0.01;
        double dyFactor = (facing == Direction.UP || facing == Direction.DOWN) ? 0.21 : 0.01;
        double dzFactor = (facing == Direction.NORTH || facing == Direction.SOUTH) ? 0.21 : 0.01;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new DustParticleOptions(new Vector3f(red, green, blue), 0.5f),
                    midX, midY, midZ,
                    particleCount,
                    dx * dxFactor,
                    dy * dyFactor,
                    dz * dzFactor,
                    0
            );
        }
    }

    protected void playAmethyst(Level level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.5F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(state));
    }
}
