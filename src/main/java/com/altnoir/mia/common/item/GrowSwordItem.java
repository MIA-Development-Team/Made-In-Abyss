package com.altnoir.mia.common.item;

import com.altnoir.mia.common.item.abs.IMiaTooltip;
import com.altnoir.mia.init.MiaComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class GrowSwordItem extends SwordItem implements IMiaTooltip {
    public GrowSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    /**
     * 更新物品属性（使用生物死亡事件）
     *
     * @param stack 物品栈
     */
    public void updateAttributes(ItemStack stack) {
        int killCount = getKillCount(stack);

        // 计算总伤害加成：击杀数越高，每次击杀增加的伤害越少
        double damageBonus = calculateDamageBonus(killCount);

        ItemAttributeModifiers newModifiers = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                SwordItem.BASE_ATTACK_DAMAGE_ID,
                                this.getTier().getAttackDamageBonus() + damageBonus,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(SwordItem.BASE_ATTACK_SPEED_ID, -2.4F, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, newModifiers);
    }

    /**
     * @param killCount 击杀数
     * @return 伤害加成总值
     */
    private double calculateDamageBonus(int killCount) {
        if (killCount <= 0) {
            return 0;
        }
        // 等差数列[1, 2, 3, ... , n]
        // 使用数学公式计算等级n：满足 n*(n+1)/2 <= killCount 的最大整数n
        // 由二次方程 n^2 + n - 2*killCount <= 0 得：
        // n = floor( (sqrt(1+8*killCount) - 1) / 2 )
        return Math.floor((Math.sqrt(1 + 8.0 * killCount) - 1) / 2);
    }

    /**
     * 获取击杀计数
     *
     * @param stack 物品栈
     * @return 击杀计数
     */
    private int getKillCount(ItemStack stack) {
        var component = stack.get(MiaComponents.KILL_COUNT);
        return component != null ? component : 0;
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        int killCount = getKillCount(stack);
        if (killCount > 0) {
            tooltip.add(1,Component.literal("§c击杀数: " + killCount));

            double damageBonus = calculateDamageBonus(killCount);
            tooltip.add(2,Component.literal("§a伤害加成: +" + String.format("%.1f", damageBonus)));
        } else {
            tooltip.add(1,Component.literal("§c击杀数: 0"));
        }
        IMiaTooltip.super.appendTooltip(stack, tooltip);
    }
}