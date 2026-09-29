/*
 * Portions Copyright (c) 2022 The Create Team.
 * Adapted from Ponder/Catnip mc26.1/dev, ff9313616c963e94a14065d49f708e75e1b9e62a.
 * SPDX-License-Identifier: MIT
 * See META-INF/licenses/ponder-MIT.txt.
 */
package com.mementoinabyss.recall.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record GradientRectRenderState(
        Matrix3x2f pose,
        float left,
        float top,
        float right,
        float bottom,
        int startColor,
        int endColor,
        boolean horizontal,
        @Nullable ScreenRectangle scissorArea)
        implements GuiElementRenderState {
    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    public GradientRectRenderState(
            Matrix3x2f pose,
            float left,
            float top,
            float right,
            float bottom,
            int startColor,
            int endColor,
            @Nullable ScreenRectangle scissorArea) {
        this(pose, left, top, right, bottom, startColor, endColor, false, scissorArea);
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        if (horizontal) {
            consumer.addVertexWith2DPose(pose, right, top).setColor(endColor);
            consumer.addVertexWith2DPose(pose, left, top).setColor(startColor);
            consumer.addVertexWith2DPose(pose, left, bottom).setColor(startColor);
            consumer.addVertexWith2DPose(pose, right, bottom).setColor(endColor);
        } else {
            consumer.addVertexWith2DPose(pose, right, top).setColor(startColor);
            consumer.addVertexWith2DPose(pose, left, top).setColor(startColor);
            consumer.addVertexWith2DPose(pose, left, bottom).setColor(endColor);
            consumer.addVertexWith2DPose(pose, right, bottom).setColor(endColor);
        }
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle bounds =
                new ScreenRectangle(
                                (int) Math.floor(left),
                                (int) Math.floor(top),
                                (int) Math.ceil(right) - (int) Math.floor(left),
                                (int) Math.ceil(bottom) - (int) Math.floor(top))
                        .transformMaxBounds(pose);
        return scissorArea == null ? bounds : bounds.intersection(scissorArea);
    }
}
