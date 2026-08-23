package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallStructurePlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaStructurePlacementTypes {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PLACEMENT, MIA.MOD_ID);

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<AbyssWallStructurePlacement>> ABYSS_WALL =
            STRUCTURE_PLACEMENT_TYPES.register("abyss_wall", () -> () -> AbyssWallStructurePlacement.CODEC);

    private MiaStructurePlacementTypes() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_PLACEMENT_TYPES.register(eventBus);
    }
}
