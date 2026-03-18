package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.structure.MiaJigsawStructure;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPE = DeferredRegister.create(BuiltInRegistries.STRUCTURE_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<MiaJigsawStructure>> MIA_JIGSAW =
            STRUCTURE_TYPE.register("jigsaw", () -> () -> MiaJigsawStructure.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPE.register(eventBus);
    }
}
