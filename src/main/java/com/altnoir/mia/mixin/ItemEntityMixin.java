package com.altnoir.mia.mixin;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.AbyssGravity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "getDefaultGravity", at = @At("RETURN"), cancellable = true)
    private void injectGravity(CallbackInfoReturnable<Double> cir) {
        Entity entity = (Entity) (Object) this;

        if (AbyssGravity.isTheAbyssGravity(entity)) {
            double value = cir.getReturnValue();
            double gravity = AbyssGravity.getAbyssGravity(entity, value);
            cir.setReturnValue(gravity);
        } else if (AbyssGravity.isInvertedForest(entity)) {
            cir.setReturnValue(0.0);
        }
    }
}
