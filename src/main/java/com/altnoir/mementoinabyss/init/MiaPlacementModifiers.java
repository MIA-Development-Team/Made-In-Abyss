package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.placement.InvertedCountOnEveryLayerPlacement;
import com.altnoir.mementoinabyss.worldgen.placement.TreeOnEveryLayerPlacement;
import com.altnoir.mementoinabyss.worldgen.placement.WaterOnEveryLayerPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaPlacementModifiers {
    private static final DeferredRegister<PlacementModifierType<?>> TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<InvertedCountOnEveryLayerPlacement>> INVERTED_COUNT_ON_EVERY_LAYER =
            TYPES.register("inverted_count_on_every_layer", () -> () -> InvertedCountOnEveryLayerPlacement.CODEC);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<TreeOnEveryLayerPlacement>> TREE_ON_EVERY_LAYER =
            TYPES.register("tree_on_every_layer", () -> () -> TreeOnEveryLayerPlacement.CODEC);
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<WaterOnEveryLayerPlacement>> WATER_ON_EVERY_LAYER =
            TYPES.register("water_on_every_layer", () -> () -> WaterOnEveryLayerPlacement.CODEC);

    public static void register(IEventBus bus) { TYPES.register(bus); }
    private MiaPlacementModifiers() {}
}
