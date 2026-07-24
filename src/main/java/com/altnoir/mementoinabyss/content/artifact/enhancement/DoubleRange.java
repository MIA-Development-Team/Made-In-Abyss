package com.altnoir.mementoinabyss.content.artifact.enhancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record DoubleRange(double min, double max) {
    public static final Codec<DoubleRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("min").forGetter(DoubleRange::min),
            Codec.DOUBLE.fieldOf("max").forGetter(DoubleRange::max)
    ).apply(instance, DoubleRange::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleRange> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, DoubleRange::min,
                    ByteBufCodecs.DOUBLE, DoubleRange::max,
                    DoubleRange::new
            );

    public DoubleRange {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid artifact enhancement range [" + min + ", " + max + "]");
        }
    }

    public double randomValue(RandomSource random) {
        return min == max ? min : Mth.nextDouble(random, min, max);
    }

    public static DoubleRange between(double min, double max) {
        return new DoubleRange(min, max);
    }
}
