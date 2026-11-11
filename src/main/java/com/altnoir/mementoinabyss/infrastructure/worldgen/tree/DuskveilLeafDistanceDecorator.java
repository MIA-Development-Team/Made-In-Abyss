package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.content.abyss.plant.DuskveilLeavesBlock;
import com.altnoir.mementoinabyss.init.MiaTreeDecorators;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class DuskveilLeafDistanceDecorator extends TreeDecorator {
    public static final DuskveilLeafDistanceDecorator INSTANCE =
            new DuskveilLeafDistanceDecorator();
    public static final MapCodec<DuskveilLeafDistanceDecorator> CODEC = MapCodec.unit(INSTANCE);
    private static final int GENERATED_DISTANCE_LIMIT = 12;
    private static final Direction[] NEIGHBORS = Direction.values();

    private DuskveilLeafDistanceDecorator() {}

    @Override
    protected TreeDecoratorType<?> type() {
        return MiaTreeDecorators.DUSKVEIL_LEAF_DISTANCE.get();
    }

    @Override
    public void place(Context context) {
        Long2IntOpenHashMap distances = new Long2IntOpenHashMap(context.leaves().size());
        distances.defaultReturnValue(-1);
        for (BlockPos leaf : context.leaves()) {
            if (context.level().getBlockState(leaf).hasProperty(DuskveilLeavesBlock.DISTANCE)) {
                distances.put(leaf.asLong(), 0);
            }
        }

        LongArrayFIFOQueue pending = new LongArrayFIFOQueue();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (BlockPos log : context.logs()) {
            if (context.level().getBlockState(log).is(BlockTags.PREVENTS_NEARBY_LEAF_DECAY)) {
                visitNeighbors(log, 1, distances, pending, pos);
            }
        }
        while (!pending.isEmpty()) {
            long leaf = pending.dequeueLong();
            int nextDistance = distances.get(leaf) + 1;
            if (nextDistance < GENERATED_DISTANCE_LIMIT) {
                visitNeighbors(BlockPos.of(leaf), nextDistance, distances, pending, pos);
            }
        }

        // Register the leaves as decorations too so the vanilla tree updates their edges.
        context.leaves()
                .removeIf(
                        leaf -> {
                            int distance = distances.get(leaf.asLong());
                            if (distance > 0) {
                                context.setBlock(
                                        leaf,
                                        context.level()
                                                .getBlockState(leaf)
                                                .setValue(DuskveilLeavesBlock.DISTANCE, distance));
                            } else if (distance == 0) {
                                context.setBlock(leaf, Blocks.AIR.defaultBlockState());
                            }
                            return distance <= 0;
                        });
    }

    private static void visitNeighbors(
            BlockPos center,
            int distance,
            Long2IntOpenHashMap distances,
            LongArrayFIFOQueue pending,
            BlockPos.MutableBlockPos pos) {
        for (Direction direction : NEIGHBORS) {
            long neighbor = pos.setWithOffset(center, direction).asLong();
            if (distances.get(neighbor) == 0) {
                distances.put(neighbor, distance);
                pending.enqueue(neighbor);
            }
        }
    }
}
