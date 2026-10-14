package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

/**
 * Implemented by {@code NoiseChunk} through a mixin.
 *
 * <p>The router's {@code preliminary_surface_level} reports the carved height inside the abyss, which
 * is right for the surface-rule band and for structure placement but wrong for the aquifer's
 * sampling cut-off: the aquifer looks up to two chunks away and would otherwise lower the cut-off
 * for columns far outside the affected radius. The aquifer and the iceberg/badlands surface
 * extensions therefore read the untouched estimate through this interface.
 */
public interface AbyssOriginalSurface {
    @Nullable DensityFunction mia$originalPreliminarySurface();

    /** Bridge onto {@code NoiseChunk.wrap}, which is protected and therefore not referenceable. */
    DensityFunction mia$wrap(DensityFunction function);
}
