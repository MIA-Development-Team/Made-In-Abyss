package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbstractAbilityCard;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HealthAbilityCard extends AbstractAbilityCard {
    public HealthAbilityCard(Properties properties) {
        super(properties);
    }

    @Override
    public int getWeight() {
        return 3;
    }

    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(ResourceLocation id) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        attributeModifiers.put(
                Attributes.MAX_HEALTH,
                new AttributeModifier(id, 4, AttributeModifier.Operation.ADD_VALUE)
        );
        return attributeModifiers;
    }
}
