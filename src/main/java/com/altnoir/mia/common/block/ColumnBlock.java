package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.properties.ColumnSide;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ColumnBlock extends Block {
    public static final MapCodec<ColumnBlock> CODEC = simpleCodec(ColumnBlock::new);
    public static final EnumProperty<ColumnSide> COLUMN = EnumProperty.create("column", ColumnSide.class);
    protected static final VoxelShape SHAPE_TOP = Shapes.or(
            Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0),
            Block.box(4.0, 0.0, 4.0, 12.0, 13.0, 12.0)
    );
    protected static final VoxelShape SHAPE_MIDDLE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    protected static final VoxelShape SHAPE_BOTTOM = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
            Block.box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0)
    );
    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0),
            Block.box(4.0, 3.0, 4.0, 12.0, 13.0, 12.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
    );

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public ColumnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(COLUMN, ColumnSide.NONE));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        ColumnSide side = state.getValue(COLUMN);
        return switch (side) {
            case TOP -> SHAPE_TOP;
            case BOTTOM -> SHAPE_BOTTOM;
            case MIDDLE -> SHAPE_MIDDLE;
            case NONE -> SHAPE;
        };
    }

    private ColumnSide getColumnState(Level level, BlockPos pos) {
        boolean hasAbove = level.getBlockState(pos.above()).getBlock() == this;
        boolean hasBelow = level.getBlockState(pos.below()).getBlock() == this;
        int stateKey = (hasAbove ? 1 : 0) << 1 | (hasBelow ? 1 : 0);
        return switch (stateKey) {
            case 3 -> ColumnSide.MIDDLE;  // 11: 上下都有
            case 2 -> ColumnSide.BOTTOM;  // 10: 只有上方
            case 1 -> ColumnSide.TOP;     // 01: 只有下方
            case 0 -> ColumnSide.NONE;    // 00: 都没有
            default -> ColumnSide.NONE;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        ColumnSide side = getColumnState(context.getLevel(), context.getClickedPos());
        return this.defaultBlockState().setValue(COLUMN, side);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (neighborPos.equals(pos.above()) || neighborPos.equals(pos.below())) {
            ColumnSide side = getColumnState(level, pos);

            if (state.getValue(COLUMN) != side) {
                level.setBlock(pos, state.setValue(COLUMN, side), 3);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLUMN);
    }
}
