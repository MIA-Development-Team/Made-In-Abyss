package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.component.ArtifactBundleInventoryComponent;
import com.altnoir.mia.common.component.ArtifactEnhancementComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArtifactEnhancementComponent>> ARTIFACT_ENHANCEMENT = register(
            "artifact_enhancement",
            builder -> builder
                    .persistent(ArtifactEnhancementComponent.CODEC)
                    .networkSynchronized(ArtifactEnhancementComponent.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SKILL_COOLDOWN = register(
            "skill_cooldown",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> KILL_COUNT = register(
            "kill_count",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}