package com.altnoir.mia.common.recipe;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class LampTubeRecipeBuilder implements RecipeBuilder {
    private final SizedIngredient ingredient;
    private final ItemStack result;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private static final String RECIPE_TYPE = MiaRecipes.LAMP_TUBE_TYPE.getId().getPath();

    public LampTubeRecipeBuilder(SizedIngredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public static LampTubeRecipeBuilder lampTube(ItemLike ingredient, ItemLike result, int count) {
        return lampTube(ingredient, 1, result, count);
    }

    public static LampTubeRecipeBuilder lampTube(TagKey<Item> tag, ItemLike result, int count) {
        return lampTube(tag, 1, result, count);
    }

    public static LampTubeRecipeBuilder lampTube(ItemLike ingredient, int count, ItemLike result, int resultCount) {
        return new LampTubeRecipeBuilder(SizedIngredient.of(ingredient, count), new ItemStack(result, resultCount));
    }

    public static LampTubeRecipeBuilder lampTube(TagKey<Item> tag, int count, ItemLike result, int resultCount) {
        return new LampTubeRecipeBuilder(SizedIngredient.of(tag, count), new ItemStack(result, resultCount));
    }

    public @NotNull LampTubeRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result.getItem();
    }

    public void save(@NotNull RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResult()));
    }

    public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
        ResourceLocation resourceLocation = getDefaultRecipeId(this.getResult());
        ResourceLocation resourceLocation1 = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, RECIPE_TYPE + "/" + id);
        if (resourceLocation1.equals(resourceLocation)) {
            throw new IllegalStateException(
                    "Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        } else {
            this.save(recipeOutput, resourceLocation1);
        }
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        ensureValid(id);
        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, id.getPath());

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        criteria.forEach(advancementBuilder::addCriterion);

        LampTubeRecipe recipe = new LampTubeRecipe(ingredient, result);
        recipeOutput.accept(id, recipe, advancementBuilder.build(advancementId.withPrefix("recipes/")));
    }

    public static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, RECIPE_TYPE + "/" + itemId.getPath());
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}