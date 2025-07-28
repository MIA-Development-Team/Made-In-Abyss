package com.altnoir.mia.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMiaTooltip {
    default void appendTooltip(ItemStack stack, List<Component> tooltip) {
        // 放最后面
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String description = String.format("tooltip.mia.description.%s", rl.getPath());
        if (I18n.exists(description)) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable(description).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        }
    }
}
