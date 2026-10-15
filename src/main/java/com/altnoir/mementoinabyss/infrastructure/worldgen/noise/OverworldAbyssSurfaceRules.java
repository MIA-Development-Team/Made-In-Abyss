package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

/** Grass on dry floor faces, including sheltered ledges, at every world height. */
public final class OverworldAbyssSurfaceRules {
    private static final SurfaceRules.RuleSource HABITAT =
            SurfaceRules.ifTrue(
                    SurfaceRules.isBiome(MiaBiomes.OVERWORLD_ABYSS),
                    SurfaceRules.ifTrue(
                            SurfaceRules.waterBlockCheck(0, 0),
                            SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.ON_FLOOR,
                                            SurfaceRules.state(
                                                    Blocks.GRASS_BLOCK.defaultBlockState())),
                                    SurfaceRules.ifTrue(
                                            SurfaceRules.stoneDepthCheck(
                                                    3, false, 0, CaveSurface.FLOOR),
                                            SurfaceRules.state(Blocks.DIRT.defaultBlockState())))));

    public static SurfaceRules.RuleSource withOriginal(SurfaceRules.RuleSource original) {
        // This must precede vanilla's preliminary-surface and sea-level gates. No sky or
        // preliminary height condition: the biome's cave floors are intentionally eligible.
        return SurfaceRules.sequence(HABITAT, original);
    }

    private OverworldAbyssSurfaceRules() {}
}
