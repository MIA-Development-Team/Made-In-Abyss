package com.altnoir.mementoinabyss.content.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class PedestalBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT_START = 1;
    public static final int OUTPUT_SLOT_COUNT = 8;
    private static final int CONTAINER_SIZE = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;
    private static final int[] INPUT_ACCESS_SLOTS = {INPUT_SLOT};
    private static final int[] OUTPUT_ACCESS_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final String LEGACY_INPUT_INVENTORY = "input_inventory";
    private static final String LEGACY_OUTPUT_INVENTORY = "output_inventory";

    @Getter
    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public PedestalBlockEntity(
            BlockEntityType<PedestalBlockEntity> type,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    public boolean tryInsertItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()
                || !items.get(INPUT_SLOT).isEmpty()
                || !insertInput(stack, true).isEmpty()) {
            return false;
        }
        if (!simulate) {
            insertInput(stack, false);
        }
        return true;
    }

    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        ItemStack remainder = insertIntoSlot(INPUT_SLOT, stack, simulate);
        if (!simulate && remainder.getCount() != stack.getCount()) {
            markInventoryChanged();
        }
        return remainder;
    }

    public ItemStack extractInput(int amount, boolean simulate) {
        ItemStack extracted = extractFromSlot(INPUT_SLOT, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            markInventoryChanged();
        }
        return extracted;
    }

    public boolean insertOutput(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !insertAcrossOutputs(stack, true).isEmpty()) {
            return false;
        }
        if (!simulate) {
            insertAcrossOutputs(stack, false);
            markInventoryChanged();
        }
        return true;
    }

    public ItemStack extractOutput(int amount, boolean simulate) {
        for (int slot = OUTPUT_SLOT_START; slot < CONTAINER_SIZE; slot++) {
            ItemStack extracted = extractFromSlot(slot, amount, simulate);
            if (!extracted.isEmpty()) {
                if (!simulate) {
                    markInventoryChanged();
                }
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack tryExtractItem(int amount, boolean simulate) {
        ItemStack input = extractInput(amount, simulate);
        return input.isEmpty() ? extractOutput(amount, simulate) : input;
    }

    private ItemStack insertAcrossOutputs(ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int slot = OUTPUT_SLOT_START; slot < CONTAINER_SIZE && !remainder.isEmpty(); slot++) {
            remainder = insertIntoSlot(slot, remainder, simulate);
        }
        return remainder;
    }

    private ItemStack insertIntoSlot(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items.get(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack.copy();
        }

        int currentCount = existing.isEmpty() ? 0 : existing.getCount();
        int capacity = Math.min(getMaxStackSize(stack), stack.getMaxStackSize()) - currentCount;
        if (capacity <= 0) {
            return stack.copy();
        }

        int inserted = Math.min(capacity, stack.getCount());
        if (!simulate) {
            if (existing.isEmpty()) {
                items.set(slot, stack.copyWithCount(inserted));
            } else {
                existing.grow(inserted);
            }
        }
        return inserted == stack.getCount()
                ? ItemStack.EMPTY
                : stack.copyWithCount(stack.getCount() - inserted);
    }

    private ItemStack extractFromSlot(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extractedCount = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copyWithCount(extractedCount);
        if (!simulate) {
            if (extractedCount == existing.getCount()) {
                items.set(slot, ItemStack.EMPTY);
            } else {
                existing.shrink(extractedCount);
            }
        }
        return extracted;
    }

    private void markInventoryChanged() {
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items, true);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        if (isEmpty()) {
            loadLegacyInventories(input);
        }
    }

    private void loadLegacyInventories(ValueInput input) {
        NonNullList<ItemStack> legacyInput = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty(LEGACY_INPUT_INVENTORY), legacyInput);
        items.set(INPUT_SLOT, legacyInput.getFirst());

        NonNullList<ItemStack> legacyOutput = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty(LEGACY_OUTPUT_INVENTORY), legacyOutput);
        for (int slot = 0; slot < legacyOutput.size(); slot++) {
            items.set(OUTPUT_SLOT_START + slot, legacyOutput.get(slot));
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? OUTPUT_ACCESS_SLOTS : INPUT_ACCESS_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return direction != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && slot >= OUTPUT_SLOT_START && slot < CONTAINER_SIZE;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < OUTPUT_SLOT_START || slot >= CONTAINER_SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            markInventoryChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < OUTPUT_SLOT_START || slot >= CONTAINER_SIZE) {
            return ItemStack.EMPTY;
        }
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        setItem(slot, stack, false);
    }

    @Override
    public void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        if (!insideTransaction) {
            markInventoryChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot != INPUT_SLOT || stack.isEmpty()) {
            return false;
        }
        ItemStack existing = items.get(INPUT_SLOT);
        return existing.isEmpty()
                || ItemStack.isSameItemSameComponents(existing, stack)
                && existing.getCount() < getMaxStackSize(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        markInventoryChanged();
    }
}
