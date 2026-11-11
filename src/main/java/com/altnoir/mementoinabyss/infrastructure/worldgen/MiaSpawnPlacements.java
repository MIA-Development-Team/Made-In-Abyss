package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.init.MiaTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public final class MiaSpawnPlacements {
    private MiaSpawnPlacements() {}

    public static void register(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityType.NAUTILUS,
                (type, level, reason, pos, random) ->
                        reason == EntitySpawnReason.NATURAL
                                && level.getLevel()
                                        .dimension()
                                        .equals(MiaDimensions.THE_ABYSS_LEVEL)
                                && level.getBiome(pos).is(MiaTags.Biomes.SPAWNS_ABYSS_NAUTILUS.tag)
                                && level.getFluidState(pos).is(FluidTags.WATER)
                                && level.getFluidState(pos.below()).is(FluidTags.WATER)
                                && level.getBlockState(pos.above()).is(Blocks.WATER),
                RegisterSpawnPlacementsEvent.Operation.OR);
    }
}
