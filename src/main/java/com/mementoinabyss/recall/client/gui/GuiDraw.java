/* Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Adapted from Catnip BoxElement and UIRenderHelper; See META-INF/licenses/ponder-MIT.txt. */
package com.mementoinabyss.recall.client.gui;

import com.mementoinabyss.recall.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** The two upstream GUI primitives used by Recall, without Catnip's unrelated widget framework. */
public final class GuiDraw {
    public static void box(
            GuiGraphicsExtractor graphics,
            float x,
            float y,
            float width,
            float height,
            int background,
            int top,
            int bottom) {
        ((GuiGraphicsExtractorAccessor) graphics)
                .recall$getGuiRenderState()
                .addGuiElement(
                        new BoxElementRenderState(
                                new Matrix3x2f(graphics.pose()),
                                x,
                                y,
                                width,
                                height,
                                2,
                                background,
                                top,
                                bottom,
                                graphics.textRenderer().defaultParameters().scissor()));
    }

    public static void gradient(
            GuiGraphicsExtractor graphics,
            float left,
            float top,
            float right,
            float bottom,
            int startColor,
            int endColor) {
        addGradient(graphics, left, top, right, bottom, startColor, endColor, false);
    }

    public static void horizontalGradient(
            GuiGraphicsExtractor graphics,
            float left,
            float top,
            float right,
            float bottom,
            int leftColor,
            int rightColor) {
        addGradient(graphics, left, top, right, bottom, leftColor, rightColor, true);
    }

    private static void addGradient(
            GuiGraphicsExtractor graphics,
            float left,
            float top,
            float right,
            float bottom,
            int startColor,
            int endColor,
            boolean horizontal) {
        if (right <= left || bottom <= top) return;
        ((GuiGraphicsExtractorAccessor) graphics)
                .recall$getGuiRenderState()
                .addGuiElement(
                        new GradientRectRenderState(
                                new Matrix3x2f(graphics.pose()),
                                left,
                                top,
                                right,
                                bottom,
                                startColor,
                                endColor,
                                horizontal,
                                graphics.textRenderer().defaultParameters().scissor()));
    }

    private GuiDraw() {}
}
