package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record Layer2FeatureConfiguration(
        Holder<ConfiguredFeature<?, ?>> feature, boolean allowFluids, boolean submerged)
        implements FeatureConfiguration {
    public static final Codec<Layer2FeatureConfiguration> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            ConfiguredFeature.CODEC
                                                    .fieldOf("feature")
                                                    .forGetter(Layer2FeatureConfiguration::feature),
                                            Codec.BOOL
                                                    .optionalFieldOf("allow_fluids", false)
                                                    .forGetter(
                                                            Layer2FeatureConfiguration
                                                                    ::allowFluids),
                                            Codec.BOOL
                                                    .optionalFieldOf("submerged", false)
                                                    .forGetter(
                                                            Layer2FeatureConfiguration::submerged))
                                    .apply(instance, Layer2FeatureConfiguration::new));

    public Layer2FeatureConfiguration(
            Holder<ConfiguredFeature<?, ?>> feature, boolean allowFluids) {
        this(feature, allowFluids, false);
    }

    @Override
    public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures() {
        return Stream.of(feature);
    }
}
