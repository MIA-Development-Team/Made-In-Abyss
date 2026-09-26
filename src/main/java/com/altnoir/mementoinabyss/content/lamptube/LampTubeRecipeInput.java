package com.altnoir.mementoinabyss.content.lamptube;

import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record LampTubeRecipeInput(ItemStack item, HeatStack heat) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
