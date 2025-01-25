package com.altnoir.mia.content.worldgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;

import java.util.Map;

public class RingCarverConfiguration extends CarverConfiguration {
    public static final Codec<RingCarverConfiguration> CODEC = RecordCodecBuilder.create((p_159184_) -> {
        return p_159184_.group(CarverConfiguration.CODEC.forGetter((p_159192_) -> {
            return p_159192_;
        }), Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("inner_critical_radius").forGetter((pH) -> {
            return pH.innerCriticalRadius;
        }), Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("outer_critical_radius").forGetter((pH) -> {
                    return pH.outerCriticalRadius;
        }), Codec.INT.optionalFieldOf("inner_mix_width", 0).forGetter((pH) -> {
            return pH.innerMixWidth;
        }), Codec.INT.optionalFieldOf("outer_mix_width", 0).forGetter((pH) -> {
            return pH.outerMixWidth;
        }), Codec.DOUBLE.optionalFieldOf("inner_noise_horizontal_multiplier", 1.0).forGetter((pH) -> {
            return pH.iNHM;
        }), Codec.DOUBLE.optionalFieldOf("inner_noise_vertical_multiplier", 1.0).forGetter((pH) -> {
            return pH.iNVM;
        }), Codec.DOUBLE.optionalFieldOf("outer_noise_horizontal_multiplier", 1.0).forGetter((pH) -> {
            return pH.oNHM;
        }), Codec.DOUBLE.optionalFieldOf("outer_noise_vertical_multiplier", 1.0).forGetter((pH) -> {
            return pH.oNVM;
        }), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("placeable").forGetter((p_224841_) -> {
            return p_224841_.placeable;
        }), Codec.DOUBLE.optionalFieldOf("continue_probability", 0.9).forGetter((pH) -> {
            return pH.continueProbability;
        })).apply(p_159184_, RingCarverConfiguration::new);
    });

    public final Map<String, Integer> innerCriticalRadius;
    public final Map<String, Integer> outerCriticalRadius;
    public final int innerMixWidth, outerMixWidth;
    public final double iNHM, iNVM, oNHM, oNVM;
    public final HolderSet<Block> placeable;
    public final double continueProbability;


    public RingCarverConfiguration(CarverConfiguration pCarverConfiguration, Map<String, Integer> innerCriticalRadius, Map<String, Integer> outerCriticalRadius, int innerMixWidth, int outerMixWidth, double iNHM, double iNVM, double oNHM, double oNVM, HolderSet<Block> placeable, double continueProbability) {
        super(pCarverConfiguration.probability, pCarverConfiguration.y, pCarverConfiguration.yScale, pCarverConfiguration.lavaLevel, pCarverConfiguration.debugSettings, pCarverConfiguration.replaceable);
        this.innerCriticalRadius = innerCriticalRadius;
        this.outerCriticalRadius = outerCriticalRadius;
        this.innerMixWidth = innerMixWidth;
        this.outerMixWidth = outerMixWidth;
        this.iNHM = iNHM;
        this.iNVM = iNVM;
        this.oNHM = oNHM;
        this.oNVM = oNVM;
        this.placeable = placeable;
        this.continueProbability = continueProbability;
    }
}
