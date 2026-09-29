/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Adapted from PonderSceneRenderState; See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.data.scene.SceneTimeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

/** Snapshot in local GUI units; the parent pose is applied only when the PiP image is blitted. */
public record SceneRenderState(
        Matrix3x2f pose,
        RecallScene scene,
        SceneTimeline.Frame frame,
        SceneTransform transform,
        int width,
        int height,
        @Nullable RecallScene.Selection selection,
        float highlightAlpha,
        @Nullable ScreenRectangle scissorArea)
        implements PictureInPictureRenderState {
    @Override
    public int x0() {
        return 0;
    }

    @Override
    public int y0() {
        return 0;
    }

    @Override
    public int x1() {
        return width;
    }

    @Override
    public int y1() {
        return height;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle bounds = new ScreenRectangle(0, 0, width, height).transformMaxBounds(pose);
        return scissorArea == null ? bounds : bounds.intersection(scissorArea);
    }
}
