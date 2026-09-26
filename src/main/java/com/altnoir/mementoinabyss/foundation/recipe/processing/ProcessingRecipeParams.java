package com.altnoir.mementoinabyss.foundation.recipe.processing;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record ProcessingRecipeParams(
        List<SizedIngredient> ingredients,
        List<SizedHeatIngredient> heatIngredients,
        List<ItemStackTemplate> results) {
    private static final Codec<Either<SizedHeatIngredient, SizedIngredient>> INGREDIENT_CODEC =
            Codec.either(SizedHeatIngredient.CODEC, SizedIngredient.NESTED_CODEC);

    public static final MapCodec<ProcessingRecipeParams> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            INGREDIENT_CODEC
                                                    .listOf()
                                                    .fieldOf("ingredients")
                                                    .forGetter(
                                                            ProcessingRecipeParams
                                                                    ::encodedIngredients),
                                            ItemStackTemplate.CODEC
                                                    .listOf()
                                                    .fieldOf("results")
                                                    .forGetter(ProcessingRecipeParams::results))
                                    .apply(instance, ProcessingRecipeParams::fromEncoded));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipeParams> STREAM_CODEC =
            StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ProcessingRecipeParams::ingredients,
                    SizedHeatIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ProcessingRecipeParams::heatIngredients,
                    ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ProcessingRecipeParams::results,
                    ProcessingRecipeParams::new);

    public ProcessingRecipeParams(
            List<SizedIngredient> ingredients,
            List<SizedHeatIngredient> heatIngredients,
            List<ItemStackTemplate> results) {
        this.ingredients = List.copyOf(ingredients);
        this.heatIngredients = List.copyOf(heatIngredients);
        this.results = List.copyOf(results);
    }

    public static ProcessingRecipeParams of(SizedIngredient ingredient, ItemStackTemplate result) {
        return new ProcessingRecipeParams(List.of(ingredient), List.of(), List.of(result));
    }

    public static ProcessingRecipeParams of(
            SizedIngredient ingredient, SizedHeatIngredient heat, ItemStackTemplate result) {
        return new ProcessingRecipeParams(List.of(ingredient), List.of(heat), List.of(result));
    }

    private List<Either<SizedHeatIngredient, SizedIngredient>> encodedIngredients() {
        List<Either<SizedHeatIngredient, SizedIngredient>> encoded = new ArrayList<>();
        for (SizedIngredient ingredient : ingredients) {
            encoded.add(Either.right(ingredient));
        }
        for (SizedHeatIngredient heat : heatIngredients) {
            encoded.add(Either.left(heat));
        }
        return encoded;
    }

    private static ProcessingRecipeParams fromEncoded(
            List<Either<SizedHeatIngredient, SizedIngredient>> ingredients,
            List<ItemStackTemplate> results) {
        List<SizedIngredient> items = new ArrayList<>();
        List<SizedHeatIngredient> heat = new ArrayList<>();
        for (Either<SizedHeatIngredient, SizedIngredient> ingredient : ingredients) {
            ingredient.ifRight(items::add).ifLeft(heat::add);
        }
        return new ProcessingRecipeParams(items, heat, results);
    }
}
