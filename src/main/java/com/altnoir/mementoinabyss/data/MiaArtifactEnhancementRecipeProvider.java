package com.altnoir.mementoinabyss.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.enhancement.ArtifactEnhancementRecipe;
import com.altnoir.mementoinabyss.content.artifact.enhancement.DoubleRange;
import com.altnoir.mementoinabyss.init.MiaAttributes;
import com.altnoir.mementoinabyss.init.MiaItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public final class MiaArtifactEnhancementRecipeProvider extends RecipeProvider {
    private MiaArtifactEnhancementRecipeProvider(
            HolderLookup.Provider registries,
            RecipeOutput output
    ) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        save(MiaItems.PRASIOLITE_SHARD.get(), 8, Attributes.BLOCK_BREAK_SPEED,
                DoubleRange.between(0.1, 0.3), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        save(Items.IRON_INGOT, 2, Attributes.KNOCKBACK_RESISTANCE,
                DoubleRange.between(0.2, 0.4), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        save(Items.COPPER_INGOT, 3, Attributes.ARMOR,
                DoubleRange.between(0.5, 1.0), AttributeModifier.Operation.ADD_VALUE);
        save(Items.GOLD_INGOT, 2, Attributes.ATTACK_DAMAGE,
                DoubleRange.between(0.1, 5.0), AttributeModifier.Operation.ADD_VALUE);
        save(Items.LAPIS_LAZULI, 1, Attributes.ATTACK_SPEED,
                DoubleRange.between(0.5, 1.5), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        save(Items.DIAMOND, 1, Attributes.SCALE,
                DoubleRange.between(-0.5, 1.5), AttributeModifier.Operation.ADD_VALUE);
        save(Items.EMERALD, 1, MiaAttributes.CRITICAL_HIT,
                DoubleRange.between(0.5, 0.9), AttributeModifier.Operation.ADD_VALUE);
        save(Items.NETHERITE_INGOT, 1, Attributes.GRAVITY,
                DoubleRange.between(-0.5, 0.5), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private void save(
            ItemLike material,
            int count,
            Holder<Attribute> attribute,
            DoubleRange range,
            AttributeModifier.Operation operation
    ) {
        var materialId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(material.asItem()).getPath();
        var id = ResourceKey.create(
                Registries.RECIPE,
                MementoInAbyss.asResource("artifact_enhancement/" + materialId)
        );
        output.accept(
                id,
                new ArtifactEnhancementRecipe(
                        material.asItem().builtInRegistryHolder(),
                        count,
                        attribute,
                        range,
                        operation
                ),
                null
        );
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(
                PackOutput output,
                CompletableFuture<HolderLookup.Provider> registries
        ) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(
                HolderLookup.Provider registries,
                RecipeOutput output
        ) {
            return new MiaArtifactEnhancementRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Memento in Abyss Artifact Enhancement Recipes";
        }
    }
}
