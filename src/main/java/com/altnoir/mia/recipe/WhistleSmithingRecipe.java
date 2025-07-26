package com.altnoir.mia.recipe;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.item.abs.AbstractWhistle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;

public class WhistleSmithingRecipe implements SmithingRecipe {

    final ItemStack base;
    final Ingredient addition;

    public WhistleSmithingRecipe(ItemStack base, Ingredient addition) {
        this.base = base;
        this.addition = addition;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, Provider registries) {
        if (input.base().has(MiaComponents.WHISTLE_STATS)) {
            int whistleLevel = input.base().get(MiaComponents.WHISTLE_STATS).getLevel();
            ItemStack newWhistle = input.base().transmuteCopy(this.base.getItem(), this.base.getCount());
            newWhistle.set(MiaComponents.WHISTLE_STATS,
                    input.base().get(MiaComponents.WHISTLE_STATS).setLevel(whistleLevel + 1));
            return newWhistle;
        }
        return input.base().copy();
    }

    @Override
    public ItemStack getResultItem(Provider registries) {
        return this.base;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiaRecipes.WHISTLE_SMITHING_SERIALIZER.get();
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!this.addition.test(input.addition())) {
            return false;
        }
        if ((input.base().getItem() instanceof AbstractWhistle whistle)
                && input.base().has(MiaComponents.WHISTLE_STATS)) {
            int whistleLevel = input.base().get(MiaComponents.WHISTLE_STATS).getLevel();
            return whistleLevel < whistle.GetMaxLevel();
        }
        return false;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.getItem().equals(this.base.getItem());
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return false;
    }

    public static class Serializer implements RecipeSerializer<WhistleSmithingRecipe> {

        private static final MapCodec<WhistleSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
            return codec.group(ItemStack.CODEC.fieldOf("base").forGetter((recipe) -> {
                return recipe.base;
            }), Ingredient.CODEC.fieldOf("addition").forGetter((recipe) -> {
                return recipe.addition;
            })).apply(codec, WhistleSmithingRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, WhistleSmithingRecipe> STREAM_CODEC = StreamCodec
                .of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<WhistleSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WhistleSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static WhistleSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            ItemStack base = (ItemStack) ItemStack.STREAM_CODEC.decode(buffer);
            Ingredient ingredient = (Ingredient) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            return new WhistleSmithingRecipe(base, ingredient);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, WhistleSmithingRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
        }
    }
}
