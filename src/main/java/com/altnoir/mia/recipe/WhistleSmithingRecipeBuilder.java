package com.altnoir.mia.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.altnoir.mia.MIA;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class WhistleSmithingRecipeBuilder {

    private final Ingredient whistle;
    private final ItemStack material;
    private final Holder<Attribute> attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();

    private WhistleSmithingRecipeBuilder(Ingredient whistle, ItemStack addition, Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation) {
        this.whistle = whistle;
        this.material = addition;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    public static WhistleSmithingRecipeBuilder smithing(Ingredient base, ItemStack addition,
            Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation) {
        return new WhistleSmithingRecipeBuilder(base, addition, attribute, amount, operation);
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

        WhistleSmithingRecipe recipe = new WhistleSmithingRecipe(this.whistle, this.material, this.attribute,
                this.amount, this.operation);
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
