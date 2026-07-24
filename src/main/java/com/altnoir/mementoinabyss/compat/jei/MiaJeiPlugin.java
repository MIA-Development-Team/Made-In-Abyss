package com.altnoir.mementoinabyss.compat.jei;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.ArtifactEnhancementClientRecipes;
import com.altnoir.mementoinabyss.client.screen.ArtifactEnhancementScreen;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class MiaJeiPlugin implements IModPlugin {
    private static final Identifier ID = MementoInAbyss.asResource("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ArtifactEnhancementRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()
        ));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                ArtifactEnhancementRecipeCategory.TYPE,
                ArtifactEnhancementClientRecipes.all()
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                ArtifactEnhancementRecipeCategory.TYPE,
                MiaBlocks.ARTIFACT_SMITHING_TABLE.get()
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                ArtifactEnhancementScreen.class,
                132,
                32,
                28,
                24,
                ArtifactEnhancementRecipeCategory.TYPE
        );
    }
}
