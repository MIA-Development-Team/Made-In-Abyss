package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.place.InvertedCountOnEveryLayerPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaPlacements {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPE = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<InvertedCountOnEveryLayerPlacement>> INVERTED_COUNT_ON_EVERY_LAYER = PLACEMENT_MODIFIER_TYPE.register(
            "inverted_count_on_every_layer", () -> () -> InvertedCountOnEveryLayerPlacement.CODEC);

    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIER_TYPE.register(eventBus);
    }
}