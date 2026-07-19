package com.altnoir.mementoinabyss.content.block.column;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ColumnBlock extends Block {
    public static final MapCodec<ColumnBlock> CODEC = simpleCodec(ColumnBlock::new);
    public static final EnumProperty<ColumnSide> COLUMN = EnumProperty.create("column", ColumnSide.class);

    private static final VoxelShape TOP_SHAPE = Shapes.or(
            box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0),
            box(4.0, 0.0, 4.0, 12.0, 13.0, 12.0));
    private static final VoxelShape MIDDLE_SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape BOTTOM_SHAPE = Shapes.or(
            box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0));
    private static final VoxelShape SINGLE_SHAPE = Shapes.or(
            box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0),
            box(4.0, 3.0, 4.0, 12.0, 13.0, 12.0),
            box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0));

    public ColumnBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(COLUMN, ColumnSide.NONE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(COLUMN)) {
            case TOP -> TOP_SHAPE;
            case MIDDLE -> MIDDLE_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
            case NONE -> SINGLE_SHAPE;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(COLUMN, getColumnSide(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        var side = getColumnSide(level, pos);
        if (state.getValue(COLUMN) != side) {
            level.setBlock(pos, state.setValue(COLUMN, side), Block.UPDATE_ALL);
        }
    }

    private ColumnSide getColumnSide(Level level, BlockPos pos) {
        boolean above = level.getBlockState(pos.above()).is(this);
        boolean below = level.getBlockState(pos.below()).is(this);
        if (above && below) return ColumnSide.MIDDLE;
        if (above) return ColumnSide.BOTTOM;
        if (below) return ColumnSide.TOP;
        return ColumnSide.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLUMN);
    }
}
