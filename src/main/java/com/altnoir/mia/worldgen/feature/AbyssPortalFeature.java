package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.init.MiaBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class AbyssPortalFeature extends Feature<NoneFeatureConfiguration> {
    public AbyssPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        createPortalStructure(context.level(), context.origin(), 5, 2, false);
        return true;
    }

    public static void createPortalStructure(ServerLevelAccessor level, BlockPos pos, int height, int radius, boolean dropBlocks) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        final double radiusSquared = radius * radius;
        final int searchRadius = radius + 1;

        // 预计算边界值，避免在循环中重复计算
        final int minX = -searchRadius;
        final int minZ = -searchRadius;

        for (int y = 0; y < height; y++) {
            // 根据高度层决定默认方块状态
            final BlockState defaultBlockState = (y == 0) ? MiaBlocks.ABYSS_PORTAL.get().defaultBlockState() : Blocks.AIR.defaultBlockState();
            final boolean isBottomLayer = (y == 0);

            for (int x = minX; x <= searchRadius; x++) {
                // 预计算x的平方，避免在内部循环中重复计算
                final int xSquared = x * x;

                for (int z = minZ; z <= searchRadius; z++) {
                    mutablePos.set(pos).move(x, y, z);

                    // 计算当前点到圆心的距离平方
                    final double distanceSquared = xSquared + z * z;

                    // 判断是否在圆形内部
                    final boolean isInsideCircle = distanceSquared < radiusSquared;

                    // 判断是否应该放置墙壁块
                    boolean shouldPlaceWall = false;
                    if (!isInsideCircle) {
                        // 优化：使用位运算和预计算值减少计算量
                        // 检查当前点是否紧邻圆形内部（只检查4个正交方向）
                        shouldPlaceWall = (xSquared + (z - 1) * (z - 1) < radiusSquared) ||  // 前
                                ((x - 1) * (x - 1) + z * z < radiusSquared) ||  // 左
                                (xSquared + (z + 1) * (z + 1) < radiusSquared) ||  // 后
                                ((x + 1) * (x + 1) + z * z < radiusSquared);    // 右
                    }

                    // 确定要放置的方块状态
                    final BlockState blockState;
                    if (isInsideCircle) {
                        blockState = defaultBlockState;
                        // 如果不是底层且需要掉落方块，先销毁现有方块
                        if (!isBottomLayer && dropBlocks) {
                            final Block currentBlock = level.getBlockState(mutablePos).getBlock();
                            if (currentBlock != Blocks.AIR) {
                                level.destroyBlock(mutablePos, true, null);
                            }
                        }
                    } else if (shouldPlaceWall) {
                        blockState = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                    } else {
                        continue; // 跳过不需要放置方块的位置
                    }

                    // 设置方块状态
                    level.setBlock(mutablePos, blockState, 3);
                }
            }
        }
    }
}
