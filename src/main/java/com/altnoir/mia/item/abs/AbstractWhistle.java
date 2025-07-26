package com.altnoir.mia.item.abs;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.IMiaTooltip;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;

public abstract class AbstractWhistle extends Item implements ICurioItem, IMiaTooltip {

    public AbstractWhistle(Properties properties) {
        super(properties.component(MiaComponents.WHISTLE_LEVEL, 1));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        int level = stack.getComponents().getOrDefault(MiaComponents.WHISTLE_LEVEL.get(), 1);
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = LinkedHashMultimap.create();
        attributeModifiers.put(Attributes.MAX_HEALTH,
                new AttributeModifier(id, level * 2, AttributeModifier.Operation.ADD_VALUE));
        return attributeModifiers;

    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            System.out.println("add");
            curiosInventory.addPermanentSlotModifier("artifact",
                    ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "whistle_artifact_slot"),
                    GetArtifactSlotCount(),
                    AttributeModifier.Operation.ADD_VALUE);
        });
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            System.out.println("remove");
            curiosInventory.removeSlotModifier("artifact",
                    ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "whistle_artifact_slot"));
        });
    }

    public abstract int GetArtifactSlotCount();

    public abstract int GetMaxLevel();

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(1, Component.translatable("tooltip.mia.whistle.level", Component
                .literal(Optional
                        .ofNullable(stack.get(MiaComponents.WHISTLE_LEVEL.get()))
                        .map(Object::toString)
                        .orElse("0"))
                .withStyle(ChatFormatting.YELLOW))
                .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tooltip.add(2, Component.translatable(String.format("tooltip.mia.whistle.%s", rl.getPath()))
                .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
    }
}
