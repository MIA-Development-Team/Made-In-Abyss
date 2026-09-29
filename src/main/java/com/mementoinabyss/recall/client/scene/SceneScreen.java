package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.client.GuideSpring;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

/** Expands the existing scene viewport into a fullscreen presentation and reverses on close. */
public final class SceneScreen extends Screen {
    private final Screen parent;
    private final SceneView scene;
    @Nullable private final ScenePresentation origin;
    private final GuideSpring expansion = new GuideSpring(.36F);
    @Nullable private ScenePresentation presentation;
    private long lastFrameMillis;
    private boolean dragging;
    private boolean closing;

    public SceneScreen(Screen parent, SceneView scene) {
        super(Component.literal("Guide scene"));
        this.parent = parent;
        this.scene = scene;
        origin = scene.getPresentation();
        scene.setExpanded(true);
        expansion.snap(0);
        expansion.setTarget(1);
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        parent.extractBackground(graphics, -1, -1, partialTick);
        graphics.nextStratum();
        long now = Util.getMillis();
        expansion.step(
                lastFrameMillis == 0 ? 0 : Math.clamp((now - lastFrameMillis) / 1000F, 0, .1F));
        lastFrameMillis = now;
        var target = ScenePresentation.fullscreen(width, height);
        float progress = Math.clamp(expansion.getValue(), 0, 1);
        presentation = (origin == null ? target : origin).interpolate(target, progress);
        graphics.fill(0, 0, width, height, ((int) (0xD0 * progress) << 24) | 0x101012);
        var clip = presentation.clip();
        graphics.enableScissor(clip.left(), clip.top(), clip.right(), clip.bottom());
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(presentation.x(), presentation.y());
            graphics.pose().scale(presentation.scale());
            scene.extract(
                    graphics,
                    0,
                    0,
                    Math.max(1, Math.round(presentation.width())),
                    Math.max(1, Math.round(presentation.height())),
                    presentation.localX(mouseX),
                    presentation.localY(mouseY),
                    true,
                    progress);
        } finally {
            graphics.pose().popMatrix();
            graphics.disableScissor();
        }
    }

    @Override
    public void removed() {
        scene.setExpanded(false);
        scene.mouseReleased();
    }

    @Override
    public void tick() {
        if (closing && expansion.isSettled()) minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return true;
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);
        if (presentation == null
                || !presentation.clip().containsPoint((int) event.x(), (int) event.y()))
            return true;
        if (scene.mouseClicked(
                presentation.localX(event.x()), presentation.localY(event.y()), doubleClick))
            onClose();
        else dragging = true;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!dragging || closing || presentation == null) return super.mouseDragged(event, dx, dy);
        scene.mouseDragged(dx / presentation.scale(), dy / presentation.scale());
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        scene.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!closing && presentation != null && presentation.clip().containsPoint((int) x, (int) y))
            scene.zoom(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_SPACE && !closing) {
            scene.togglePlayback();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        scene.mouseReleased();
        dragging = false;
        closing = true;
        expansion.setTarget(0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return false;
    }
}
