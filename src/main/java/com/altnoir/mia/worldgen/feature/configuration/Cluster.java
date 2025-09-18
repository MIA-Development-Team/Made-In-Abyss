package com.altnoir.mia.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class Cluster implements FeatureConfiguration {
    public static final Codec<Cluster> CODEC = RecordCodecBuilder.create(
            apply -> apply.group(
                            IntProvider.codec(1, 20).fieldOf("column_radius").forGetter(cluster -> cluster.radiusProvider),
                            FloatProvider.codec(1.0F, 10.0F).fieldOf("column_height").forGetter(cluster -> cluster.heightProvider),
                            Codec.floatRange(0.1F, 5.0F)
                                    .fieldOf("height_variance")
                                    .orElse(1.0F)
                                    .forGetter(cluster -> cluster.heightVariance),
                            Codec.intRange(0, 100)
                                    .fieldOf("density_factor")
                                    .orElse(50)
                                    .forGetter(cluster -> cluster.densityFactor)
                    )
                    .apply(apply, Cluster::new)
    );
    public final IntProvider radiusProvider;
    public final FloatProvider heightProvider;
    public final float heightVariance;
    public final int densityFactor;

    public Cluster(
            IntProvider radiusProvider,
            FloatProvider heightProvider,
            float heightVariance,
            int densityFactor
    ) {
        this.radiusProvider = radiusProvider;
        this.heightProvider = heightProvider;
        this.heightVariance = heightVariance;
        this.densityFactor = densityFactor;
    }
}