package com.altnoir.mia.compat.jei;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.recipe.LampTubeRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class LampTubeRecipeCategory implements IRecipeCategory<RecipeHolder<LampTubeRecipe>> {
    public IJeiHelpers jeiHelpers;

    public LampTubeRecipeCategory(IJeiHelpers jeiHelpers) {
        this.jeiHelpers = jeiHelpers;
    }

    @Override
    public RecipeType<RecipeHolder<LampTubeRecipe>> getRecipeType() {
        return MiaJeiPlugin.LAMP_TUBE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("emi.category.mia.lamp_tube");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return jeiHelpers.getGuiHelper().createDrawableItemLike(MiaBlocks.AMETHYST_LAMPTUBE);
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 34;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LampTubeRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 9)
                .setStandardSlotBackground()
                .addIngredients(recipe.value().ingredient());

        builder.addOutputSlot(61, 9)
                .setOutputSlotBackground()
                .addItemStack(recipe.value().result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<LampTubeRecipe> recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(26, 9);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(RecipeHolder<LampTubeRecipe> recipe) {
        return recipe.id();
    }
}
