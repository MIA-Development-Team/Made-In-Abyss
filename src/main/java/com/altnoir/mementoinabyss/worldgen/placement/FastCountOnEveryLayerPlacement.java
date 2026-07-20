package com.altnoir.mementoinabyss.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * Equivalent to vanilla's count-on-every-layer placement, but resolves the
 * containing chunk once per vertical scan instead of once per block lookup.
 */
public final class FastCountOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<FastCountOnEveryLayerPlacement> CODEC = IntProviders.codec(0, 256)
            .fieldOf("count")
            .xmap(FastCountOnEveryLayerPlacement::new, placement -> placement.count);

    private final IntProvider count;

    private FastCountOnEveryLayerPlacement(IntProvider count) {
        this.count = count;
    }

    public static FastCountOnEveryLayerPlacement of(int count) {
        return new FastCountOnEveryLayerPlacement(ConstantInt.of(count));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        Stream.Builder<BlockPos> positions = Stream.builder();
        int layer = 0;
        boolean foundAny;
        do {
            foundAny = false;
            int attempts = this.count.sample(random);
            for (int attempt = 0; attempt < attempts; attempt++) {
                int x = origin.getX() + random.nextInt(16);
                int z = origin.getZ() + random.nextInt(16);
                int topY = context.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                ChunkAccess chunk = context.getLevel().getChunk(
                        SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
                int y = findOnGroundYPosition(context, chunk, x, topY, z, layer);
                if (y != Integer.MAX_VALUE) {
                    positions.add(new BlockPos(x, y, z));
                    foundAny = true;
                }
            }
            layer++;
        } while (foundAny);

        return positions.build();
    }

    private static int findOnGroundYPosition(PlacementContext context, ChunkAccess chunk,
                                             int x, int topY, int z, int targetLayer) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, topY, z);
        BlockState current = chunk.getBlockState(cursor);
        int layer = 0;
        for (int y = topY; y >= context.getMinY() + 1; y--) {
            cursor.setY(y - 1);
            BlockState below = chunk.getBlockState(cursor);
            if (!isEmpty(below) && isEmpty(current) && !below.is(Blocks.BEDROCK)) {
                if (layer == targetLayer) return y;
                layer++;
            }
            current = below;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.FAST_COUNT_ON_EVERY_LAYER.get();
    }
}
