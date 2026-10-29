package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class Layer2FlowerSurfacePlacement extends PlacementModifier {
    private static final Layer2FlowerSurfacePlacement INSTANCE = new Layer2FlowerSurfacePlacement();
    public static final MapCodec<Layer2FlowerSurfacePlacement> CODEC = MapCodec.unit(INSTANCE);

    private Layer2FlowerSurfacePlacement() {}

    public static Layer2FlowerSurfacePlacement of() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        int maxY = Math.min(32, context.getLevel().getLevel().getMaxY() - 1);
        var scan =
                new LayeredPlacementScan(context, LayeredPlacementScan.Surface.FLOOR, -136, maxY);
        var positions = Stream.<BlockPos>builder();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                for (int layer = 0; ; layer++) {
                    int y = scan.find(x, z, layer);
                    if (y == Integer.MAX_VALUE) break;
                    var pos = new BlockPos(x, y, z);
                    if (context.getLevel().isEmptyBlock(pos)) positions.add(pos);
                }
            }
        }
        return positions.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.LAYER2_FLOWER_SURFACE.get();
    }
}
