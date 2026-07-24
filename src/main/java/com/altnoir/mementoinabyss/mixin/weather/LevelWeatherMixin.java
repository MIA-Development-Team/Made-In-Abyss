package com.altnoir.mementoinabyss.mixin.weather;

import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelWeatherMixin {
    @Inject(method = "canHaveWeather", at = @At("HEAD"), cancellable = true)
    private void mia$disableWeather(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (MiaDimensions.isMiaDimension(level.dimension())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void mia$removeRainLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level level = (Level) (Object) this;
        if (MiaDimensions.isMiaDimension(level.dimension())) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void mia$removeThunderLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
        Level level = (Level) (Object) this;
        if (MiaDimensions.isMiaDimension(level.dimension())) {
            cir.setReturnValue(0.0F);
        }
    }
}
