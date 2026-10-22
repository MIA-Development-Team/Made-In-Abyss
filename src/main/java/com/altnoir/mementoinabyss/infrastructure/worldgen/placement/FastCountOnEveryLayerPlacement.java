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

public final class FastCountOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<FastCountOnEveryLayerPlacement> CODEC =
            IntProviders.codec(0, 256)
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
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        Stream.Builder<BlockPos> positions = Stream.builder();
        LayeredPlacementScan scan =
                new LayeredPlacementScan(context, LayeredPlacementScan.Surface.FLOOR);
        int layer = 0;
        boolean foundAny;
        do {
            foundAny = false;
            int attempts = this.count.sample(random);
            for (int attempt = 0; attempt < attempts; attempt++) {
                int x = origin.getX() + random.nextInt(16);
                int z = origin.getZ() + random.nextInt(16);
                int y = scan.find(x, z, layer);
                if (y != Integer.MAX_VALUE) {
                    positions.add(new BlockPos(x, y, z));
                    foundAny = true;
                }
            }
            layer++;
        } while (foundAny);

        return positions.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.FAST_COUNT_ON_EVERY_LAYER.get();
    }
}
