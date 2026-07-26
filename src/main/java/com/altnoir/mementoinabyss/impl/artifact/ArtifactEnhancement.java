package com.altnoir.mementoinabyss.impl.artifact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

public record ArtifactEnhancement(int level, List<ArtifactAttribute> attributes) {
    public static final ArtifactEnhancement EMPTY = new ArtifactEnhancement(0, List.of());
    public static final Codec<ArtifactEnhancement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("level").forGetter(ArtifactEnhancement::level),
            ArtifactAttribute.CODEC.listOf().fieldOf("attributes").forGetter(ArtifactEnhancement::attributes)
    ).apply(instance, ArtifactEnhancement::new));

    public ArtifactEnhancement {
        if (level < 0) {
            throw new IllegalArgumentException("Artifact enhancement level cannot be negative");
        }
        attributes = List.copyOf(attributes);
    }

    public ArtifactEnhancement withLevel(int newLevel) {
        return new ArtifactEnhancement(newLevel, attributes);
    }

    public ArtifactEnhancement addAttribute(
            Holder<Attribute> attribute,
            Identifier modifierId,
            double amount,
            AttributeModifier.Operation operation
    ) {
        List<ArtifactAttribute> updated = new ArrayList<>(attributes);
        for (int i = 0; i < updated.size(); i++) {
            ArtifactAttribute existing = updated.get(i);
            if (existing.attribute().equals(attribute) && existing.modifier().operation() == operation) {
                updated.set(i, new ArtifactAttribute(
                        attribute,
                        existing.modifier().id(),
                        existing.modifier().amount() + amount,
                        operation
                ));
                return new ArtifactEnhancement(level, updated);
            }
        }
        updated.add(new ArtifactAttribute(attribute, modifierId, amount, operation));
        return new ArtifactEnhancement(level, updated);
    }
}
