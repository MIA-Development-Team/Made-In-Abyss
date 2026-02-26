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
        createPortalStructure(context.level(), context.origin(), MiaBlocks.ABYSS_PORTAL.get(), 0);
        return true;
    }

    public static void createPortalStructure(ServerLevelAccessor level, BlockPos pos, int height) {
        basePortalStructure(level, pos, Blocks.AIR, height, 8, true);
    }

    public static void createPortalStructure(ServerLevelAccessor level, BlockPos pos, Block block, int height) {
        basePortalStructure(level, pos, block, height, 8, false);
    }

    public static void basePortalStructure(ServerLevelAccessor level, BlockPos pos, Block block, int height, int radius, boolean dropBlocks) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        final double radiusSquared = radius * radius;
        final int wallRadius = radius + 1;

        for (int x = -wallRadius; x <= wallRadius; x++) {
            final int xSquared = x * x;

            for (int z = -wallRadius; z <= wallRadius; z++) {
                mutablePos.set(pos).move(x, height, z);
                final double distanceSquared = xSquared + z * z;
                // 判断是否在圆形内部
                final boolean isInsideCircle = distanceSquared < radiusSquared;

                // 判断是否应该放置墙壁块
                boolean shouldPlaceWall = false;
                if (!isInsideCircle) {
                    shouldPlaceWall = (xSquared + (z - 1) * (z - 1) < radiusSquared) // 前
                            || ((x - 1) * (x - 1) + z * z < radiusSquared) // 左
                            || (xSquared + (z + 1) * (z + 1) < radiusSquared) // 后
                            || ((x + 1) * (x + 1) + z * z < radiusSquared); // 右
                }
                final BlockState blockState;
                boolean isCenterColumn = (x == 0 && z == 0);

                if (isInsideCircle && !isCenterColumn) {
                    blockState = block.defaultBlockState();
                    if (dropBlocks) {
                        final Block currentBlock = level.getBlockState(mutablePos).getBlock();
                        if (currentBlock != Blocks.AIR) {
                            level.destroyBlock(mutablePos, true, null);
                        }
                    }
                } else if (isCenterColumn) {
                    blockState = MiaBlocks.FOSSILIZED_WOOD.get().defaultBlockState();
                } else if (shouldPlaceWall) {
                    var random = level.getRandom().nextInt(3);
                    blockState = switch (random) {
                        case 0 -> MiaBlocks.FOSSILIZED_WOOD.get().defaultBlockState();
                        case 1 -> Blocks.POLISHED_TUFF.defaultBlockState();
                        default -> Blocks.TUFF.defaultBlockState();
                    };
                } else {
                    continue;
                }

                level.setBlock(mutablePos, blockState, 3);
            }
        }
    }
}
