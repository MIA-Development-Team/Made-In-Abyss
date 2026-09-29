package com.mementoinabyss.recall.client.scene;

import lombok.AccessLevel;
import lombok.Getter;

/** Playback remains independent of rendering, so embedded and fullscreen views share one clock. */
final class ScenePlayback {
    private float duration;

    @Getter(AccessLevel.PACKAGE)
    private float time;

    @Getter(AccessLevel.PACKAGE)
    private boolean playing;

    void reset(float duration) {
        this.duration = duration;
        time = 0;
        playing = duration > 0;
    }

    void advance(float seconds) {
        if (!playing) return;
        time = Math.min(duration, time + Math.max(0, seconds));
        if (time == duration) playing = false;
    }

    void toggle() {
        if (duration == 0) return;
        if (!playing && time >= duration) time = 0;
        playing = !playing;
    }

    void seek(float fraction) {
        time = Math.clamp(fraction, 0, 1) * duration;
    }

    float progress() {
        return duration == 0 ? 1 : time / duration;
    }
}
