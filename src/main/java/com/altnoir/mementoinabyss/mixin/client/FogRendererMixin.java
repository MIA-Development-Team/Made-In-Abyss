package com.altnoir.mementoinabyss.mixin.client;

import com.altnoir.mementoinabyss.client.render.sky.OverworldAbyssSky;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyExpressionValue(
            method = "computeFogColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float mia$adjustVoidDarkness(
            float bottomDarkness,
            Camera camera,
            float partialTicks,
            ClientLevel level,
            int renderDistance,
            float darkenWorldAmount,
            Vector4f dest,
            @Local FogType fogType) {
        if (OverworldAbyssSky.visible(level, camera.position().x, camera.position().z)) {
            return 0.0F;
        }

        if (level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)
                && fogType == FogType.ATMOSPHERIC) {
            return 0.0F;
        }

        if (!level.dimension().equals(MiaDimensions.GREAT_FAULT_LEVEL)) {
            return bottomDarkness;
        }

        float range = level.getLevelData().voidDarknessOnsetRange();
        float topDarkness =
                Mth.clamp(
                        (range + (float) camera.position().y - level.getMaxY()) / range,
                        0.0F,
                        1.0F);
        return Math.max(bottomDarkness, topDarkness);
    }
}
