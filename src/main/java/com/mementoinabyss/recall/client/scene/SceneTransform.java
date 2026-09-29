/*
 * Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Adapted from PonderScene.SceneTransform and WorldSectionElementImpl.transformMS.
 * See META-INF/licenses/ponder-MIT.txt.
 */
package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.data.scene.SceneStructure;
import com.mementoinabyss.recall.data.scene.SceneTimeline;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** One immutable camera snapshot for the PiP target, picking and overlay projection. */
final class SceneTransform {
    private final Matrix4f matrix;
    private final Matrix4f inverse;
    private final Matrix3f normals;
    private final int guiScale;

    SceneTransform(
            int width,
            int height,
            int guiScale,
            float yaw,
            float pitch,
            float scale,
            float centerX,
            float centerY,
            float centerZ) {
        if (width <= 0 || height <= 0 || guiScale <= 0 || !Float.isFinite(scale) || scale <= 0)
            throw new IllegalArgumentException("Invalid scene viewport");
        this.guiScale = guiScale;
        // Ponder's target-space centering, negative camera pitch, and GUI Y reflection.
        // No scene-slide animation is needed for an embedded single-scene viewport.
        var orientation =
                new Matrix4f()
                        .rotateX((float) Math.toRadians(-pitch))
                        .rotateY((float) Math.toRadians(yaw))
                        .scale(1, -1, 1);
        // PiP has a fixed depth range. Zoom/GUI scaling must affect X/Y, not clip Z.
        // Lighting still uses the unflattened camera orientation, independent of zoom.
        normals = new Matrix3f(orientation);
        matrix =
                new Matrix4f()
                        .translation(width * guiScale / 2F, height * guiScale / 2F, 0)
                        .scale(scale * guiScale, scale * guiScale, 1)
                        .mul(orientation)
                        .translate(-centerX, -centerY, -centerZ);
        inverse = new Matrix4f(matrix).invert();
    }

    Matrix4f matrix() {
        return new Matrix4f(matrix);
    }

    Matrix3f normalMatrix() {
        return new Matrix3f(normals);
    }

    Vector3f sceneToScreen(Vector3f point) {
        Vector3f projected = matrix.transformPosition(new Vector3f(point));
        projected.x /= guiScale;
        projected.y /= guiScale;
        return projected;
    }

    Vector3f screenToScene(float x, float y, float depth) {
        return inverse.transformPosition(new Vector3f(x * guiScale, y * guiScale, depth));
    }

    static Matrix4f blockMatrix(
            SceneTimeline.GroupPose group,
            float pivotX,
            float pivotY,
            float pivotZ,
            SceneStructure.Position pos) {
        var offset = group.offset();
        var rotation = group.rotation();
        // Same post-multiplication order as Ponder's WorldSectionElementImpl.transformMS.
        return new Matrix4f()
                .translation(offset.x() + pivotX, offset.y() + pivotY, offset.z() + pivotZ)
                .rotateX((float) Math.toRadians(rotation.x()))
                .rotateY((float) Math.toRadians(rotation.y()))
                .rotateZ((float) Math.toRadians(rotation.z()))
                .translate(-pivotX, -pivotY, -pivotZ)
                .translate(pos.x(), pos.y(), pos.z());
    }
}
