package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class FossilTreeOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<FossilTreeOnEveryLayerPlacement> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            IntProviders.codec(0, 256)
                                                    .fieldOf("count")
                                                    .forGetter(p -> p.count),
                                            Codec.BOOL
                                                    .optionalFieldOf("water", false)
                                                    .forGetter(p -> p.water))
                                    .apply(i, FossilTreeOnEveryLayerPlacement::new));

    private final IntProvider count;
    private final boolean water;

    private FossilTreeOnEveryLayerPlacement(IntProvider count, boolean water) {
        this.count = count;
        this.water = water;
    }

    public static FossilTreeOnEveryLayerPlacement of(int count, boolean water) {
        return new FossilTreeOnEveryLayerPlacement(ConstantInt.of(count), water);
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        var surface =
                water ? LayeredPlacementScan.Surface.WATER_TREE : LayeredPlacementScan.Surface.TREE;
        var scan = new LayeredPlacementScan(context, surface, -128, 0);
        var positions = Stream.<BlockPos>builder();
        int attempts = count.sample(random);
        boolean found;
        int layer = 0;
        do {
            found = false;
            for (int i = 0; i < attempts; i++) {
                int x = origin.getX() + random.nextInt(16);
                int z = origin.getZ() + random.nextInt(16);
                int y = scan.find(x, z, layer);
                if (y != Integer.MAX_VALUE) {
                    positions.add(new BlockPos(x, y, z));
                    found = true;
                }
            }
            layer++;
        } while (found);
        return positions.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.FOSSIL_TREE_ON_EVERY_LAYER.get();
    }
}
