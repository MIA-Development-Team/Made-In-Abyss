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

public final class WaterOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<WaterOnEveryLayerPlacement> CODEC =
            IntProviders.codec(0, 256)
                    .fieldOf("count")
                    .xmap(WaterOnEveryLayerPlacement::new, placement -> placement.count);
    private final IntProvider count;

    private WaterOnEveryLayerPlacement(IntProvider count) {
        this.count = count;
    }

    public static WaterOnEveryLayerPlacement of(int count) {
        return new WaterOnEveryLayerPlacement(ConstantInt.of(count));
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        Stream.Builder<BlockPos> positions = Stream.builder();
        LayeredPlacementScan scan =
                new LayeredPlacementScan(context, LayeredPlacementScan.Surface.WATER);
        for (int sample = 0; sample < count.sample(random); sample++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int y = scan.find(x, z, 0);
            if (y != Integer.MAX_VALUE) positions.add(new BlockPos(x, y, z));
        }
        return positions.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.WATER_ON_EVERY_LAYER.get();
    }
}
