package com.altnoir.mia.core.event.common;

import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CriticalDamageEvent {
    public static float onLivingCriticalDamage(LivingEntity entity, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            var chance = player.getAttributeValue(MiaAttributes.CRITICAL_HIT);
            var baseDamage = player.getAttributeValue(MiaAttributes.CRITICAL_HIT_DAMAGE);

            if (chance > 0 && entity.getRandom().nextDouble() < chance) {
                return damage * (float) (baseDamage + Math.max(0, chance - 1.0));
            }
        }
        return damage;
    }
}
