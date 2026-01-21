package com.altnoir.mia.common.item.abs;

import com.altnoir.mia.init.MiaComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public abstract class AbsSkill extends Item implements IArtifactSkill {
    public AbsSkill(Properties properties) {
        super(properties.component(MiaComponents.SKILL_COOLDOWN.get(), 0).stacksTo(1));
    }
    @Override
    public ItemStack getItemStack() {
        return new ItemStack(this);
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
