package com.altnoir.mia.worldgen.structure.wall;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AbyssWallPlanConfig(
        int minY,
        int maxY,
        int candidateCount,
        int minAnchorDistance,
        int maxCorrection,
        int embedDepth,
        int attemptsPerBand,
        int salt,
        int contractVersion
) {
    public static final AbyssWallPlanConfig DEFAULT = new AbyssWallPlanConfig(
            -96, 125, 20, 32, 16, 3, 64, 0x4D494157, 2
    );

    public static final Codec<AbyssWallPlanConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(-2048, 2048).fieldOf("min_y").forGetter(AbyssWallPlanConfig::minY),
            Codec.intRange(-2048, 2048).fieldOf("max_y").forGetter(AbyssWallPlanConfig::maxY),
            Codec.intRange(1, 64).fieldOf("candidate_count").forGetter(AbyssWallPlanConfig::candidateCount),
            Codec.intRange(1, 1024).fieldOf("min_anchor_distance").forGetter(AbyssWallPlanConfig::minAnchorDistance),
            Codec.intRange(1, 256).fieldOf("max_correction").forGetter(AbyssWallPlanConfig::maxCorrection),
            Codec.intRange(0, 32).fieldOf("embed_depth").forGetter(AbyssWallPlanConfig::embedDepth),
            Codec.intRange(1, 256).fieldOf("attempts_per_band").forGetter(AbyssWallPlanConfig::attemptsPerBand),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("salt").forGetter(AbyssWallPlanConfig::salt),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("contract_version").forGetter(AbyssWallPlanConfig::contractVersion)
    ).apply(instance, AbyssWallPlanConfig::new));

    public AbyssWallPlanConfig {
        if (maxY < minY) {
            throw new IllegalArgumentException("maxY must be greater than or equal to minY");
        }
    }

    public int maxWallAnchorOffset() {
        return this.embedDepth;
    }

    public int planningAnchorDistance() {
        int maximumAnchorShift = (int) Math.ceil(
                this.maxCorrection + this.maxWallAnchorOffset() + Math.sqrt(2.0)
        );
        return this.minAnchorDistance + 2 * maximumAnchorShift;
    }
}
