package com.altnoir.mementoinabyss.content.block;

import com.altnoir.mementoinabyss.content.block.entity.CaveExplorerBeaconBlockEntity;
import com.altnoir.mementoinabyss.init.MiaBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class CaveExplorerBeaconBlock extends BaseEntityBlock implements BeaconBeamBlock {
    public static final MapCodec<CaveExplorerBeaconBlock> CODEC =
            simpleCodec(CaveExplorerBeaconBlock::new);
    private static final VoxelShape SHAPE =
            Shapes.or(
                    Block.box(0.0, 0.0, 0.0, 4.0, 4.0, 4.0),
                    Block.box(0.0, 0.0, 12.0, 4.0, 4.0, 16.0),
                    Block.box(12.0, 0.0, 0.0, 16.0, 4.0, 4.0),
                    Block.box(12.0, 0.0, 12.0, 16.0, 4.0, 16.0),
                    Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));

    public CaveExplorerBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return MiaBlockEntityTypes.CAVE_EXPLORER_BEACON.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                MiaBlockEntityTypes.CAVE_EXPLORER_BEACON.get(),
                CaveExplorerBeaconBlockEntity::tick);
    }
}
