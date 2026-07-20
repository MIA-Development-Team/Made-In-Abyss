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

public class InvertedCountOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<InvertedCountOnEveryLayerPlacement> CODEC = IntProviders.codec(0, 256)
            .fieldOf("count")
            .xmap(InvertedCountOnEveryLayerPlacement::new, p_191611_ -> p_191611_.count);
    private final IntProvider count;

    public InvertedCountOnEveryLayerPlacement(IntProvider count) {
        this.count = count;
    }

    public static InvertedCountOnEveryLayerPlacement of(IntProvider count) {
        return new InvertedCountOnEveryLayerPlacement(count);
    }

    public static InvertedCountOnEveryLayerPlacement of(int count) {
        return of(ConstantInt.of(count));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        Stream.Builder<BlockPos> builder = Stream.builder();
        for (int j = 0; j < this.count.sample(random); j++) {
            int x = random.nextInt(16) + pos.getX();
            int z = random.nextInt(16) + pos.getZ();
            int topY = context.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            ChunkAccess chunk = context.getLevel().getChunk(
                    SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            int y = findLowestRootedDirtCeiling(context, chunk, x, topY, z);
            if (y != Integer.MAX_VALUE) builder.add(new BlockPos(x, y, z));
        }

        return builder.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.INVERTED_COUNT_ON_EVERY_LAYER.get();
    }

    private static int findLowestRootedDirtCeiling(PlacementContext context, ChunkAccess chunk,
                                                   int x, int topY, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, context.getMinY(), z);
        BlockState current = chunk.getBlockState(pos);
        for (int y = context.getMinY(); y < topY; y++) {
            pos.setY(y + 1);
            BlockState above = chunk.getBlockState(pos);
            if (isEmpty(current) && above.is(Blocks.ROOTED_DIRT)) return y;
            current = above;
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}
