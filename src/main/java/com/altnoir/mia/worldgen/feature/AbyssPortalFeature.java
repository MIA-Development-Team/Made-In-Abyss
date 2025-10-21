package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.init.MiaBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
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

    public static void createPortalStructure(ServerLevelAccessor level, BlockPos pos, int height, int size, boolean dropBlocks) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        for (int y = 0; y < height; y++) {
            for (int x = -size; x <= size; x++) {
                for (int z = -size; z <= size; z++) {
                    mutablePos.set(pos).move(x, y, z);

                    boolean flag = Math.abs(x) <= size - 1 && Math.abs(z) <= size - 1;
                    boolean flag2 = Math.abs(x) == size && Math.abs(z) == size;
                    if (y == 0) {
                        if (flag) {
                            level.setBlock(mutablePos, MiaBlocks.ABYSS_PORTAL.get().defaultBlockState(), 3);
                        } else if (!flag2) {
                            level.setBlock(mutablePos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                        }
                    } else {
                        if (flag) {
                            if (dropBlocks && !level.getBlockState(mutablePos).is(Blocks.AIR)) {
                                level.destroyBlock(mutablePos, true, null);
                            }
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 3);
                        } else if (!flag2) {
                            level.setBlock(mutablePos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }
}
