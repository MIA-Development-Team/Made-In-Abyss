package com.altnoir.mia.compat.kubejs;

import com.altnoir.mia.compat.kubejs.event.MiaSkillEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KubeJSHelper {
    public static void skillTriggerEvent(Player player, ItemStack itemStack) {
        try {
            if (MiaSkillEvents.TRIGGERED.hasListeners()) {
                MiaSkillEvents.TRIGGERED.post(new MiaSkillEvents.TriggeredEventJS(player, itemStack));
            }
        } catch (Exception ignored) {
        }
    }
}