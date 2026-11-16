package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.init.MiaMenus;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ReturnChestMenu extends AbstractContainerMenu {
    public static final int COLUMNS = 8;
    public static final int ROWS = 6;
    public static final int SLOTS = COLUMNS * ROWS;
    private static final int CONTENT_X = 8;
    private static final int CONTENT_Y = 18;
    private static final int PLAYER_X = 8;
    private static final int PLAYER_Y = 140;
    private static final int DATA_ROW = 0;
    private static final int DATA_MAX_ROW = 1;
    private static final int PLAYER_SLOTS = 36;

    private final ReturnStorage.Channel channel;
    private final @Nullable ReturnChestBlockEntity core;
    private final BlockPos accessPos;
    private final int connection;
    private final Container window;
    private final Runnable contentListener = this::contentChanged;
    private int scrollRow;
    private int maxRow;
    private final ContainerData data =
            new ContainerData() {
                @Override
                public int get(int index) {
                    return index == DATA_ROW ? scrollRow : maxRow;
                }

                @Override
                public void set(int index, int value) {
                    if (index == DATA_ROW) {
                        if (channel == null) moveTo(value);
                    } else {
                        maxRow = Math.max(0, value);
                        if (channel == null) moveTo(scrollRow);
                    }
                }

                @Override
                public int getCount() {
                    return 2;
                }
            };

    public ReturnChestMenu(int id, Inventory inventory) {
        this(id, inventory, null, null, BlockPos.ZERO, 0);
    }

    ReturnChestMenu(
            int id,
            Inventory inventory,
            ReturnStorage.Channel channel,
            @Nullable ReturnChestBlockEntity core,
            BlockPos accessPos,
            int connection) {
        super(MiaMenus.RETURN_CHEST.get(), id);
        this.channel = channel;
        this.core = core;
        this.accessPos = accessPos;
        this.connection = connection;
        this.window = channel == null ? new Buffer() : new Window(channel);
        if (channel != null) {
            channel.addListener(contentListener);
            channel.compact();
            contentChanged();
        }
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int slot = row * COLUMNS + column;
                addSlot(
                        new Slot(window, slot, CONTENT_X + column * 18, CONTENT_Y + row * 18) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }

                            @Override
                            public ItemStack remove(int amount) {
                                return super.remove(Math.min(amount, getMaxStackSize()));
                            }
                        });
            }
        }
        addStandardInventorySlots(inventory, PLAYER_X, PLAYER_Y);
        addDataSlots(data);
    }

    public static int maxRowFor(int occupied) {
        return Math.max(0, (occupied + COLUMNS - 1) / COLUMNS - (ROWS - 1));
    }

    public int scrollRow() {
        return scrollRow;
    }

    public int maxRow() {
        return maxRow;
    }

    private void moveTo(int row) {
        int clamped = Math.max(0, Math.min(row, maxRow));
        scrollRow = clamped;
        if (window instanceof Window view) view.setRow(clamped);
    }

    private void contentChanged() {
        int rows = maxRowFor(channel.occupied());
        if (rows != maxRow) maxRow = rows;
        if (scrollRow > maxRow) moveTo(maxRow);
    }

    @Override
    public void removed(Player player) {
        if (channel != null) channel.removeListener(contentListener);
        super.removed(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id > maxRow) return false;
        moveTo(id);
        return true;
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        boolean content = slot >= 0 && slot < SLOTS;
        if (content && input == ContainerInput.SWAP) return;
        if (content && !getCarried().isEmpty()) {
            if (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_CRAFT) return;
        }
        super.clicked(slot, button, input, player);
        if (channel != null) {
            channel.compact();
            contentChanged();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < SLOTS) {
            if (!moveItemStackTo(stack, SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            int hotbarStart = SLOTS + 27;
            boolean fromHotbar = index >= hotbarStart;
            boolean moved =
                    fromHotbar
                            ? moveItemStackTo(stack, SLOTS, hotbarStart, false)
                            : moveItemStackTo(stack, hotbarStart, slots.size(), false);
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY, original);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level() instanceof ServerLevel level
                && core != null
                && core.getLevel() == level
                && ReturnDimensions.canUseChest(level)
                && core.connection() == connection
                && player.isWithinBlockInteractionRange(accessPos, 4.0F)
                && core.controller(level) == core;
    }

    boolean isFor(BlockPos center, UUID structure) {
        return core != null
                && core.getBlockPos().equals(center)
                && core.matchesStructure(structure);
    }

    private static final class Buffer implements Container {
        private final ItemStack[] items = new ItemStack[SLOTS];

        private Buffer() {
            Arrays.fill(items, ItemStack.EMPTY);
        }

        @Override
        public int getContainerSize() {
            return SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < SLOTS ? items[slot] : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= SLOTS || amount <= 0) return ItemStack.EMPTY;
            ItemStack stack = items[slot];
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (stack.getCount() <= amount) {
                items[slot] = ItemStack.EMPTY;
                return stack;
            }
            return stack.split(amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = getItem(slot);
            if (slot >= 0 && slot < SLOTS) items[slot] = ItemStack.EMPTY;
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < SLOTS) items[slot] = stack;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            Arrays.fill(items, ItemStack.EMPTY);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }
    }

    private static final class Window implements Container {
        private final ReturnStorage.Channel channel;
        private int row;

        private Window(ReturnStorage.Channel channel) {
            this.channel = channel;
        }

        private void setRow(int row) {
            this.row = row;
        }

        private int index(int slot) {
            return row * COLUMNS + slot;
        }

        @Override
        public int getContainerSize() {
            return SLOTS;
        }

        @Override
        public boolean isEmpty() {
            return channel.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return channel.getItem(index(slot));
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return channel.removeItem(index(slot), amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return channel.removeItemNoUpdate(index(slot));
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            channel.setItem(index(slot), stack);
        }

        @Override
        public int getMaxStackSize() {
            return channel.getMaxStackSize();
        }

        @Override
        public void setChanged() {
            channel.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            channel.clearContent();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }
    }
}
