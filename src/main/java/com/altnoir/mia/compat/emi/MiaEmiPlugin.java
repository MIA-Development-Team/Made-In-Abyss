package com.altnoir.mia.compat.emi;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.recipe.LampTubeRecipe;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.util.MiaUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.ParametersAreNonnullByDefault;

@EmiEntrypoint
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MiaEmiPlugin implements EmiPlugin {
    public static final ResourceLocation WIDGETS = MiaUtil.id("emi", "textures/gui/widgets.png");
    public static final EmiStack WORKSTATION = EmiStack.of(MiaBlocks.LAMP_TUBE.asItem());
    public static EmiRecipeCategory LAMP_TUBE = new EmiRecipeCategory(MiaUtil.id(MIA.MOD_ID, "lamp_tube"),
            WORKSTATION, new EmiTexture(WIDGETS, 160, 240, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(LAMP_TUBE);
        registry.addWorkstation(LAMP_TUBE, WORKSTATION);

        for (RecipeHolder<LampTubeRecipe> recipe : getRecipes(registry, MiaRecipes.LAMP_TUBE_TYPE.get())) {
            registry.addRecipe(new LampTubeEmiRecipe(recipe));
        }
    }

    private static <C extends RecipeInput, T extends Recipe<C>> Iterable<RecipeHolder<T>> getRecipes(EmiRegistry registry, RecipeType<T> type) {
        return registry.getRecipeManager().getAllRecipesFor(type);
    }
    /*private static <C extends RecipeInput, T extends Recipe<C>> Iterable<T> getRecipes(EmiRegistry registry, RecipeType<T> type) {
        return registry.getRecipeManager().getAllRecipesFor(type).stream().map(RecipeHolder::value)::iterator;
    }*/
}