package com.altnoir.mia.mixin;

import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogRenderer.class)
public class FogRendererMixin {
    @Unique
    private static float mia$start;
    @Unique
    private static float mia$end;
    @Unique
    private static FogShape mia$shape = FogShape.SPHERE;

    @Inject(method = "setupFog", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 0,
            shift = At.Shift.AFTER),
            cancellable = true)
    private static void setupFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        var player = camera.getEntity();
        var level = player.level();

        if (level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)) {
            float entityY = (float) player.getY();

            float maxY = 256.0F; // 雾起高度
            float minY = -16.0F; // 雾止高度

            boolean fogSky = level.isRaining() || level.isThundering();
            float start = fogSky ? 0.0F : farPlaneDistance - Mth.clamp(farPlaneDistance / 10.0F, 4.0F, 64.0F);

            boolean noFogBiome = level.getBiome(player.blockPosition()).is(MiaBiomes.TEMPTATION_FOREST);
            float maxStart = noFogBiome ? start : farPlaneDistance * 0.05F;
            float maxEnd = noFogBiome ? farPlaneDistance : Math.min(farPlaneDistance, 192.0F) * 0.5F;

            float fogIntensity = Mth.clamp(1.0F - (entityY - minY) / (maxY - minY), 0.0F, 1.0F);
            mia$start = Mth.lerp(fogIntensity, start, maxStart);
            mia$end = Mth.lerp(fogIntensity, farPlaneDistance, maxEnd);
            if (mia$end >= farPlaneDistance) {
                mia$end = farPlaneDistance;
                mia$shape = FogShape.CYLINDER;
            }

            RenderSystem.setShaderFogStart(mia$start);
            RenderSystem.setShaderFogEnd(mia$end);
            RenderSystem.setShaderFogShape(mia$shape);
            ClientHooks.onFogRender(fogMode, camera.getFluidInCamera(), camera, partialTick, farPlaneDistance, mia$start, mia$end, mia$shape);

            ci.cancel();
        }
    }
}