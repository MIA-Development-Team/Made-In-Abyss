package com.altnoir.mia.mixin;

import com.altnoir.mia.core.AbyssGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Boat.class)
public class BoatMixin {
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
}
