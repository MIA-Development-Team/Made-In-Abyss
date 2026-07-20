package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.biome.AbyssNoiseBiomeSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaBiomeSources {
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MementoInAbyss.ID);

    static {
        SOURCES.register("the_abyss", () -> AbyssNoiseBiomeSource.CODEC);
    }

    public static void register(IEventBus bus) {
        SOURCES.register(bus);
    }

    private MiaBiomeSources() {
    }
}
