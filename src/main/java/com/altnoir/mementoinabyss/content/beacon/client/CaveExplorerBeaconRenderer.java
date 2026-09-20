package com.altnoir.mementoinabyss.content.beacon.client;

import com.altnoir.mementoinabyss.content.beacon.CaveExplorerBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class CaveExplorerBeaconRenderer
        implements BlockEntityRenderer<
                CaveExplorerBeaconBlockEntity, CaveExplorerBeaconRenderState> {
    private static final float MODEL_MIN_X = 4.0F / 16.0F;
    private static final float MODEL_MIN_Y = 5.0F / 16.0F;
    private static final float MODEL_MIN_Z = 4.0F / 16.0F;
    private static final float MODEL_MAX_X = 12.0F / 16.0F;
    private static final float MODEL_MAX_Y = 13.0F / 16.0F;
    private static final float MODEL_MAX_Z = 12.0F / 16.0F;
    private static final float GLOW_SCALE = 0.1F;
    private static final float LASER_SCALE = 0.22F;
    private static final float INNER_RADIUS = 0.08F;
    private static final float FADE_START = 0.4F;

    public CaveExplorerBeaconRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CaveExplorerBeaconRenderState createRenderState() {
        return new CaveExplorerBeaconRenderState();
    }

    @Override
    public void extractRenderState(
            CaveExplorerBeaconBlockEntity blockEntity,
            CaveExplorerBeaconRenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity, state, partialTick, cameraPosition, breakProgress);

        Level level = blockEntity.getLevel();
        if (level == null) {
            state.glow = 0.0F;
            state.beamLength = 0.0F;
            return;
        }

        float time = level.getGameTime() + partialTick;
        blockEntity.updateClientGlow(time, blockEntity.getLevels() > 0);
        state.glow = blockEntity.getGlow();
        float range = blockEntity.getEffectBeamLength();
        state.beamLength = range > 0.0F ? range + (1.0F - MODEL_MAX_Y) : 0.0F;
        if (state.glow <= 0.001F) {
            return;
        }

        state.pulse = (Mth.sin(time * 0.12F) + 1.0F) * 0.5F;
    }

    @Override
    public void submit(
            CaveExplorerBeaconRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        if (state.glow <= 0.001F) {
            return;
        }

        float glow = GLOW_SCALE * state.glow;
        float alpha = state.glow * (0.25F + 0.15F * state.pulse);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                CaveExplorerBeaconRenderTypes.glow(),
                (pose, consumer) -> {
                    for (int layer = 0; layer < 4; layer++) {
                        float layerScale = 1.0F - layer * 0.25F;
                        float layerAlpha = alpha * (0.25F + layer * 0.25F);
                        float saturation = 0.25F + layer * 0.25F;
                        renderGlowBox(
                                pose,
                                consumer,
                                MODEL_MIN_X - glow * layerScale,
                                MODEL_MAX_X + glow * layerScale,
                                MODEL_MIN_Y - glow * layerScale,
                                MODEL_MAX_Y + glow * layerScale,
                                MODEL_MIN_Z - glow * layerScale,
                                MODEL_MAX_Z + glow * layerScale,
                                layerAlpha,
                                Mth.lerp(saturation, 0.75F, 0.30F),
                                1.0F,
                                Mth.lerp(saturation, 0.72F, 0.35F));
                    }
                });

        if (state.beamLength > 0.5F) {
            submitLaser(state, poseStack, submitNodeCollector);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static void submitLaser(
            CaveExplorerBeaconRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector) {
        float outerAlpha = state.glow * Mth.lerp(state.pulse, 0.08F, 0.32F);
        float innerAlpha = state.glow * Mth.lerp(state.pulse, 0.35F, 0.90F);
        float length = state.beamLength;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, 0.0F, MODEL_MAX_Y - 0.5F);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                CaveExplorerBeaconRenderTypes.glow(),
                (pose, consumer) ->
                        renderInnerBeam(pose, consumer, length, INNER_RADIUS, innerAlpha));

        poseStack.pushPose();
        poseStack.scale(LASER_SCALE, LASER_SCALE, 1.0F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                CaveExplorerBeaconRenderTypes.glow(),
                (pose, consumer) -> renderLaser(pose, consumer, length, outerAlpha));
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderInnerBeam(
            PoseStack.Pose pose, VertexConsumer consumer, float length, float radius, float alpha) {
        renderFadedBeamQuad(
                pose, consumer, -radius, -radius, -radius, radius, length, alpha, 0.85F, 1.0F,
                0.88F);
        renderFadedBeamQuad(
                pose, consumer, radius, radius, radius, -radius, length, alpha, 0.85F, 1.0F, 0.88F);
        renderFadedBeamQuad(
                pose, consumer, -radius, radius, radius, radius, length, alpha, 0.85F, 1.0F, 0.88F);
        renderFadedBeamQuad(
                pose, consumer, radius, -radius, -radius, -radius, length, alpha, 0.85F, 1.0F,
                0.88F);
    }

    private static void renderFadedBeamQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x1,
            float y1,
            float x2,
            float y2,
            float length,
            float alpha,
            float red,
            float green,
            float blue) {
        float fadeStart = length * FADE_START;
        addColoredVertex(pose, consumer, x1, y1, 0.0F, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x2, y2, 0.0F, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x2, y2, fadeStart, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x1, y1, fadeStart, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x1, y1, fadeStart, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x2, y2, fadeStart, red, green, blue, alpha);
        addColoredVertex(pose, consumer, x2, y2, length, red, green, blue, 0.0F);
        addColoredVertex(pose, consumer, x1, y1, length, red, green, blue, 0.0F);
    }

    private static void renderLaser(
            PoseStack.Pose pose, VertexConsumer consumer, float length, float alpha) {
        float fadeStart = length * FADE_START;
        for (int i = 0; i < 4; i++) {
            float angle = i * Mth.HALF_PI;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            addLaserVertex(pose, consumer, 0.0F, 0.0F, 0.0F, cos, sin, alpha);
            addLaserVertex(pose, consumer, 1.0F, 0.0F, 0.0F, cos, sin, alpha);
            addLaserVertex(pose, consumer, 1.0F, 0.0F, fadeStart, cos, sin, alpha);
            addLaserVertex(pose, consumer, 0.0F, 0.0F, fadeStart, cos, sin, alpha);
            addLaserVertex(pose, consumer, 0.0F, 0.0F, fadeStart, cos, sin, alpha);
            addLaserVertex(pose, consumer, 1.0F, 0.0F, fadeStart, cos, sin, alpha);
            addLaserVertex(pose, consumer, 1.0F, 0.0F, length, cos, sin, 0.0F);
            addLaserVertex(pose, consumer, 0.0F, 0.0F, length, cos, sin, 0.0F);
        }
    }

    private static void addColoredVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            float red,
            float green,
            float blue,
            float alpha) {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }

    private static void addLaserVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            float cos,
            float sin,
            float alpha) {
        float dx = x - 0.5F;
        float dy = y - 0.5F;
        consumer.addVertex(pose, 0.5F + dx * cos - dy * sin, 0.5F + dx * sin + dy * cos, z)
                .setColor(0.30F, 1.0F, 0.35F, alpha);
    }

    private static void renderGlowBox(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float minX,
            float maxX,
            float minY,
            float maxY,
            float minZ,
            float maxZ,
            float alpha,
            float red,
            float green,
            float blue) {
        renderQuad(
                pose, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY,
                maxZ, alpha, red, green, blue);
        renderQuad(
                pose, consumer, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY,
                minZ, alpha, red, green, blue);
        renderQuad(
                pose, consumer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY,
                minZ, alpha, red, green, blue);
        renderQuad(
                pose, consumer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY,
                maxZ, alpha, red, green, blue);
        renderQuad(
                pose, consumer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY,
                minZ, alpha, red, green, blue);
        renderQuad(
                pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY,
                maxZ, alpha, red, green, blue);
    }

    private static void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4,
            float alpha,
            float red,
            float green,
            float blue) {
        consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha);
        consumer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha);
        consumer.addVertex(pose, x3, y3, z3).setColor(red, green, blue, alpha);
        consumer.addVertex(pose, x4, y4, z4).setColor(red, green, blue, alpha);
    }
}
