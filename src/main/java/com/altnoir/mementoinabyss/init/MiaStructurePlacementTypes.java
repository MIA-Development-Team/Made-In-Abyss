package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.Layer2LandmarkPlacement;
import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.wall.AbyssWallStructurePlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaStructurePlacementTypes {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PLACEMENT, MementoInAbyss.ID);

    public static final DeferredHolder<
                    StructurePlacementType<?>, StructurePlacementType<Layer2LandmarkPlacement>>
            LAYER2_LANDMARK =
                    STRUCTURE_PLACEMENT_TYPES.register(
                            "layer2_landmark", () -> () -> Layer2LandmarkPlacement.CODEC);

    public static final DeferredHolder<
                    StructurePlacementType<?>, StructurePlacementType<AbyssWallStructurePlacement>>
            ABYSS_WALL =
                    STRUCTURE_PLACEMENT_TYPES.register(
                            "abyss_wall", () -> () -> AbyssWallStructurePlacement.CODEC);

    private MiaStructurePlacementTypes() {}

    public static void register(IEventBus eventBus) {
        STRUCTURE_PLACEMENT_TYPES.register(eventBus);
    }
}
