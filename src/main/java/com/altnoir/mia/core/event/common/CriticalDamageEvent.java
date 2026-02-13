package com.altnoir.mia.core.event.common;

import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CriticalDamageEvent {
    public static float onLivingCriticalDamage(LivingEntity entity, DamageSource source, float damage) {
        double critChance = entity.getAttribute(MiaAttributes.CRITICAL_HIT).getValue();
        double critDamage = entity.getAttribute(MiaAttributes.CRITICAL_HIT_DAMAGE).getValue();

        if (source.getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal(critChance * 100 + "% 暴击率, 暴击伤害: " + critDamage));
        }

        if (critChance > 0 && entity.getRandom().nextDouble() < critChance) {
            return damage * (float) (critDamage + Math.max(0, critChance - 1.0));
        }
        return damage;
    }
}
