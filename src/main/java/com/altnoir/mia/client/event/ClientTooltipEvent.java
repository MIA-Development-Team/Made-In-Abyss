package com.altnoir.mia.client.event;

import com.altnoir.mia.item.IMiaTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@OnlyIn(Dist.CLIENT)
public class ClientTooltipEvent {
    public static void onTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var item = stack.getItem();

        if (item instanceof IMiaTooltip tooltipProvider) {
            event.getToolTip().add(
                    Component.translatable(
                    "tooltip.mia.hold_shift",
                    Component.literal("Shift").withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.GRAY));
            if (Screen.hasShiftDown()) {
                tooltipProvider.appendTooltip(stack, event.getToolTip());
            }
        }
    }
}
