package com.altnoir.mementoinabyss.impl.artifact.enhancement;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ArtifactEnhancementRecipeInput(
        ItemStack artifact,
        ItemStack material,
        RandomSource random
) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> artifact;
            case 1 -> material;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
