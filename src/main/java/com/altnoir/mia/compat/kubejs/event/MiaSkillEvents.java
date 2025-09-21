package com.altnoir.mia.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MiaSkillEvents {
    public static final EventGroup GROUP = EventGroup.of("MiaSkillEvents");
    public static final EventHandler TRIGGERED = GROUP.server("triggered", () ->
            TriggeredEventJS.class).hasResult();

    public static final EventHandler triggered = TRIGGERED;


    public static class TriggeredEventJS implements KubeEvent {
        private final Player player;
        private final ItemStack itemStack;

        public TriggeredEventJS(Player player, ItemStack itemStack) {
            this.player = player;
            this.itemStack = itemStack;
        }

        public Player getPlayer() {
            return player;
        }

        public ItemStack getItem() {
            return itemStack;
        }

        public String getItemId() {
            return itemStack.getItem().toString();
        }
    }
}
