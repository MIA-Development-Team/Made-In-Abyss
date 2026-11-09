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
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public final class DuskveilVinesPlantBlock extends GrowingPlantBodyBlock implements CaveVines {
    public static final MapCodec<DuskveilVinesPlantBlock> CODEC =
            simpleCodec(DuskveilVinesPlantBlock::new);

    public DuskveilVinesPlantBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
        registerDefaultState(stateDefinition.any().setValue(BERRIES, false));
    }

    @Override
    public MapCodec<DuskveilVinesPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return MiaBlocks.DUSKVEIL_VINES.get();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return DuskveilVines.supportedByLeaves(level, pos) || super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateHeadAfterConvertedFromBody(
            BlockState bodyState, BlockState headState) {
        return headState.setValue(BERRIES, bodyState.getValue(BERRIES));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
