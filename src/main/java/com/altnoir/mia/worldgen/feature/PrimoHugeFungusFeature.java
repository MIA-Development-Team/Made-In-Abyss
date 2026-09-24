package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.worldgen.feature.configurations.PrimoHugeFungusConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class PrimoHugeFungusFeature extends Feature<PrimoHugeFungusConfiguration> {
    public PrimoHugeFungusFeature(Codec<PrimoHugeFungusConfiguration> codec) {
        super(codec);
    }

    private static boolean isReplaceable(
            WorldGenLevel level,
            BlockPos pos,
            PrimoHugeFungusConfiguration config,
            boolean checkNonReplaceablePlants) {
        if (level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            return true;
        }
        return checkNonReplaceablePlants && config.replaceableBlocks().test(level, pos);
    }

    @Override
    public boolean place(FeaturePlaceContext<PrimoHugeFungusConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        PrimoHugeFungusConfiguration config = context.config();
        BlockPos newOrigin = null;
        if (level.getBlockState(origin.below()).is(config.validBaseTag())) {
            newOrigin = origin;
        }

        if (newOrigin == null) {
            return false;
        }

        int totalHeight = Mth.nextInt(random, 4, 13);
        if (random.nextInt(12) == 0) {
            totalHeight *= 2;
        }

        level.setBlock(origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_INVISIBLE);
        this.placeStem(level, config, newOrigin, totalHeight);
        this.placeHat(level, random, config, newOrigin, totalHeight);
        return true;
    }

    private void placeStem(
            WorldGenLevel level,
            PrimoHugeFungusConfiguration config,
            BlockPos surfaceOrigin,
            int totalHeight) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockState stem = config.stemState();

        for (int dy = 0; dy < totalHeight; dy++) {
            blockPos.setWithOffset(surfaceOrigin, 0, dy, 0);
            if (isReplaceable(level, blockPos, config, true)) {
                if (!level.getBlockState(blockPos.below()).isAir()) {
                    level.removeBlock(blockPos, false);
                }
                level.setBlock(blockPos, stem, 3);
            }
        }
    }

    private void placeHat(
            WorldGenLevel level,
            RandomSource random,
            PrimoHugeFungusConfiguration config,
            BlockPos surfaceOrigin,
            int totalHeight) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int hatHeight = Math.min(random.nextInt(1 + totalHeight / 3) + 5, totalHeight);
        int hatStartY = totalHeight - hatHeight;

        for (int dy = hatStartY; dy <= totalHeight; dy++) {
            int radius = dy < totalHeight - random.nextInt(3) ? 2 : 1;
            if (hatHeight > 8 && dy < hatStartY + 4) {
                radius = 3;
            }

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    boolean isEdgeX = dx == -radius || dx == radius;
                    boolean isEdgeZ = dz == -radius || dz == radius;
                    boolean inside = !isEdgeX && !isEdgeZ && dy != totalHeight;
                    boolean corner = isEdgeX && isEdgeZ;
                    boolean isHatBottom = dy < hatStartY + 3;
                    blockPos.setWithOffset(surfaceOrigin, dx, dy, dz);

                    if (isReplaceable(level, blockPos, config, false)) {
                        if (!level.getBlockState(blockPos.below()).isAir()) {
                            level.removeBlock(blockPos, false);
                        }

                        if (isHatBottom) {
                            if (!inside) {
                                this.placeHatDropBlock(level, random, blockPos, config.hatState());
                            }
                        } else if (inside) {
                            this.placeHatBlock(level, random, config, blockPos, 0.1F, 0.2F);
                        } else if (corner) {
                            this.placeHatBlock(level, random, config, blockPos, 0.01F, 0.7F);
                        } else {
                            this.placeHatBlock(level, random, config, blockPos, 5.0E-4F, 0.98F);
                        }
                    }
                }
            }
        }
    }

    private void placeHatBlock(
            LevelAccessor level,
            RandomSource random,
            PrimoHugeFungusConfiguration config,
            BlockPos.MutableBlockPos blockPos,
            float decorBlockProbability,
            float hatBlockProbability) {
        if (config.decorState().isPresent() && random.nextFloat() < decorBlockProbability) {
            this.setBlock(level, blockPos, config.decorState().get());
        } else if (random.nextFloat() < hatBlockProbability) {
            this.setBlock(level, blockPos, config.hatState());
        }
    }

    private void placeHatDropBlock(
            LevelAccessor level, RandomSource random, BlockPos blockPos, BlockState hatState) {
        if (level.getBlockState(blockPos.below()).is(hatState.getBlock())) {
            this.setBlock(level, blockPos, hatState);
        } else if (random.nextFloat() < 0.15) {
            this.setBlock(level, blockPos, hatState);
        }
    }
}
