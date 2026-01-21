package com.altnoir.mia.common.recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.altnoir.mia.MIA;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

public class ArtifactBundleUpgradeRecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final ItemStack resultStack;
    private final List<String> rows;
    private final Map<Character, Ingredient> key;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private boolean showNotification;

    public ArtifactBundleUpgradeRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        this(category, new ItemStack(result, count));
    }

    public ArtifactBundleUpgradeRecipeBuilder(RecipeCategory category, ItemStack result) {
        this.rows = Lists.newArrayList();
        this.key = Maps.newLinkedHashMap();
        this.criteria = new LinkedHashMap();
        this.showNotification = true;
        this.category = category;
        this.result = result.getItem();
        this.resultStack = result;
    }

    public static ArtifactBundleUpgradeRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return shaped(category, result, 1);
    }

    public static ArtifactBundleUpgradeRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return new ArtifactBundleUpgradeRecipeBuilder(category, result, count);
    }

    public static ArtifactBundleUpgradeRecipeBuilder shaped(RecipeCategory category, ItemStack result) {
        return new ArtifactBundleUpgradeRecipeBuilder(category, result);
    }

    public ArtifactBundleUpgradeRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return this.define(symbol, Ingredient.of(tag));
    }

    public ArtifactBundleUpgradeRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(new ItemLike[] { item }));
    }

    public ArtifactBundleUpgradeRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, ingredient);
            return this;
        }
    }

    public ArtifactBundleUpgradeRecipeBuilder pattern(String pattern) {
        if (!this.rows.isEmpty() && pattern.length() != ((String) this.rows.get(0)).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pattern);
            return this;
        }
    }

    public ArtifactBundleUpgradeRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public ArtifactBundleUpgradeRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    public ArtifactBundleUpgradeRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResult()));
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        ShapedRecipePattern shapedrecipepattern = this.ensureValid(id);

        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, id.getPath());

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(advancementBuilder);

        criteria.forEach(advancementBuilder::addCriterion);

        ArtifactBundleUpgradeRecipe shapedrecipe = new ArtifactBundleUpgradeRecipe(
                (String) Objects.requireNonNullElse(this.group, ""),
                RecipeBuilder.determineBookCategory(this.category), shapedrecipepattern, this.resultStack,
                this.showNotification);

        recipeOutput.accept(id, shapedrecipe,
                advancementBuilder.build(advancementId.withPrefix("recipes/")));
    }

    public static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "bundle_upgrade/" + itemId.getPath());
    }

    private ShapedRecipePattern ensureValid(ResourceLocation loaction) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(loaction));
        } else {
            return ShapedRecipePattern.of(this.key, this.rows);
        }
    }

}
