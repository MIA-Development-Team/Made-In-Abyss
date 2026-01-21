package com.altnoir.mia.common.block.entity.renderer;

import com.altnoir.mia.common.block.entity.AbyssSpawnerBlockEntity;
import com.altnoir.mia.client.render.MiaRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AbyssSpawnerRenderer implements BlockEntityRenderer<AbyssSpawnerBlockEntity> {
    private static final int SPHERE_SEGMENTS = 16;
    private static final int SPHERE_RINGS = 12;
    private static final float BASE_RADIUS = 0.25f;

    private static final int CORE_COLOR = FastColor.ARGB32.color(250, 120, 60, 160);
    private static final int OUTER_COLOR = FastColor.ARGB32.color(100, 160, 100, 220);

    public AbyssSpawnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AbyssSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        if (!blockEntity.hasValidPattern()) {
            return;
        }

        var spawner = blockEntity.getAbyssSpawner();
        float time = (level.getGameTime() + partialTick) * 0.05f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        double spin = Mth.lerp(partialTick, spawner.getOSpin(), spawner.getSpin());
        poseStack.mulPose(Axis.YP.rotationDegrees((float) spin));

        float bob = Mth.sin(time * 0.5f) * 0.05f;
        poseStack.translate(0, bob, 0);

        float pulse = 1.0f + Mth.sin(time * 2.0f) * 0.1f;
        float radius = BASE_RADIUS * pulse;

        poseStack.scale(1.25f, 1.25f, 1.25f);
        renderDistortedSphere(poseStack, bufferSource, radius, time, packedLight);
        poseStack.scale(1.1f, 1.1f, 1.1f);
        renderDistortedSphere(poseStack, bufferSource, radius, time * 0.5f, packedLight);
        //poseStack.scale(0.75f, 0.75f, 0.75f);
        //renderGlowingSphere(poseStack, bufferSource, radius * 0.8f, time);

        poseStack.popPose();
    }

    private void renderDistortedSphere(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float time, int packedLight) {
        VertexConsumer buffer = bufferSource.getBuffer(MiaRenderType.ABYSS_ORB);
        Matrix4f matrix = poseStack.last().pose();

        RandomSource random = RandomSource.create(42L);

        for (int ring = 0; ring < SPHERE_RINGS; ring++) {
            float theta1 = (float) Math.PI * ring / SPHERE_RINGS;
            float theta2 = (float) Math.PI * (ring + 1) / SPHERE_RINGS;

            for (int seg = 0; seg < SPHERE_SEGMENTS; seg++) {
                float phi1 = 2.0f * (float) Math.PI * seg / SPHERE_SEGMENTS;
                float phi2 = 2.0f * (float) Math.PI * (seg + 1) / SPHERE_SEGMENTS;

                float distort1 = getDistortion(theta1, phi1, time, random);
                float distort2 = getDistortion(theta2, phi1, time, random);
                float distort3 = getDistortion(theta2, phi2, time, random);
                float distort4 = getDistortion(theta1, phi2, time, random);

                Vector3f p1 = spherePoint(theta1, phi1, radius * distort1);
                Vector3f p2 = spherePoint(theta2, phi1, radius * distort2);
                Vector3f p3 = spherePoint(theta2, phi2, radius * distort3);
                Vector3f p4 = spherePoint(theta1, phi2, radius * distort4);

                float v1 = (float) ring / SPHERE_RINGS;
                float v2 = (float) (ring + 1) / SPHERE_RINGS;

                int color1 = lerpColor(CORE_COLOR, OUTER_COLOR, v1);
                int color2 = lerpColor(CORE_COLOR, OUTER_COLOR, v2);

                addVertex(buffer, matrix, p1, color1, packedLight);
                addVertex(buffer, matrix, p2, color2, packedLight);
                addVertex(buffer, matrix, p3, color2, packedLight);

                addVertex(buffer, matrix, p1, color1, packedLight);
                addVertex(buffer, matrix, p3, color2, packedLight);
                addVertex(buffer, matrix, p4, color1, packedLight);
            }
        }
    }

    private void renderGlowingSphere(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float time) {
        VertexConsumer buffer = bufferSource.getBuffer(MiaRenderType.ABYSS_ORB_GLOW);
        Matrix4f matrix = poseStack.last().pose();

        float glowPulse = Mth.sin(time * 3.0f) * 0.5f + 0.5f;
        int glowAlpha = (int) (150 + glowPulse * 105);
        int glowColor = FastColor.ARGB32.color(glowAlpha, 180, 80, 220);

        for (int ring = 0; ring < SPHERE_RINGS / 2; ring++) {
            float theta1 = (float) Math.PI * ring / (SPHERE_RINGS / 2);
            float theta2 = (float) Math.PI * (ring + 1) / (SPHERE_RINGS / 2);

            for (int seg = 0; seg < SPHERE_SEGMENTS / 2; seg++) {
                float phi1 = 2.0f * (float) Math.PI * seg / (SPHERE_SEGMENTS / 2);
                float phi2 = 2.0f * (float) Math.PI * (seg + 1) / (SPHERE_SEGMENTS / 2);

                Vector3f p1 = spherePoint(theta1, phi1, radius);
                Vector3f p2 = spherePoint(theta2, phi1, radius);
                Vector3f p3 = spherePoint(theta2, phi2, radius);
                Vector3f p4 = spherePoint(theta1, phi2, radius);

                addVertex(buffer, matrix, p1, glowColor, LightTexture.FULL_BRIGHT);
                addVertex(buffer, matrix, p2, glowColor, LightTexture.FULL_BRIGHT);
                addVertex(buffer, matrix, p3, glowColor, LightTexture.FULL_BRIGHT);

                addVertex(buffer, matrix, p1, glowColor, LightTexture.FULL_BRIGHT);
                addVertex(buffer, matrix, p3, glowColor, LightTexture.FULL_BRIGHT);
                addVertex(buffer, matrix, p4, glowColor, LightTexture.FULL_BRIGHT);
            }
        }
    }

    private float getDistortion(float theta, float phi, float time, RandomSource random) {
        float noise = Mth.sin(theta * 5 + time) * Mth.cos(phi * 3 + time * 0.7f);
        float secondary = Mth.sin(theta * 8 - time * 1.3f) * Mth.sin(phi * 6 + time * 0.5f);
        return 1.0f + noise * 0.15f + secondary * 0.08f;
    }

    private Vector3f spherePoint(float theta, float phi, float radius) {
        float x = radius * Mth.sin(theta) * Mth.cos(phi);
        float y = radius * Mth.cos(theta);
        float z = radius * Mth.sin(theta) * Mth.sin(phi);
        return new Vector3f(x, y, z);
    }

    private void addVertex(VertexConsumer buffer, Matrix4f matrix, Vector3f pos, int color, int light) {
        buffer.addVertex(matrix, pos.x, pos.y, pos.z)
                .setColor(color)
                .setLight(light);
    }

    private int lerpColor(int color1, int color2, float t) {
        int a1 = FastColor.ARGB32.alpha(color1);
        int r1 = FastColor.ARGB32.red(color1);
        int g1 = FastColor.ARGB32.green(color1);
        int b1 = FastColor.ARGB32.blue(color1);

        int a2 = FastColor.ARGB32.alpha(color2);
        int r2 = FastColor.ARGB32.red(color2);
        int g2 = FastColor.ARGB32.green(color2);
        int b2 = FastColor.ARGB32.blue(color2);

        int a = (int) Mth.lerp(t, a1, a2);
        int r = (int) Mth.lerp(t, r1, r2);
        int g = (int) Mth.lerp(t, g1, g2);
        int b = (int) Mth.lerp(t, b1, b2);

        return FastColor.ARGB32.color(a, r, g, b);
    }

    @Override
    public boolean shouldRenderOffScreen(AbyssSpawnerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
