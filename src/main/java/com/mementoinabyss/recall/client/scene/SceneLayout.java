package com.mementoinabyss.recall.client.scene;

import net.minecraft.client.gui.navigation.ScreenRectangle;

/** Disjoint scene, subtitle and progress bands, shared by rendering and pointer input. */
record SceneLayout(ScreenRectangle bounds) {
    ScreenRectangle viewport() {
        return new ScreenRectangle(
                bounds.left() + 3,
                bounds.top() + 23,
                Math.max(0, bounds.width() - 6),
                Math.max(0, bounds.height() - 79));
    }

    int captionY() {
        return bounds.bottom() - 50;
    }

    int progressY() {
        return bounds.bottom() - 23;
    }
}
