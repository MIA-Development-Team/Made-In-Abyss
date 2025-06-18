package com.altnoir.mia.block;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AbyssAndesiteBlock extends Block implements BonemealableBlock {
    public AbyssAndesiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, @NotNull BlockState state) {
        if (!level.getBlockState(pos.above()).propagatesSkylightDown(level, pos)) {
            return false;
        } else {
            for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (level.getBlockState(blockpos).is(MiaBlockTags.COVERGRASS)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        boolean flag = false;

        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            BlockState blockstate = level.getBlockState(blockpos);

            if (blockstate.is(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())) {
                flag = true;
            }

            if (flag) {
                break;
            }
        }

        if (flag) {
            level.setBlock(pos, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get().defaultBlockState(), 3);
        }
    }

    @Override
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }
}
