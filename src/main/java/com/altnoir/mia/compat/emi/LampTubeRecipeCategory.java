/*
package com.altnoir.mia.compat.emi;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.recipe.LampTubeRecipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LampTubeRecipeCategory implements IRecipeCategory<LampTubeRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "lamp_tube");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "textures/gui/container/lamp_tube_jei.png");

    public static final RecipeType<LampTubeRecipe> LAMP_TUBE_RECIPE_TYPE = new RecipeType<>(UID, LampTubeRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public LampTubeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 80);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MIABlocks.LAMP_TUBE));
    }

    @Override
    public RecipeType<LampTubeRecipe> getRecipeType() {
        return LAMP_TUBE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.mia.lamp_tube");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LampTubeRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 1).addItemStack(recipe.getResultItem(null));
    }
}
*/
