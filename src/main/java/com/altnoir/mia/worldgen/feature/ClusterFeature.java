package com.altnoir.mia.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;

public class ClusterFeature extends Feature<LargeDripstoneConfiguration> {
    public ClusterFeature(Codec<LargeDripstoneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // 检查是否为有效地板（下方为固体方块）
        if (!level.getBlockState(pos).isSolid()) {
            return false;
        }

        // 生成高度（5-8层）
        int height = random.nextInt(4) + 5;

        // 倾斜方向（-1, 0, 1）
        int tiltX = random.nextInt(3) - 1;
        int tiltZ = random.nextInt(3) - 1;

        // 生成水晶柱
        for (int y = 0; y < height; y++) {
            BlockPos currentPos = pos.above(y);
            // 应用倾斜偏移
            BlockPos offsetPos = currentPos.offset(tiltX * y, 0, tiltZ * y);

            // 检查空间有效性
            if (level.getBlockState(offsetPos).isAir()) {
                level.setBlock(offsetPos, Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
            }
        }

        return true;
    }
}
