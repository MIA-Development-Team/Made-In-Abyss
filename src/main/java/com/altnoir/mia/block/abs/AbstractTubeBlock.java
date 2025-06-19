package com.altnoir.mia.block.abs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractTubeBlock extends DirectionalBlock {
    protected static final VoxelShape Y_AXIS_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0);

    protected AbstractTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        switch (state.getValue(FACING).getAxis()) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}

