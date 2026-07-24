package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.structure.MiaJigsawStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaStructureTypes {
    private static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<StructureType<?>, StructureType<MiaJigsawStructure>> JIGSAW =
            TYPES.register("jigsaw", () -> () -> MiaJigsawStructure.CODEC);

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private MiaStructureTypes() {
    }
}
