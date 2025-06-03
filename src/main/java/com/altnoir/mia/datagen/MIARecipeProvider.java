package com.altnoir.mia.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class MIARecipeProvider extends RecipeProvider implements IConditionBuilder {
    public MIARecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
//        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, PSItems.POOP_DUMPLINGS.get(), 1)
//                .requires(PSItems.POOP_BALL.get())
//                .requires(ItemTags.LEAVES)
//                .unlockedBy("has_poop_ball", has(PSItems.POOP_BALL.get()))
//                .save(recipeOutput);
    }
}
