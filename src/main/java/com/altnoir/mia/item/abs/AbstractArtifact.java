package com.altnoir.mia.item.abs;

import com.altnoir.mia.item.IMiaTooltip;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public abstract class AbstractArtifact extends Item implements ICurioItem, IMiaTooltip {
    // private static Multimap<Holder<Attribute>, AttributeModifier>
    // attributeModifiers;

    public AbstractArtifact(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(1,
                Component
                        .translatable("tooltip.mia.artifact.weight", Component
                                .literal(Integer.toString(getWeight()))
                                .withStyle(ChatFormatting.YELLOW))
                        .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tooltip.add(2, Component.translatable(String.format("tooltip.mia.artifact.%s", rl.getPath()))
                .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
    }

    public abstract int getWeight();

}
