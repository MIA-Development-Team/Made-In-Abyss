package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

final class ReturnDimensions {
    private static final TagKey<DimensionType> LAUNCH =
            TagKey.create(
                    Registries.DIMENSION_TYPE, MementoInAbyss.asResource("balloon_launch_allowed"));
    private static final TagKey<DimensionType> CHEST =
            TagKey.create(
                    Registries.DIMENSION_TYPE, MementoInAbyss.asResource("return_chest_allowed"));

    static boolean canLaunch(Level level) {
        return level.dimensionTypeRegistration().is(LAUNCH)
                && MiaDimensions.isMiaDimension(level.dimension());
    }

    static boolean canUseChest(Level level) {
        return level.dimensionTypeRegistration().is(CHEST)
                && (Level.OVERWORLD.equals(level.dimension())
                        || MiaDimensions.isMiaDimension(level.dimension()));
    }

    private ReturnDimensions() {}
}
