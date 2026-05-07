package com.altnoir.mementoinabyss.content.block.abyss_andesite;

import com.altnoir.mementoinabyss.content.block.base.TillableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AbyssAndesiteBlock extends Block implements TillableBlock, BonemealableBlock {
    public AbyssAndesiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getTilledState(BlockState state, Level level, BlockPos pos, Player player) {
        return Blocks.FARMLAND.defaultBlockState();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {

    }
}
