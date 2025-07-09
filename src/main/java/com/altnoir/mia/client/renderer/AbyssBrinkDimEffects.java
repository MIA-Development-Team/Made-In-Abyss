package com.altnoir.mia.client.renderer;

import com.altnoir.mia.util.MiaUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AbyssBrinkDimEffects extends DimensionSpecialEffects {
    public AbyssBrinkDimEffects() {
        super(24.0F, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public @Nullable float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        ResourceLocation sunTexture = MiaUtil.miaId("textures/skybox/abyss_brink.png");

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, sunTexture);

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        float sunSize = 500.0F;
        float sunHeight = 350.0F;

        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.addVertex(pose, -sunSize, sunHeight, -sunSize).setUv(0.0F, 0.0F);
        bufferbuilder.addVertex(pose, sunSize, sunHeight, -sunSize).setUv(1.0F, 0.0F);
        bufferbuilder.addVertex(pose, sunSize, sunHeight, sunSize).setUv(1.0F, 1.0F);
        bufferbuilder.addVertex(pose, -sunSize, sunHeight, sunSize).setUv(0.0F, 1.0F);

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        return true;
    }
}
