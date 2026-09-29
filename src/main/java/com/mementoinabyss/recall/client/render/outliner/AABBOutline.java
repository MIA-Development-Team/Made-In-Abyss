/*
 * Portions Copyright (c) 2022 The Create Team.
 * Adapted from Ponder/Catnip mc26.1/dev, ff9313616c963e94a14065d49f708e75e1b9e62a.
 * SPDX-License-Identifier: MIT
 * See META-INF/licenses/ponder-MIT.txt.
 */
package com.mementoinabyss.recall.client.render.outliner;

import com.mementoinabyss.recall.client.render.RecallRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Catnip's cuboid AABB edges, with a positive gap between every edge and the model. */
public final class AABBOutline extends CuboidLine {
    private final Vector3f originTemp = new Vector3f();
    public static final float LINE_WIDTH = 1 / 64F;
    public static final float SURFACE_GAP = 1 / 128F;

    public static AABB expandedBounds(AABB bounds, float width) {
        // Upstream inflates by only 1/128: at width 1/64 its inner faces are coplanar.
        return bounds.inflate(width / 2 + SURFACE_GAP);
    }

    public void render(PoseStack pose, MultiBufferSource buffer, AABB bounds, float alpha) {
        if (alpha <= 0) return;
        float width = LINE_WIDTH * Math.clamp(alpha, 0, 1);
        AABB box = expandedBounds(bounds, width);
        renderBoxEdges(
                pose,
                buffer.getBuffer(RecallRenderTypes.outlineSolid()),
                new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ),
                new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ),
                width,
                new Vector4f(239 / 255F, 239 / 255F, 239 / 255F, 1),
                LightCoordsUtil.FULL_BRIGHT,
                true);
    }

    protected void renderBoxEdges(
            PoseStack ms,
            VertexConsumer consumer,
            Vector3f minPos,
            Vector3f maxPos,
            float lineWidth,
            Vector4f color,
            int lightmap,
            boolean disableNormals) {
        Vector3f origin = originTemp;

        PoseStack.Pose pose = ms.last();

        float lineLengthX = maxPos.x() - minPos.x();
        float lineLengthY = maxPos.y() - minPos.y();
        float lineLengthZ = maxPos.z() - minPos.z();

        origin.set(minPos);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.EAST,
                lineLengthX,
                lineWidth,
                color,
                lightmap,
                disableNormals);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.UP,
                lineLengthY,
                lineWidth,
                color,
                lightmap,
                disableNormals);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.SOUTH,
                lineLengthZ,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(maxPos.x(), minPos.y(), minPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.UP,
                lineLengthY,
                lineWidth,
                color,
                lightmap,
                disableNormals);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.SOUTH,
                lineLengthZ,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(minPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.EAST,
                lineLengthX,
                lineWidth,
                color,
                lightmap,
                disableNormals);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.SOUTH,
                lineLengthZ,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(minPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.EAST,
                lineLengthX,
                lineWidth,
                color,
                lightmap,
                disableNormals);
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.UP,
                lineLengthY,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(minPos.x(), maxPos.y(), maxPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.EAST,
                lineLengthX,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(maxPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.UP,
                lineLengthY,
                lineWidth,
                color,
                lightmap,
                disableNormals);

        origin.set(maxPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(
                pose,
                consumer,
                origin,
                Direction.SOUTH,
                lineLengthZ,
                lineWidth,
                color,
                lightmap,
                disableNormals);
    }
}
