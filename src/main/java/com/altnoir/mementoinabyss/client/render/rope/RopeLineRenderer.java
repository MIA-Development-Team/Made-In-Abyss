package com.altnoir.mementoinabyss.client.render.rope;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Two-layer rope mesh. Every segment keeps its own perpendicular end rings;
 * separate joint faces bridge the differently rotated rings at a node.
 */
public final class RopeLineRenderer {
    public static final Identifier TEXTURE =
            MementoInAbyss.asResource("textures/block/rope.png");
    private static final float CORE_HALF_WIDTH = 2.0F / 16.0F;
    private static final float OVERLAY_HALF_WIDTH = 2.25F / 16.0F;
    private static final float JOINT_TEXTURE_LENGTH = 1.0F / 16.0F;

    public static void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            RopeSimulation rope,
            double partialTick,
            double cameraX,
            double cameraY,
            double cameraZ,
            int light
    ) {
        float[] points = new float[rope.pointCount() * 3];
        for (int point = 0; point < rope.pointCount(); point++) {
            points[point * 3] = (float) (rope.interpolatedX(point, partialTick) - cameraX);
            points[point * 3 + 1] = (float) (rope.interpolatedY(point, partialTick) - cameraY);
            points[point * 3 + 2] = (float) (rope.interpolatedZ(point, partialTick) - cameraZ);
        }
        renderPoints(poseStack.last(), consumer, points, light);
    }

    public static void renderPoints(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float[] points,
            int light
    ) {
        int pointCount = points.length / 3;
        int segmentCount = pointCount - 1;
        if (segmentCount < 1) {
            return;
        }

        float[] coreStarts = new float[segmentCount * 12];
        float[] coreEnds = new float[segmentCount * 12];
        float[] overlayStarts = new float[segmentCount * 12];
        float[] overlayEnds = new float[segmentCount * 12];
        float[] tangents = new float[segmentCount * 3];
        buildSegmentRings(
                points, coreStarts, coreEnds, overlayStarts, overlayEnds, tangents);

        for (int segment = 0; segment < segmentCount; segment++) {
            int pointOffset = segment * 3;
            float segmentLength = length(
                    points[pointOffset + 3] - points[pointOffset],
                    points[pointOffset + 4] - points[pointOffset + 1],
                    points[pointOffset + 5] - points[pointOffset + 2]
            );
            float textureLength = Math.min(1.0F, segmentLength);
            renderSegmentLayer(pose, consumer, coreStarts, coreEnds, segment,
                    0.0F, 0.25F, textureLength, light);
            renderSegmentLayer(pose, consumer, overlayStarts, overlayEnds, segment,
                    0.25F, 0.5F, textureLength, light);
        }

        for (int joint = 1; joint < pointCount - 1; joint++) {
            int centerOffset = joint * 3;
            renderJointLayer(pose, consumer,
                    coreEnds, (joint - 1) * 12,
                    coreStarts, joint * 12,
                    points[centerOffset], points[centerOffset + 1], points[centerOffset + 2],
                    0.0F, 0.25F, light);
            renderJointLayer(pose, consumer,
                    overlayEnds, (joint - 1) * 12,
                    overlayStarts, joint * 12,
                    points[centerOffset], points[centerOffset + 1], points[centerOffset + 2],
                    0.25F, 0.5F, light);
        }

        renderCap(pose, consumer, coreStarts, 0, tangents, 0, true, light);
        renderCap(pose, consumer, coreEnds, (segmentCount - 1) * 12,
                tangents, (segmentCount - 1) * 3, false, light);
    }

    private static void buildSegmentRings(
            float[] points,
            float[] coreStarts,
            float[] coreEnds,
            float[] overlayStarts,
            float[] overlayEnds,
            float[] tangents
    ) {
        int segmentCount = points.length / 3 - 1;

        for (int segment = 0; segment < segmentCount; segment++) {
            int pointOffset = segment * 3;
            float ax = points[pointOffset];
            float ay = points[pointOffset + 1];
            float az = points[pointOffset + 2];
            float bx = points[pointOffset + 3];
            float by = points[pointOffset + 4];
            float bz = points[pointOffset + 5];
            float tx = bx - ax;
            float ty = by - ay;
            float tz = bz - az;
            float tangentLength = length(tx, ty, tz);
            if (tangentLength < 1.0E-6F) {
                tx = 0.0F;
                ty = 1.0F;
                tz = 0.0F;
            } else {
                tx /= tangentLength;
                ty /= tangentLength;
                tz /= tangentLength;
            }
            tangents[pointOffset] = tx;
            tangents[pointOffset + 1] = ty;
            tangents[pointOffset + 2] = tz;

            // Build a world-stable frame from this segment alone. Transporting
            // the previous segment's frame made roll changes at the parent
            // propagate through every otherwise stationary child segment.
            float sx;
            float sy;
            float sz;
            float ux;
            float uy;
            float uz;
            if (tz < -0.9999F) {
                sx = 0.0F;
                sy = -1.0F;
                sz = 0.0F;
                ux = -1.0F;
                uy = 0.0F;
                uz = 0.0F;
            } else {
                float inverse = 1.0F / (1.0F + tz);
                float cross = -tx * ty * inverse;
                sx = 1.0F - tx * tx * inverse;
                sy = cross;
                sz = -tx;
                ux = cross;
                uy = 1.0F - ty * ty * inverse;
                uz = -ty;
            }

            int ringOffset = segment * 12;
            fillRing(coreStarts, ringOffset, ax, ay, az,
                    sx, sy, sz, ux, uy, uz, CORE_HALF_WIDTH);
            fillRing(coreEnds, ringOffset, bx, by, bz,
                    sx, sy, sz, ux, uy, uz, CORE_HALF_WIDTH);
            fillRing(overlayStarts, ringOffset, ax, ay, az,
                    sx, sy, sz, ux, uy, uz, OVERLAY_HALF_WIDTH);
            fillRing(overlayEnds, ringOffset, bx, by, bz,
                    sx, sy, sz, ux, uy, uz, OVERLAY_HALF_WIDTH);
        }
    }

    private static void renderSegmentLayer(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float[] starts,
            float[] ends,
            int segment,
            float u0,
            float u1,
            float textureLength,
            int light
    ) {
        int ringOffset = segment * 12;
        for (int side = 0; side < 4; side++) {
            int next = (side + 1) & 3;
            int a = ringOffset + side * 3;
            int aNext = ringOffset + next * 3;
            float nx = starts[a] + starts[aNext] - starts[ringOffset] - starts[ringOffset + 6];
            float ny = starts[a + 1] + starts[aNext + 1]
                    - starts[ringOffset + 1] - starts[ringOffset + 7];
            float nz = starts[a + 2] + starts[aNext + 2]
                    - starts[ringOffset + 2] - starts[ringOffset + 8];
            float inverseNormal = 1.0F / Math.max(1.0E-6F, length(nx, ny, nz));
            nx *= inverseNormal;
            ny *= inverseNormal;
            nz *= inverseNormal;
            vertex(pose, consumer, starts, aNext, u1, 0.0F, light, nx, ny, nz);
            vertex(pose, consumer, ends, aNext, u1, textureLength, light, nx, ny, nz);
            vertex(pose, consumer, ends, a, u0, textureLength, light, nx, ny, nz);
            vertex(pose, consumer, starts, a, u0, 0.0F, light, nx, ny, nz);
        }
    }

    private static void renderJointLayer(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float[] previousRing,
            int previousOffset,
            float[] nextRing,
            int nextOffset,
            float centerX,
            float centerY,
            float centerZ,
            float u0,
            float u1,
            int light
    ) {
        for (int side = 0; side < 4; side++) {
            int next = (side + 1) & 3;
            int previous = previousOffset + side * 3;
            int previousNext = previousOffset + next * 3;
            int following = nextOffset + side * 3;
            int followingNext = nextOffset + next * 3;
            float nx = previousRing[previous] + previousRing[previousNext]
                    + nextRing[following] + nextRing[followingNext] - centerX * 4.0F;
            float ny = previousRing[previous + 1] + previousRing[previousNext + 1]
                    + nextRing[following + 1] + nextRing[followingNext + 1] - centerY * 4.0F;
            float nz = previousRing[previous + 2] + previousRing[previousNext + 2]
                    + nextRing[following + 2] + nextRing[followingNext + 2] - centerZ * 4.0F;
            float inverseNormal = 1.0F / Math.max(1.0E-6F, length(nx, ny, nz));
            nx *= inverseNormal;
            ny *= inverseNormal;
            nz *= inverseNormal;
            vertex(pose, consumer, previousRing, previousNext,
                    u1, 0.0F, light, nx, ny, nz);
            vertex(pose, consumer, nextRing, followingNext,
                    u1, JOINT_TEXTURE_LENGTH, light, nx, ny, nz);
            vertex(pose, consumer, nextRing, following,
                    u0, JOINT_TEXTURE_LENGTH, light, nx, ny, nz);
            vertex(pose, consumer, previousRing, previous,
                    u0, 0.0F, light, nx, ny, nz);
        }
    }

    private static void renderCap(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float[] rings,
            int ringOffset,
            float[] tangents,
            int tangentOffset,
            boolean start,
            int light
    ) {
        float direction = start ? -1.0F : 1.0F;
        float nx = tangents[tangentOffset] * direction;
        float ny = tangents[tangentOffset + 1] * direction;
        float nz = tangents[tangentOffset + 2] * direction;
        int a = ringOffset;
        int b = ringOffset + 3;
        int c = ringOffset + 6;
        int d = ringOffset + 9;
        if (start) {
            vertex(pose, consumer, rings, d, 0.75F, 0.0F, light, nx, ny, nz);
            vertex(pose, consumer, rings, c, 0.50F, 0.0F, light, nx, ny, nz);
            vertex(pose, consumer, rings, b, 0.50F, 0.25F, light, nx, ny, nz);
            vertex(pose, consumer, rings, a, 0.75F, 0.25F, light, nx, ny, nz);
        } else {
            vertex(pose, consumer, rings, a, 0.75F, 0.25F, light, nx, ny, nz);
            vertex(pose, consumer, rings, b, 0.50F, 0.25F, light, nx, ny, nz);
            vertex(pose, consumer, rings, c, 0.50F, 0.0F, light, nx, ny, nz);
            vertex(pose, consumer, rings, d, 0.75F, 0.0F, light, nx, ny, nz);
        }
    }

    private static void fillRing(
            float[] ring,
            int offset,
            float x, float y, float z,
            float sx, float sy, float sz,
            float ux, float uy, float uz,
            float halfWidth
    ) {
        ring[offset] = x + (sx + ux) * halfWidth;
        ring[offset + 1] = y + (sy + uy) * halfWidth;
        ring[offset + 2] = z + (sz + uz) * halfWidth;
        ring[offset + 3] = x + (-sx + ux) * halfWidth;
        ring[offset + 4] = y + (-sy + uy) * halfWidth;
        ring[offset + 5] = z + (-sz + uz) * halfWidth;
        ring[offset + 6] = x + (-sx - ux) * halfWidth;
        ring[offset + 7] = y + (-sy - uy) * halfWidth;
        ring[offset + 8] = z + (-sz - uz) * halfWidth;
        ring[offset + 9] = x + (sx - ux) * halfWidth;
        ring[offset + 10] = y + (sy - uy) * halfWidth;
        ring[offset + 11] = z + (sz - uz) * halfWidth;
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float[] point,
            int offset,
            float u,
            float v,
            int light,
            float nx,
            float ny,
            float nz
    ) {
        consumer.addVertex(pose, point[offset], point[offset + 1], point[offset + 2])
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private RopeLineRenderer() {
    }
}
