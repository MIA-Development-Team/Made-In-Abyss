package com.altnoir.mia.mixin;

import com.altnoir.mia.Time;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public class RenderStateShardMixin {
    @Inject(method = "setupGlintTexturing", at = @At("HEAD"), cancellable = true)
    private static void setupGlintTexturing(float p_110187_, CallbackInfo ci) {
        if (Time.get()) {
            long i = (long) ((double) Time.millis * Minecraft.getInstance().options.glintSpeed().get() * 8.0D);
            float f = (float) (i % 110000L) / 110000.0F;
            float f1 = (float) (i % 30000L) / 30000.0F;
            Matrix4f matrix4f = (new Matrix4f()).translation(-f, f1, 0.0F);
            matrix4f.rotateZ(0.17453292F).scale(p_110187_);
            RenderSystem.setTextureMatrix(matrix4f);
            ci.cancel();
        }
    }
}