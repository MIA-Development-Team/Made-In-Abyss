package com.altnoir.mementoinabyss.content.abyss.plant;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public final class DuskveilVinesBlock extends GrowingPlantHeadBlock implements CaveVines {
    public static final MapCodec<DuskveilVinesBlock> CODEC = simpleCodec(DuskveilVinesBlock::new);

    public DuskveilVinesBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.1);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(BERRIES, false));
    }

    @Override
    public MapCodec<DuskveilVinesBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block getBodyBlock() {
        return MiaBlocks.DUSKVEIL_VINES_PLANT.get();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return DuskveilVines.supportedByLeaves(level, pos) || super.canSurvive(state, level, pos);
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1;
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    @Override
    protected BlockState updateBodyAfterConvertedFromHead(
            BlockState headState, BlockState bodyState) {
        return bodyState.setValue(BERRIES, headState.getValue(BERRIES));
    }

    @Override
    protected BlockState getGrowIntoState(BlockState growFromState, RandomSource random) {
        return super.getGrowIntoState(growFromState, random)
                .setValue(BERRIES, random.nextFloat() < 0.11F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BERRIES);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(BERRIES);
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(BERRIES, true), Block.UPDATE_CLIENTS);
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(MiaItems.DUSKVEIL_BERRIES.get());
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return DuskveilVines.harvest(state, level, pos, player);
    }
}
