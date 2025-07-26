package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.component.ArtifactBundleInventoryComponent;
import com.altnoir.mia.component.WhistleStatsComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class MiaComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArtifactBundleInventoryComponent>> ARTIFACT_BUNDLE_INVENTORY = register(
            "artifact_bundle_inventory",
            builder -> builder
                    .persistent(ArtifactBundleInventoryComponent.CODEC)
                    .networkSynchronized(ArtifactBundleInventoryComponent.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WhistleStatsComponent>> WHISTLE_STATS = register(
            "whistle_stats",
            builder -> builder
                    .persistent(WhistleStatsComponent.CODEC)
                    .networkSynchronized(WhistleStatsComponent.STREAM_CODEC));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}
