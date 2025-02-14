package com.altnoir.mia.datagen;

import com.altnoir.mia.BlocksRegister;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class MIARecipesProvider extends RecipeProvider implements IConditionBuilder {
    public MIARecipesProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BlocksRegister.EXAMPLE_BLOCK.get(), 1)
                .requires(BlocksRegister.EXAMPLE_BLOCK.get())
                .unlockedBy(getHasName(BlocksRegister.EXAMPLE_BLOCK.get()), has(BlocksRegister.EXAMPLE_BLOCK.get()))
                .save(consumer);
    }
}
