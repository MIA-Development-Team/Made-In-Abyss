package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.AbyssNoiseBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaBiomeSources {
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MementoInAbyss.ID);

    static {
        SOURCES.register(
                "abyss_layer2",
                () ->
                        com.altnoir.mementoinabyss.infrastructure.worldgen.biome
                                .AbyssLayer2BiomeSource.CODEC);
        SOURCES.register("the_abyss", () -> AbyssNoiseBiomeSource.CODEC);
        SOURCES.register("overworld_abyss", () -> OverworldAbyssBiomeSource.CODEC);
    }

    public static void register(IEventBus bus) {
        SOURCES.register(bus);
    }

    private MiaBiomeSources() {}
}
