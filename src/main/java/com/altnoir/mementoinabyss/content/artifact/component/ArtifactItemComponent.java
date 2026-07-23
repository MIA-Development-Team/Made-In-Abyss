package com.altnoir.mementoinabyss.content.artifact.component;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public interface ArtifactItemComponent {
    default void applyDefaults(Item.Properties properties) {}

    default void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipFlag flag,
            List<Component> tooltip
    ) {}

    default void addAttributeModifiers(
            ItemStack stack,
            CurioAttributeModifiers.Builder modifiers
    ) {}

    default void curioTick(SlotContext slotContext, ItemStack stack) {}

    default void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {}

    default void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {}

    default boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    default boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
