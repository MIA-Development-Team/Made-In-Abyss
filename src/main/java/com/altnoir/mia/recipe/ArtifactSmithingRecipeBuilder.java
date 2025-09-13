package com.altnoir.mia.recipe;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.altnoir.mia.MIA;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.Slots;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ArtifactSmithingRecipeBuilder {

    private final Ingredient whistle;
    private final ItemStack material;
    private final Holder<Attribute> attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap();

    private ArtifactSmithingRecipeBuilder(Ingredient whistle, ItemStack addition, Holder<Attribute> attribute,
                                          double amount, AttributeModifier.Operation operation) {
        this.whistle = whistle;
        this.material = addition;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    public static ArtifactSmithingRecipeBuilder create(Ingredient base, ItemStack addition,
                                                       Holder<Attribute> attribute,
                                                       double amount, AttributeModifier.Operation operation) {
        return new ArtifactSmithingRecipeBuilder(base, addition, attribute, amount, operation);
    }

    public ArtifactSmithingRecipeBuilder unlockedBy(String key, Criterion<?> criterion) {
        this.criteria.put(key, criterion);
        return this;
    }

    public ArtifactSmithingRecipeBuilder unlockedByMaterial() {
        String name = BuiltInRegistries.ITEM.getKey(material.getItem()).getPath();
        return this.unlockedBy("has_" + name, has(material.getItem()));
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike... items) {
        ItemPredicate[] predicates = Arrays.stream(items)
                .map(i -> ItemPredicate.Builder.item().of(i).build())
                .toArray(ItemPredicate[]::new);

        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                new InventoryChangeTrigger.TriggerInstance(
                        Optional.empty(),
                        Slots.ANY,
                        List.of(predicates)));
    }

    // protected static Criterion<InventoryChangeTrigger.TriggerInstance>
    // has(ItemLike itemLike) {
    // return inventoryTrigger(ItemPredicate.Builder.item().of(new ItemLike[] {
    // itemLike }));
    // }

    // protected static Criterion<InventoryChangeTrigger.TriggerInstance>
    // inventoryTrigger(ItemPredicate... predicates) {
    // return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
    // new InventoryChangeTrigger.TriggerInstance(Optional.empty(), Slots.ANY,
    // List.of(predicates)));
    // }

    // protected static Criterion<InventoryChangeTrigger.TriggerInstance>
    // inventoryTrigger(
    // ItemPredicate.Builder... items) {
    // return inventoryTrigger(
    // (ItemPredicate[])
    // Arrays.stream(items).map(ItemPredicate.Builder::build).toArray((x$0) -> {
    // return new ItemPredicate[x$0];
    // }));
    // }

    public void save(RecipeOutput recipeOutput) {
        String name = BuiltInRegistries.ITEM.getKey(material.getItem()).getPath();
        this.save(recipeOutput,
                ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "artifact_enhancement/" + name));
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        this.ensureValid(id);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(advancementBuilder);
        criteria.forEach(advancementBuilder::addCriterion);

        ArtifactSmithingRecipe recipe = new ArtifactSmithingRecipe(this.whistle, this.material, this.attribute,
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
