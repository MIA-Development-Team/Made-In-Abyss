package com.altnoir.mementoinabyss.foundation.transfer.heat;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.StacksResourceHandler;

/**
 * {@code ResourceHandler<HeatResource>} backed by {@link HeatStack}s. Parallel to {@code
 * FluidStacksResourceHandler}.
 */
public class HeatStacksResourceHandler extends StacksResourceHandler<HeatStack, HeatResource> {
    protected int capacity;

    public HeatStacksResourceHandler(int size, int capacity) {
        super(size, HeatStack.EMPTY, HeatStack.OPTIONAL_CODEC);
        this.capacity = capacity;
    }

    public HeatStacksResourceHandler(NonNullList<HeatStack> stacks, int capacity) {
        super(stacks, HeatStack.EMPTY, HeatStack.OPTIONAL_CODEC);
        this.capacity = capacity;
    }

    @Override
    public HeatResource getResourceFrom(HeatStack stack) {
        return stack.resource();
    }

    @Override
    public int getAmountFrom(HeatStack stack) {
        return stack.amount();
    }

    @Override
    protected HeatStack getStackFrom(HeatResource resource, int amount) {
        return resource.toStack(amount);
    }

    @Override
    protected int getCapacity(int index, HeatResource resource) {
        return capacity;
    }

    @Override
    protected HeatStack copyOf(HeatStack stack) {
        return stack.copy();
    }

    @Override
    public boolean matches(HeatStack stack, HeatResource resource) {
        return resource.matches(stack);
    }
}
