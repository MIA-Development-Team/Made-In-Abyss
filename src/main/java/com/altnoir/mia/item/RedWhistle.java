package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.Whistle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RedWhistle extends Whistle implements IMiaTooltip {
    public RedWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.mia.item.red_whistle"));
    }
}
