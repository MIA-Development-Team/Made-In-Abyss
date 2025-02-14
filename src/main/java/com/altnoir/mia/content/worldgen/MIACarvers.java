package com.altnoir.mia.content.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.worldgen.carver.CentralHoleCarver;
import com.altnoir.mia.content.worldgen.carver.CentralHoleCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MIACarvers {
    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, MIA.MOD_ID);

    public static final RegistryObject<WorldCarver<CentralHoleCarverConfiguration>> CENTRAL_HOLE_CARVER = register("abyss_central_hole",
            () -> new CentralHoleCarver(CentralHoleCarverConfiguration.CODEC));

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> RegistryObject<F> register(String pKey, final Supplier<F> pCarver) {
        return CARVERS.register(pKey, pCarver);
    }
    public static void register(IEventBus modEventBus){
        CARVERS.register(modEventBus);
    }
}
