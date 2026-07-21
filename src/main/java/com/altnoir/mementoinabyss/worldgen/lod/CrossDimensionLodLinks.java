package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/** Central registry. Adding another cross-dimension view only requires registering another link. */
public final class CrossDimensionLodLinks {
    public static final CrossDimensionLodLink GREAT_FAULT_BELOW_ABYSS = new CrossDimensionLodLink(
            MementoInAbyss.asResource("great_fault_below_abyss"),
            MiaDimensions.GREAT_FAULT_LEVEL, MiaDimensions.THE_ABYSS_LEVEL,
            0, 0, MiaHeight.THE_ABYSS.minY() - MiaHeight.GREAT_FAULT.maxY(),
            MiaHeight.THE_ABYSS.minY() - 24);
    private static final List<CrossDimensionLodLink> LINKS = List.of(GREAT_FAULT_BELOW_ABYSS);

    public static Optional<CrossDimensionLodLink> forSource(ResourceKey<Level> dimension) {
        return LINKS.stream().filter(link -> link.source().equals(dimension)).findFirst();
    }

    public static Optional<CrossDimensionLodLink> forTarget(ResourceKey<Level> dimension) {
        return LINKS.stream().filter(link -> link.target().equals(dimension)).findFirst();
    }

    public static int radius(CrossDimensionLodLink link) {
        if (link == GREAT_FAULT_BELOW_ABYSS) {
            return (int) Math.ceil(Math.max(
                    MementoInAbyss.CONFIGS.guiSection.crossDimensionLodMinimumDiameter.get() * .5,
                    HopperAbyssHole.abyssRadius() + MementoInAbyss.CONFIGS.guiSection.crossDimensionLodMargin.get()));
        }
        return MementoInAbyss.CONFIGS.guiSection.crossDimensionLodMinimumDiameter.get() / 2;
    }

    private CrossDimensionLodLinks() {}
}
