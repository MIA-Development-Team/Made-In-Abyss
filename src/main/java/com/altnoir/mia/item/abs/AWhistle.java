package com.altnoir.mia.item.abs;

import com.altnoir.mia.MIA;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;

public abstract class AWhistle extends Item implements ICurioItem, IMiaTooltip {

    public AWhistle(Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            curiosInventory.addPermanentSlotModifier("artifact",
                    ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "whistle_artifact_slot"),
                    getArtifactSlotCount(),
                    AttributeModifier.Operation.ADD_VALUE);
        });
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        Optional<ICuriosItemHandler> maybeCuriosInventory = CuriosApi.getCuriosInventory(slotContext.entity());
        maybeCuriosInventory.ifPresent(curiosInventory -> {
            curiosInventory.removeSlotModifier("artifact",
                    ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "whistle_artifact_slot"));
        });
    }

    public abstract int getArtifactSlotCount();

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        IMiaTooltip.super.appendTooltip(stack, tooltip);
    }
}
