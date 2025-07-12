package com.altnoir.mia.item.abs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

public abstract class AbstractAbilityCard extends Item {
    public AbstractAbilityCard(Properties properties) {
        super(properties);
    }

    public int getWeight() {
        return 1;
    }

    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(ResourceLocation id) {
        return HashMultimap.create();
    }
}
