package com.altnoir.mia.client.renderer;

import com.altnoir.mia.common.entity.projectile.HookEntity;
import com.altnoir.mia.init.MiaItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

// 直接用的鱼钩渲染
// TODO 等个有缘人重写渲染
public class HookEntityRenderer extends EntityRenderer<HookEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public HookEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(HookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player player = entity.getPlayer();
        if (player != null) {
            poseStack.pushPose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            PoseStack.Pose posestack$pose = poseStack.last();
            VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
            vertex(vertexconsumer, posestack$pose, packedLight, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, posestack$pose, packedLight, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, posestack$pose, packedLight, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, posestack$pose, packedLight, 0.0F, 1, 0, 0);
            poseStack.popPose();
            float f = player.getAttackAnim(partialTicks);
            float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
            Vec3 vec3 = this.getPlayerHandPos(player, f1, partialTicks);
            Vec3 vec31 = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0);
            float f2 = (float)(vec3.x - vec31.x);
            float f3 = (float)(vec3.y - vec31.y);
            float f4 = (float)(vec3.z - vec31.z);
            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose posestack$pose1 = poseStack.last();
            int i = 16;

            for (int j = 0; j <= 16; j++) {
                stringVertex(f2, f3, f4, vertexconsumer1, posestack$pose1, fraction(j, 16), fraction(j + 1, 16));
            }

            poseStack.popPose();
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    private Vec3 getPlayerHandPos(Player player, float p_340872_, float partialTick) {
        int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.is(MiaItems.HOOK_ITEM)) {
            i = -i;
        }

        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double d4 = VIEW_BOBBING_SCALE / (double)this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 vec3 = this.entityRenderDispatcher
                    .camera
                    .getNearPlane()
                    .getPointOnPlane((float)i * 0.525F, -0.1F)
                    .scale(d4)
                    .yRot(p_340872_ * 0.5F)
                    .xRot(-p_340872_ * 0.7F);
            return player.getEyePosition(partialTick).add(vec3);
        } else {
            float f = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
            double d0 = Mth.sin(f);
            double d1 = Mth.cos(f);
            float f1 = player.getScale();
            double d2 = (double)i * 0.35 * (double)f1;
            double d3 = 0.8 * (double)f1;
            float f2 = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(partialTick).add(-d1 * d2 - d0 * d3, (double)f2 - 0.45 * (double)f1, -d0 * d2 + d1 * d3);
        }
    }

    private static float fraction(int numerator, int denominator) {
        return (float)numerator / (float)denominator;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight, float x, int y, int u, int v) {
        consumer.addVertex(pose, x - 0.5F, (float)y - 0.5F, 0.0F)
                .setColor(-1)
                .setUv((float)u, (float)v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(
            float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose, float stringFraction, float nextStringFraction
    ) {
        float f = x * stringFraction;
        float f1 = y * stringFraction;
        float f2 = z * stringFraction;
        float f3 = x * nextStringFraction - f;
        float f4 = y * nextStringFraction - f1;
        float f5 = z * nextStringFraction - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        consumer.addVertex(pose, f, f1, f2).setColor(-8421505).setNormal(pose, f3, f4, f5);
    }

    public ResourceLocation getTextureLocation(HookEntity entity) {
        return TEXTURE_LOCATION;
    }
}
