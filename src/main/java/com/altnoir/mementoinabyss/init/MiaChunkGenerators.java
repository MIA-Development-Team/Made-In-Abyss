package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.TheAbyssChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaChunkGenerators {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> TYPES =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MementoInAbyss.ID);

    static {
        TYPES.register("the_abyss", () -> TheAbyssChunkGenerator.CODEC);
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private MiaChunkGenerators() {}
}
