package com.altnoir.mia.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record DoubleRange(double min, double max) {
    public double getRandomValue(RandomSource random) {
        return Mth.nextDouble(random, min, max);
    }

    public static DoubleRange between(double min, double max) {
        return new DoubleRange(min, max);
    }

    public static final Codec<DoubleRange> CODEC = RecordCodecBuilder.create(codec ->
            codec.group(
                    Codec.DOUBLE.fieldOf("min").forGetter(DoubleRange::min),
                    Codec.DOUBLE.fieldOf("max").forGetter(DoubleRange::max)
            ).apply(codec, DoubleRange::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleRange> STREAM_CODEC = StreamCodec.of(
            (buffer, range) -> {
                ByteBufCodecs.DOUBLE.encode(buffer, range.min);
                ByteBufCodecs.DOUBLE.encode(buffer, range.max);
            },
            buffer -> new DoubleRange(
                    ByteBufCodecs.DOUBLE.decode(buffer),
                    ByteBufCodecs.DOUBLE.decode(buffer)
            )
    );
}
