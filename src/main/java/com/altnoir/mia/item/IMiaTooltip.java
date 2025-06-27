package com.altnoir.mia.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMiaTooltip {
    void appendTooltip(ItemStack stack, List<Component> tooltip);
}
