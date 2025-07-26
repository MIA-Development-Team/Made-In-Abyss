package com.altnoir.mia.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class WhistleStatsComponent {
    public static final WhistleStatsComponent EMPTY = new WhistleStatsComponent(0, List.of());

    private final int level;
    private final List<WhistleStat> modifiers;

    public WhistleStatsComponent(int level, List<WhistleStat> modifiers) {
        this.level = level;
        this.modifiers = modifiers;
    }

    public static final Codec<WhistleStatsComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT
                    .fieldOf("level")
                    .forGetter(WhistleStatsComponent::getLevel),
            Codec.list(WhistleStat.CODEC).fieldOf("modifiers")
                    .forGetter(WhistleStatsComponent::getWhistleStats))
            .apply(inst, WhistleStatsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WhistleStatsComponent> STREAM_CODEC = ByteBufCodecs
            .fromCodecWithRegistries(CODEC);

    public int getLevel() {
        return this.level;
    }

    public List<WhistleStat> getWhistleStats() {
        return this.modifiers;
    }

    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers() {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        for (WhistleStat modifier : modifiers) {
            builder.put(modifier.attribute(), modifier.modifier());
        }
        return builder.build();
    }

    public WhistleStatsComponent setLevel(int level) {
        return new WhistleStatsComponent(this.level + 1, getWhistleStats());
    }

    public WhistleStatsComponent addAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {

        var newList = new ArrayList<WhistleStat>(this.modifiers);
        newList.add(new WhistleStat(attribute, modifier));
        return new WhistleStatsComponent(this.getLevel(), newList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WhistleStatsComponent that))
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
        return "WhistleStatsComponent[level=" + level + ", modifiers=" + modifiers + "]";
    }

    public record WhistleStat(Holder<Attribute> attribute, AttributeModifier modifier) {
        public static final Codec<WhistleStat> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Attribute.CODEC.fieldOf("attribute").forGetter(WhistleStat::attribute),
                AttributeModifier.CODEC.fieldOf("modifier").forGetter(WhistleStat::modifier))
                .apply(inst, WhistleStat::new));

    }
}
