package com.altnoir.mia.common.block;

import com.altnoir.mia.common.block.abs.AbsCanFarmBlock;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.common.item.PrasioliteHoeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbyssAndesiteBlock extends AbsCanFarmBlock implements BonemealableBlock {
    public AbyssAndesiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, @NotNull BlockState state) {
        if (!level.getBlockState(pos.above()).propagatesSkylightDown(level, pos)) {
            return false;
        } else {
            for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (level.getBlockState(blockpos).is(MiaTags.Blocks.COVERGRASS)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (context.getItemInHand().getItem() instanceof PrasioliteHoeItem) {
            if (state.is(this)) {
                return MiaBlocks.HOPPER_FARMLAND.get().defaultBlockState().setValue(BlockStateProperties.MOISTURE, 0);
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
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

            if (blockstate.is(MiaTags.Blocks.COVERGRASS)) {
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
