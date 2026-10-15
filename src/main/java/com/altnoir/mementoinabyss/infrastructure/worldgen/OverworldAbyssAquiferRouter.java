package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssClimate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;

/** Removes terrain-only climate adjustments from the router used for aquifer fluid selection. */
public final class OverworldAbyssAquiferRouter {
    /**
     * Aquifer fluid selection uses erosion and depth only for the deep-dark cutoff. Keep those
     * inputs at their vanilla values so this query does not scan the composed terrain surface.
     * The NoiseChunk continues using its normal router for density and biome climate sampling.
     */
    public static NoiseRouter forAquifer(NoiseRouter router) {
        return new NoiseRouter(
                router.barrierNoise(),
                router.fluidLevelFloodednessNoise(),
                router.fluidLevelSpreadNoise(),
                router.lavaNoise(),
                router.temperature(),
                router.vegetation(),
                router.continents(),
                climateInput(router.erosion(), OverworldAbyssClimate.Component.EROSION),
                climateInput(router.depth(), OverworldAbyssClimate.Component.DEPTH),
                router.ridges(),
                router.preliminarySurfaceLevel(),
                router.finalDensity(),
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap());
    }

    private static DensityFunction climateInput(
            DensityFunction function, OverworldAbyssClimate.Component expected) {
        if (function instanceof OverworldAbyssClimate climate && climate.component() == expected) {
            return climate.input();
        }
        return function;
    }

    private OverworldAbyssAquiferRouter() {}
}
