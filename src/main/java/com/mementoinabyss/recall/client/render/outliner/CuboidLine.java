/*
 * Portions Copyright (c) 2022 The Create Team.
 * Adapted from Ponder/Catnip mc26.1/dev, ff9313616c963e94a14065d49f708e75e1b9e62a.
 * SPDX-License-Identifier: MIT
 * See META-INF/licenses/ponder-MIT.txt.
 */
package com.mementoinabyss.recall.client.render.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** The untextured solid cuboid-edge geometry from Catnip Outline. */
class CuboidLine {
    private final Vector3f minPosTemp = new Vector3f();
    private final Vector3f maxPosTemp = new Vector3f();
    private final Vector4f posTransformTemp = new Vector4f();
    private final Vector3f normalTransformTemp = new Vector3f();

    public void bufferCuboidLine(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vector3f origin,
            Direction direction,
            float length,
            float width,
            Vector4f color,
            int lightmap,
            boolean disableNormals) {
        Vector3f minPos = minPosTemp;
        Vector3f maxPos = maxPosTemp;

        float halfWidth = width / 2;
        minPos.set(origin.x() - halfWidth, origin.y() - halfWidth, origin.z() - halfWidth);
        maxPos.set(origin.x() + halfWidth, origin.y() + halfWidth, origin.z() + halfWidth);

        switch (direction) {
            case DOWN -> {
                minPos.add(0, -length, 0);
            }
            case UP -> {
                maxPos.add(0, length, 0);
            }
            case NORTH -> {
                minPos.add(0, 0, -length);
            }
            case SOUTH -> {
                maxPos.add(0, 0, length);
            }
            case WEST -> {
                minPos.add(-length, 0, 0);
            }
            case EAST -> {
                maxPos.add(length, 0, 0);
            }
        }

        bufferCuboid(pose, consumer, minPos, maxPos, color, lightmap, disableNormals);
    }

    public void bufferCuboid(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vector3f minPos,
            Vector3f maxPos,
            Vector4f color,
            int lightmap,
            boolean disableNormals) {
        Vector4f posTransformTemp = this.posTransformTemp;
        Vector3f normalTransformTemp = this.normalTransformTemp;

        float minX = minPos.x();
        float minY = minPos.y();
        float minZ = minPos.z();
        float maxX = maxPos.x();
        float maxY = maxPos.y();
        float maxZ = maxPos.z();

        Matrix4f posMatrix = pose.pose();

        posTransformTemp.set(minX, minY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x0 = posTransformTemp.x();
        float y0 = posTransformTemp.y();
        float z0 = posTransformTemp.z();

        posTransformTemp.set(minX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x1 = posTransformTemp.x();
        float y1 = posTransformTemp.y();
        float z1 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x2 = posTransformTemp.x();
        float y2 = posTransformTemp.y();
        float z2 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x3 = posTransformTemp.x();
        float y3 = posTransformTemp.y();
        float z3 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x4 = posTransformTemp.x();
        float y4 = posTransformTemp.y();
        float z4 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x5 = posTransformTemp.x();
        float y5 = posTransformTemp.y();
        float z5 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x6 = posTransformTemp.x();
        float y6 = posTransformTemp.y();
        float z6 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x7 = posTransformTemp.x();
        float y7 = posTransformTemp.y();
        float z7 = posTransformTemp.z();

        float r = color.x();
        float g = color.y();
        float b = color.z();
        float a = color.w();

        // down

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, -1, 0);
        }
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx0 = normalTransformTemp.x();
        float ny0 = normalTransformTemp.y();
        float nz0 = normalTransformTemp.z();

        consumer.addVertex(x0, y0, z0)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx0, ny0, nz0);

        consumer.addVertex(x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx0, ny0, nz0);

        consumer.addVertex(x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx0, ny0, nz0);

        consumer.addVertex(x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx0, ny0, nz0);

        // up

        normalTransformTemp.set(0, 1, 0);
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx1 = normalTransformTemp.x();
        float ny1 = normalTransformTemp.y();
        float nz1 = normalTransformTemp.z();

        consumer.addVertex(x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx1, ny1, nz1);

        consumer.addVertex(x5, y5, z5)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx1, ny1, nz1);

        consumer.addVertex(x6, y6, z6)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx1, ny1, nz1);

        consumer.addVertex(x7, y7, z7)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx1, ny1, nz1);

        // north

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, -1);
        }
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx2 = normalTransformTemp.x();
        float ny2 = normalTransformTemp.y();
        float nz2 = normalTransformTemp.z();

        consumer.addVertex(x7, y7, z7)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx2, ny2, nz2);

        consumer.addVertex(x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx2, ny2, nz2);

        consumer.addVertex(x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx2, ny2, nz2);

        consumer.addVertex(x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx2, ny2, nz2);

        // south

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, 1);
        }
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx3 = normalTransformTemp.x();
        float ny3 = normalTransformTemp.y();
        float nz3 = normalTransformTemp.z();

        consumer.addVertex(x5, y5, z5)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx3, ny3, nz3);

        consumer.addVertex(x0, y0, z0)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx3, ny3, nz3);

        consumer.addVertex(x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx3, ny3, nz3);

        consumer.addVertex(x6, y6, z6)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx3, ny3, nz3);

        // west

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(-1, 0, 0);
        }
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx4 = normalTransformTemp.x();
        float ny4 = normalTransformTemp.y();
        float nz4 = normalTransformTemp.z();

        consumer.addVertex(x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx4, ny4, nz4);

        consumer.addVertex(x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx4, ny4, nz4);

        consumer.addVertex(x0, y0, z0)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx4, ny4, nz4);

        consumer.addVertex(x5, y5, z5)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx4, ny4, nz4);

        // east

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(1, 0, 0);
        }
        pose.transformNormal(normalTransformTemp, normalTransformTemp);
        float nx5 = normalTransformTemp.x();
        float ny5 = normalTransformTemp.y();
        float nz5 = normalTransformTemp.z();

        consumer.addVertex(x6, y6, z6)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx5, ny5, nz5);

        consumer.addVertex(x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx5, ny5, nz5);

        consumer.addVertex(x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx5, ny5, nz5);

        consumer.addVertex(x7, y7, z7)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(nx5, ny5, nz5);
    }
}
