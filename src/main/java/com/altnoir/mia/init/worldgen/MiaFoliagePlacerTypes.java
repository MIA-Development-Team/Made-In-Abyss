package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.feature.foliage.InvertedFoliagePlacer;
import com.altnoir.mia.worldgen.feature.foliage.MegaInvertedFoliagePlacer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaFoliagePlacerTypes {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE = DeferredRegister.create(BuiltInRegistries.FOLIAGE_PLACER_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<InvertedFoliagePlacer>> INVERTED_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPE.register("inverted_foliage_placer", () -> new FoliagePlacerType<>(InvertedFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<MegaInvertedFoliagePlacer>> MEGA_INVERTED_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPE.register("mega_inverted_foliage_placer", () -> new FoliagePlacerType<>(MegaInvertedFoliagePlacer.CODEC));

    public static void register(IEventBus eventBus) {
        FOLIAGE_PLACER_TYPE.register(eventBus);
    }
}
