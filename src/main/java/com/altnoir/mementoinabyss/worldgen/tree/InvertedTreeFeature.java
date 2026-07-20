package com.altnoir.mementoinabyss.worldgen.tree;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class InvertedTreeFeature extends Feature<TreeConfiguration> {
    public InvertedTreeFeature(Codec<TreeConfiguration> codec) { super(codec); }

    @Override public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!level.getBlockState(origin.above()).is(Blocks.ROOTED_DIRT)) return false;
        TreeConfiguration config = context.config();
        int height = config.trunkPlacer.getTreeHeight(random);
        int configuredRadius = config.foliagePlacer.foliageRadius(random, height);
        boolean mega = configuredRadius >= 4;
        int width = mega ? 2 : 1;
        BlockPos[] trunkPath = new BlockPos[height];
        int offsetX = 0;
        int offsetZ = 0;
        Direction bend = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nextBend = 3 + random.nextInt(4);
        for (int y = 0; y < height; y++) {
            if (y == nextBend && y < height - 3) {
                if (random.nextFloat() < 0.35F) bend = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                offsetX = Math.clamp(offsetX + bend.getStepX(), -2, 2);
                offsetZ = Math.clamp(offsetZ + bend.getStepZ(), -2, 2);
                nextBend += 3 + random.nextInt(5);
            }
            trunkPath[y] = origin.offset(offsetX, -y, offsetZ);
            for (int x = 0; x < width; x++) for (int z = 0; z < width; z++) {
                BlockPos pos = trunkPath[y].offset(x, 0, z);
                if (!level.getBlockState(pos).canBeReplaced()) return false;
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) for (int z = 0; z < width; z++) {
                BlockPos pos = trunkPath[y].offset(x, 0, z);
                BlockState trunk = config.trunkProvider.getState(level, random, pos);
                level.setBlock(pos, trunk, 19);
            }
        }
        int branchCount = mega ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
        for (int branch = 0; branch < branchCount; branch++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int startIndex = Math.max(2, height - 5 - random.nextInt(3));
            int length = (mega ? 3 : 2) + random.nextInt(2);
            BlockPos branchPos = trunkPath[Math.min(startIndex, height - 2)];
            for (int step = 1; step <= length; step++) {
                branchPos = branchPos.relative(direction).below(step > 1 && random.nextBoolean() ? 1 : 0);
                if (!level.getBlockState(branchPos).canBeReplaced()) break;
                level.setBlock(branchPos, config.trunkProvider.getState(level, random, branchPos), 19);
            }
            placeSkyfogCrown(level, random, config, branchPos, mega ? 3 : 2);
        }
        placeSkyfogCrown(level, random, config, trunkPath[height - 1], mega ? 5 : 4);
        return true;
    }

    private static void placeSkyfogCrown(WorldGenLevel level, RandomSource random, TreeConfiguration config, BlockPos center, int radius) {
        int[] layerRadii = {Math.max(1, radius - 2), radius - 1, radius, radius - 1};
        for (int layer = 0; layer < layerRadii.length; layer++) {
            int dy = layer - 1;
            int layerRadius = layerRadii[layer];
            int shiftX = layer == 2 ? random.nextInt(3) - 1 : 0;
            int shiftZ = layer == 2 ? random.nextInt(3) - 1 : 0;
            for (int dx = -layerRadius; dx <= layerRadius; dx++) for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                boolean corner = Math.abs(dx) == layerRadius && Math.abs(dz) == layerRadius;
                boolean edge = Math.abs(dx) + Math.abs(dz) >= layerRadius * 2 - 1;
                if (corner || edge && random.nextFloat() < 0.45F) continue;
                placeLeaf(level, random, config, center.offset(dx + shiftX, dy, dz + shiftZ));
                if (layer == 0 && edge && random.nextFloat() < 0.22F) {
                    placeLeaf(level, random, config, center.offset(dx + shiftX, dy - 1, dz + shiftZ));
                }
            }
        }
    }

    private static void placeLeaf(WorldGenLevel level, RandomSource random, TreeConfiguration config, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced()) return;
        BlockState leaves = config.foliageProvider.getState(level, random, pos);
        if (leaves.hasProperty(LeavesBlock.DISTANCE)) leaves = leaves.setValue(LeavesBlock.DISTANCE, 1);
        level.setBlock(pos, leaves, 19);
    }
}
