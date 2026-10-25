package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssNoodleTaper;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssTongueDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.GeneralAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.GreatFaultProfile;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.NoodleAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCaveGuard;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCliff;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssClimate;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssInterpolated;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssNoise;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSlide;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSloped;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSurface;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssTerrain;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SparseAquiferGate;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaDensityFunctionTypes {
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MementoInAbyss.ID);

    static {
        TYPES.register("great_fault_profile", GreatFaultProfile.CODEC::codec);
        TYPES.register("shared_abyss_wall", SharedAbyssWall.CODEC::codec);
        TYPES.register("abyss_seam", AbyssSeamDensity.CODEC::codec);
        TYPES.register("abyss_tongues", AbyssTongueDensity.CODEC::codec);
        TYPES.register("abyss_noodle_taper", AbyssNoodleTaper.CODEC::codec);
        TYPES.register(
                "abyss_layer2_cave_mask",
                com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssLayer2CaveMask.CODEC
                        ::codec);
        TYPES.register(
                "abyss_river_mask",
                com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssRiverMask.CODEC
                        ::codec);
        TYPES.register("overworld_abyss_climate", OverworldAbyssClimate.CODEC::codec);
        TYPES.register("overworld_abyss_terrain", OverworldAbyssTerrain.CODEC::codec);
        TYPES.register("overworld_abyss_interpolated", OverworldAbyssInterpolated.CODEC::codec);
        TYPES.register("overworld_abyss_surface", OverworldAbyssSurface.CODEC::codec);
        TYPES.register("overworld_abyss_sloped", OverworldAbyssSloped.CODEC::codec);
        TYPES.register("overworld_abyss_slide", OverworldAbyssSlide.CODEC::codec);
        TYPES.register("overworld_abyss_cave_guard", OverworldAbyssCaveGuard.CODEC::codec);
        TYPES.register("overworld_abyss_cliff", OverworldAbyssCliff.CODEC::codec);
    }

    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            ABYSS_HOLE = TYPES.register("abyss_hole", HopperAbyssHole.CODEC::codec);
    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            OVERWORLD_ABYSS_HOLE =
                    TYPES.register("overworld_abyss_hole", OverworldAbyssHole.CODEC::codec);
    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            OVERWORLD_ABYSS_NOISE =
                    TYPES.register("overworld_abyss_noise", OverworldAbyssNoise.CODEC::codec);
    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            GENERAL_ABYSS_HOLE =
                    TYPES.register("general_abyss_hole", GeneralAbyssHole.CODEC::codec);
    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            NOODLE_ABYSS_HOLE = TYPES.register("noodle_abyss_hole", NoodleAbyssHole.CODEC::codec);
    public static final DeferredHolder<
                    MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>>
            SPARSE_AQUIFER_GATE =
                    TYPES.register("sparse_aquifer_gate", SparseAquiferGate.CODEC::codec);

    public static DensityFunction hopper() {
        return hopper(0.0F);
    }

    public static DensityFunction hopper(float radius) {
        return hopper(radius, 1.0F);
    }

    public static DensityFunction hopper(float radius, float multiplier) {
        return hopper(radius, multiplier, 64.0F);
    }

    public static HopperAbyssHole hopper(float radius, float multiplier, float slope) {
        return new HopperAbyssHole(radius, multiplier, slope);
    }

    public static DensityFunction general(float radius) {
        return general(radius, 1.0F);
    }

    public static GeneralAbyssHole general(float radius, float multiplier) {
        return new GeneralAbyssHole(radius, multiplier);
    }

    public static NoodleAbyssHole noodle(float radius, float multiplier) {
        return new NoodleAbyssHole(radius, multiplier);
    }

    public static SparseAquiferGate sparseAquifer(int oneIn) {
        return new SparseAquiferGate(oneIn);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private MiaDensityFunctionTypes() {}
}
