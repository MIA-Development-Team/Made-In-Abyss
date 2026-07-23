package com.altnoir.mementoinabyss.content.artifact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ArtifactAttribute(Holder<Attribute> attribute, AttributeModifier modifier) {
    public static final Codec<ArtifactAttribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(ArtifactAttribute::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(ArtifactAttribute::modifier)
    ).apply(instance, ArtifactAttribute::new));

    public ArtifactAttribute(
            Holder<Attribute> attribute,
            Identifier modifierId,
            double amount,
            AttributeModifier.Operation operation
    ) {
        this(attribute, new AttributeModifier(modifierId, amount, operation));
    }

}
