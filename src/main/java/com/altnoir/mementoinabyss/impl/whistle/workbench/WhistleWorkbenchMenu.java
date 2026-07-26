package com.altnoir.mementoinabyss.impl.whistle.workbench;

import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridCell;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridRotation;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class WhistleWorkbenchMenu extends AbstractContainerMenu {
    private static final int ACTION_MASK = 0xF000;
    private static final int ACTION_PLACE = 0x1000;
    private static final int ACTION_REMOVE = 0x2000;

    private static final int WHISTLE_SLOT = 0;
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    private final ContainerLevelAccess access;
    private final SimpleContainer whistleContainer;
    private final Slot whistleSlot;

    public WhistleWorkbenchMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public WhistleWorkbenchMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access
    ) {
        super(MiaMenus.WHISTLE_WORKBENCH.get(), containerId);
        this.access = access;
        this.whistleContainer = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                WhistleWorkbenchMenu.this.slotsChanged(this);
            }
        };
        this.whistleSlot = addSlot(new Slot(whistleContainer, 0, 20, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return WhistleApi.isWhistle(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addStandardInventorySlots(inventory, 8, 102);
    }

    public ItemStack whistle() {
        return whistleSlot.getItem();
    }

    public ItemStack heldFragment() {
        return WhistleApi.isFragment(getCarried()) ? getCarried() : ItemStack.EMPTY;
    }

    public static int placeButton(int x, int y, GridRotation rotation) {
        return ACTION_PLACE
                | (rotation.ordinal() & 3) << 8
                | (y & 15) << 4
                | (x & 15);
    }

    public static int removeButton(int x, int y) {
        return ACTION_REMOVE | (y & 15) << 4 | (x & 15);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        int action = buttonId & ACTION_MASK;
        int x = buttonId & 15;
        int y = buttonId >> 4 & 15;
        if ((action != ACTION_PLACE && action != ACTION_REMOVE)
                || !validCellCoordinates(x, y)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }

        boolean changed = switch (action) {
            case ACTION_PLACE -> placeHeldFragment(
                    x,
                    y,
                    GridRotation.values()[buttonId >> 8 & 3]
            );
            case ACTION_REMOVE -> removeFragment(x, y);
            default -> false;
        };
        if (changed) {
            whistleSlot.setChanged();
            broadcastChanges();
        }
        return changed;
    }

    private boolean placeHeldFragment(int x, int y, GridRotation rotation) {
        ItemStack held = getCarried();
        if (!WhistleApi.install(whistle(), held, x, y, rotation)) {
            return false;
        }
        held.shrink(1);
        if (held.isEmpty()) {
            setCarried(ItemStack.EMPTY);
        }
        return true;
    }

    private boolean removeFragment(int x, int y) {
        if (!getCarried().isEmpty()) {
            return false;
        }
        return WhistleApi.removeAt(whistle(), new GridCell(x, y))
                .map(stack -> {
                    setCarried(stack);
                    return true;
                })
                .orElse(false);
    }

    private boolean validCellCoordinates(int x, int y) {
        return WhistleApi.grid(whistle())
                .map(grid -> x < grid.width() && y < grid.height())
                .orElse(false);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, MiaBlocks.WHISTLE_WORKBENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copied = stack.copy();
        if (index == WHISTLE_SLOT) {
            if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (WhistleApi.isWhistle(stack)) {
            if (!moveItemStackTo(stack, WHISTLE_SLOT, WHISTLE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= INVENTORY_START && index < INVENTORY_END) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END
                && !moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        }
        slot.setChanged();
        if (stack.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copied;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, whistleContainer));
    }
}
