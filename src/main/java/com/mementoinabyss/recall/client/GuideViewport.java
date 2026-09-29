package com.mementoinabyss.recall.client;

/** Only explicit view movement toward an edge can automatically collapse a card. */
final class GuideViewport {
    private static final float EDGE = 4;

    static boolean shouldCollapse(Bounds before, Bounds after, float width, float height) {
        return (after.left <= EDGE && after.left < before.left)
                || (after.top <= EDGE && after.top < before.top)
                || (after.right >= width - EDGE && after.right > before.right)
                || (after.bottom >= height - EDGE && after.bottom > before.bottom);
    }

    record Bounds(float left, float top, float right, float bottom) {}

    private GuideViewport() {}
}
