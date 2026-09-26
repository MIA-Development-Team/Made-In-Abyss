package com.altnoir.mementoinabyss.content.lamptube;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.recipe.processing.ProcessingRecipeParams;
import com.altnoir.mementoinabyss.foundation.recipe.processing.SizedHeatIngredient;
import com.altnoir.mementoinabyss.init.MiaHeatTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class LampTubeRecipeBuilder {
    private final SizedIngredient ingredient;
    private final ItemStackTemplate result;
    private int heatAmount;

    private LampTubeRecipeBuilder(SizedIngredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static LampTubeRecipeBuilder lampTube(ItemLike ingredient, int count, ItemLike result) {
        return lampTube(ingredient, count, result, 1);
    }

    public static LampTubeRecipeBuilder lampTube(
            ItemLike ingredient, int count, ItemLike result, int resultCount) {
        return new LampTubeRecipeBuilder(
                SizedIngredient.of(ingredient, count),
                new ItemStackTemplate(result.asItem(), resultCount));
    }

    public static LampTubeRecipeBuilder lampTube(
            Ingredient ingredient, int count, ItemLike result) {
        return lampTube(ingredient, count, result, 1);
    }

    public static LampTubeRecipeBuilder lampTube(
            Ingredient ingredient, int count, ItemLike result, int resultCount) {
        return new LampTubeRecipeBuilder(
                new SizedIngredient(ingredient, count),
                new ItemStackTemplate(result.asItem(), resultCount));
    }

    public LampTubeRecipeBuilder heat(int amount) {
        this.heatAmount = amount;
        return this;
    }

    public LampTubeRecipe build() {
        if (heatAmount <= 0) {
            return new LampTubeRecipe(ProcessingRecipeParams.of(ingredient, result));
        }
        return new LampTubeRecipe(
                ProcessingRecipeParams.of(
                        ingredient,
                        SizedHeatIngredient.of(MiaHeatTypes.HEAT.get(), heatAmount),
                        result));
    }

    public void save(RecipeOutput output, String path) {
        ResourceKey<Recipe<?>> id =
                ResourceKey.create(
                        Registries.RECIPE, MementoInAbyss.asResource("lamp_tube/" + path));
        output.accept(id, build(), null);
    }
}
