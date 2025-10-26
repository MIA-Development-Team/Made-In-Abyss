package com.altnoir.mia.block.entity.renderer;

import com.altnoir.mia.block.entity.SunStoneBlockEntity;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SunStoneBlockRenderer implements BlockEntityRenderer<SunStoneBlockEntity> {
    public SunStoneBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SunStoneBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderRays(poseStack, bufferSource.getBuffer(RenderType.dragonRays()));
        renderRays(poseStack, bufferSource.getBuffer(RenderType.dragonRaysDepth()));
        poseStack.popPose();
    }

    private static void renderRays(PoseStack poseStack, VertexConsumer buffer) {
        float timeConstant = (float) (Blaze3D.getTime() * (double) 20.0F) / 200f; //Controls how fast the rays move.
        poseStack.pushPose();
        float rotationControl = Math.min(timeConstant > 0.8F ? (timeConstant - 0.8F) / 0.2F : 0.0F, 1.0F);
        int startColor = FastColor.ARGB32.colorFromFloat(1.0F - 0f, 1.0F, 1.0F, 1.0F);
        int endColor = 0x3D8F3D;
        RandomSource randomsource = RandomSource.create(432L); //Very important, cannot replace with a static field. it's related to the continuity of the rays movement.
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f1 = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();
        int raysCount = 20;

        for (int l = 0; l < raysCount; l++) {
            quaternionf.rotationXYZ(
                            randomsource.nextFloat() * (float) (Math.PI * 2),
                            randomsource.nextFloat() * (float) (Math.PI * 2),
                            randomsource.nextFloat() * (float) (Math.PI * 2)
                    )
                    .rotateXYZ(
                            randomsource.nextFloat() * (float) (Math.PI * 2),
                            randomsource.nextFloat() * (float) (Math.PI * 2),
                            randomsource.nextFloat() * (float) (Math.PI * 2) + timeConstant * (float) (Math.PI / 2)
                    );
            poseStack.mulPose(quaternionf);
            float f1 = randomsource.nextFloat() * 20.0F + 5.0F + rotationControl * 10.0F;
            float f2 = randomsource.nextFloat() * 2.0F + 1.0F + rotationControl * 2.0F;
            vector3f1.set(-(Math.sqrt(3f) / 2f) * f2, f1, -0.5F * f2);
            vector3f2.set((Math.sqrt(3f) / 2f) * f2, f1, -0.5F * f2);
            vector3f3.set(0.0F, f1, f2);
            PoseStack.Pose posestack$pose = poseStack.last();
            buffer.addVertex(posestack$pose, vector3f).setColor(startColor);
            buffer.addVertex(posestack$pose, vector3f1).setColor(endColor);
            buffer.addVertex(posestack$pose, vector3f2).setColor(endColor);
            buffer.addVertex(posestack$pose, vector3f).setColor(startColor);
            buffer.addVertex(posestack$pose, vector3f2).setColor(endColor);
            buffer.addVertex(posestack$pose, vector3f3).setColor(endColor);
            buffer.addVertex(posestack$pose, vector3f).setColor(startColor);
            buffer.addVertex(posestack$pose, vector3f3).setColor(endColor);
            buffer.addVertex(posestack$pose, vector3f1).setColor(endColor);
        }

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance() * 2;
    }

    @Override
    public boolean shouldRenderOffScreen(SunStoneBlockEntity blockEntity) {
        return true;
    }
}
