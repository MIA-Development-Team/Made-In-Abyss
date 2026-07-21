package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.density.GeneralAbyssHole;
import com.altnoir.mementoinabyss.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.worldgen.density.NoodleAbyssHole;
import com.altnoir.mementoinabyss.worldgen.density.SparseAquiferGate;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaDensityFunctionTypes {
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>> ABYSS_HOLE =
            TYPES.register("abyss_hole", () -> HopperAbyssHole.CODEC.codec());
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>> GENERAL_ABYSS_HOLE =
            TYPES.register("general_abyss_hole", () -> GeneralAbyssHole.CODEC.codec());
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>> NOODLE_ABYSS_HOLE =
            TYPES.register("noodle_abyss_hole", () -> NoodleAbyssHole.CODEC.codec());
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<? extends DensityFunction>> SPARSE_AQUIFER_GATE =
            TYPES.register("sparse_aquifer_gate", () -> SparseAquiferGate.CODEC.codec());

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

    private MiaDensityFunctionTypes() {
    }
}
