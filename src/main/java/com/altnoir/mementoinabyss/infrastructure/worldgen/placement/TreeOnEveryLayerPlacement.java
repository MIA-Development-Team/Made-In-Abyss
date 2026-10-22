package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class TreeOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<TreeOnEveryLayerPlacement> CODEC =
            IntProviders.codec(0, 256)
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
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos pos) {
        Stream.Builder<BlockPos> builder = Stream.builder();
        LayeredPlacementScan scan =
                new LayeredPlacementScan(context, LayeredPlacementScan.Surface.TREE);
        int i = 0;

        boolean flag;
        do {
            flag = false;

            for (int j = 0; j < this.count.sample(random); j++) {
                int k = random.nextInt(16) + pos.getX();
                int l = random.nextInt(16) + pos.getZ();
                int j1 = scan.find(k, l, i);
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
        return MiaPlacementModifiers.TREE_ON_EVERY_LAYER.get();
    }
}
