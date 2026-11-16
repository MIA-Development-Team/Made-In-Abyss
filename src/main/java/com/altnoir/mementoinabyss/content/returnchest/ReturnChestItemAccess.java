package com.altnoir.mementoinabyss.content.returnchest;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class ReturnChestItemAccess implements WorldlyContainer {
    private final ReturnChestBlockEntity chest;

    ReturnChestItemAccess(ReturnChestBlockEntity chest) {
        this.chest = chest;
    }

    private Container channel() {
        if (!(chest.getLevel() instanceof ServerLevel level)) return null;
        ReturnChestBlockEntity core = chest.controller(level);
        if (core == null || core.connection() == 0) return null;
        return ReturnStorage.get(level).channel(ReturnChests.color(core.connection()));
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        Container channel = channel();
        if (channel == null) return new int[0];
        int[] slots = new int[channel.getContainerSize()];
        for (int slot = 0; slot < slots.length; slot++) {
            slots[slot] = slot;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public int getContainerSize() {
        Container channel = channel();
        return channel == null ? 0 : channel.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        Container channel = channel();
        return channel == null || channel.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        Container channel = channel();
        return channel == null ? ItemStack.EMPTY : channel.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        Container channel = channel();
        return channel == null ? ItemStack.EMPTY : channel.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Container channel = channel();
        return channel == null ? ItemStack.EMPTY : channel.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Container channel = channel();
        if (channel != null) channel.setItem(slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        Container channel = channel();
        if (channel != null) channel.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return !chest.isRemoved();
    }

    @Override
    public void clearContent() {
        Container channel = channel();
        if (channel != null) channel.clearContent();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }
}
