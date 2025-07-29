package com.altnoir.mia.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ArtifactEnhancementRecipeInput(ItemStack base, ItemStack material)
        implements RecipeInput {
    public ItemStack getItem(int slot) {
        ItemStack rv;
        switch (slot) {
            case 0:
                rv = this.base;
                break;
            case 1:
                rv = this.material;
                break;
            default:
                throw new IllegalArgumentException("Recipe does not contain slot " + slot);
        }
        return rv;
    }

    public int size() {
        return 2;
    }

    public boolean isEmpty() {
        return this.base.isEmpty() && this.material.isEmpty();
    }

    public ItemStack base() {
        return this.base;
    }

    public ItemStack material() {
        return this.material;
    }
}