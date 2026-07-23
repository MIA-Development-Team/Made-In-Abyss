package com.altnoir.mementoinabyss.content.artifact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ArtifactProfile(ArtifactGrade grade, int weight, int maxEnhancementLevel) {
    public static final Codec<ArtifactProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ArtifactGrade.CODEC.fieldOf("grade").forGetter(ArtifactProfile::grade),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ArtifactProfile::weight),
            Codec.intRange(0, Integer.MAX_VALUE)
                    .fieldOf("max_enhancement_level")
                    .forGetter(ArtifactProfile::maxEnhancementLevel)
    ).apply(instance, ArtifactProfile::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactProfile> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ArtifactProfile {
        if (weight < 0) {
            throw new IllegalArgumentException("Artifact weight cannot be negative");
        }
        if (maxEnhancementLevel < 0) {
            throw new IllegalArgumentException("Artifact max enhancement level cannot be negative");
        }
    }

    public static ArtifactProfile of(ArtifactGrade grade, int weight) {
        return new ArtifactProfile(grade, weight, grade.defaultMaxEnhancementLevel());
    }
}
