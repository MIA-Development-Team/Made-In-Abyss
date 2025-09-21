package com.altnoir.mia.item.abs;

import com.altnoir.mia.init.MiaComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IArtifactSkill extends IArtifactItem {
    public static final String TOOLTIP_SKILL_COOLDOWN_VALUE = "tooltip.mia.skill.cooldown_value";
    /**
     * 获取组合键序列
     * 0 = 上, 1 = 下, 2 = 左, 3 = 右
     */
    List<Integer> getComboSequence();

    /**
     * 获取技能物品的 ItemStack 实例
     */
    ItemStack getItemStack();

    /**
     * 当组合键成功输入后执行的技能
     */
    default void serverSkillPlay(Level level, Player player) {}

    /**
     * 获取技能冷却时间（tick）
     * 默认冷却时间为300 ticks（15秒）
     */
    default int cooldownTicks() {
        return 300;
    }

    /**
     * 设置技能冷却时间
     */
    default void setCooldown(ItemStack stack) {
        stack.set(MiaComponents.SKILL_COOLDOWN.get(), cooldownTicks());
    }
    /**
     * 显示技能冷却时间
     */
    @Override
    default void appendTooltip(ItemStack stack, List<Component> tooltip) {
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        if (cooldownValue != null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.options.advancedItemTooltips) {
                tooltip.add(2, Component.translatable(TOOLTIP_SKILL_COOLDOWN_VALUE, cooldownValue).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        IArtifactItem.super.appendTooltip(stack, tooltip);
    }
}
