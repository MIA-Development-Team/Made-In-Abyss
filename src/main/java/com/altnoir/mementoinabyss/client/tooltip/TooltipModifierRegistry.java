package com.altnoir.mementoinabyss.client.tooltip;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class TooltipModifierRegistry {
    private static final Map<Item, TooltipModifier> RESOLVED = new IdentityHashMap<>();
    private static final List<PendingModifier> PENDING = new ArrayList<>();

    public static void register(Supplier<? extends Item> item, TooltipModifier modifier) {
        PENDING.add(new PendingModifier(item, modifier));
    }

    public static TooltipModifier get(Item item) {
        TooltipModifier resolved = RESOLVED.get(item);
        if (resolved != null) {
            return resolved;
        }
        for (PendingModifier pending : PENDING) {
            if (pending.item().get() == item) {
                RESOLVED.put(item, pending.modifier());
                return pending.modifier();
            }
        }
        return TooltipModifier.EMPTY;
    }

    private record PendingModifier(Supplier<? extends Item> item, TooltipModifier modifier) {}

    private TooltipModifierRegistry() {}
}
