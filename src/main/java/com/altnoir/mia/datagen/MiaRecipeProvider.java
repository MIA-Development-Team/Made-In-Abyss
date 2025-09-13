package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaAttributes;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.recipe.ArtifactBundleUpgradeRecipeBuilder;
import com.altnoir.mia.recipe.ArtifactSmithingRecipeBuilder;
import com.altnoir.mia.recipe.LampTubeRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        ArtifactBundleUpgrade(recipeOutput);
        ArtifactEnhancement(recipeOutput);
    }

    private static void ArtifactBundleUpgrade(RecipeOutput recipeOutput) {
        ArtifactBundleUpgradeRecipeBuilder.shaped(RecipeCategory.TOOLS, MiaItems.FANCY_ARTIFACT_BUNDLE, 1)
                .define('#', Items.IRON_INGOT)
                .define('$', MiaItems.GRAY_ARTIFACT_BUNDLE.get())
                .pattern("###")
                .pattern("#$#")
                .pattern("###")
                .unlockedBy(getHasName(MiaItems.GRAY_ARTIFACT_BUNDLE.get()), has(MiaItems.GRAY_ARTIFACT_BUNDLE.get()))
                .save(recipeOutput);
    }

    private static void ArtifactEnhancement(RecipeOutput recipeOutput) {
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(MiaItems.MISTFUZZ_PEACH.get(), 4),
                        Attributes.MAX_HEALTH, 2, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.IRON_INGOT, 1),
                        Attributes.ARMOR, 0.3, Operation.ADD_MULTIPLIED_BASE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.EMERALD, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_MULTIPLIED_TOTAL)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.STONE, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.FIRE_CHARGE, 2),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.WATER_BUCKET, 3),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.BAMBOO, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.COBBLED_DEEPSLATE, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.OAK_LEAVES, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.OAK_PLANKS, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.OAK_SAPLING, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(MiaItems.PRASIOLITE_SHARD.get(), 8),
                        Attributes.BLOCK_BREAK_SPEED, 0.2, Operation.ADD_MULTIPLIED_TOTAL)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.STICK, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_MULTIPLIED_BASE)
                .unlockedByMaterial().save(recipeOutput);
        ArtifactSmithingRecipeBuilder.create(Ingredient.of(MiaTags.Items.ENHANCEABLE_ARTIFACT),
                        new ItemStack(Items.DIAMOND, 1),
                        Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_MULTIPLIED_BASE)
                .unlockedByMaterial().save(recipeOutput);
    }

    @SuppressWarnings("unused")
    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output) {
        lampTube(recipeOutput, input, output, 1);
    }

    @SuppressWarnings("unused")
    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, String id) {
        lampTube(recipeOutput, input, output, 1, id);
    }

    @SuppressWarnings("unused")
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
