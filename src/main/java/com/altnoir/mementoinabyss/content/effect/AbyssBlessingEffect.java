package com.altnoir.mementoinabyss.content.effect;

import com.altnoir.mementoinabyss.impl.curse.CurseAttachment;
import com.altnoir.mementoinabyss.init.MiaDataAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AbyssBlessingEffect extends MobEffect {
    public AbyssBlessingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplification) {
        if (entity instanceof Player player) {
            refreshCurseFloor(player);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplification) {
        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            refreshCurseFloor(player);
        }
    }

    private static void refreshCurseFloor(Player player) {
        CurseAttachment curse = player.getData(MiaDataAttachments.CURSE);
        curse.setMinY((int) player.getY());
        curse.setLevel(0);
    }
}
