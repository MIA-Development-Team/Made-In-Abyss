package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeFogSettings(Map<TagKey<Biome>, Float> values) {
    public static final BiomeFogSettings EMPTY = new BiomeFogSettings(Map.of());
    private static final Codec<Float> MULTIPLIER =
            Codec.FLOAT.validate(
                    value ->
                            valid(value)
                                    ? DataResult.success(value)
                                    : DataResult.error(
                                            () ->
                                                    "Fog multiplier must be finite and within [0,"
                                                            + " 8]"));
    public static final Codec<BiomeFogSettings> CODEC =
            Codec.unboundedMap(TagKey.hashedCodec(Registries.BIOME), MULTIPLIER)
                    .xmap(BiomeFogSettings::new, BiomeFogSettings::values);

    public BiomeFogSettings {
        values = Map.copyOf(values);
        if (values.values().stream().anyMatch(value -> !valid(value)))
            throw new IllegalArgumentException("Fog multiplier must be finite and within [0, 8]");
    }

    public float multiplier(Holder<Biome> biome) {
        float result = -1.0F;
        for (var entry : values.entrySet()) {
            if (biome.is(entry.getKey()))
                result = result < 0.0F ? entry.getValue() : Math.min(result, entry.getValue());
        }
        return result;
    }

    private static boolean valid(float value) {
        return Float.isFinite(value) && value >= 0.0F && value <= 8.0F;
    }
}
