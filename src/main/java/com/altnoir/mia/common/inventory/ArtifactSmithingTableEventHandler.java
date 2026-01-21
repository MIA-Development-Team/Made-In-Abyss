package com.altnoir.mia.common.inventory;

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
