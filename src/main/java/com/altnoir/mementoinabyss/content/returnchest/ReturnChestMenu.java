package com.altnoir.mementoinabyss.content.returnchest;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

final class ReturnChestMenu extends ChestMenu {
    private final ReturnStorage.Channel channel;
    private final EnderChestBlockEntity chest;
    private final int connection;

    ReturnChestMenu(
            int id,
            Inventory inventory,
            ReturnStorage.Channel channel,
            EnderChestBlockEntity chest,
            int connection) {
        super(MenuType.GENERIC_9x3, id, inventory, channel, 3);
        this.channel = channel;
        this.chest = chest;
        this.connection = connection;
    }

    @Override
    protected Slot addSlot(Slot slot) {
        if (slot.container instanceof ReturnStorage.Channel storage) {
            return super.addSlot(
                    new Slot(storage, slot.getContainerSlot(), slot.x, slot.y) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return !storage.inTransit();
                        }
                    });
        }
        return super.addSlot(slot);
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        // Item-specific stacking behavior (for example bundles) runs before the normal slot checks.
        if (channel.inTransit()
                && slot >= 0
                && slot < ReturnStorage.SIZE
                && input == ContainerInput.PICKUP
                && !getCarried().isEmpty()) return;
        super.clicked(slot, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        // Vanilla quick-move can merge into existing stacks without consulting Slot.mayPlace.
        return slot >= ReturnStorage.SIZE && channel.inTransit()
                ? ItemStack.EMPTY
                : super.quickMoveStack(player, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level() == chest.getLevel()
                && !chest.isRemoved()
                && ReturnDimensions.canUseChest(player.level())
                && chest.getBlockState().getValue(ReturnChestBlock.CONNECTION) == connection
                && chest.stillValid(player);
    }

    boolean isFor(EnderChestBlockEntity target) {
        return chest == target;
    }
}
