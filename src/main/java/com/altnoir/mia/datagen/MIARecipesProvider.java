package com.altnoir.mia.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class MIARecipesProvider extends RecipeProvider implements IConditionBuilder {
    public MIARecipesProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {

    }
}
