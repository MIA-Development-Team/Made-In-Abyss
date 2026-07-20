package com.altnoir.mementoinabyss.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public final class WaterOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<WaterOnEveryLayerPlacement> CODEC = IntProviders.codec(0, 256)
            .fieldOf("count").xmap(WaterOnEveryLayerPlacement::new, placement -> placement.count);
    private final IntProvider count;

    private WaterOnEveryLayerPlacement(IntProvider count) { this.count = count; }
    public static WaterOnEveryLayerPlacement of(int count) { return new WaterOnEveryLayerPlacement(ConstantInt.of(count)); }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        Stream.Builder<BlockPos> positions = Stream.builder();
        for (int sample = 0; sample < count.sample(random); sample++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int top = context.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, top, z);
            for (int y = top; y > context.getMinY(); y--) {
                cursor.setY(y);
                if (!context.getBlockState(cursor).isAir()) continue;
                cursor.setY(y - 1);
                if (context.getBlockState(cursor).is(Blocks.WATER)) {
                    positions.add(new BlockPos(x, y, z));
                    break;
                }
            }
        }
        return positions.build();
    }

    @Override
    public PlacementModifierType<?> type() { return MiaPlacementModifiers.WATER_ON_EVERY_LAYER.get(); }
}
