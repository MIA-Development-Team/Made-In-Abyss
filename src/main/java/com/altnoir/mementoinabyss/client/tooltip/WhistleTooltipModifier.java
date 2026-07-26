package com.altnoir.mementoinabyss.client.tooltip;

import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import com.altnoir.mementoinabyss.impl.whistle.component.PlacedWhistleFragment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class WhistleTooltipModifier implements TooltipModifier {
    public static final WhistleTooltipModifier INSTANCE = new WhistleTooltipModifier();

    @Override
    public void modify(ItemTooltipEvent event) {
        if (!WhistleApi.isWhistle(event.getItemStack())
                || !Minecraft.getInstance().hasControlDown()) {
            return;
        }
        WhistleApi.grid(event.getItemStack()).ifPresent(grid -> {
            event.getToolTip().add(CommonComponents.EMPTY);
            event.getToolTip().add(Component.translatable(
                    "tooltip.mementoinabyss.whistle.grid_usage",
                    Component.literal(Integer.toString(WhistleApi.usedCells(event.getItemStack())))
                            .withStyle(TooltipPalette.MIA.highlight()),
                    Component.literal(Integer.toString(grid.usableCells()))
                            .withStyle(TooltipPalette.MIA.highlight())
            ).withStyle(TooltipPalette.MIA.primary()));

            if (!WhistleApi.loadout(event.getItemStack()).fragments().isEmpty()) {
                event.getToolTip().add(Component.translatable(
                        "tooltip.mementoinabyss.whistle.installed_fragments"
                ).withStyle(TooltipPalette.MIA.highlight()));
                for (PlacedWhistleFragment fragment : WhistleApi.loadout(event.getItemStack()).fragments()) {
                    event.getToolTip().add(Component.literal(" \u2022 ")
                            .withStyle(TooltipPalette.MIA.muted())
                            .append(fragment.createStack().getHoverName().copy()
                                    .withStyle(TooltipPalette.MIA.primary())));
                }
            }
        });
    }

    private WhistleTooltipModifier() {}
}
