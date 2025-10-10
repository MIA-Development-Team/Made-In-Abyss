package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.init.MiaBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class AbyssBrinkPortalFeature extends Feature<NoneFeatureConfiguration> {
    public AbyssBrinkPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position
     *                the feature is being placed at
     */
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        createAbyssBrinkPlatform(context.level(), context.origin(), false);
        return true;
    }

    public static void createAbyssBrinkPlatform(ServerLevelAccessor level, BlockPos pos, boolean dropBlocks) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();

        for (int z1 = -2; z1 <= 2; z1++) {
            for (int x1 = -2; x1 <= 2; x1++) {
                for (int y1 = -1; y1 < 3; y1++) {
                    BlockPos blockpos = blockpos$mutableblockpos.set(pos).move(x1, y1, z1);
                    Block block = y1 == -1 ? MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get() : Blocks.AIR;
                    if (!level.getBlockState(blockpos).is(block)) {
                        if (dropBlocks) {
                            level.destroyBlock(blockpos, true, null);
                        }

                        level.setBlock(blockpos, block.defaultBlockState(), 3);
                    }
                }
            }
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos portalPos = blockpos$mutableblockpos.set(pos).move(x, 3, z);

                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    if (!level.getBlockState(portalPos).is(Blocks.DEEPSLATE_BRICKS)) {
                        level.setBlock(portalPos, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                    }
                } else if (!level.getBlockState(portalPos).is(MiaBlocks.ABYSS_PORTAL.get())) {
                    level.setBlock(portalPos, MiaBlocks.ABYSS_PORTAL.get().defaultBlockState(), 3);
                }
            }
        }
    }
}