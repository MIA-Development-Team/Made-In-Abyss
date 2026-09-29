package com.mementoinabyss.recall.client.scene;

import com.mementoinabyss.recall.client.gui.GuiDraw;
import com.mementoinabyss.recall.data.scene.SceneDefinition;
import com.mementoinabyss.recall.data.scene.SceneTimeline;
import com.mementoinabyss.recall.mixin.GuiGraphicsExtractorAccessor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared embedded/fullscreen controller. Input, controls and layout all use local GUI units. */
public final class SceneView implements SceneProgressBar.Playback {
    private static final Logger LOGGER = LoggerFactory.getLogger(SceneView.class);
    private final Identifier id;
    private final ScenePlayback playback = new ScenePlayback();
    private final SceneProgressBar progressBar = new SceneProgressBar(this);
    @Nullable private SceneDefinition definition;
    @Nullable private RecallScene scene;
    private ScreenRectangle controls = ScreenRectangle.empty();

    @Getter(AccessLevel.PACKAGE)
    @Nullable private ScenePresentation presentation;

    private final SceneCamera camera = new SceneCamera();
    private int[] keyframes = new int[0];
    private boolean rotating;

    @Setter(AccessLevel.PACKAGE)
    private boolean expanded;

    private float basePitch = 30;
    @Nullable private RecallScene.Selection outlineSelection;
    private float highlightAlpha;
    private float tickRemainder;
    private long lastFrameMillis;

    public SceneView(Identifier id) {
        this.id = id;
    }

    public void reset() {
        camera.reset();
    }

    public void togglePlayback() {
        playback.toggle();
    }

    public void zoom(double amount) {
        if (scene == null) return;
        ScreenRectangle area = new SceneLayout(controls).viewport();
        float fitScale = scene.fitScale(area.width(), area.height());
        if (fitScale > 0) camera.zoom(amount, fitScale);
    }

    /** Returns true only for the fullscreen button. The owning screen performs navigation. */
    public boolean mouseClicked(double mouseX, double mouseY, boolean doubleClick) {
        rotating = false;
        double x = mouseX - controls.left(), y = mouseY - controls.top();
        if (x < 0 || x >= controls.width() || y < 0 || y >= controls.height()) return false;
        if (y >= 3 && y < 21) {
            if (x >= controls.width() - 22 && x < controls.width() - 3) return true;
            if (x >= controls.width() - 43 && x < controls.width() - 23) {
                playback.toggle();
                return false;
            }
        }
        // Ponder seeks to keyframes on click; it does not scrub continuously or pause on drag.
        if (progressBar.mouseClicked(mouseX, mouseY)) return false;
        if (new SceneLayout(controls).viewport().containsPoint((int) mouseX, (int) mouseY)) {
            rotating = true;
            if (doubleClick) reset();
        }
        return false;
    }

    public void mouseDragged(double dx, double dy) {
        if (rotating) camera.drag(dx, dy, basePitch);
    }

    public void mouseReleased() {
        rotating = false;
    }

    public void extract(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY,
            boolean fullscreen,
            float expansion) {
        var client = Minecraft.getInstance();
        graphics.fill(x, y, x + width, y + height, 0xFF121218);
        // The parent card remains visible behind its expanded view, but this shared
        // scene must not advance its clock or overwrite input bounds a second time.
        if (expanded && !fullscreen) return;
        controls = new ScreenRectangle(x, y, width, height);
        presentation =
                ScenePresentation.capture(
                        controls,
                        new Matrix3x2f(graphics.pose()),
                        graphics.textRenderer().defaultParameters().scissor());
        SceneLayout layout = new SceneLayout(controls);
        progressBar.layout(x + 10, layout.progressY(), Math.max(1, width - 20));
        refreshDefinition();
        if (definition == null || scene == null) {
            graphics.textWithWordWrap(
                    client.font,
                    Component.literal("Scene unavailable: " + id),
                    x + 6,
                    y + 8,
                    Math.max(1, width - 12),
                    0xFFB6B6C0);
            return;
        }

        ScreenRectangle area = layout.viewport();
        Matrix3x2f pose = new Matrix3x2f(graphics.pose()).translate(area.left(), area.top());
        ScreenRectangle scissor = graphics.textRenderer().defaultParameters().scissor();
        ScreenRectangle visible =
                new ScreenRectangle(0, 0, area.width(), area.height()).transformMaxBounds(pose);
        if (scissor != null) visible = visible.intersection(scissor);
        long now = Util.getMillis();
        float delta =
                lastFrameMillis == 0 ? 0 : Math.clamp((now - lastFrameMillis) / 1000F, 0, .05F);
        lastFrameMillis = now;
        float fitScale = scene.fitScale(area.width(), area.height());
        if (fitScale > 0) camera.step(delta, fitScale);
        boolean visibleScene = visible != null && visible.width() > 0 && visible.height() > 0;
        if (visibleScene) playback.advance(delta);
        tickRemainder += delta * 20;
        while (tickRemainder >= 1) {
            progressBar.tick();
            tickRemainder--;
        }

        SceneTimeline.Frame frame =
                definition.timeline().evaluate(playback.getTime(), definition.groups());
        basePitch = frame.pitch();
        drawControls(graphics, frame.title(), frame.caption(), expansion);
        progressBar.extract(graphics, mouseX, mouseY, tickRemainder);
        if (!visibleScene) return;

        SceneTransform transform =
                scene.transform(
                        frame,
                        area.width(),
                        area.height(),
                        client.getWindow().getGuiScale(),
                        camera.getYawOffset(),
                        camera.getPitchOffset(),
                        camera.scale(fitScale));
        float pointerX = (float) mouseX - area.left(), pointerY = (float) mouseY - area.top();
        Vector2f pointerOnScreen =
                graphics.pose().transformPosition((float) mouseX, (float) mouseY, new Vector2f());
        RecallScene.Selection hovered = null;
        if (!rotating
                && pointerX >= 0
                && pointerY >= 0
                && pointerX < area.width()
                && pointerY < area.height()
                && (scissor == null
                        || scissor.containsPoint((int) pointerOnScreen.x, (int) pointerOnScreen.y)))
            hovered = scene.pick(frame, transform, pointerX, pointerY);
        if (hovered != null) outlineSelection = hovered;
        // Frame-rate independent equivalent of Catnip's short outline fade.
        float chase = 1 - (float) Math.exp(-delta * (hovered == null ? 12 : 20));
        highlightAlpha += ((hovered == null ? 0 : 1) - highlightAlpha) * chase;
        if (highlightAlpha < .01F && hovered == null) outlineSelection = null;

        // The PiP blit AND all annotations are clipped to the actual scene band,
        // intersected with the enclosing article, never merely the article rectangle.
        graphics.enableScissor(area.left(), area.top(), area.right(), area.bottom());
        try {
            ((GuiGraphicsExtractorAccessor) graphics)
                    .recall$getGuiRenderState()
                    .addPicturesInPictureState(
                            new SceneRenderState(
                                    pose,
                                    scene,
                                    frame,
                                    transform,
                                    area.width(),
                                    area.height(),
                                    outlineSelection,
                                    highlightAlpha,
                                    visible));
            if (frame.tooltip() != null) {
                var anchor = scene.tooltipAnchor(frame, transform, frame.tooltip().block());
                if (anchor != null
                        && anchor.x >= 0
                        && anchor.y >= 0
                        && anchor.x < area.width()
                        && anchor.y < area.height())
                    drawTextWindow(
                            graphics,
                            area,
                            Component.literal(frame.tooltip().text()),
                            area.left() + anchor.x,
                            area.top() + anchor.y);
            } else if (hovered != null) {
                drawTextWindow(
                        graphics,
                        area,
                        hovered.block().name(),
                        area.left() + hovered.screenPoint().x,
                        area.top() + hovered.screenPoint().y);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private void refreshDefinition() {
        SceneDefinition latest = SceneReloadListener.get(id);
        if (definition == latest) return;
        definition = latest;
        scene = null;
        outlineSelection = null;
        highlightAlpha = tickRemainder = 0;
        camera.clear();
        rotating = false;
        lastFrameMillis = 0;
        playback.reset(latest == null ? 0 : latest.timeline().duration());
        progressBar.reset();
        keyframes =
                latest == null
                        ? new int[0]
                        : latest.timeline().instructions().stream()
                                .mapToInt(instruction -> Math.round(instruction.at() * 20))
                                .filter(tick -> tick > 0 && tick < getTotalTime())
                                .distinct()
                                .sorted()
                                .toArray();
        if (latest != null) {
            try {
                scene = new RecallScene(latest);
            } catch (Exception exception) {
                LOGGER.error("Could not prepare Recall scene {}", id, exception);
            }
        }
    }

    private void drawControls(
            GuiGraphicsExtractor graphics, String title, String caption, float expansion) {
        var font = Minecraft.getInstance().font;
        int x = controls.left(), y = controls.top(), width = controls.width();
        graphics.text(
                font,
                font.plainSubstrByWidth(title, Math.max(1, width - 54)),
                x + 6,
                y + 7,
                0xFFC0C0FF);
        int playX = x + width - 42, fullX = x + width - 21;
        graphics.fill(playX, y + 3, playX + 18, y + 21, 0xFF292935);
        graphics.fill(fullX, y + 3, fullX + 18, y + 21, 0xFF292935);
        if (playback.isPlaying()) {
            graphics.fill(playX + 5, y + 8, playX + 7, y + 16, 0xFFF1F1F1);
            graphics.fill(playX + 11, y + 8, playX + 13, y + 16, 0xFFF1F1F1);
        } else {
            for (int i = 0; i < 6; i++)
                graphics.fill(playX + 6 + i, y + 6 + i, playX + 7 + i, y + 18 - i, 0xFFF1F1F1);
        }
        float inset = 4 + 3 * expansion;
        float iconLeft = fullX + inset, iconTop = y + 3 + inset, iconSize = 18 - 2 * inset;
        int iconColor = 0xFFF1F1F1;
        GuiDraw.gradient(
                graphics,
                iconLeft,
                iconTop,
                iconLeft + iconSize,
                iconTop + 1,
                iconColor,
                iconColor);
        GuiDraw.gradient(
                graphics,
                iconLeft,
                iconTop + iconSize - 1,
                iconLeft + iconSize,
                iconTop + iconSize,
                iconColor,
                iconColor);
        GuiDraw.gradient(
                graphics,
                iconLeft,
                iconTop + 1,
                iconLeft + 1,
                iconTop + iconSize - 1,
                iconColor,
                iconColor);
        GuiDraw.gradient(
                graphics,
                iconLeft + iconSize - 1,
                iconTop + 1,
                iconLeft + iconSize,
                iconTop + iconSize - 1,
                iconColor,
                iconColor);
        if (expansion < 1) {
            int mask = (Math.round(255 * (1 - expansion)) << 24) | 0x292935;
            graphics.fill(fullX + 8, y + 6, fullX + 10, y + 9, mask);
            graphics.fill(fullX + 8, y + 15, fullX + 10, y + 18, mask);
            graphics.fill(fullX + 3, y + 11, fullX + 6, y + 13, mask);
            graphics.fill(fullX + 12, y + 11, fullX + 15, y + 13, mask);
        }
        var lines = font.split(Component.literal(caption), Math.max(1, width - 12));
        for (int i = 0; i < Math.min(2, lines.size()); i++)
            graphics.text(
                    font,
                    lines.get(i),
                    x + 6,
                    new SceneLayout(controls).captionY() + i * 10,
                    0xFFF1F1F1);
    }

    private void drawTextWindow(
            GuiGraphicsExtractor graphics,
            ScreenRectangle area,
            Component text,
            float anchorX,
            float anchorY) {
        var font = Minecraft.getInstance().font;
        var lines = font.split(text, Math.clamp(area.width() - 24, 1, 180));
        int count = Math.clamp((area.height() - 20) / font.lineHeight, 1, lines.size());
        int boxWidth = 8;
        for (int i = 0; i < count; i++) boxWidth = Math.max(boxWidth, font.width(lines.get(i)) + 8);
        boxWidth = Math.clamp(area.width() - 16, 1, boxWidth);
        int boxHeight = count * font.lineHeight + 4;
        int left =
                Math.clamp(
                        Math.round(anchorX + 16),
                        area.left() + 8,
                        Math.max(area.left() + 8, area.right() - boxWidth - 8));
        int top =
                Math.clamp(
                        Math.round(anchorY - boxHeight - 16),
                        area.top() + 8,
                        Math.max(area.top() + 8, area.bottom() - boxHeight - 8));
        float dx = (anchorX < left ? left : left + boxWidth) - anchorX,
                dy = top + boxHeight / 2F - anchorY;
        float length = Mth.sqrt(dx * dx + dy * dy);
        if (length > 1) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(anchorX, anchorY).rotate((float) Mth.atan2(dy, dx));
            GuiDraw.gradient(graphics, 0, 0, length, 1, 0xFFF0D090, 0x607A6000);
            graphics.pose().popMatrix();
        }
        GuiDraw.box(graphics, left, top, boxWidth, boxHeight, 0xFF000000, 0x607A6000, 0x207A6000);
        for (int i = 0; i < count; i++)
            graphics.text(font, lines.get(i), left + 4, top + 2 + i * font.lineHeight, 0xFFFFF0B0);
    }

    @Override
    public float getSceneProgress() {
        return playback.progress();
    }

    @Override
    public int getTotalTime() {
        return definition == null || definition.timeline().duration() == 0
                ? 0
                : Math.max(1, Math.round(definition.timeline().duration() * 20));
    }

    @Override
    public int getCurrentTime() {
        return Math.round(playback.getTime() * 20);
    }

    @Override
    public int getKeyframeCount() {
        return keyframes.length;
    }

    @Override
    public int getKeyframeTime(int index) {
        return keyframes[index];
    }

    @Override
    public void seekToTime(int ticks) {
        if (getTotalTime() > 0) playback.seek((float) ticks / getTotalTime());
    }
}
