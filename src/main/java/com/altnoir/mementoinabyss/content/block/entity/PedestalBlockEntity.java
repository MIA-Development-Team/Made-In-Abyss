package com.altnoir.mementoinabyss.content.block.entity;

import com.altnoir.mementoinabyss.init.MiaBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class PedestalBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(MiaBlockEntityTypes.PEDESTAL.get(), pos, state);
    }

    public boolean insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !items.getFirst().isEmpty()) {
            return false;
        }
        if (!simulate) {
            items.set(0, stack.copyWithCount(1));
            changed();
        }
        return true;
    }

    public ItemStack extract(boolean simulate) {
        if (items.getFirst().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = items.getFirst().copy();
        if (!simulate) {
            items.set(0, ItemStack.EMPTY);
            changed();
        }
        return result;
    }

    public ItemStack displayedItem() {
        return items.getFirst();
    }

    public void dropContents() {
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.75,
                    worldPosition.getZ() + 0.5, items.getFirst());
        }
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.child("input_inventory"), items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty("input_inventory"), items);
    }
}
