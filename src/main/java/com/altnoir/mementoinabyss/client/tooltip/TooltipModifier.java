package com.altnoir.mementoinabyss.client.tooltip;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@FunctionalInterface
public interface TooltipModifier {
    TooltipModifier EMPTY = _ -> {};

    void modify(ItemTooltipEvent event);

    default TooltipModifier andThen(TooltipModifier after) {
        if (after == EMPTY) {
            return this;
        }
        if (this == EMPTY) {
            return after;
        }
        return event -> {
            modify(event);
            after.modify(event);
        };
    }
}
