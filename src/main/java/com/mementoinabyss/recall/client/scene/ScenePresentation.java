package com.mementoinabyss.recall.client.scene;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

/** A whole-panel transform, including typography and clipping, not just a resized 3D texture. */
record ScenePresentation(
        float x, float y, float width, float height, float scale, ScreenRectangle clip) {
    static ScenePresentation capture(
            ScreenRectangle bounds, Matrix3x2f pose, @Nullable ScreenRectangle scissor) {
        var origin = pose.transformPosition(bounds.left(), bounds.top(), new Vector2f());
        float scale = (float) Math.hypot(pose.m00(), pose.m01());
        var clip = bounds.transformMaxBounds(pose);
        if (scissor != null) clip = clip.intersection(scissor);
        return new ScenePresentation(
                origin.x,
                origin.y,
                bounds.width(),
                bounds.height(),
                scale,
                clip == null ? ScreenRectangle.empty() : clip);
    }

    static ScenePresentation fullscreen(int width, int height) {
        var bounds = new ScreenRectangle(12, 12, Math.max(1, width - 24), Math.max(1, height - 24));
        return new ScenePresentation(12, 12, bounds.width(), bounds.height(), 1, bounds);
    }

    ScenePresentation interpolate(ScenePresentation target, float progress) {
        float scale = mix(this.scale, target.scale, progress);
        return new ScenePresentation(
                mix(x, target.x, progress),
                mix(y, target.y, progress),
                mix(width * this.scale, target.width * target.scale, progress) / scale,
                mix(height * this.scale, target.height * target.scale, progress) / scale,
                scale,
                new ScreenRectangle(
                        Math.round(mix(clip.left(), target.clip.left(), progress)),
                        Math.round(mix(clip.top(), target.clip.top(), progress)),
                        Math.max(0, Math.round(mix(clip.width(), target.clip.width(), progress))),
                        Math.max(
                                0,
                                Math.round(mix(clip.height(), target.clip.height(), progress)))));
    }

    double localX(double screenX) {
        return (screenX - x) / scale;
    }

    double localY(double screenY) {
        return (screenY - y) / scale;
    }

    private static float mix(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}
