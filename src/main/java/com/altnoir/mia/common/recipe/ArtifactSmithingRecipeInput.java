package com.altnoir.mia.common.recipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ArtifactSmithingRecipeInput(ItemStack base, ItemStack material,
                                          RandomSource random) implements RecipeInput {
    public ArtifactSmithingRecipeInput(ItemStack base, ItemStack material) {
        this(base, material, null);
    }

    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> this.base;
            case 1 -> this.material;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}