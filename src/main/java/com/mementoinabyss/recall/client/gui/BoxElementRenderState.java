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

public record BoxElementRenderState(
        Matrix3x2f pose,
        float x,
        float y,
        float width,
        float height,
        int f,
        int c1,
        int c2,
        int c3,
        @Nullable ScreenRectangle scissorArea)
        implements GuiElementRenderState {
    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    // total box width = 1 * 2 (outer border) + 1 * 2 (inner color border) + 2 * borderOffset +
    // width
    // defaults to 2 + 2 + 4 + 16 = 24px
    @Override
    public void buildVertices(VertexConsumer consumer) {

        // outer top
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f - 2).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 2).setColor(c1);
        // outer left
        consumer.addVertexWith2DPose(pose, x - f - 2, y - f - 1).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 2, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1);
        // outer bottom
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + 2 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 2 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1);
        // outer right
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 2 + width, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 2 + width, y - f - 1).setColor(c1);
        // inner background - also render behind the inner edges
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c1);
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c1);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c1);
        // inner top - includes corners
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f - 1).setColor(c2);
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f).setColor(c2);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f).setColor(c2);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f - 1).setColor(c2);
        // inner left - excludes corners
        consumer.addVertexWith2DPose(pose, x - f - 1, y - f).setColor(c2);
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x - f, y + f + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x - f, y - f).setColor(c2);
        // inner bottom - includes corners
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x - f - 1, y + f + 1 + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + 1 + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + height).setColor(c3);
        // inner right - excludes corners
        consumer.addVertexWith2DPose(pose, x + f + width, y - f).setColor(c2);
        consumer.addVertexWith2DPose(pose, x + f + width, y + f + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y + f + height).setColor(c3);
        consumer.addVertexWith2DPose(pose, x + f + 1 + width, y - f).setColor(c2);
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
        int left = (int) Math.floor(x - f - 2);
        int top = (int) Math.floor(y - f - 2);
        ScreenRectangle bounds =
                new ScreenRectangle(
                                left,
                                top,
                                (int) Math.ceil(x + width + f + 2) - left,
                                (int) Math.ceil(y + height + f + 2) - top)
                        .transformMaxBounds(pose);
        return scissorArea == null ? bounds : bounds.intersection(scissorArea);
    }
}
