package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.component.WhistleInventoryComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class MiaComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MIA.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHISTLE_LEVEL = register("integer",
            builder -> builder.persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WhistleInventoryComponent>> WHISTLE_INVENTORY = register("whistle_inventory",
            builder -> builder
                    .persistent(WhistleInventoryComponent.CODEC)
                    .networkSynchronized(WhistleInventoryComponent.STREAM_CODEC));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}
