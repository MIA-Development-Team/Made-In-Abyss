package com.altnoir.mia.client.renderer;

import com.altnoir.mia.init.MiaTags;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TheAbyssFogRenderer {
    private static final Map<Long, Float> FOG_DENSITY_CACHE = new HashMap<>();

    // 过渡控制变量
    private static final int TRANSITION_RANGE = 16; // 过渡范围
    private static float lastFogStart = 0;
    private static float lastFogEnd = 192;
    private static long lastUpdateTime = 0;

    // 性能保护变量
    private static int samplesThisFrame = 0;
    private static long lastFrameTime = 0;
    private static final int MAX_SAMPLES_PER_FRAME = 8;

    public static boolean renderFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, float partialTick) {
        var player = camera.getEntity();
        var level = player.level();
        var fogData = new FogData(fogMode);

        float entityY = (float) player.getY();
        double entityX = player.getX();
        double entityZ = player.getZ();

        float maxY = 256.0F; // 雾起高度
        float minY = -16.0F; // 雾止高度

        float baseStart = farPlaneDistance - Mth.clamp(farPlaneDistance / 10.0F, 4.0F, 64.0F);
        float baseEnd = farPlaneDistance;

        float maxStart = farPlaneDistance * 0.05F;
        float maxEnd = Math.min(farPlaneDistance, 192.0F) * 0.5F;

        boolean fogSky = level.isRaining() || level.isThundering();
        var biome = level.getBiome(new BlockPos(BlockPos.containing(entityX, entityY, entityZ)));
        if (fogSky) {
            baseStart = 0.0F;
        } else if (biome.is(MiaTags.Biomes.THE_ABYSS_DENSE)) {
            baseStart = farPlaneDistance * 0.025F;
            baseEnd = farPlaneDistance * 0.25F;
            maxStart = farPlaneDistance * 0.0025F;
            maxEnd = Math.min(farPlaneDistance, 192.0F) * 0.125F;
        }

        // 根据高度计算雾强度
        float fogIntensity = Mth.clamp(1.0F - (entityY - minY) / (maxY - minY), 0.0F, 1.0F);

        // 获取平滑后的群系混合因子
        float biomeFactor = getSmoothedBiomeFactor(level, entityX, entityY, entityZ);
        float biomeFogStart = baseStart, biomeFogEnd = baseEnd;

        float mainStart = Mth.lerp(fogIntensity, baseStart, maxStart);     // 其他群系：根据高度变化的起始
        float mainEnd = Mth.lerp(fogIntensity, farPlaneDistance, maxEnd);   // 其他群系：根据高度变化的结束

        // 关键修复：混合因子逻辑
        // biomeFactor = 1: 全是无雾群系 → 使用noFog参数（稀薄雾）
        // biomeFactor = 0: 没有无雾群系 → 使用other参数（浓雾）
        fogData.start = Mth.lerp(biomeFactor, mainStart, biomeFogStart);
        fogData.end = Mth.lerp(biomeFactor, mainEnd, biomeFogEnd);

        // 时间平滑过渡（避免帧间跳变）
        long currentTime = System.currentTimeMillis();
        float timeDelta = (currentTime - lastUpdateTime) / 1000.0f; // 转换为秒
        float smoothFactor = Mth.clamp(timeDelta * 2.0f, 0, 1); // 0.5秒内完成过渡

        fogData.start = Mth.lerp(smoothFactor, lastFogStart, fogData.start);
        fogData.end = Mth.lerp(smoothFactor, lastFogEnd, fogData.end);

        // 更新缓存值
        lastFogStart = fogData.start;
        lastFogEnd = fogData.end;
        lastUpdateTime = currentTime;

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

    @OnlyIn(Dist.CLIENT)
    static class FogData {
        public final FogRenderer.FogMode mode;
        public float start;
        public float end;
        public FogShape shape = FogShape.SPHERE;

        public FogData(FogRenderer.FogMode mode) {
            this.mode = mode;
        }
    }

    private static float getSmoothedBiomeFactor(Level level, double x, double y, double z) {
        // 计算在过渡网格中的位置
        int gridX = Mth.floor(x / TRANSITION_RANGE);
        int gridZ = Mth.floor(z / TRANSITION_RANGE);

        // 获取四个角点的混合因子
        float[] factors = new float[4];
        factors[0] = calculateBiomeBlendFactor(level, gridX * TRANSITION_RANGE, y, gridZ * TRANSITION_RANGE);
        factors[1] = calculateBiomeBlendFactor(level, (gridX + 1) * TRANSITION_RANGE, y, gridZ * TRANSITION_RANGE);
        factors[2] = calculateBiomeBlendFactor(level, gridX * TRANSITION_RANGE, y, (gridZ + 1) * TRANSITION_RANGE);
        factors[3] = calculateBiomeBlendFactor(level, (gridX + 1) * TRANSITION_RANGE, y, (gridZ + 1) * TRANSITION_RANGE);

        // 计算在网格内的相对位置（0-1）
        float dx = (float) (x - gridX * TRANSITION_RANGE) / TRANSITION_RANGE;
        float dz = (float) (z - gridZ * TRANSITION_RANGE) / TRANSITION_RANGE;

        // 双线性插值
        float a = Mth.lerp(dx, factors[0], factors[1]);
        float b = Mth.lerp(dx, factors[2], factors[3]);

        return Mth.lerp(dz, a, b);
    }

    private static float calculateBiomeBlendFactor(Level level, double x, double y, double z) {
        // 性能保护：每帧重置采样计数器
        long currentTime = System.currentTimeMillis();
        if (currentTime != lastFrameTime) {
            samplesThisFrame = 0;
            lastFrameTime = currentTime;
        }
        // 如果超过每帧最大采样次数，返回默认值
        if (samplesThisFrame >= MAX_SAMPLES_PER_FRAME) {
            return 0.5f; // 默认值，表示混合状态
        }

        // 将世界坐标转换为区块坐标
        int chunkX = Mth.floor(x / 16);
        int chunkY = Mth.floor(y / 16);
        int chunkZ = Mth.floor(z / 16);
        long key = ((long) (chunkX & 0x1FFFFF) << 42)
                | ((long) (chunkZ & 0x1FFFFF) << 21)
                | (chunkY & 0x1FFFFFL);

        // 检查缓存
        if (FOG_DENSITY_CACHE.containsKey(key)) {
            return FOG_DENSITY_CACHE.get(key);
        }

        samplesThisFrame++; // 增加采样计数

        // 采样3x3区块区域
        int biomeFogCount = 0;
        int totalSamples = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = new BlockPos((chunkX + dx) * 16 + 8, (int) y, (chunkZ + dz) * 16 + 8);
                var biome = level.getBiome(pos);

                if (biome.is(MiaTags.Biomes.THE_ABYSS_CLEAR)) {
                    biomeFogCount++;
                }
                totalSamples++;
            }
        }

        // 计算比例
        float blendFactor = (float) biomeFogCount / totalSamples;

        // 存入缓存
        FOG_DENSITY_CACHE.put(key, blendFactor);

        // 限制缓存大小，避免内存泄漏
        if (FOG_DENSITY_CACHE.size() > 256) {
            Iterator<Map.Entry<Long, Float>> it = FOG_DENSITY_CACHE.entrySet().iterator();
            if (it.hasNext()) {
                it.remove();
            }
        }

        return blendFactor;
    }
}