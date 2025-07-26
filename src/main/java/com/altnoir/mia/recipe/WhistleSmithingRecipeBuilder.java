package com.altnoir.mia.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.altnoir.mia.MIA;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.ItemLike;

public class WhistleSmithingRecipeBuilder {

    private final ItemStack base;
    private final Ingredient addition;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();

    private WhistleSmithingRecipeBuilder(ItemStack base, Ingredient addition) {
        this.base = base;
        this.addition = addition;
    }

    public static WhistleSmithingRecipeBuilder smithing(ItemStack base, Ingredient addition) {
        return new WhistleSmithingRecipeBuilder(base, addition);
    }

    public WhistleSmithingRecipeBuilder unlockedBy(String key, Criterion<?> criterion) {
        this.criteria.put(key, criterion);
        return this;
    }

    public void save(RecipeOutput recipeOutput, String name) {
        this.save(recipeOutput, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "whistle/smithing_" + name));
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        this.ensureValid(id);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(advancementBuilder);
        criteria.forEach(advancementBuilder::addCriterion);

        WhistleSmithingRecipe recipe = new WhistleSmithingRecipe(this.base, this.addition);
        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, id.getPath());
        recipeOutput.accept(id, recipe,
                advancementBuilder.build(advancementId.withPrefix("recipes/")));
    }

    private void ensureValid(ResourceLocation location) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(location));
        }
    }

}
