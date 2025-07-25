package com.altnoir.mia.item;

import java.util.ArrayList;
import java.util.List;

import com.altnoir.mia.item.abs.AbstractArtifact;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SimpleStatsArtifact extends AbstractArtifact {
    private int weight;
    private final List<ArtifactStat> modifiers;

    public SimpleStatsArtifact(Builder builder) {
        super(builder.props);
        this.weight = builder.weight;
        this.modifiers = builder.mods;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        int rlId = 0;
        for (ArtifactStat stat : modifiers) {
            ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    String.format("%s.%d", id.getPath(), rlId++));
            attributeModifiers.put(
                    stat.attribute,
                    new AttributeModifier(uniqueId, stat.amount, stat.operation));
        }

        return attributeModifiers;
    }

    public static class Builder {
        private final Properties props;
        private int weight;
        private final List<ArtifactStat> mods = new ArrayList<>();

        public Builder(Properties props) {
            this.props = props;
        }

        public Builder weight(int w) {
            this.weight = w;
            return this;
        }

        public Builder addModifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
            mods.add(new ArtifactStat(attribute, amount, operation));
            return this;
        }

        public SimpleStatsArtifact build() {
            return new SimpleStatsArtifact(this);
        }

    }

    public record ArtifactStat(
            Holder<Attribute> attribute,
            double amount,
            AttributeModifier.Operation operation) {
    }

}
