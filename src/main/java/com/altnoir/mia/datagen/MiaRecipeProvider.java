package com.altnoir.mia.datagen;

import com.altnoir.mia.datagen.recipebuilder.LampTubeRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MiaRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public MiaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        lampTube(recipeOutput, Items.STONE, Items.DEEPSLATE, 2);
    }


    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output) {
        lampTube(recipeOutput, input, output, 1);
    }
    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, String id) {
        lampTube(recipeOutput, input, output, 1, id);
    }
    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, String hasName) {
        lampTube(recipeOutput, tag, output, 1, hasName);
    }

    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, Integer count) {
        LampTubeRecipeBuilder.lampTube(input, output, count)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput);
    }
    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, Integer count, String id) {
        LampTubeRecipeBuilder.lampTube(input, output, count)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput, getItemName(output) + "_from_" + id);
    }
    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, Integer count, String hasName) {
        LampTubeRecipeBuilder.lampTube(tag, output, count)
                .unlockedBy("has_" + hasName, has(output))
                .save(recipeOutput);
    }

}
