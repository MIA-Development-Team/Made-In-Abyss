package com.altnoir.mia.recipe;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.item.abs.AbstractWhistle;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class WhistleSmithingRecipe implements Recipe<WhistleSmithingRecipeInput> {

    final Ingredient whistle;
    final ItemStack material;
    final Holder<Attribute> attribute;
    final double amount;
    final AttributeModifier.Operation operation;

    public WhistleSmithingRecipe(Ingredient whistle, ItemStack material, Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation) {
        this.whistle = whistle;
        this.material = material;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public ItemStack assemble(WhistleSmithingRecipeInput input, Provider registries) {
        if (input.base().has(MiaComponents.WHISTLE_STATS)) {
            int whistleLevel = input.base().get(MiaComponents.WHISTLE_STATS).getLevel();
            ItemStack newWhistle = input.base().transmuteCopy(input.base().getItem(), input.base().getCount());
            newWhistle.set(MiaComponents.WHISTLE_STATS, input.base().get(MiaComponents.WHISTLE_STATS)
                    .setLevel(whistleLevel + 1)
                    .addAttributeModifier(this.attribute, this.amount, this.operation));
            return newWhistle;
        }
        return input.base().copy();
    }

    @Override
    public ItemStack getResultItem(Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiaRecipes.WHISTLE_SMITHING_SERIALIZER.get();
    }

    @Override
    public boolean matches(WhistleSmithingRecipeInput input, Level level) {
        if (!this.material.equals(input.material())) {
            return false;
        }
        if ((input.base().getItem() instanceof AbstractWhistle whistle)
                && input.base().has(MiaComponents.WHISTLE_STATS)) {
            int whistleLevel = input.base().get(MiaComponents.WHISTLE_STATS).getLevel();
            return whistleLevel < whistle.getMaxLevel();
        }
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int arg0, int arg1) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return MiaRecipes.WHISTLE_SMITHING_TYPE.get();
    }

    public boolean isMaterialIngredient(ItemStack stack) {
        return this.material.getItem().equals(stack.getItem());
    }

    public ItemStack getMaterial() {
        return this.material;
    }

    public boolean isWhistleIngredient(ItemStack stack) {
        return this.whistle.test(stack);
    }

    // @Override
    // public boolean isTemplateIngredient(ItemStack stack) {
    // return false;
    // }

    public static class Serializer implements RecipeSerializer<WhistleSmithingRecipe> {

        private static final MapCodec<WhistleSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
            return codec.group(
                    Ingredient.CODEC.fieldOf("whistle").forGetter((recipe) -> {
                        return recipe.whistle;
                    }),
                    ItemStack.CODEC.fieldOf("material").forGetter((recipe) -> {
                        return recipe.material;
                    }),
                    Attribute.CODEC.fieldOf("attribute").forGetter((recipe) -> {
                        return recipe.attribute;
                    }),
                    Codec.DOUBLE.fieldOf("amount").forGetter((recipe) -> {
                        return recipe.amount;
                    }),
                    AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter((recipe) -> {
                        return recipe.operation;
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
            Ingredient whistle = (Ingredient) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack ingredient = (ItemStack) ItemStack.STREAM_CODEC.decode(buffer);
            Holder<Attribute> attribute = (Holder<Attribute>) Attribute.STREAM_CODEC.decode(buffer);
            double amount = (double) ByteBufCodecs.DOUBLE.decode(buffer);
            AttributeModifier.Operation operation = (AttributeModifier.Operation) AttributeModifier.Operation.STREAM_CODEC
                    .decode(buffer);
            return new WhistleSmithingRecipe(whistle, ingredient, attribute, amount, operation);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, WhistleSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.whistle);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.material);
            Attribute.STREAM_CODEC.encode(buffer, recipe.attribute);
            ByteBufCodecs.DOUBLE.encode(buffer, recipe.amount);
            AttributeModifier.Operation.STREAM_CODEC.encode(buffer, recipe.operation);
        }
    }

}
