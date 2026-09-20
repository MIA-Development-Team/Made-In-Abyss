package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** Central registry. Adding another cross-dimension view only requires registering another link. */
public final class CrossDimensionLodLinks {
    public static final CrossDimensionLodLink GREAT_FAULT_BELOW_ABYSS =
            new CrossDimensionLodLink(
                    MementoInAbyss.asResource("great_fault_below_abyss"),
                    MiaDimensions.GREAT_FAULT_LEVEL,
                    MiaDimensions.THE_ABYSS_LEVEL,
                    MiaHeight.GREAT_FAULT,
                    MiaHeight.THE_ABYSS.minY() - MiaHeight.GREAT_FAULT.maxY());
    public static final CrossDimensionLodLink ABYSS_ABOVE_GREAT_FAULT =
            new CrossDimensionLodLink(
                    MementoInAbyss.asResource("abyss_above_great_fault"),
                    MiaDimensions.THE_ABYSS_LEVEL,
                    MiaDimensions.GREAT_FAULT_LEVEL,
                    MiaHeight.THE_ABYSS,
                    MiaHeight.GREAT_FAULT.maxY() - MiaHeight.THE_ABYSS.minY());
    private static final List<CrossDimensionLodLink> LINKS =
            List.of(GREAT_FAULT_BELOW_ABYSS, ABYSS_ABOVE_GREAT_FAULT);

    public static List<CrossDimensionLodLink> all() {
        return LINKS;
    }

    public static List<CrossDimensionLodLink> fromSource(ResourceKey<Level> dimension) {
        return LINKS.stream().filter(link -> link.source().equals(dimension)).toList();
    }

    public static Optional<CrossDimensionLodLink> forTarget(ResourceKey<Level> dimension) {
        return LINKS.stream().filter(link -> link.target().equals(dimension)).findFirst();
    }

    public static int radius(CrossDimensionLodLink link) {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
    }

    /** Configured radius of the center-first lazy-generation area. */
    public static int centralGenerationRadius() {
        return Mth.ceil(HopperAbyssHole.abyssRadius());
    }

    private CrossDimensionLodLinks() {}
}
