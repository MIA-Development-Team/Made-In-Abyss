package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeAmbientLightSettings(Map<TagKey<Biome>, Float> values) {
    public static final BiomeAmbientLightSettings EMPTY = new BiomeAmbientLightSettings(Map.of());
    private static final Codec<Float> BRIGHTNESS =
            Codec.FLOAT.validate(
                    value ->
                            valid(value)
                                    ? DataResult.success(value)
                                    : DataResult.error(
                                            () ->
                                                    "Ambient light must be finite and within [0,"
                                                            + " 1]"));
    public static final Codec<BiomeAmbientLightSettings> CODEC =
            Codec.unboundedMap(TagKey.hashedCodec(Registries.BIOME), BRIGHTNESS)
                    .xmap(BiomeAmbientLightSettings::new, BiomeAmbientLightSettings::values);

    public BiomeAmbientLightSettings {
        values = Map.copyOf(values);
        if (values.values().stream().anyMatch(value -> !valid(value)))
            throw new IllegalArgumentException("Ambient light must be finite and within [0, 1]");
    }

    /** Overlapping tags use the darkest matching override; -1 leaves native ambient light intact. */
    public float brightness(Holder<Biome> biome) {
        float result = -1.0F;
        for (var entry : values.entrySet()) {
            if (biome.is(entry.getKey()))
                result = result < 0 ? entry.getValue() : Math.min(result, entry.getValue());
        }
        return result;
    }

    private static boolean valid(float value) {
        return Float.isFinite(value) && value >= 0.0F && value <= 1.0F;
    }
}
