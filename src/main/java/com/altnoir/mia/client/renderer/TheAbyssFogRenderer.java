package com.altnoir.mia.client.renderer;

import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.ClientHooks;

public class TheAbyssFogRenderer {
    public static boolean renderFog(Camera camera, float farPlaneDistance) {
        var player = camera.getEntity();
        var level = player.level();

        if (level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)) {
            float entityY = (float) player.getY();

            float maxY = 256.0F; // 雾起高度
            float minY = -16.0F; // 雾止高度

            boolean fogSky = level.isRaining() || level.isThundering();
            float start = fogSky ? 0.0F : farPlaneDistance - Mth.clamp(farPlaneDistance / 10.0F, 4.0F, 64.0F);

            float maxStart = farPlaneDistance * 0.05F;
            float maxEnd = Math.min(farPlaneDistance, 192.0F) * 0.5F;

            float fogIntensity = Mth.clamp(1.0F - (entityY - minY) / (maxY - minY), 0.0F, 1.0F);

            float mia$start, mia$end;
            if (level.getBiome(player.blockPosition()).is(MiaBiomes.TEMPTATION_FOREST)) {
                mia$start = start;
                mia$end = farPlaneDistance;
            } else {
                mia$start = Mth.lerp(fogIntensity, start, maxStart);
                mia$end = Mth.lerp(fogIntensity, farPlaneDistance, maxEnd);
            }

            RenderSystem.setShaderFogStart(mia$start);
            RenderSystem.setShaderFogEnd(mia$end);

            return true;
        }
        return false;
    }
}
