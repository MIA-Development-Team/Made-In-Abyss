package com.altnoir.mia.init.worldgen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.feature.ClusterFeature;
import com.altnoir.mia.worldgen.feature.configurations.ClusterConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaFeature {
    public static final DeferredRegister<Feature<?>> FEATURE = DeferredRegister.create(Registries.FEATURE, MIA.MOD_ID);

    public static final DeferredHolder<Feature<?>, ClusterFeature> CLUSTER = FEATURE.register(
            "cluster", () -> new ClusterFeature(ClusterConfiguration.CODEC)
    );

    public static void register(IEventBus eventBus) {
        FEATURE.register(eventBus);
    }
}
