package com.altnoir.mementoinabyss.content.lamptube;

import com.altnoir.mementoinabyss.foundation.recipe.processing.ProcessingRecipe;
import com.altnoir.mementoinabyss.foundation.recipe.processing.ProcessingRecipeParams;
import com.altnoir.mementoinabyss.foundation.recipe.processing.SizedHeatIngredient;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

public final class LampTubeRecipe extends ProcessingRecipe<LampTubeRecipeInput> {
    public static final MapCodec<LampTubeRecipe> CODEC =
            ProcessingRecipe.codec(LampTubeRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, LampTubeRecipe> STREAM_CODEC =
            ProcessingRecipe.streamCodec(LampTubeRecipe::new);

    public LampTubeRecipe(ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxHeatInputCount() {
        return 1;
    }

    @Override
    public List<String> validate() {
        List<String> errors = super.validate();
        if (getIngredients().size() != 1) {
            errors.add("Lamp tube recipes require exactly one item ingredient.");
        }
        if (getHeatIngredients().size() > 1) {
            errors.add("Lamp tube recipes accept at most one heat ingredient.");
        }
        if (getResults().size() != 1) {
            errors.add("Lamp tube recipes require exactly one result.");
        }
        return errors;
    }

    public SizedIngredient itemInput() {
        return getIngredients().getFirst();
    }

    public @Nullable SizedHeatIngredient heatInput() {
        return getHeatIngredients().isEmpty() ? null : getHeatIngredients().getFirst();
    }

    public ItemStackTemplate result() {
        return getResults().getFirst();
    }

    public int itemCount(int multiplier) {
        return itemInput().count() * multiplier;
    }

    public int heatAmount(int multiplier) {
        SizedHeatIngredient heat = heatInput();
        return heat == null ? 0 : heat.amount() * multiplier;
    }

    public ItemStack resultStack(int multiplier) {
        ItemStack stack = result().create();
        int count = Math.min(stack.getCount() * multiplier, stack.getMaxStackSize());
        return stack.copyWithCount(count);
    }

    @Override
    public boolean matches(LampTubeRecipeInput input, Level level) {
        SizedHeatIngredient heat = heatInput();
        return itemInput().test(input.item()) && (heat == null || heat.test(input.heat()));
    }

    @Override
    public ItemStack assemble(LampTubeRecipeInput input) {
        return result().create();
    }

    @Override
    public RecipeSerializer<LampTubeRecipe> getSerializer() {
        return MiaRecipes.LAMP_TUBE_SERIALIZER.get();
    }

    @Override
    public RecipeType<LampTubeRecipe> getType() {
        return MiaRecipes.LAMP_TUBE_TYPE.get();
    }
}
