package com.altnoir.mementoinabyss.client.render.rope;

import com.altnoir.mementoinabyss.client.render.state.RopeConnectorRenderState;
import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RopeConnectorRenderer
        implements BlockEntityRenderer<RopeConnectorBlockEntity, RopeConnectorRenderState> {
    public RopeConnectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RopeConnectorRenderState createRenderState() {
        return new RopeConnectorRenderState();
    }

    @Override
    public void extractRenderState(
            RopeConnectorBlockEntity connector,
            RopeConnectorRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                connector, state, partialTicks, cameraPosition, breakProgress);
        RopeSimulation rope = connector.getClientRope();
        if (rope == null) {
            state.points = new float[0];
            return;
        }

        int pointCount = rope.pointCount();
        float[] points = state.points.length == pointCount * 3
                ? state.points
                : new float[pointCount * 3];
        double originX = connector.getBlockPos().getX();
        double originY = connector.getBlockPos().getY();
        double originZ = connector.getBlockPos().getZ();
        for (int i = 0; i < pointCount; i++) {
            points[i * 3] = (float) (rope.interpolatedX(i, partialTicks) - originX);
            points[i * 3 + 1] = (float) (rope.interpolatedY(i, partialTicks) - originY);
            points[i * 3 + 2] = (float) (rope.interpolatedZ(i, partialTicks) - originZ);
        }
        state.points = points;
    }

    @Override
    public void submit(
            RopeConnectorRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (state.points.length < 6) {
            return;
        }

        float[] points = state.points;
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityCutout(RopeLineRenderer.TEXTURE, false),
                (pose, consumer) -> RopeLineRenderer.renderPoints(
                        pose, consumer, points, 15_728_880)
        );
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
