package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.recipe.LampTubeRecipeBuilder;
import com.altnoir.mia.recipe.WhistleSmithingRecipe;
import com.altnoir.mia.recipe.WhistleSmithingRecipeBuilder;
import com.altnoir.mia.recipe.WhistleUpgradeRecipeBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MiaRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public MiaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.STONE_PICKAXE, 1)
                .define('#', MiaBlocks.SKYFOG_PLANKS.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy(getHasName(MiaBlocks.SKYFOG_PLANKS.get()), has(MiaBlocks.SKYFOG_PLANKS.get()))
                .save(recipeOutput);

        lampTube(recipeOutput, Items.STONE, Items.DEEPSLATE, 2);
        whistleUpgrade(recipeOutput);
        whistleSmithing(recipeOutput);
    }

    private static void whistleUpgrade(RecipeOutput recipeOutput) {
        // 没高级笛子，暂时红笛合红笛
        WhistleUpgradeRecipeBuilder.shaped(RecipeCategory.TOOLS, MiaItems.RED_WHISTLE, 1)
                .define('#', Items.IRON_INGOT)
                .define('$', MiaItems.RED_WHISTLE.get())
                .pattern("###")
                .pattern("#$#")
                .pattern("###")
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput);
    }

    private static void whistleSmithing(RecipeOutput recipeOutput) {
        WhistleSmithingRecipeBuilder.smithing(new ItemStack(MiaItems.RED_WHISTLE.get()),
                Ingredient.of(Items.IRON_INGOT, Items.GOLD_INGOT))
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput, "red_whistle_iron_gold");
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

    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, Integer count,
            String hasName) {
        LampTubeRecipeBuilder.lampTube(tag, output, count)
                .unlockedBy("has_" + hasName, has(output))
                .save(recipeOutput);
    }

}
