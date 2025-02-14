package com.altnoir.mia.client.entity;

import com.altnoir.mia.client.entity.KnifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class KnifeRenderer extends EntityRenderer<KnifeEntity> {
    public KnifeRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public ResourceLocation getTextureLocation(KnifeEntity p_114482_) {
        return new ResourceLocation("mia", "textures/entity/knife.png");
    }

    @Override
    public void render(KnifeEntity entity, float yRotation, float partialTick, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(1F,1.4F,1.4F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        float tex1x1 = 0.0F;
        float tex1x2 = 0.5F;
        float tex1x2h = 0.125F;
        float tex1y1 = 0.0F;
        float tex1y2 = 0.15625F;
        float tex2x1 = 0.0F;
        float tex2x2 = 0.15625F;
        float tex2y1 = 0.15625F;
        float tex2y2 = 0.3125F;
        float scale = 0.0375F;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-4.0D, 0.0D, 0.0D);
        VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        PoseStack.Pose matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, tex2x1, tex2y1, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, tex2x2, tex2y1, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, tex2x2, tex2y2, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, tex2x1, tex2y2, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, tex2x1, tex2y1, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, tex2x2, tex2y1, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, tex2x2, tex2y2, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, tex2x1, tex2y2, 1, 0, 0, packedLight);
        for (int j = 0; j < 4; ++j) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(matrix4f, matrix3f, ivertexbuilder, -8, -2, 0, tex1x1, tex1y1, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, j % 2 == 1 ? 8 : -4, -2, 0, j % 2 == 1 ? tex1x2 : tex1x2h, tex1y1, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, j % 2 == 1 ? 8 : -4, 2, 0, j % 2 == 1 ? tex1x2 : tex1x2h, tex1y2, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, -8, 2, 0, tex1x1, tex1y2, 0, 1, 0, packedLight);
        }
        matrixStack.popPose();
    }

    public void vertex(Matrix4f p_254392_, Matrix3f p_254011_, VertexConsumer p_253902_, int p_254058_, int p_254338_, int p_254196_, float p_254003_, float p_254165_, int p_253982_, int p_254037_, int p_254038_, int p_254271_) {
        p_253902_.vertex(p_254392_, (float) p_254058_, (float) p_254338_, (float) p_254196_).color(255, 255, 255, 255).uv(p_254003_, p_254165_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_254271_).normal(p_254011_, (float) p_253982_, (float) p_254038_, (float) p_254037_).endVertex();
    }
}
