package com.altnoir.mia.item.abs;

import com.altnoir.mia.init.MiaComponents;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public abstract class Whistle extends Item implements ICurioItem {
    private static Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers;
    public Whistle(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        int amount = 2 * stack.getComponents().getOrDefault(MiaComponents.WHISTLE_LEVEL.get(), 1);


        attributeModifiers = HashMultimap.create();
        attributeModifiers.put(
                Attributes.MAX_HEALTH,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE)
        );
        return attributeModifiers;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.get(MiaComponents.WHISTLE_LEVEL) == null) return;
        tooltipComponents.add(Component.translatable("tooltip.mia.whistle.level", stack.get(MiaComponents.WHISTLE_LEVEL.get())).withStyle(style -> style.withColor(ChatFormatting.GOLD)));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
