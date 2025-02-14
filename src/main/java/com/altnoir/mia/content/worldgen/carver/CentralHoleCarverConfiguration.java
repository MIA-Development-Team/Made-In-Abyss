package com.altnoir.mia.content.worldgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

import java.util.Map;

public class CentralHoleCarverConfiguration extends CarverConfiguration {
    public static final Codec<CentralHoleCarverConfiguration> CODEC = RecordCodecBuilder.create((p_159184_) -> {
        return p_159184_.group(CarverConfiguration.CODEC.forGetter((p_159192_) -> {
            return p_159192_;
        }), Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("critical_radius").forGetter((pH) -> {
            return pH.criticalRadius;
        }), Codec.INT.optionalFieldOf("mix_width", 0).forGetter((pH) -> {
            return pH.mixWidth;
        }), Codec.DOUBLE.optionalFieldOf("noise_horizontal_multiplier", 1.0).forGetter((pH) -> {
            return pH.nHM;
        }), Codec.DOUBLE.optionalFieldOf("noise_vertical_multiplier", 1.0).forGetter((pH) -> {
            return pH.nVM;
        })).apply(p_159184_, CentralHoleCarverConfiguration::new);
    });

    public final Map<String, Integer> criticalRadius;
    public final int mixWidth;
    public final double nHM;
    public final double nVM;

    public CentralHoleCarverConfiguration(CarverConfiguration pCarverConfiguration, Map<String, Integer> criticalRadius, int mixWidth, double nHM, double nVM) {
        super(pCarverConfiguration.probability, pCarverConfiguration.y, pCarverConfiguration.yScale, pCarverConfiguration.lavaLevel, pCarverConfiguration.debugSettings, pCarverConfiguration.replaceable);
        this.criticalRadius = criticalRadius;
        this.mixWidth = mixWidth;
        this.nHM = nHM;
        this.nVM = nVM;
    }
}
