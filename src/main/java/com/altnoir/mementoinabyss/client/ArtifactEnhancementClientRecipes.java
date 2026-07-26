package com.altnoir.mementoinabyss.client;

import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementRecipe;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.Comparator;
import java.util.List;

public final class ArtifactEnhancementClientRecipes {
    private static List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = List.of();

    public static void update(RecipeMap recipeMap) {
        recipes = recipeMap.byType(MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get()).stream()
                .sorted(Comparator.comparing(holder -> holder.id().identifier()))
                .toList();
    }

    public static List<RecipeHolder<ArtifactEnhancementRecipe>> all() {
        return recipes;
    }

    public static void clear() {
        recipes = List.of();
    }

    private ArtifactEnhancementClientRecipes() {}
}
