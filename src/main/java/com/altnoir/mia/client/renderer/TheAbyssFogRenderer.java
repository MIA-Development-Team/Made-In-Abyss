package com.altnoir.mia.client.renderer;

import com.altnoir.mia.init.MiaTags;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;

public class TheAbyssFogRenderer {
    private static final int TRANSITION_RANGE = 16;

    private static float lastFogStart = -1;
    private static float lastFogEnd = -1;
    private static float lastGameTime = -1;

    private static int lastSampleGridX = Integer.MIN_VALUE;
    private static int lastSampleGridZ = Integer.MIN_VALUE;
    private static int lastSampleChunkY = Integer.MIN_VALUE;
    private static float lastClearFactor = 1;

    public static boolean renderFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, float partialTick) {
        var player = camera.getEntity();
        var level = player.level();
        var fogData = new FogData(fogMode);

        float entityY = (float) player.getY();
        double entityX = player.getX();
        double entityZ = player.getZ();

        float maxY = 256.0F;
        float minY = -16.0F;

        float thinFogStart = farPlaneDistance - Mth.clamp(farPlaneDistance / 10.0F, 4.0F, 64.0F);
        float thinFogEnd = farPlaneDistance;

        float thickFogStart = farPlaneDistance * 0.05F;
        float thickFogEnd = Math.min(farPlaneDistance, 192.0F) * 0.5F;

        boolean fogSky = level.isRaining() || level.isThundering();

        if (fogSky) {
            thinFogStart = 0.0F;
            thickFogStart = farPlaneDistance * 0.025F;
        }

        float heightFactor = Mth.clamp(1.0F - (entityY - minY) / (maxY - minY), 0.0F, 1.0F);

        int currentGridX = Mth.floor(entityX / TRANSITION_RANGE);
        int currentGridZ = Mth.floor(entityZ / TRANSITION_RANGE);
        int currentChunkY = Mth.floor(entityY / 16);

        float clearFactor;
        if (currentGridX == lastSampleGridX && currentGridZ == lastSampleGridZ && currentChunkY == lastSampleChunkY) {
            clearFactor = lastClearFactor;
        } else {
            clearFactor = getSmoothedClearFactor(level, entityX, entityY, entityZ);
            lastSampleGridX = currentGridX;
            lastSampleGridZ = currentGridZ;
            lastSampleChunkY = currentChunkY;
            lastClearFactor = clearFactor;
        }

        float effectiveHeightFactor = heightFactor * (1.0F - clearFactor);
        fogData.start = Mth.lerp(effectiveHeightFactor, thinFogStart, thickFogStart);
        fogData.end = Mth.lerp(effectiveHeightFactor, thinFogEnd, thickFogEnd);

        float currentGameTime = level.getGameTime() + partialTick;
        if (lastGameTime < 0 || lastFogStart < 0 || lastFogEnd < 0) {
            lastFogStart = fogData.start;
            lastFogEnd = fogData.end;
            lastGameTime = currentGameTime;
        } else {
            float timeDelta = currentGameTime - lastGameTime;
            lastGameTime = currentGameTime;

            float smoothFactor = Mth.clamp(timeDelta * 0.2f, 0.0f, 1.0f);

            fogData.start = Mth.lerp(smoothFactor, lastFogStart, fogData.start);
            fogData.end = Mth.lerp(smoothFactor, lastFogEnd, fogData.end);

            lastFogStart = fogData.start;
            lastFogEnd = fogData.end;
        }

        if (fogData.end >= farPlaneDistance) {
            fogData.end = farPlaneDistance;
            fogData.shape = FogShape.CYLINDER;
        } else {
            fogData.shape = FogShape.SPHERE;
        }

        RenderSystem.setShaderFogStart(fogData.start);
        RenderSystem.setShaderFogEnd(fogData.end);
        RenderSystem.setShaderFogShape(fogData.shape);
        ClientHooks.onFogRender(fogMode, camera.getFluidInCamera(), camera, partialTick, farPlaneDistance, fogData.start, fogData.end, fogData.shape);
        return true;
    }

    static class FogData {
        public final FogRenderer.FogMode mode;
        public float start;
        public float end;
        public FogShape shape = FogShape.SPHERE;

        public FogData(FogRenderer.FogMode mode) {
            this.mode = mode;
        }
    }

    private static float getSmoothedClearFactor(Level level, double x, double y, double z) {
        int gridX = Mth.floor(x / TRANSITION_RANGE);
        int gridZ = Mth.floor(z / TRANSITION_RANGE);

        float[] factors = new float[4];
        factors[0] = calculateClearBiomeRatio(level, gridX * TRANSITION_RANGE, y, gridZ * TRANSITION_RANGE);
        factors[1] = calculateClearBiomeRatio(level, (gridX + 1) * TRANSITION_RANGE, y, gridZ * TRANSITION_RANGE);
        factors[2] = calculateClearBiomeRatio(level, gridX * TRANSITION_RANGE, y, (gridZ + 1) * TRANSITION_RANGE);
        factors[3] = calculateClearBiomeRatio(level, (gridX + 1) * TRANSITION_RANGE, y, (gridZ + 1) * TRANSITION_RANGE);

        float dx = (float) (x - gridX * TRANSITION_RANGE) / TRANSITION_RANGE;
        float dz = (float) (z - gridZ * TRANSITION_RANGE) / TRANSITION_RANGE;

        float a = Mth.lerp(dx, factors[0], factors[1]);
        float b = Mth.lerp(dx, factors[2], factors[3]);

        return Mth.lerp(dz, a, b);
    }

    private static float calculateClearBiomeRatio(Level level, double x, double y, double z) {
        int chunkX = Mth.floor(x / 16);
        int chunkZ = Mth.floor(z / 16);
        int blockY = Mth.floor(y);
        int qy = QuartPos.fromBlock(blockY);

        int clearBiomeCount = 0;
        int totalSamples = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int blockX = (chunkX + dx) * 16 + 8;
                int blockZ = (chunkZ + dz) * 16 + 8;
                if (!level.hasChunkAt(blockX, blockZ)) {
                    continue;
                }
                int qx = QuartPos.fromBlock(blockX);
                int qz = QuartPos.fromBlock(blockZ);
                var biome = level.getNoiseBiome(qx, qy, qz);
                if (biome.is(MiaTags.Biomes.THE_ABYSS_CLEAR)) {
                    clearBiomeCount++;
                }
                totalSamples++;
            }
        }

        if (totalSamples == 0) {
            return 1.0f;
        }
        return (float) clearBiomeCount / totalSamples;
    }

    public static void clearCache() {
        lastFogStart = -1;
        lastFogEnd = -1;
        lastGameTime = -1;
        lastSampleGridX = Integer.MIN_VALUE;
        lastSampleGridZ = Integer.MIN_VALUE;
        lastSampleChunkY = Integer.MIN_VALUE;
        lastClearFactor = 1;
    }
}