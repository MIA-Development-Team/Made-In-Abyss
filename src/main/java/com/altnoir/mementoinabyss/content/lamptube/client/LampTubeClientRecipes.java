package com.altnoir.mementoinabyss.content.lamptube.client;

import com.altnoir.mementoinabyss.content.lamptube.LampTubeRecipe;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

public final class LampTubeClientRecipes {
    private static List<RecipeHolder<LampTubeRecipe>> recipes = List.of();

    public static void update(RecipeMap recipeMap) {
        recipes =
                recipeMap.byType(MiaRecipes.LAMP_TUBE_TYPE.get()).stream()
                        .sorted(Comparator.comparing(holder -> holder.id().identifier()))
                        .toList();
    }

    public static List<RecipeHolder<LampTubeRecipe>> all() {
        return recipes;
    }

    public static void clear() {
        recipes = List.of();
    }

    private LampTubeClientRecipes() {}
}
