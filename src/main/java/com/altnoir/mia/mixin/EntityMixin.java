package com.altnoir.mia.mixin;

import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract double getY();

    @Shadow
    public abstract Level level();

    @Shadow
    protected abstract void onBelowWorld();

    @Inject(method = "checkBelowWorld", at = @At("HEAD"))
    private void checkBelowWorld(CallbackInfo ci) {
        if (this.level().dimension() == MiaDimensions.THE_ABYSS_LEVEL && this.getY() > (double) (this.level().getMaxBuildHeight() + 64)) {
            this.onBelowWorld();
        }
    }
}
