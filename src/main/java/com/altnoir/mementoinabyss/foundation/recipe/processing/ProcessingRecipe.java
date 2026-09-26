package com.altnoir.mementoinabyss.foundation.recipe.processing;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

@Getter
public abstract class ProcessingRecipe<I extends RecipeInput> implements Recipe<I> {
    private final List<SizedIngredient> ingredients;
    private final List<SizedHeatIngredient> heatIngredients;
    private final List<ItemStackTemplate> results;

    protected ProcessingRecipe(ProcessingRecipeParams params) {
        this.ingredients = params.ingredients();
        this.heatIngredients = params.heatIngredients();
        this.results = params.results();
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName()
                            + " failed validation:\n"
                            + String.join("\n", errors));
        }
    }

    protected abstract int getMaxInputCount();

    protected abstract int getMaxOutputCount();

    protected int getMaxHeatInputCount() {
        return 0;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (ingredients.size() > getMaxInputCount()) {
            errors.add(
                    "Recipe has more item inputs ("
                            + ingredients.size()
                            + ") than supported ("
                            + getMaxInputCount()
                            + ").");
        }
        if (results.size() > getMaxOutputCount()) {
            errors.add(
                    "Recipe has more item outputs ("
                            + results.size()
                            + ") than supported ("
                            + getMaxOutputCount()
                            + ").");
        }
        if (heatIngredients.size() > getMaxHeatInputCount()) {
            errors.add(
                    "Recipe has more heat inputs ("
                            + heatIngredients.size()
                            + ") than supported ("
                            + getMaxHeatInputCount()
                            + ").");
        }
        return errors;
    }

    public ProcessingRecipeParams params() {
        return new ProcessingRecipeParams(ingredients, heatIngredients, results);
    }

    public static <R extends ProcessingRecipe<?>> MapCodec<R> codec(
            Function<ProcessingRecipeParams, R> factory) {
        return ProcessingRecipeParams.MAP_CODEC.xmap(factory, ProcessingRecipe::params);
    }

    public static <R extends ProcessingRecipe<?>>
            StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(
                    Function<ProcessingRecipeParams, R> factory) {
        return ProcessingRecipeParams.STREAM_CODEC.map(factory, ProcessingRecipe::params);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
