package com.altnoir.mia.common.item;

import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugAttributeTool extends Item {
    public DebugAttributeTool(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        double critChance = 0;
        double critDamage = 0;
        double damage = 0;

        double baseCritChance = 0;
        double baseCritDamage = 0;
        double baseDamage = 0;

        if (!level.isClientSide()) {
            critChance = getFinalAttributeValue(player, MiaAttributes.CRITICAL_HIT);
            critDamage = getFinalAttributeValue(player, MiaAttributes.CRITICAL_HIT_DAMAGE);
            damage = getFinalAttributeValue(player, Attributes.ATTACK_DAMAGE);

            baseCritChance = getBaseAttributeValue(player, MiaAttributes.CRITICAL_HIT);
            baseCritDamage = getBaseAttributeValue(player, MiaAttributes.CRITICAL_HIT_DAMAGE);
            baseDamage = getBaseAttributeValue(player, Attributes.ATTACK_DAMAGE);

            player.sendSystemMessage(
                    Component.literal("=== 最终属性信息 ===")
                            .withStyle(ChatFormatting.AQUA)
            );
            player.sendSystemMessage(
                    Component.literal("攻击力: " + String.format("%.2f", damage) + " (基础: " + String.format("%.2f", baseDamage) + ")")
                            .withStyle(ChatFormatting.GREEN)
            );
            player.sendSystemMessage(
                    Component.literal("暴击率: " + String.format("%.2f%%", critChance * 100) + " (基础: " + String.format("%.2f%%", baseCritChance * 100) + ")")
                            .withStyle(ChatFormatting.YELLOW)
            );
            player.sendSystemMessage(
                    Component.literal("暴击伤害: " + String.format("%.2fx", critDamage) + " (基础: " + String.format("%.2fx", baseCritDamage) + ")")
                            .withStyle(ChatFormatting.RED)
            );
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    /**
     * 获取属性的最终值（包含所有修饰符）
     */
    private double getFinalAttributeValue(Player player, Holder<Attribute> attribute) {
        if (player.getAttribute(attribute) != null) {
            return player.getAttribute(attribute).getValue();
        }
        return 0.0;
    }

    /**
     * 获取属性的基础值（不包含修饰符）
     */
    private double getBaseAttributeValue(Player player, Holder<Attribute> attribute) {
        if (player.getAttribute(attribute) != null) {
            return player.getAttribute(attribute).getBaseValue();
        }
        return 0.0;
    }
}
