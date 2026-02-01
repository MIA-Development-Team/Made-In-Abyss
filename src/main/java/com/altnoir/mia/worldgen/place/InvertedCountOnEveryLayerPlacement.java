package com.altnoir.mia.worldgen.place;

import com.altnoir.mia.init.worldgen.MiaPlacements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class InvertedCountOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<InvertedCountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256)
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
        int i = 0;

        boolean flag;
        do {
            flag = false;

            for (int j = 0; j < this.count.sample(random); j++) {
                int k = random.nextInt(16) + pos.getX();
                int l = random.nextInt(16) + pos.getZ();
                int i1 = context.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
                int j1 = findOnnCeilingYPosition(context, k, i1, l, i);
                if (j1 != Integer.MAX_VALUE) {
                    builder.add(new BlockPos(k, j1, l));
                    flag = true;
                }
            }

            i++;
        } while (flag);

        return builder.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacements.INVERTED_COUNT_ON_EVERY_LAYER.get();
    }

    private static int findOnnCeilingYPosition(PlacementContext context, int x, int y, int z, int count) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);
        int i = 0;
        BlockState blockstate = context.getBlockState(blockpos$mutableblockpos);

        for (int j = context.getMinBuildHeight() + 1; j <= y; j++) {
            blockpos$mutableblockpos.setY(j + 1);
            BlockState blockstate1 = context.getBlockState(blockpos$mutableblockpos);
            if (isEmpty(blockstate) && !isEmpty(blockstate1) && !blockstate1.is(Blocks.BEDROCK)) {
                if (i == count) {
                    return blockpos$mutableblockpos.getY() - 1;
                }

                i++;
            }

            blockstate = blockstate1;
        }

        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}
