package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class VerdantTreeFeature extends Feature<VerdantTreeConfiguration> {
    public VerdantTreeFeature(Codec<VerdantTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VerdantTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        VerdantTreeConfiguration config = context.config();
        if (!config.canPlaceOn().test(level, origin.below())) return false;

        RandomSource random = context.random();
        int height = config.height().sample(random);
        if (origin.getY() < level.getMinY() || origin.getY() + height + 1 >= level.getMaxY())
            return false;

        int radius = config.crownRadius().sample(random);
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int leanHeight = random.nextBoolean() ? height / 2 + random.nextInt(3) : -1;
        Long2ObjectLinkedOpenHashMap<BlockState> blocks = new Long2ObjectLinkedOpenHashMap<>();
        BlockPos.MutableBlockPos cursor = origin.mutable();
        BlockState upright = orient(config.stemState(), Direction.Axis.Y);
        int offsetX = 0;
        int offsetZ = 0;
        for (int y = 0; y < height; y++) {
            cursor.setWithOffset(origin, offsetX, y, offsetZ);
            blocks.put(cursor.asLong(), upright);
            if (y == leanHeight) {
                offsetX += lean.getStepX();
                offsetZ += lean.getStepZ();
                cursor.move(lean);
                blocks.put(cursor.asLong(), orient(config.stemState(), lean.getAxis()));
            }
        }

        BlockPos elbow = cursor.immutable();
        BlockState horizontal = orient(config.stemState(), direction.getAxis());
        int branchLength = 2 + random.nextInt(2);
        for (int step = 1; step <= branchLength; step++) {
            cursor.setWithOffset(
                    elbow, direction.getStepX() * step, 0, direction.getStepZ() * step);
            blocks.put(cursor.asLong(), horizontal);
        }
        placeCrown(
                blocks, elbow.relative(direction), direction, radius, random, config.leavesState());

        // Validate the entire leaf and stalk before writing, so cave ceilings cannot leave partial
        // trees.
        for (long position : blocks.keySet()) {
            cursor.set(position);
            if (level.isOutsideBuildHeight(cursor)
                    || !level.ensureCanWrite(cursor)
                    || !canReplace(level.getBlockState(cursor), config.leavesState())) return false;
        }
        for (Long2ObjectMap.Entry<BlockState> entry : blocks.long2ObjectEntrySet()) {
            cursor.set(entry.getLongKey());
            level.setBlock(cursor, entry.getValue(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
        }
        return true;
    }

    private static void placeCrown(
            Long2ObjectLinkedOpenHashMap<BlockState> blocks,
            BlockPos center,
            Direction direction,
            int radius,
            RandomSource random,
            BlockState leaves) {
        double width = radius * (0.7 + random.nextDouble() * 0.08);
        double phase = random.nextDouble() * Math.PI * 2.0;
        int halfWidth = (int) Math.ceil(width) + 1;
        int halfLength = radius + 1;
        int[][] surface = new int[halfLength * 2 + 1][halfWidth * 2 + 1];
        for (int[] row : surface) Arrays.fill(row, Integer.MIN_VALUE);
        for (int along = -radius - 1; along <= radius + 1; along++) {
            for (int across = -halfWidth; across <= halfWidth; across++) {
                double u = (double) along / radius;
                double v = across / width;
                double distance = Math.sqrt(u * u + v * v);
                double angle = Math.atan2(v, u);
                double edge =
                        1.0
                                + 0.06 * Math.sin(3.0 * angle + phase)
                                + 0.035 * Math.sin(5.0 * angle - phase);
                if (distance > edge) continue;

                double lift =
                        0.45 * u
                                + 0.45 * Math.sin(2.0 * v + phase)
                                + 0.15 * Math.sin(2.0 * u - phase) * Math.abs(v);
                int top = Math.clamp(1 + (int) Math.round(lift), 0, 2);
                if (across == 0 && Math.abs(along) <= radius / 2) top = Math.max(1, top);
                surface[along + halfLength][across + halfWidth] = top;
            }
        }
        for (int i = 0; i < surface.length; i++) {
            for (int j = 0; j < surface[i].length; j++) {
                int top = surface[i][j];
                if (top == Integer.MIN_VALUE) continue;
                int bottom = top;
                // Adjacent columns overlap at height changes, keeping the thin blade
                // face-connected.
                for (Direction neighbour : Direction.Plane.HORIZONTAL) {
                    int neighbourI = i + neighbour.getStepX();
                    int neighbourJ = j + neighbour.getStepZ();
                    if (neighbourI >= 0
                            && neighbourI < surface.length
                            && neighbourJ >= 0
                            && neighbourJ < surface[i].length
                            && surface[neighbourI][neighbourJ] != Integer.MIN_VALUE)
                        bottom = Math.min(bottom, surface[neighbourI][neighbourJ]);
                }
                int along = i - halfLength;
                int across = j - halfWidth;
                if (across == 0 && Math.abs(along) <= radius / 2) bottom = 0;
                int x =
                        center.getX()
                                + along * direction.getStepX()
                                - across * direction.getStepZ();
                int z =
                        center.getZ()
                                + along * direction.getStepZ()
                                + across * direction.getStepX();
                for (int y = bottom; y <= top; y++) {
                    blocks.putIfAbsent(BlockPos.asLong(x, center.getY() + y, z), leaves);
                }
            }
        }
    }

    private static BlockState orient(BlockState state, Direction.Axis axis) {
        return state.hasProperty(RotatedPillarBlock.AXIS)
                ? state.setValue(RotatedPillarBlock.AXIS, axis)
                : state;
    }

    private static boolean canReplace(BlockState state, BlockState leaves) {
        return state.getFluidState().isEmpty()
                && (state.canBeReplaced()
                        || state.is(BlockTags.REPLACEABLE_BY_TREES)
                        || state.is(BlockTags.LEAVES)
                        || state.is(leaves.getBlock()));
    }
}
