package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkyLightRegionSettings(
        int centerX, int centerZ, double radiusMultiplier, double fadeDistance) {
    private static final Codec<Double> NON_NEGATIVE_FINITE =
            Codec.DOUBLE.validate(
                    value ->
                            Double.isFinite(value) && value >= 0
                                    ? DataResult.success(value)
                                    : DataResult.error(
                                            () -> "Expected a finite non-negative value"));
    public static final Codec<SkyLightRegionSettings> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            Codec.INT
                                                    .optionalFieldOf("center_x", 0)
                                                    .forGetter(SkyLightRegionSettings::centerX),
                                            Codec.INT
                                                    .optionalFieldOf("center_z", 0)
                                                    .forGetter(SkyLightRegionSettings::centerZ),
                                            NON_NEGATIVE_FINITE
                                                    .fieldOf("radius_multiplier")
                                                    .forGetter(
                                                            SkyLightRegionSettings
                                                                    ::radiusMultiplier),
                                            NON_NEGATIVE_FINITE
                                                    .fieldOf("fade_distance")
                                                    .forGetter(
                                                            SkyLightRegionSettings::fadeDistance))
                                    .apply(instance, SkyLightRegionSettings::new));

    public SkyLightRegionSettings {
        if (!Double.isFinite(radiusMultiplier)
                || radiusMultiplier < 0
                || !Double.isFinite(fadeDistance)
                || fadeDistance < 0)
            throw new IllegalArgumentException(
                    "Sky light distances must be finite and non-negative");
    }
}
