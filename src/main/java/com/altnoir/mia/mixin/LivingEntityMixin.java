package com.altnoir.mia.mixin;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.AbyssGravity;
import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getDefaultGravity", at = @At("RETURN"), cancellable = true)
    private void injectGravity(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;

        if (AbyssGravity.isTheAbyssGravity(entity)) {
            double value = cir.getReturnValue();
            double gravity = AbyssGravity.getAbyssGravity(entity, value);
            cir.setReturnValue(gravity);
        } else if (AbyssGravity.isInvertedForest(entity)) {
            cir.setReturnValue(cir.getReturnValue() * 0.5);
        }
    }

    @Inject(method = "createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", at = @At("RETURN"))
    private static void injectAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue()
                .add(MiaAttributes.CRITICAL_HIT)
                .add(MiaAttributes.CRITICAL_HIT_DAMAGE);
    }
}
