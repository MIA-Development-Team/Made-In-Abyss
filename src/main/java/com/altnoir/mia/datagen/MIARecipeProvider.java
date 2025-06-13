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

public class MIARecipeProvider extends RecipeProvider implements IConditionBuilder {
    public MIARecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        lampTube(recipeOutput, Items.DIAMOND, Blocks.DIAMOND_BLOCK);
        lampTube(recipeOutput, Items.IRON_INGOT, Blocks.IRON_BLOCK, "ingot");
        lampTube(recipeOutput, ItemTags.LOGS, Blocks.COAL_BLOCK, "logs");
    }

    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output, String id) {
        LampTubeRecipeBuilder.lampTube(input, output)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput, getItemName(output) + "_from_" + id);
    }
    private static void lampTube(RecipeOutput recipeOutput, ItemLike input, ItemLike output) {
        LampTubeRecipeBuilder.lampTube(input, output)
                .unlockedBy(getHasName(input), has(output))
                .save(recipeOutput);
    }
    private static void lampTube(RecipeOutput recipeOutput, TagKey<Item> tag, ItemLike output, String name) {
        LampTubeRecipeBuilder.lampTube(tag, output)
                .unlockedBy("has_" + name, has(output))
                .save(recipeOutput);
    }
}
