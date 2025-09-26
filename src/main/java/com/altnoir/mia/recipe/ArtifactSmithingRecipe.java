package com.altnoir.mia.recipe;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaRecipes;
import com.altnoir.mia.item.abs.IEArtifact;
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

public class ArtifactSmithingRecipe implements Recipe<ArtifactSmithingRecipeInput> {

    final Ingredient artifact;
    final ItemStack material;
    final Holder<Attribute> attribute;
    final double amount;
    final AttributeModifier.Operation operation;

    public ArtifactSmithingRecipe(Ingredient artifact, ItemStack material, Holder<Attribute> attribute,
                                  double amount, AttributeModifier.Operation operation) {
        this.artifact = artifact;
        this.material = material;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    public ItemStack assemble(ArtifactSmithingRecipeInput input, Provider registries) {
        if (input.base().has(MiaComponents.ARTIFACT_ENHANCEMENT)) {
            int artifactLevel = input.base().get(MiaComponents.ARTIFACT_ENHANCEMENT).getLevel();
            ItemStack newArtifact = input.base().transmuteCopy(input.base().getItem(), input.base().getCount());
            newArtifact.set(MiaComponents.ARTIFACT_ENHANCEMENT, input.base().get(MiaComponents.ARTIFACT_ENHANCEMENT)
                    .setLevel(artifactLevel + 1)
                    .addAttributeModifier(this.attribute, this.amount, this.operation));
            return newArtifact;
        }
        return input.base().copy();
    }

    @Override
    public ItemStack getResultItem(Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiaRecipes.ARTIFACT_SMITHING_SERIALIZER.get();
    }

    @Override
    public boolean matches(ArtifactSmithingRecipeInput input, Level level) {
        if (!this.material.getItem().equals(input.material().getItem()) || this.material.getCount() > input.material().getCount()) {
            return false;
        }
        if ((input.base().getItem() instanceof IEArtifact artifact)
                && input.base().has(MiaComponents.ARTIFACT_ENHANCEMENT)) {
            int artifactLevel = input.base().get(MiaComponents.ARTIFACT_ENHANCEMENT).getLevel();
            return artifactLevel < artifact.getMaxLevel();
        }
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int arg0, int arg1) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return MiaRecipes.ARTIFACT_SMITHING_TYPE.get();
    }

    public boolean isMaterialIngredient(ItemStack stack) {
        return this.material.getItem().equals(stack.getItem()) && this.material.getCount() <= stack.getCount();
    }

    public ItemStack getMaterial() {
        return this.material;
    }

    public double getAttributeAmount() {
        return this.amount;
    }

    public AttributeModifier.Operation getAttributeOperation() {
        return this.operation;
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public boolean isArtifactIngredient(ItemStack stack) {
        return this.artifact.test(stack);
    }

    // @Override
    // public boolean isTemplateIngredient(ItemStack stack) {
    // return false;
    // }

    public static class Serializer implements RecipeSerializer<ArtifactSmithingRecipe> {

        private static final MapCodec<ArtifactSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((codec) -> {
            return codec.group(
                    Ingredient.CODEC.fieldOf("artifact").forGetter((recipe) -> {
                        return recipe.artifact;
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
                    })).apply(codec, ArtifactSmithingRecipe::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactSmithingRecipe> STREAM_CODEC = StreamCodec
                .of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<ArtifactSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArtifactSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ArtifactSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient artifact = (Ingredient) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack ingredient = (ItemStack) ItemStack.STREAM_CODEC.decode(buffer);
            Holder<Attribute> attribute = (Holder<Attribute>) Attribute.STREAM_CODEC.decode(buffer);
            double amount = (double) ByteBufCodecs.DOUBLE.decode(buffer);
            AttributeModifier.Operation operation = (AttributeModifier.Operation) AttributeModifier.Operation.STREAM_CODEC
                    .decode(buffer);
            return new ArtifactSmithingRecipe(artifact, ingredient, attribute, amount, operation);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ArtifactSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.artifact);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.material);
            Attribute.STREAM_CODEC.encode(buffer, recipe.attribute);
            ByteBufCodecs.DOUBLE.encode(buffer, recipe.amount);
            AttributeModifier.Operation.STREAM_CODEC.encode(buffer, recipe.operation);
        }
    }

}
