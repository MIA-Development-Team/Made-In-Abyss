package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbsSkill;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

public class HasteSkill extends AbsSkill {
    public HasteSkill(Properties properties) {
        super(properties);
    }

    @Override
    public List<Integer> getComboSequence() {
        return Arrays.asList(0, 0, 0);
    }

    @Override
    public void serverSkillPlay(Level level, Player player) {
        if (player.level().isClientSide) return;
        // 检查玩家是否已有急迫效果
        MobEffectInstance hasteEffect = player.getEffect(MobEffects.DIG_SPEED);

        if (hasteEffect == null) {
            // 如果没有急迫效果，添加急迫II，持续3分钟
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 3600, 1));
        } else {
            // 如果已有急迫效果，延长3分钟
            int newDuration = hasteEffect.getDuration() + 3600;
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, newDuration, hasteEffect.getAmplifier()));
        }

        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 0.5F, 1.0F);
    }

    @Override
    public Grade getGrade() {
        return Grade.A;
    }

    @Override
    public int cooldownTicks() {
        return 7200;
    }
}
