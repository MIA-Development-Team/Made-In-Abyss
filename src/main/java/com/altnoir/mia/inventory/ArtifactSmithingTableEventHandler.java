package com.altnoir.mia.inventory;

import com.altnoir.mia.MIA;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ArtifactSmithingTableEventHandler {

    // onInventoryChanged 检测不到新物品进入背包
    // @SubscribeEvent
    // public void onInventoryChanged(PlayerTickEvent.Post event) {
    // Player player = event.getEntity();
    // if (!player.level().isClientSide()) {
    // if (player.containerMenu instanceof ArtifactSmithingTableMenu menu) {
    // if (menu.inventoryTimesChanged != player.getInventory().getTimesChanged()) {
    // menu.fetchRecipesForArtifact();
    // menu.tryMatchRecipe();
    // menu.inventoryTimesChanged = player.getInventory().getTimesChanged();
    // }
    // }
    // }
    // }

}
