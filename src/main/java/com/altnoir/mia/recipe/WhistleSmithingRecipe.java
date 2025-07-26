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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class WhistleSmithingRecipe implements SmithingRecipe {

    final Ingredient base;
    final Ingredient addition;
    final Holder<Attribute> attribute;
    final double amount;
    final AttributeModifier.Operation operation;

    public WhistleSmithingRecipe(Ingredient base, Ingredient addition, Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation) {
        this.base = base;
        this.addition = addition;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, Provider registries) {
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
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!this.addition.test(input.addition())) {
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
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return false;
    }

    public static class Serializer implements RecipeSerializer<WhistleSmithingRecipe> {

        private static final MapCodec<WhistleSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
            return codec.group(
                    Ingredient.CODEC.fieldOf("base").forGetter((recipe) -> {
                        return recipe.base;
                    }),
                    Ingredient.CODEC.fieldOf("addition").forGetter((recipe) -> {
                        return recipe.addition;
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
            Ingredient base = (Ingredient) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient = (Ingredient) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Holder<Attribute> attribute = (Holder<Attribute>) Attribute.STREAM_CODEC.decode(buffer);
            double amount = (double) ByteBufCodecs.DOUBLE.decode(buffer);
            AttributeModifier.Operation operation = (AttributeModifier.Operation) AttributeModifier.Operation.STREAM_CODEC
                    .decode(buffer);
            return new WhistleSmithingRecipe(base, ingredient, attribute, amount, operation);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, WhistleSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
            Attribute.STREAM_CODEC.encode(buffer, recipe.attribute);
            ByteBufCodecs.DOUBLE.encode(buffer, recipe.amount);
            AttributeModifier.Operation.STREAM_CODEC.encode(buffer, recipe.operation);
        }
    }
}
