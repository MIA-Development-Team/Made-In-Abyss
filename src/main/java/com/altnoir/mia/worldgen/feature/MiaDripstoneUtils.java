package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.init.MiaTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MiaDripstoneUtils {
    protected static double getDripstoneHeight(double radius, double maxRadius, double scale, double minRadius) {
        if (radius < minRadius) {
            radius = minRadius;
        }

        double d0 = 0.384;
        double d1 = radius / maxRadius * 0.384;
        double d2 = 0.75 * Math.pow(d1, 1.3333333333333333);
        double d3 = Math.pow(d1, 0.6666666666666666);
        double d4 = 0.3333333333333333 * Math.log(d1);
        double d5 = scale * (d2 - d3 - d4);
        d5 = Math.max(d5, 0.0);
        return d5 / 0.384 * maxRadius;
    }

    protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel level, BlockPos pos, int radius) {
        if (isEmptyOrWaterOrLava(level, pos)) {
            return false;
        } else {
            float f = 6.0F;
            float f1 = 6.0F / (float) radius;

            for (float f2 = 0.0F; f2 < (float) (Math.PI * 2); f2 += f1) {
                int i = (int) (Mth.cos(f2) * (float) radius);
                int j = (int) (Mth.sin(f2) * (float) radius);
                if (isEmptyOrWaterOrLava(level, pos.offset(i, 0, j))) {
                    return false;
                }
            }

            return true;
        }
    }

    protected static boolean isEmptyOrWater(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, MiaDripstoneUtils::isEmptyOrWater);
    }

    protected static boolean isEmptyOrWaterOrLava(LevelAccessor level, BlockPos pos) {
        return level.isStateAtPosition(pos, MiaDripstoneUtils::isEmptyOrWaterOrLava);
    }

    public static boolean isDripstoneBaseOrLava(BlockState state) {
        return isDripstoneBase(state) || state.is(Blocks.LAVA);
    }

    public static boolean isDripstoneBase(BlockState state) {
        return !state.is(MiaTags.Blocks.ABYSS_DRIPSTONE_UNREPLACEABLE);
    }

    public static boolean isEmptyOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }

    public static boolean isNeitherEmptyNorWater(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER);
    }

    public static boolean isEmptyOrWaterOrLava(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}
