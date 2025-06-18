package com.altnoir.mia.compat.jei;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.recipe.LampTubeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class MIAJeiPlugin implements IModPlugin {
    public static final RecipeType<RecipeHolder<LampTubeRecipe>> LAMP_TUBE_RECIPE_TYPE = RecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "lamp_tube_recipe"));

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new LampTubeRecipeCategory(registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MiaBlocks.LAMP_TUBE, MIAJeiPlugin.LAMP_TUBE_RECIPE_TYPE);
    }

    public RecipeManager getRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null) {
            return level.getRecipeManager();
        } else {
            throw new NullPointerException("minecraft level must not be null");
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = this.getRecipeManager();
        registration.addRecipes(MIAJeiPlugin.LAMP_TUBE_RECIPE_TYPE, recipeManager.getAllRecipesFor(MiaRecipes.LAMP_TUBE_TYPE.get()));
    }
}
