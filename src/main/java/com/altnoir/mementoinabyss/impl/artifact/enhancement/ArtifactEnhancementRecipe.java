package com.altnoir.mementoinabyss.impl.artifact.enhancement;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Locale;

public record ArtifactEnhancementRecipe(
        Holder<Item> material,
        int materialCount,
        Holder<Attribute> attribute,
        DoubleRange value,
        AttributeModifier.Operation operation
) implements Recipe<ArtifactEnhancementRecipeInput> {
    public static final MapCodec<ArtifactEnhancementRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Item.CODEC.fieldOf("material").forGetter(ArtifactEnhancementRecipe::material),
            Codec.intRange(1, 64).fieldOf("count").forGetter(ArtifactEnhancementRecipe::materialCount),
            Attribute.CODEC.fieldOf("attribute").forGetter(ArtifactEnhancementRecipe::attribute),
            DoubleRange.CODEC.fieldOf("value").forGetter(ArtifactEnhancementRecipe::value),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(ArtifactEnhancementRecipe::operation)
    ).apply(instance, ArtifactEnhancementRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactEnhancementRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Item.STREAM_CODEC, ArtifactEnhancementRecipe::material,
                    ByteBufCodecs.VAR_INT, ArtifactEnhancementRecipe::materialCount,
                    Attribute.STREAM_CODEC, ArtifactEnhancementRecipe::attribute,
                    DoubleRange.STREAM_CODEC, ArtifactEnhancementRecipe::value,
                    AttributeModifier.Operation.STREAM_CODEC, ArtifactEnhancementRecipe::operation,
                    ArtifactEnhancementRecipe::new
            );

    public ArtifactEnhancementRecipe {
        if (materialCount < 1 || materialCount > 64) {
            throw new IllegalArgumentException("Artifact enhancement material count must be between 1 and 64");
        }
    }

    @Override
    public boolean matches(ArtifactEnhancementRecipeInput input, Level level) {
        return ArtifactApi.canEnhance(input.artifact())
                && isMaterial(input.material());
    }

    @Override
    public ItemStack assemble(ArtifactEnhancementRecipeInput input) {
        if (!ArtifactApi.canEnhance(input.artifact()) || !isMaterial(input.material())) {
            return ItemStack.EMPTY;
        }
        ItemStack result = input.artifact().copyWithCount(1);
        double amount = Math.round(value.randomValue(input.random()) * 100.0) / 100.0;
        return ArtifactApi.addEnhancement(result, attribute, modifierId(), amount, operation)
                ? result : ItemStack.EMPTY;
    }

    public boolean isMaterial(ItemStack stack) {
        return stack.is(material) && stack.getCount() >= materialCount;
    }

    public Identifier modifierId() {
        String attributePath = attribute.unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("unknown");
        return MementoInAbyss.asResource("artifact/enhancement/" + attributePath + "/"
                + operation.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<ArtifactEnhancementRecipeInput>> getSerializer() {
        return MiaRecipes.ARTIFACT_ENHANCEMENT_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<ArtifactEnhancementRecipeInput>> getType() {
        return MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.SMITHING;
    }
}
