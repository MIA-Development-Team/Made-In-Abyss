/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Ported from PonderTooltipHandler; See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client;

/** Ponder's 20 Hz hold-to-open ramp, release decay, and slightly accelerated visual fill. */
final class RecallHoldProgress {
    private float previous;
    private float value;

    boolean tick(boolean holding) {
        if (value >= 1) {
            reset();
            return true;
        }
        previous = value;
        value =
                holding
                        ? Math.min(1, value + Math.max(.25F, value) * .25F)
                        : Math.max(0, value - .05F);
        return false;
    }

    float visual(float partialTick) {
        return Math.min(1, (previous + (value - previous) * Math.clamp(partialTick, 0, 1)) * 8 / 7);
    }

    void reset() {
        previous = value = 0;
    }
}
