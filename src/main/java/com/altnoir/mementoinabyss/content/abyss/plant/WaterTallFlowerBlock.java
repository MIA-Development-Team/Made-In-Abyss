package com.altnoir.mementoinabyss.content.abyss.plant;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WaterTallFlowerBlock extends WaterDoublePlantBlock implements BonemealableBlock {
    public WaterTallFlowerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultPlantState(defaultBlockState()));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(MiaBlocks.ABYSS_CHLOROPHYTE_ORE.get())
                || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(this));
    }
}
