package com.altnoir.mia.mixin;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Mob.class)
public class MobMixin {
    @Redirect(
            method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean redirectHurt(Entity entity, DamageSource damageSource, float damage) {
        // TODO 暴击和暴伤无法检测
        try {
            double critChance = ((Mob) (Object) this).getAttributeValue(MiaAttributes.CRITICAL_HIT);
            double critDamage = ((Mob) (Object) this).getAttributeValue(MiaAttributes.CRITICAL_HIT_DAMAGE);

            MIA.LOGGER.info(critChance * 100 + "% 暴击率, 暴击伤害: " + critDamage);

            if (critChance > 0 && ((Mob) (Object) this).getRandom().nextDouble() < critChance) {
                float newDamage = damage * (float) (critDamage + Math.max(0, critChance - 1.0));
                return entity.hurt(damageSource, newDamage);
            }
        } catch (Exception e) {
            MIA.LOGGER.error(String.valueOf(e));
        }

        return entity.hurt(damageSource, damage);
    }
}
