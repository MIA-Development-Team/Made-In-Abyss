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

public class TreeOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<TreeOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256)
            .fieldOf("count")
            .xmap(TreeOnEveryLayerPlacement::new, s -> s.count);
    private final IntProvider count;

    public TreeOnEveryLayerPlacement(IntProvider count) {
        this.count = count;
    }

    public static TreeOnEveryLayerPlacement of(IntProvider count) {
        return new TreeOnEveryLayerPlacement(count);
    }

    public static TreeOnEveryLayerPlacement of(int count) {
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
                int j1 = findOnGroundYPosition(context, k, i1, l, i);
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
        return MiaPlacements.TREE_ON_EVERY_LAYER.get();
    }

    private static int findOnGroundYPosition(PlacementContext context, int x, int y, int z, int count) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);
        int i = 0;
        BlockState blockstate = context.getBlockState(blockpos$mutableblockpos);

        for (int j = y; j >= context.getMinBuildHeight() + 1; j--) {
            blockpos$mutableblockpos.setY(j - 1);
            BlockState blockstate1 = context.getBlockState(blockpos$mutableblockpos);
            if (!isEmpty(blockstate1) && isEmpty(blockstate) && !blockstate1.is(Blocks.BEDROCK)) {
                if (i == count) {
                    return blockpos$mutableblockpos.getY() + 1;
                }

                i++;
            }

            blockstate = blockstate1;
        }

        return Integer.MAX_VALUE;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir();
    }
}
