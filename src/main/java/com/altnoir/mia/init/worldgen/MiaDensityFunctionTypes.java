package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.GeneralAbyssHole;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.HopperAbyssHole;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaDensityFunctionTypes {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<HopperAbyssHole>> ABYSS_HOLE =
            DENSITY_FUNCTION_TYPE.register("abyss_hole", HopperAbyssHole.CODEC::codec);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<GeneralAbyssHole>> GENERAL_ABYSS_HOLE =
            DENSITY_FUNCTION_TYPE.register("general_abyss_hole", GeneralAbyssHole.CODEC::codec);

    public static DensityFunction hopperAbyssHole() {
        return hopperAbyssHole(0.0F);
    }

    public static DensityFunction hopperAbyssHole(float radius) {
        return hopperAbyssHole(radius, 1.0F);
    }

    public static DensityFunction hopperAbyssHole(float radius, float mul) {
        return new HopperAbyssHole(radius, mul);
    }


    public static DensityFunction generalAbyssHole(float radius) {
        return generalAbyssHole(radius, 1.0F);
    }

    public static DensityFunction generalAbyssHole(float radius, float mul) {
        return new GeneralAbyssHole(radius, mul);
    }

    public static void register(IEventBus eventBus) {
        DENSITY_FUNCTION_TYPE.register(eventBus);
    }
}
