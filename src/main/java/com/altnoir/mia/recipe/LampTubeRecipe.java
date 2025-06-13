package com.altnoir.mia.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record LampTubeRecipe(Ingredient ingredient, ItemStack output) implements Recipe<LampTubeRecipeInput> {
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        return ingredients;
    }

    @Override
    public boolean matches(LampTubeRecipeInput input, Level level) {
        if (level.isClientSide) return false;

        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(LampTubeRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MIARecipes.LAMP_TUBE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return MIARecipes.LAMP_TUBE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<LampTubeRecipe> {
        public static final MapCodec<LampTubeRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(LampTubeRecipe::ingredient),
                        ItemStack.CODEC.fieldOf("output").forGetter(LampTubeRecipe::output)
                ).apply(builder, LampTubeRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, LampTubeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, LampTubeRecipe::ingredient,
                ItemStack.STREAM_CODEC, LampTubeRecipe::output,
                LampTubeRecipe::new
        );

        @Override
        public MapCodec<LampTubeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LampTubeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
