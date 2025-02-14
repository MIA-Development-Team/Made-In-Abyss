package com.altnoir.mia.content.items;

import com.altnoir.mia.SoundsRegister;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.altnoir.mia.client.entity.KnifeEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KnifeItem extends Item {
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public KnifeItem(Properties p_41383_) {
        super(p_41383_);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -1, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND)
            return attributeModifiers;
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        int knivesToThrow = player.isShiftKeyDown() ? handStack.getCount() : 1;
        if (!level.isClientSide()) {
            for (int i = 0; i < knivesToThrow; i++) {
                KnifeEntity knifeEntity = KnifeEntity.create(level, player);
                knifeEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, i == 0 ? 1.0F : 28.0F);
                level.addFreshEntity(knifeEntity);
            }
            if (!player.isCreative())
                handStack.shrink(knivesToThrow);
        }
        player.playSound(SoundsRegister.THROW.get());
        player.swing(hand);
        return super.use(level, player, hand);
    }
}