package com.altnoir.mia.common.item.abs;

import com.altnoir.mia.core.MiaColors;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMiaTooltip {
    default void appendTooltip(ItemStack stack, List<Component> tooltip) {
        var rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var description = String.format("tooltip.mia.description.%s", rl.getPath());
        if (I18n.exists(description)) {
            tooltip.add(1, Component.translatable(description).withColor(MiaColors.GREEN.getColor()));
        }
    }
}
