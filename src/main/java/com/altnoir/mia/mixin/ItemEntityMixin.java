package com.altnoir.mia.mixin;

import com.altnoir.mia.common.gravity.AbyssGravity;
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

        if (AbyssGravity.isTheAbyssGravity(entity) || AbyssGravity.isInvertedForest(entity)) {
            cir.setReturnValue(-cir.getReturnValue());
        }
    }
}
