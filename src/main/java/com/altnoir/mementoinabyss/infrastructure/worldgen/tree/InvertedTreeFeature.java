package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class InvertedTreeFeature extends Feature<TreeConfiguration> {
    private static final int MAX_PATH_ADJUSTMENTS = 2;

    public InvertedTreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (!level.getBlockState(cursor.set(origin.getX(), origin.getY() + 1, origin.getZ()))
                .is(Blocks.ROOTED_DIRT)) return false;
        TreeConfiguration config = context.config();
        int height = config.trunkPlacer.getTreeHeight(random);
        if (origin.getY() - height - 1 < level.getMinY() || origin.getY() + 1 >= level.getMaxY())
            return false;
        int configuredRadius = config.foliagePlacer.foliageRadius(random, height);
        boolean mega = configuredRadius >= 4;
        int width = mega ? 2 : 1;
        long[] trunkPath = new long[height];
        int offsetX = 0;
        int offsetZ = 0;
        int adjustments = 0;
        Direction bend = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int nextBend = 3 + random.nextInt(4);
        for (int y = 0; y < height; y++) {
            int previousX = offsetX;
            int previousZ = offsetZ;
            if (y == nextBend && y < height - 3) {
                if (random.nextFloat() < 0.35F)
                    bend = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                offsetX = Math.clamp(offsetX + bend.getStepX(), -2, 2);
                offsetZ = Math.clamp(offsetZ + bend.getStepZ(), -2, 2);
                nextBend += 3 + random.nextInt(5);
            }
            int blockY = origin.getY() - y;
            if (!fitsTrunk(
                    level,
                    cursor,
                    origin.getX() + offsetX,
                    blockY,
                    origin.getZ() + offsetZ,
                    width)) {
                if (y == 0 || adjustments++ >= MAX_PATH_ADJUSTMENTS) return false;
                if ((offsetX != previousX || offsetZ != previousZ)
                        && fitsTrunk(
                                level,
                                cursor,
                                origin.getX() + previousX,
                                blockY,
                                origin.getZ() + previousZ,
                                width)) {
                    offsetX = previousX;
                    offsetZ = previousZ;
                } else {
                    Direction alternative =
                            offsetX == previousX && offsetZ == previousZ
                                    ? bend
                                    : bend.getClockWise();
                    int alternativeX = Math.clamp(previousX + alternative.getStepX(), -2, 2);
                    int alternativeZ = Math.clamp(previousZ + alternative.getStepZ(), -2, 2);
                    if (alternativeX == offsetX && alternativeZ == offsetZ
                            || !fitsTrunk(
                                    level,
                                    cursor,
                                    origin.getX() + alternativeX,
                                    blockY,
                                    origin.getZ() + alternativeZ,
                                    width)) return false;
                    offsetX = alternativeX;
                    offsetZ = alternativeZ;
                }
            }
            trunkPath[y] =
                    BlockPos.asLong(origin.getX() + offsetX, blockY, origin.getZ() + offsetZ);
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                for (int z = 0; z < width; z++) {
                    cursor.set(
                            BlockPos.getX(trunkPath[y]) + x,
                            BlockPos.getY(trunkPath[y]),
                            BlockPos.getZ(trunkPath[y]) + z);
                    BlockState trunk = config.trunkProvider.getState(level, random, cursor);
                    level.setBlock(cursor, trunk, 19);
                }
        }
        int branchCount = mega ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
        for (int branch = 0; branch < branchCount; branch++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int startIndex = Math.max(2, height - 5 - random.nextInt(3));
            int length = (mega ? 3 : 2) + random.nextInt(2);
            cursor.set(trunkPath[Math.min(startIndex, height - 2)]);
            for (int step = 1; step <= length; step++) {
                int x = cursor.getX();
                int y = cursor.getY();
                int z = cursor.getZ();
                cursor.move(direction).move(0, step > 1 && random.nextBoolean() ? -1 : 0, 0);
                if (!canReplace(level.getBlockState(cursor))) {
                    cursor.set(x, y, z);
                    break;
                }
                level.setBlock(cursor, config.trunkProvider.getState(level, random, cursor), 19);
            }
            placeSkyfogCrown(
                    level,
                    random,
                    config,
                    cursor.getX(),
                    cursor.getY(),
                    cursor.getZ(),
                    mega ? 3 : 2,
                    cursor);
        }
        long end = trunkPath[height - 1];
        placeSkyfogCrown(
                level,
                random,
                config,
                BlockPos.getX(end),
                BlockPos.getY(end),
                BlockPos.getZ(end),
                mega ? 5 : 4,
                cursor);
        return true;
    }

    public static boolean canReplace(BlockState state) {
        return state.getFluidState().isEmpty()
                && (state.canBeReplaced()
                        || state.is(BlockTags.REPLACEABLE_BY_TREES)
                        || state.getBlock() instanceof CaveVines);
    }

    private static boolean fitsTrunk(
            WorldGenLevel level, BlockPos.MutableBlockPos cursor, int x, int y, int z, int width) {
        for (int dx = 0; dx < width; dx++)
            for (int dz = 0; dz < width; dz++)
                if (!canReplace(level.getBlockState(cursor.set(x + dx, y, z + dz)))) return false;
        return true;
    }

    private static void placeSkyfogCrown(
            WorldGenLevel level,
            RandomSource random,
            TreeConfiguration config,
            int centerX,
            int centerY,
            int centerZ,
            int radius,
            BlockPos.MutableBlockPos cursor) {
        for (int layer = 0; layer < 4; layer++) {
            int dy = layer - 1;
            int layerRadius = Math.max(1, radius - Math.abs(layer - 2));
            int shiftX = layer == 2 ? random.nextInt(3) - 1 : 0;
            int shiftZ = layer == 2 ? random.nextInt(3) - 1 : 0;
            for (int dx = -layerRadius; dx <= layerRadius; dx++)
                for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                    boolean corner = Math.abs(dx) == layerRadius && Math.abs(dz) == layerRadius;
                    boolean edge = Math.abs(dx) + Math.abs(dz) >= layerRadius * 2 - 1;
                    if (corner || edge && random.nextFloat() < 0.45F) continue;
                    cursor.set(centerX + dx + shiftX, centerY + dy, centerZ + dz + shiftZ);
                    placeLeaf(level, random, config, cursor);
                    if (layer == 0 && edge && random.nextFloat() < 0.22F) {
                        placeLeaf(level, random, config, cursor.move(0, -1, 0));
                    }
                }
        }
    }

    private static void placeLeaf(
            WorldGenLevel level, RandomSource random, TreeConfiguration config, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || !canReplace(level.getBlockState(pos))) return;
        BlockState leaves = config.foliageProvider.getState(level, random, pos);
        if (leaves.hasProperty(LeavesBlock.DISTANCE))
            leaves = leaves.setValue(LeavesBlock.DISTANCE, 1);
        level.setBlock(pos, leaves, 19);
    }
}
