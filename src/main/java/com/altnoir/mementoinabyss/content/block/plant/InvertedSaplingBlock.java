package com.altnoir.mementoinabyss.content.block.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InvertedSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<InvertedSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            TreeGrower.CODEC.fieldOf("tree").forGetter(b -> b.treeGrower), propertiesCodec()).apply(i, InvertedSaplingBlock::new));
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    private static final VoxelShape SHAPE = Block.column(12.0, 4.0, 16.0);
    private final TreeGrower treeGrower;

    public InvertedSaplingBlock(TreeGrower treeGrower, Properties properties) {
        super(properties);
        this.treeGrower = treeGrower;
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override public MapCodec<InvertedSaplingBlock> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return state.is(BlockTags.DIRT) || state.isFaceSturdy(level, pos, Direction.DOWN); }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return mayPlaceOn(level.getBlockState(pos.above()), level, pos.above()); }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { if (random.nextInt(7) == 0) advanceTree(level, pos, state, random); }
    private void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) level.setBlock(pos, state.cycle(STAGE), Block.UPDATE_CLIENTS);
        else treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return random.nextFloat() < 0.45F; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) { advanceTree(level, pos, state, random); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(STAGE); }
}
