package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.worldgen.feature.configurations.ClusterConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class BigClusterFeature extends Feature<ClusterConfiguration> {
    public BigClusterFeature(Codec<ClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ClusterConfiguration> context) {
        BlockPos blockpos = context.origin();
        WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();

        ClusterConfiguration cl = context.config();
        for (; blockpos.getY() > worldgenlevel.getMinBuildHeight() + 3; blockpos = blockpos.below()) {
            if (worldgenlevel.isEmptyBlock(blockpos) || isWater(worldgenlevel, blockpos)) {
                break;
            }
        }
        int size = cl.size.sample(randomsource);
        int height = cl.height.sample(randomsource);
        int maxSize = Math.max(1, size / 2);
        int maxHeight = Math.max(1, height / 2);

        if (blockpos.getY() <= worldgenlevel.getMinBuildHeight() + maxHeight + maxSize + 1) {
            return false;
        } else {
            List<BlockPos> placedPositions = new ArrayList<>();
            int i = randomsource.nextInt(size) + 2;
            int j = randomsource.nextInt(size) + 2 + maxHeight;

            for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-i, -j, -i), blockpos.offset(i, j, i))) {
                double dx = (blockpos1.getX() - blockpos.getX()) / (double) i;
                double dy = (blockpos1.getY() - blockpos.getY()) / (double) j;
                double dz = (blockpos1.getZ() - blockpos.getZ()) / (double) i;

                if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= 1.0) {
                    worldgenlevel.setBlock(blockpos1, cl.stateProvider.getState(randomsource, blockpos1), 4);
                    placedPositions.add(blockpos1.immutable());
                }
            }
            for (BlockPos blockpos2 : placedPositions) {
                if (randomsource.nextFloat() >= cl.crystalChance) continue;

                BlockPos crystalPos1 = getCrystalPosition(worldgenlevel, blockpos2.below(), randomsource);
                if (tryPlaceCrystalDown(worldgenlevel, crystalPos1)) {
                    BlockState crystalState = cl.crystalStateProviderDown.getState(randomsource, crystalPos1);

                    if (crystalState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        crystalState = crystalState.setValue(BlockStateProperties.WATERLOGGED, isWater(worldgenlevel, crystalPos1));
                    }
                    worldgenlevel.setBlock(crystalPos1, crystalState, 4);
                    continue;
                }

                BlockPos crystalPos2 = getCrystalPosition(worldgenlevel, blockpos2.above(), randomsource);
                if (tryPlaceCrystalUp(worldgenlevel, crystalPos2)) {
                    BlockState crystalState = cl.crystalStateProviderUp.getState(randomsource, crystalPos2);

                    if (crystalState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        crystalState = crystalState.setValue(BlockStateProperties.WATERLOGGED, isWater(worldgenlevel, crystalPos2));
                    }
                    worldgenlevel.setBlock(crystalPos2, crystalState, 4);
                }
            }
        }

        return true;
    }

    private boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).getType() == Fluids.WATER;
    }

    private BlockPos getCrystalPosition(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        return level.getBlockState(basePos).isAir() ? basePos : basePos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
    }

    private boolean tryPlaceCrystalDown(WorldGenLevel level, BlockPos pos) {
        return (level.isEmptyBlock(pos) || isWater(level, pos)) && level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    private boolean tryPlaceCrystalUp(WorldGenLevel level, BlockPos pos) {
        return (level.isEmptyBlock(pos) || isWater(level, pos)) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}