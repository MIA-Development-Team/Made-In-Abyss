package com.altnoir.mementoinabyss.foundation.recipe.processing;

import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatResource;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatStack;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record SizedHeatIngredient(HeatResource heat, int amount) {
    public static final Codec<SizedHeatIngredient> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            HeatResource.CODEC
                                                    .fieldOf("heat")
                                                    .forGetter(SizedHeatIngredient::heat),
                                            ExtraCodecs.POSITIVE_INT
                                                    .fieldOf("amount")
                                                    .forGetter(SizedHeatIngredient::amount))
                                    .apply(instance, SizedHeatIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedHeatIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    HeatResource.STREAM_CODEC,
                    SizedHeatIngredient::heat,
                    ByteBufCodecs.VAR_INT,
                    SizedHeatIngredient::amount,
                    SizedHeatIngredient::new);

    public SizedHeatIngredient {
        if (heat.isEmpty()) {
            throw new IllegalArgumentException("Heat ingredient cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Heat amount must be positive");
        }
    }

    public static SizedHeatIngredient of(HeatType type, int amount) {
        return new SizedHeatIngredient(HeatResource.of(type), amount);
    }

    public boolean test(HeatStack stack) {
        return heat.matches(stack) && stack.amount() >= amount;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SizedHeatIngredient other
                && amount == other.amount
                && heat.equals(other.heat);
    }
}
