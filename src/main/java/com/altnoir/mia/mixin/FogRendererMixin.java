package com.altnoir.mia.mixin;

import com.altnoir.mia.client.renderer.TheAbyssFogRenderer;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogRenderer.class)
public class FogRendererMixin {
    @Inject(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 0,
            shift = At.Shift.AFTER),
            cancellable = true)
    private static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        var dim = camera.getEntity().level().dimension();

        if (dim.equals(MiaDimensions.THE_ABYSS_LEVEL)) {
            if (TheAbyssFogRenderer.renderFog(camera, fogMode, farPlaneDistance, partialTick)) {
                ci.cancel();
            }
        }
    }
}