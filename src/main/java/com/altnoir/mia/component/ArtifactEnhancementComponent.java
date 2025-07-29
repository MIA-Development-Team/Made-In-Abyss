package com.altnoir.mia.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.altnoir.mia.MIA;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ArtifactEnhancementComponent {
    public static final ArtifactEnhancementComponent EMPTY = new ArtifactEnhancementComponent(0, List.of());

    private final int level;
    private final List<ArtifactStat> modifiers;

    public ArtifactEnhancementComponent(int level, List<ArtifactStat> modifiers) {
        this.level = level;
        this.modifiers = modifiers;
    }

    public static final Codec<ArtifactEnhancementComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT
                    .fieldOf("level")
                    .forGetter(ArtifactEnhancementComponent::getLevel),
            Codec.list(ArtifactStat.CODEC).fieldOf("modifiers")
                    .forGetter(ArtifactEnhancementComponent::getArtifactStats))
            .apply(inst, ArtifactEnhancementComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactEnhancementComponent> STREAM_CODEC = ByteBufCodecs
            .fromCodecWithRegistries(CODEC);

    public int getLevel() {
        return this.level;
    }

    public List<ArtifactStat> getArtifactStats() {
        return this.modifiers;
    }

    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers() {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        for (ArtifactStat modifier : modifiers) {
            builder.put(modifier.attribute(), modifier.modifier());
        }
        return builder.build();
    }

    public ArtifactEnhancementComponent setLevel(int level) {
        return new ArtifactEnhancementComponent(this.level + 1, getArtifactStats());
    }

    public ArtifactEnhancementComponent addAttributeModifier(Holder<Attribute> attribute, double amount,
            AttributeModifier.Operation operation) {

        var newList = new ArrayList<ArtifactStat>(this.modifiers);
        ArtifactStat newStat = null;
        for (int i = 0; i < newList.size(); i++) {
            AttributeModifier oldModifier = newList.get(i).modifier;
            if (newList.get(i).attribute.equals(attribute) && oldModifier.operation().equals(operation)) {
                AttributeModifier newModifier = new AttributeModifier(oldModifier.id(), oldModifier.amount() + amount,
                        oldModifier.operation());
                newStat = new ArtifactStat(newList.get(i).attribute, newModifier);
                newList.remove(i);
            }
        }
        if (newStat != null) {
            newList.add(newStat);
        } else {
            newList.add(new ArtifactStat(attribute, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, getModifierKey(attribute, operation)),
                    amount,
                    operation)));
        }
        return new ArtifactEnhancementComponent(this.getLevel(), newList);
    }

    public static String getModifierKey(Holder<Attribute> attribute, AttributeModifier.Operation operation) {
        String attr = attribute.unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("unknown");
        String op = operation.name().toLowerCase();
        return String.format("%s.%s", attr, op);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ArtifactEnhancementComponent that))
            return false;
        return level == that.level
                && Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, modifiers);
    }

    @Override
    public String toString() {
        return "ArtifactEnhancementComponent[level=" + level + ", modifiers=" + modifiers + "]";
    }

    public record ArtifactStat(Holder<Attribute> attribute, AttributeModifier modifier) {
        public static final Codec<ArtifactStat> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Attribute.CODEC.fieldOf("attribute").forGetter(ArtifactStat::attribute),
                AttributeModifier.CODEC.fieldOf("modifier").forGetter(ArtifactStat::modifier))
                .apply(inst, ArtifactStat::new));

    }
}
