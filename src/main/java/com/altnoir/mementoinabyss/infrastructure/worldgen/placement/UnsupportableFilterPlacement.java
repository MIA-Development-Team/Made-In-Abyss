package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class UnsupportableFilterPlacement extends PlacementModifier {
    private static final UnsupportableFilterPlacement INSTANCE = new UnsupportableFilterPlacement();
    public static final MapCodec<UnsupportableFilterPlacement> CODEC = MapCodec.unit(INSTANCE);

    private UnsupportableFilterPlacement() {}

    public static UnsupportableFilterPlacement of() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos origin) {
        return context.getLevel().getBlockState(origin.below()).is(MiaTags.Blocks.UNSUPPORTABLE.tag)
                ? Stream.empty()
                : Stream.of(origin);
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.UNSUPPORTABLE_FILTER.get();
    }
}
