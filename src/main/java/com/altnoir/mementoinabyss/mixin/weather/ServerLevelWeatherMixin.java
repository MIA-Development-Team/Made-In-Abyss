package com.altnoir.mementoinabyss.mixin.weather;

import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherMixin {
    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void mia$keepWeatherClear(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (MiaDimensions.isMiaDimension(level.dimension())) {
            level.setRainLevel(0.0F);
            level.setThunderLevel(0.0F);
            ci.cancel();
        }
    }
}
