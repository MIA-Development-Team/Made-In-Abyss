package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbsWhistle;
import com.altnoir.mia.util.MiaUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SimpleWhistle extends AbsWhistle {
    private final int artifactSlotCount;
    private final Holder<Attribute> attribute;
    private final AttributeModifier attributeModifier;

    public static final ResourceLocation WHISTLE_ATTRIBUTE = MiaUtil.miaId("whistle_attribute");

    public SimpleWhistle(Properties properties, int artifactSlotCount, double amount) {
        super(properties.stacksTo(1));
        this.artifactSlotCount = artifactSlotCount;
        this.attributeModifier = new AttributeModifier(WHISTLE_ATTRIBUTE, amount, AttributeModifier.Operation.ADD_VALUE);
        this.attribute = Attributes.MAX_HEALTH;
    }

    @Override
    public int getArtifactSlotCount() {
        return artifactSlotCount;
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(attribute, attributeModifier);
        return builder.build();
    }
}
