package com.altnoir.mia.item;

import com.altnoir.mia.MIA;
import com.altnoir.mia.item.abs.AbstractWhistle;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SimpleWhistle extends AbstractWhistle {
    private final int artifactSlotCount;
    private final Holder<Attribute> attribute;
    private final AttributeModifier attributeModifier;

    public static final ResourceLocation WHISTLE_ATTRIBUTE = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID,
            "whistle_attribute");

    public SimpleWhistle(Properties properties, int artifactSlotCount, Holder<Attribute> attribute, double amount,
            AttributeModifier.Operation operation) {
        super(properties.stacksTo(1));
        this.artifactSlotCount = artifactSlotCount;
        this.attributeModifier = new AttributeModifier(WHISTLE_ATTRIBUTE, amount, operation);
        this.attribute = attribute;
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
