package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.entity.CaveExplorerBeaconBlockEntity;
import com.altnoir.mia.init.MiaBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CaveExplorerBeaconBlock extends BaseEntityBlock {
    public static final MapCodec<CaveExplorerBeaconBlock> CODEC = simpleCodec(CaveExplorerBeaconBlock::new);
    protected static final VoxelShape ANGLES = Shapes.or(
            box(0.0, 0.0, 0.0, 4.0, 4.0, 4.0),
            box(0.0, 0.0, 12.0, 4.0, 4.0, 16.0),
            box(12.0, 0.0, 0.0, 16.0, 4.0, 4.0),
            box(12.0, 0.0, 12.0, 16.0, 4.0, 16.0)
    );
    protected static final VoxelShape SHAPE = Shapes.or(
            ANGLES,
            box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    );

    public CaveExplorerBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CaveExplorerBeaconBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, MiaBlockEntities.CAVE_EXPLORER_BEACON_ENTITY.get(), CaveExplorerBeaconBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
