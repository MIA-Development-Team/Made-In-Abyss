package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.worldgen.feature.configurations.ClusterConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.List;

public class ClusterFeature extends Feature<ClusterConfiguration> {
    public ClusterFeature(Codec<ClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ClusterConfiguration> context) {
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();

        ClusterConfiguration cl;
        for (cl = context.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockpos = blockpos.below()) {
            if (!worldgenlevel.isEmptyBlock(blockpos.below())) {
                BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
                if (isDirt(blockstate) || isStone(blockstate)) {
                    break;
                }
            }
        }

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3) {
            return false;
        } else {
            List<BlockPos> placedPositions = new ArrayList<>();

            for (int l = 0; l < 3; l++) {
                int maxSize = Math.max(1, cl.size().sample(randomsource) / 2);
                int i = randomsource.nextInt(maxSize) + 1;
                int j = randomsource.nextInt(maxSize) + 1;
                int k = randomsource.nextInt(maxSize) + 1;

                for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-i, -j, -k), blockpos.offset(i, j, k))) {
                    double dx = (blockpos1.getX() - blockpos.getX()) / (double) i;
                    double dy = (blockpos1.getY() - blockpos.getY()) / (double) j;
                    double dz = (blockpos1.getZ() - blockpos.getZ()) / (double) k;

                    if (dx * dx + dy * dy + dz * dz <= 1.0) {
                        worldgenlevel.setBlock(blockpos1, cl.state(), 3);
                        placedPositions.add(blockpos1.immutable());
                    }
                }
                for (BlockPos blockpos2 : placedPositions) {
                    if (randomsource.nextFloat() < cl.crystalChance()) {
                        BlockPos crystalPos = blockpos2.above().offset(randomsource.nextInt(3) - 1, 0, randomsource.nextInt(3) - 1);

                        if (worldgenlevel.isEmptyBlock(crystalPos)
                                && worldgenlevel.getBlockState(crystalPos.below()).isFaceSturdy(worldgenlevel, crystalPos.below(), Direction.UP)
                                && worldgenlevel.getBlockState(crystalPos.below()) != cl.crystalState()) {
                            worldgenlevel.setBlock(crystalPos, cl.crystalState(), 3);
                        }
                    }
                }

                blockpos = blockpos.offset(-1 + randomsource.nextInt(2), -randomsource.nextInt(2), -1 + randomsource.nextInt(2));
            }

            return true;
        }
    }
}