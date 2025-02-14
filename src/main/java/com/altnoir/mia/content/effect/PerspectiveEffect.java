package com.altnoir.mia.content.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PerspectiveEffect extends ModEffect {
    public PerspectiveEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

