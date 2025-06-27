package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.noisesetting.densityfunction.AbyssBrinkBigHole;
import com.altnoir.mia.worldgen.noisesetting.densityfunction.AbyssBrinkHole;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaDensityFunctionTypes {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<AbyssBrinkHole>> ABYSS_HOLE =
            DENSITY_FUNCTION_TYPE.register("abyss_hole", AbyssBrinkHole.CODEC::codec);
    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<AbyssBrinkBigHole>> ABYSS_BIG_HOLE =
            DENSITY_FUNCTION_TYPE.register("abyss_big_hole", AbyssBrinkBigHole.CODEC::codec);

    public static DensityFunction abyssHole(long seed) {
        return new AbyssBrinkHole(seed);
    }

    public static DensityFunction abyssBigHole(long seed) {
        return new AbyssBrinkBigHole(seed);
    }

    public static void register(IEventBus eventBus) {
        DENSITY_FUNCTION_TYPE.register(eventBus);
    }
}
