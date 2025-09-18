package com.altnoir.mia.item.abs;

import com.altnoir.mia.init.MiaComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public abstract class ASkill extends Item implements IArtifactSkill {
    public ASkill(Properties properties) {
        super(properties.component(MiaComponents.SKILL_COOLDOWN.get(), 0));
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(this);
    }

    @Override
    public void appendTooltip(ItemStack stack, List<Component> tooltip) {
        // 显示技能冷却时间组件的int值
        Integer cooldownValue = stack.get(MiaComponents.SKILL_COOLDOWN.get());
        if (cooldownValue != null) {
            tooltip.add(1, Component.translatable("tooltip.mia.skill.cooldown_value", cooldownValue));
        }

        IArtifactSkill.super.appendTooltip(stack, tooltip);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide()) {
            Integer cooldown = stack.get(MiaComponents.SKILL_COOLDOWN.get());
            if (cooldown != null && cooldown > 0) {
                stack.set(MiaComponents.SKILL_COOLDOWN.get(), Math.max(0, cooldown - 1));
            }
        }
        IArtifactSkill.super.curioTick(slotContext, stack);
    }
}
