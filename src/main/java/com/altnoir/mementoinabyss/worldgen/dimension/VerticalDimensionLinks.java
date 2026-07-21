package com.altnoir.mementoinabyss.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CopyOnWriteArrayList;

/** Central registry for reusable, bidirectional vertical dimension connections. */
public final class VerticalDimensionLinks {
    public static final VerticalDimensionLink ABYSS_ABOVE_GREAT_FAULT = new VerticalDimensionLink(
            MementoInAbyss.asResource("abyss_above_great_fault"),
            MiaDimensions.THE_ABYSS_LEVEL, MiaHeight.THE_ABYSS,
            MiaDimensions.GREAT_FAULT_LEVEL, MiaHeight.GREAT_FAULT,
            8.0);

    private static final CopyOnWriteArrayList<VerticalDimensionLink> LINKS = new CopyOnWriteArrayList<>();

    static {
        register(ABYSS_ABOVE_GREAT_FAULT);
    }

    public static void register(VerticalDimensionLink link) {
        if (below(link.upperDimension()) != null || above(link.lowerDimension()) != null) {
            throw new IllegalArgumentException("A vertical boundary is already registered for " + link.id());
        }
        LINKS.add(link);
    }

    public static void unregister(VerticalDimensionLink link) {
        LINKS.remove(link);
    }

    public static @Nullable VerticalDimensionLink below(ResourceKey<Level> upperDimension) {
        for (VerticalDimensionLink link : LINKS) {
            if (link.upperDimension().equals(upperDimension)) return link;
        }
        return null;
    }

    public static @Nullable VerticalDimensionLink above(ResourceKey<Level> lowerDimension) {
        for (VerticalDimensionLink link : LINKS) {
            if (link.lowerDimension().equals(lowerDimension)) return link;
        }
        return null;
    }

    private VerticalDimensionLinks() {
    }
}
