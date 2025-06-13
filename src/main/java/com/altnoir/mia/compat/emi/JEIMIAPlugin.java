/*
package com.altnoir.mia.compat.emi;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.recipe.MIARecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JEIMIAPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LampTubeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<LampTubeRecipe> lampTubeRecipes = recipeManager.getAllRecipesFor(MIARecipes.LAMP_TUBE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        registration.addRecipes(LampTubeRecipeCategory.LAMP_TUBE_RECIPE_TYPE, lampTubeRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MIABlocks.LAMP_TUBE.asItem()),
                LampTubeRecipeCategory.LAMP_TUBE_RECIPE_TYPE);
    }
}
*/
