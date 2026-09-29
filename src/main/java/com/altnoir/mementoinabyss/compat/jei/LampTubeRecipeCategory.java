package com.altnoir.mementoinabyss.compat.jei;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.lamptube.LampTubeRecipe;
import com.altnoir.mementoinabyss.foundation.recipe.processing.SizedHeatIngredient;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class LampTubeRecipeCategory implements IRecipeCategory<RecipeHolder<LampTubeRecipe>> {
    public static final IRecipeHolderType<LampTubeRecipe> TYPE =
            IRecipeHolderType.create(MementoInAbyss.asResource("lamp_tube"));

    private final IDrawable icon;

    public LampTubeRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(MiaBlocks.AMETHYST_LAMPTUBE.get());
    }

    @Override
    public IRecipeHolderType<LampTubeRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.mementoinabyss.amethyst_lamptube");
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 38;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<LampTubeRecipe> holder,
            IFocusGroup focuses) {
        LampTubeRecipe recipe = holder.value();
        builder.addInputSlot(1, 7).setStandardSlotBackground().addItemStacks(inputStacks(recipe));
        builder.addOutputSlot(61, 7).setOutputSlotBackground().add(recipe.result());
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<LampTubeRecipe> holder,
            IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(26, 5);
        SizedHeatIngredient heat = holder.value().heatInput();
        if (heat == null) {
            return;
        }
        builder.addText(
                        Component.translatable(
                                "jei.mementoinabyss.lamp_tube.heat",
                                heat.amount(),
                                Component.translatable(
                                        heat.heat().getHeatType().getDescriptionId())),
                        80,
                        12)
                .setPosition(1, 28);
    }

    private static List<ItemStack> inputStacks(LampTubeRecipe recipe) {
        int count = recipe.itemInput().count();
        return recipe.itemInput()
                .ingredient()
                .getValues()
                .stream()
                .map(holder -> new ItemStack(holder.value(), count))
                .toList();
    }
}
