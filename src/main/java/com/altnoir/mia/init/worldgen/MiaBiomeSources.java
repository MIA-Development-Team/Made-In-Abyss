package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.biomesource.AbyssNoiseBiomeSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaBiomeSources {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCE = DeferredRegister.create(Registries.BIOME_SOURCE, MIA.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<AbyssNoiseBiomeSource>> ABYSS_BIOME_SOURCE =
            BIOME_SOURCE.register("the_abyss", () -> AbyssNoiseBiomeSource.CODEC);

    public static void register(IEventBus eventBus) {
        BIOME_SOURCE.register(eventBus);
    }
}
