package com.altnoir.mia.item.abs;

import com.altnoir.mia.component.WhistleInventoryComponent;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.IMiaTooltip;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Objects;

public abstract class AbstractWhistle extends Item implements ICurioItem, IMiaTooltip {
    private static Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers;

    public AbstractWhistle(Properties properties) {
        super(properties.component(MiaComponents.WHISTLE_INVENTORY, WhistleInventoryComponent.EMPTY));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        int amount = 2 * stack.getComponents().getOrDefault(MiaComponents.WHISTLE_LEVEL.get(), 1);
        var contents = stack.getOrDefault(MiaComponents.WHISTLE_INVENTORY, WhistleInventoryComponent.EMPTY);
        var abilityCards = contents.allItems();
        attributeModifiers = HashMultimap.create();
        attributeModifiers.put(
                Attributes.MAX_HEALTH,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE)
        );

        for (ItemStack cardStack : abilityCards) {
            if (cardStack.getItem() instanceof AbstractAbilityCard abilityCard) {
                attributeModifiers.putAll(abilityCard.getAttributeModifiers(id));
            }
        }

        return attributeModifiers;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {return true;}

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.get(MiaComponents.WHISTLE_LEVEL) == null) return;
        tooltip.add(1,
                Component.translatable("tooltip.mia.whistle.level",
                                Component.literal(
                                                Objects.requireNonNull(stack.get(MiaComponents.WHISTLE_LEVEL.get())).toString())
                                        .withStyle(ChatFormatting.YELLOW))
                        .withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        tooltip.add(3, Component.translatable("tooltip.mia.item.red_whistle").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
    }

    public int getCapacity() {
        return 4;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        var contents = itemStack.getOrDefault(MiaComponents.WHISTLE_INVENTORY, WhistleInventoryComponent.EMPTY);
        var mutable = contents.mutable();
        var other = slot.getItem();
        if (other.isEmpty()) {
            var popped = mutable.pop();
            if (popped.isEmpty()) return false;
            slot.set(popped);
            playRemoveOneSound(player);
        } else {
            if (!mutable.tryInsert(itemStack, other)) {
                return false;
            }
            playInsertSound(player);
            slot.set(ItemStack.EMPTY);
        }
        itemStack.set(MiaComponents.WHISTLE_INVENTORY, mutable.immutable());
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack itemStack,
            ItemStack other,
            Slot slot,
            ClickAction clickAction,
            Player player,
            SlotAccess slotAccess
    ) {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        var contents = itemStack.getOrDefault(MiaComponents.WHISTLE_INVENTORY, WhistleInventoryComponent.EMPTY).mutable();
        if (other.isEmpty()) {
            var stack = contents.pop();
            if (stack.isEmpty()) return false;
            slotAccess.set(itemStack);
            playRemoveOneSound(player);
            broadcastChangesOnContainerMenu(player);
        } else {
            if (!contents.tryInsert(itemStack, other)) return false;
            playInsertSound(player);
            broadcastChangesOnContainerMenu(player);
            slotAccess.set(ItemStack.EMPTY);
        }
        itemStack.set(MiaComponents.WHISTLE_INVENTORY, contents.immutable());
        return true;
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void broadcastChangesOnContainerMenu(Player player) {
        player.containerMenu.slotsChanged(player.getInventory());
    }
}
