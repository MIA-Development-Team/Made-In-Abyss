package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.recipe.LampTubeRecipeBuilder;
import com.altnoir.mia.recipe.WhistleSmithingRecipeBuilder;
import com.altnoir.mia.recipe.WhistleUpgradeRecipeBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
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
        WhistleUpgradeRecipeBuilder.shaped(RecipeCategory.TOOLS, MiaItems.BLUE_WHISTLE, 1)
                .define('#', Items.IRON_INGOT)
                .define('$', MiaItems.RED_WHISTLE.get())
                .pattern("###")
                .pattern("#$#")
                .pattern("###")
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput);
        WhistleUpgradeRecipeBuilder.shaped(RecipeCategory.TOOLS, MiaItems.MOON_WHISTLE, 1)
                .define('#', Items.DIAMOND)
                .define('$', MiaItems.BLUE_WHISTLE.get())
                .pattern("###")
                .pattern("#$#")
                .pattern("###")
                .unlockedBy(getHasName(MiaItems.BLUE_WHISTLE.get()), has(MiaItems.BLUE_WHISTLE.get()))
                .save(recipeOutput);
    }

    private static void whistleSmithing(RecipeOutput recipeOutput) {
        WhistleSmithingRecipeBuilder.smithing(
                Ingredient.of(MiaItems.RED_WHISTLE, MiaItems.BLUE_WHISTLE, MiaItems.MOON_WHISTLE,
                        MiaItems.BLACK_WHISTLE, MiaItems.WHITE_WHISTLE),
                new ItemStack(MiaItems.MISTFUZZ_PEACH.get(), 4),
                Attributes.MAX_HEALTH,
                2,
                Operation.ADD_VALUE)
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput, "peach_health");
        WhistleSmithingRecipeBuilder.smithing(
                Ingredient.of(MiaItems.RED_WHISTLE, MiaItems.BLUE_WHISTLE, MiaItems.MOON_WHISTLE,
                        MiaItems.BLACK_WHISTLE, MiaItems.WHITE_WHISTLE),
                new ItemStack(Items.IRON_INGOT, 1),
                Attributes.ARMOR,
                1,
                Operation.ADD_VALUE)
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput, "iron_armor");
        WhistleSmithingRecipeBuilder.smithing(
                Ingredient.of(MiaItems.RED_WHISTLE, MiaItems.BLUE_WHISTLE, MiaItems.MOON_WHISTLE,
                        MiaItems.BLACK_WHISTLE, MiaItems.WHITE_WHISTLE),
                new ItemStack(Items.DIAMOND, 1),
                Attributes.ATTACK_DAMAGE,
                1,
                Operation.ADD_VALUE)
                .unlockedBy(getHasName(MiaItems.RED_WHISTLE.get()), has(MiaItems.RED_WHISTLE.get()))
                .save(recipeOutput, "diamond_attack_damage");
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
