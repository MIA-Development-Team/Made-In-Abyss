/*
 * Portions Copyright (c) 2022 The Create Team. SPDX-License-Identifier: MIT
 * Ported from PonderProgressBar (mc26.1/dev, ff9313616c963e94a14065d49f708e75e1b9e62a).
 * See META-INF/licenses/ponder-MIT.txt.
 */
package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.client.gui.GuiDraw;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Ponder's one-pixel progress bar, keyframe selection, markers and seek arrows. */
final class SceneProgressBar {
    interface Playback {
        float getSceneProgress();

        int getTotalTime();

        int getCurrentTime();

        int getKeyframeCount();

        int getKeyframeTime(int index);

        void seekToTime(int ticks);
    }

    private final Playback playback;
    private float previousProgress;
    private float progress;
    private int x, y, width;
    private static final int HEIGHT = 1;

    SceneProgressBar(Playback playback) {
        this.playback = playback;
    }

    void layout(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = Math.max(1, width);
    }

    void reset() {
        previousProgress = progress = 0;
    }

    void tick() {
        // Catnip LerpedFloat.linear(), chase(target, .5f, Chaser.EXP), tickChaser().
        previousProgress = progress;
        float target = playback.getSceneProgress();
        progress =
                Mth.equal((double) progress, target)
                        ? target
                        : (float) (progress + ((double) target - progress) * .5);
    }

    float interpolatedProgress(float partialTicks) {
        return (float) Mth.lerp(partialTicks, (double) previousProgress, progress);
    }

    boolean isMouseOver(double mouseX, double mouseY) {
        return playback.getKeyframeCount() > 0
                && playback.getTotalTime() > 0
                && mouseX >= x
                && mouseX < x + width + 4
                && mouseY >= y - 3
                && mouseY < y + HEIGHT + 20;
    }

    boolean mouseClicked(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        int index = getHoveredKeyframeIndex(mouseX);
        if (index == -1) playback.seekToTime(0);
        else if (index == playback.getKeyframeCount()) playback.seekToTime(playback.getTotalTime());
        else playback.seekToTime(playback.getKeyframeTime(index));
        return true;
    }

    int getHoveredKeyframeIndex(double mouseX) {
        if (playback.getKeyframeCount() == 0 || playback.getTotalTime() <= 0) return -1;
        int clickedAtTime = (int) ((mouseX - x) / ((double) width + 4) * playback.getTotalTime());
        int lastKeyframeTime = playback.getKeyframeTime(playback.getKeyframeCount() - 1);
        int diffToEnd = playback.getTotalTime() - clickedAtTime;
        int diffToLast = clickedAtTime - lastKeyframeTime;
        if (diffToEnd > 0 && diffToEnd < diffToLast / 2) return playback.getKeyframeCount();
        int index = -1;
        for (int i = 0; i < playback.getKeyframeCount(); i++) {
            if (playback.getKeyframeTime(i) > clickedAtTime) break;
            index = i;
        }
        return index;
    }

    void extract(GuiGraphicsExtractor graphics, double mouseX, double mouseY, float partialTicks) {
        var pose = graphics.pose();
        boolean hovered = isMouseOver(mouseX, mouseY);
        GuiDraw.box(graphics, x, y, width, HEIGHT, 0xFF000000, 0x40FFEEDD, 0x20FFEEDD);
        pose.pushMatrix();
        pose.translate(x - 2, y - 2);
        pose.pushMatrix();
        pose.scale((width + 4) * interpolatedProgress(partialTicks), 1);
        GuiDraw.gradient(graphics, 0, 1, 1, 3, 0x80AAAADD, 0x80AAAADD);
        GuiDraw.gradient(graphics, 0, 3, 1, 4, 0x50AAAADD, 0x50AAAADD);
        pose.popMatrix();

        int hoverIndex = hovered ? getHoveredKeyframeIndex(mouseX) : -2;
        if (hoverIndex == -1) drawKeyframe(graphics, true, 0, 0, 0xE0FFFFFF, 0xE0FFFFFF, 8);
        else if (hoverIndex == playback.getKeyframeCount() && hovered)
            drawKeyframe(
                    graphics, true, playback.getTotalTime(), width + 4, 0xE0FFFFFF, 0xE0FFFFFF, 8);
        if (playback.getTotalTime() > 0) {
            for (int i = 0; i < playback.getKeyframeCount(); i++) {
                int time = playback.getKeyframeTime(i);
                int pos = (int) ((float) time / playback.getTotalTime() * (width + 2));
                boolean selected = i == hoverIndex;
                // Ponder applies the same alpha to both COLOR_HOVER colors here.
                int color = selected ? 0xE0FFFFFF : 0x70FFFFFF;
                drawKeyframe(graphics, selected, time, pos, color, color, selected ? 8 : 4);
            }
        }
        pose.popMatrix();
    }

    private void drawKeyframe(
            GuiGraphicsExtractor graphics,
            boolean selected,
            int keyframeTime,
            int keyframePos,
            int startColor,
            int endColor,
            int height) {
        if (selected) {
            var font = Minecraft.getInstance().font;
            GuiDraw.gradient(
                    graphics, keyframePos, 9, keyframePos + 2, 9 + height, endColor, startColor);
            graphics.pose().pushMatrix();
            String text;
            int offset;
            if (playback.getCurrentTime() < keyframeTime) {
                text = ">";
                offset = -2 - font.width(text);
            } else {
                text = "<";
                offset = 4;
            }
            graphics.text(
                    font,
                    Component.literal(text).withStyle(ChatFormatting.BOLD),
                    keyframePos + offset,
                    10,
                    endColor,
                    false);
            graphics.pose().popMatrix();
        }
        GuiDraw.gradient(
                graphics, keyframePos, 0, keyframePos + 2, 1 + height, startColor, endColor);
    }
}
