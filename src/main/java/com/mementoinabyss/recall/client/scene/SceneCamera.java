package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.client.GuideSpring;
import lombok.AccessLevel;
import lombok.Getter;

/** Orbit input and continuous zoom; reversing the wheel retargets without discarding velocity. */
final class SceneCamera {
    private static final float MIN_BLOCK_PIXELS = 2;
    private static final float MAX_BLOCK_PIXELS = 48;
    private final GuideSpring zoom = new GuideSpring(.28F);

    @Getter(AccessLevel.PACKAGE)
    private float yawOffset;

    @Getter(AccessLevel.PACKAGE)
    private float pitchOffset;

    SceneCamera() {
        zoom.snap(1);
    }

    void drag(double dx, double dy, float basePitch) {
        yawOffset = (yawOffset + (float) dx * .7F) % 360;
        pitchOffset = Math.clamp(basePitch + pitchOffset + (float) dy * .5F, 10, 80) - basePitch;
    }

    void zoom(double amount, float fitScale) {
        float lower = MIN_BLOCK_PIXELS / fitScale;
        float upper = MAX_BLOCK_PIXELS / fitScale;
        float target = Math.clamp(zoom.getTarget(), lower, upper);
        zoom.setTarget(Math.clamp((float) (target * Math.pow(1.2, amount)), lower, upper));
    }

    void step(float delta, float fitScale) {
        zoom.setTarget(
                Math.clamp(
                        zoom.getTarget(),
                        MIN_BLOCK_PIXELS / fitScale,
                        MAX_BLOCK_PIXELS / fitScale));
        zoom.step(delta);
    }

    void reset() {
        yawOffset = pitchOffset = 0;
        zoom.setTarget(1);
    }

    void clear() {
        reset();
        zoom.snap(1);
    }

    float scale(float fitScale) {
        return Math.clamp(fitScale * zoom.getValue(), MIN_BLOCK_PIXELS, MAX_BLOCK_PIXELS);
    }
}
