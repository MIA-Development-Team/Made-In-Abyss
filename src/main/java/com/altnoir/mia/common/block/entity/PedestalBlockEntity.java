package com.altnoir.mia.common.block.entity;

import com.altnoir.mia.init.MiaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity implements WorldlyContainer {
    private final ItemStackHandler InputInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ItemStackHandler OutputInventory = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public PedestalBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.PEDESTAL_ENTITY.get(), pos, blockState);
    }

    public boolean tryInsertItem(ItemStack stack, boolean simulate) {
        if (InputInventory.getStackInSlot(0).isEmpty() && !stack.isEmpty()) {
            insertInput(stack, simulate);
            return true;
        }
        return false;
    }

    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        return InputInventory.insertItem(0, stack, simulate);
    }

    public ItemStack extractInput(int amount, boolean simulate) {
        return InputInventory.extractItem(0, amount, simulate);
    }

    public boolean insertOutput(ItemStack stack, boolean simulate) {
        for (int i = 0; i < OutputInventory.getSlots(); i++) {
            var leftover = OutputInventory.insertItem(i, stack, simulate);
            if (leftover.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack extractOutput(int amount, boolean simulate) {
        for (int i = 0; i < OutputInventory.getSlots(); i++) {
            var extracted = OutputInventory.extractItem(i, amount, simulate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public int getOutputSlots() {
        return OutputInventory.getSlots();
    }

    public ItemStack getOutputStack(int slot) {
        if (slot < 0 || slot >= OutputInventory.getSlots()) {
            return ItemStack.EMPTY;
        }
        return OutputInventory.getStackInSlot(slot);
    }

    public ItemStack tryExtractItem(int amount, boolean simulate) {
        var inputStack = extractInput(amount, simulate);
        if (!inputStack.isEmpty())
            return inputStack;

        var outputStack = extractOutput(amount, simulate);
        if (!outputStack.isEmpty())
            return outputStack;

        return ItemStack.EMPTY;
    }

    public void drops() {
        var inventory = new SimpleContainer(
                InputInventory.getSlots() + OutputInventory.getSlots()
        );

        for (int i = 0; i < InputInventory.getSlots(); i++) {
            var stack = InputInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inventory.setItem(i, stack.copy());
            }
        }

        for (int i = 0; i < OutputInventory.getSlots(); i++) {
            var stack = OutputInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inventory.setItem(InputInventory.getSlots() + i, stack.copy());
            }
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("input_inventory", InputInventory.serializeNBT(registries));
        tag.put("output_inventory", OutputInventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        InputInventory.deserializeNBT(registries, tag.getCompound("input_inventory"));
        OutputInventory.deserializeNBT(registries, tag.getCompound("output_inventory"));
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
        if (direction != Direction.DOWN) {
            int[] inputSlots = new int[InputInventory.getSlots()];
            for (int i = 0; i < inputSlots.length; i++) inputSlots[i] = i;
            return inputSlots;
        }
        int[] outputSlots = new int[OutputInventory.getSlots()];
        for (int i = 0; i < outputSlots.length; i++) outputSlots[i] = i + InputInventory.getSlots();
        return outputSlots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir != Direction.DOWN && slot < InputInventory.getSlots();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN && slot >= InputInventory.getSlots();
    }

    @Override
    public int getContainerSize() {
        return InputInventory.getSlots() + OutputInventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < InputInventory.getSlots())
            return InputInventory.getStackInSlot(slot);

        return OutputInventory.getStackInSlot(slot - InputInventory.getSlots());
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < InputInventory.getSlots()) InputInventory.setStackInSlot(slot, stack);
        else OutputInventory.setStackInSlot(slot - InputInventory.getSlots(), stack);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < InputInventory.getSlots()) return ItemStack.EMPTY; // Input 不能被抽取
        return OutputInventory.extractItem(slot - InputInventory.getSlots(), amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < InputInventory.getSlots()) return ItemStack.EMPTY;
        return OutputInventory.extractItem(slot - InputInventory.getSlots(), getItem(slot).getCount(), false);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < InputInventory.getSlots(); i++) InputInventory.setStackInSlot(i, ItemStack.EMPTY);
        for (int i = 0; i < OutputInventory.getSlots(); i++) OutputInventory.setStackInSlot(i, ItemStack.EMPTY);
    }
}
