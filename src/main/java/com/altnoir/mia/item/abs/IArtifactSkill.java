package com.altnoir.mia.item.abs;

import com.altnoir.mia.init.MiaComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IArtifactSkill extends IArtifactItem {
    /**
     * 获取组合键序列
     * 0 = 上, 1 = 下, 2 = 左, 3 = 右
     */
    List<Integer> getComboSequence();

    /**
     * 当组合键成功输入后执行的技能
     */
    void serverSkillPlay(Level level, Player player);

    /**
     * 获取技能物品的 ItemStack 实例
     */
    ItemStack getItemStack();

    /**
     * 检查技能是否在冷却中
     */
    default boolean isOnCooldown(ItemStack stack) {
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        return cooldownValue != null && cooldownValue > 0;
    }

    /**
     * 设置技能冷却时间
     */
    default void setCooldown(ItemStack stack) {
        stack.set(MiaComponents.SKILL_COOLDOWN.get(), getCooldownTicks());
    }

    /**
     * 获取剩余冷却时间
     */
    default long getRemainingCooldown(ItemStack stack) {
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        return cooldownValue != null ? cooldownValue : 0;
    }

    /**
     * 获取技能冷却时间（tick）
     * 默认冷却时间为300 ticks（15秒）
     */
    default int getCooldownTicks() {
        return 300;
    }
}
