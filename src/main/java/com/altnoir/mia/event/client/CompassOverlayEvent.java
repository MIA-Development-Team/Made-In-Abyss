package com.altnoir.mia.event.client;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CompassOverlayEvent {
    private static final ResourceLocation COMPASS_ICON = MiaUtil.miaId("textures/gui/sprites/world/compass_icon.png");
    private static final int ICON_SIZE = 16;

    @Nullable
    private static BlockPos targetPosition = null;

    private static long lastTargetTime = 0;
    private static final long FADE_IN_DURATION = 800;
    private static final long DISPLAY_DURATION = 15000;
    private static final long FADE_DURATION = 3000;

    public static void setTargetPosition(BlockPos pos) {
        targetPosition = pos;
        lastTargetTime = System.currentTimeMillis();
    }

    public static void clearTargetPosition() {
        targetPosition = null;
    }

    public static void onRenderGui(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || targetPosition == null) return;

        var elapsed = System.currentTimeMillis() - lastTargetTime;
        float alpha;
        if (elapsed < FADE_IN_DURATION) {
            alpha = Math.min(1f, (float) elapsed / (float) FADE_IN_DURATION);
        } else if (elapsed <= DISPLAY_DURATION) {
            alpha = 1f;
        } else {
            alpha = Math.max(0f, 1f - (float) (elapsed - DISPLAY_DURATION) / (float) FADE_DURATION);
            if (alpha <= 0f) {
                targetPosition = null;
                return;
            }
        }

        var screenWidth = guiGraphics.guiWidth();
        var screenHeight = guiGraphics.guiHeight();

        var partialTick = deltaTracker.getGameTimeDeltaTicks();
        var cameraPos = mc.player.getEyePosition(partialTick);
        var targetVec = Vec3.atCenterOf(targetPosition);
        var yaw = mc.player.getYRot();
        var pitch = mc.player.getXRot();

        var result = projectToScreen(targetVec, cameraPos, yaw, pitch, screenWidth, screenHeight);
        var screenX = result[0];
        var screenY = result[1];
        var isBehind = result[2] < 0;

        if (isBehind || screenX < 0 || screenX > screenWidth || screenY < 0 || screenY > screenHeight) {
            var centerX = screenWidth / 2f;
            var centerY = screenHeight / 2f;
            var dirX = screenX - centerX;
            var dirY = screenY - centerY;
            if (isBehind) {
                dirX = -dirX;
                dirY = -dirY;
            }
            final float EPSILON = 1e-3f;
            var len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (len < EPSILON) {
                dirX = 0f;
                dirY = -1f;
            } else {
                dirX /= len;
                dirY /= len;
            }
            var margin = ICON_SIZE / 2 + 10;
            float maxX;
            if (Math.abs(dirX) < EPSILON) {
                maxX = Float.POSITIVE_INFINITY;
            } else {
                maxX = (screenWidth / 2f - margin) / Math.abs(dirX);
            }
            float maxY;
            if (Math.abs(dirY) < EPSILON) {
                maxY = Float.POSITIVE_INFINITY;
            } else {
                maxY = (screenHeight / 2f - margin) / Math.abs(dirY);
            }
            var maxDist = Math.min(maxX, maxY);
            screenX = centerX + dirX * maxDist;
            screenY = centerY + dirY * maxDist;
        }

        // 距离计算
        var playerPos = mc.player.blockPosition();
        var distance = Math.sqrt(
                Math.pow(targetPosition.getX() - playerPos.getX(), 2) +
                        Math.pow(targetPosition.getY() - playerPos.getY(), 2) +
                        Math.pow(targetPosition.getZ() - playerPos.getZ(), 2)
        );

        renderIcon(guiGraphics, (int) screenX, (int) screenY, alpha, distance);
    }

    private static void renderIcon(GuiGraphics graphics, int x, int y, float alpha, double distance) {
        int iconX = x - ICON_SIZE / 2;
        int iconY = y - ICON_SIZE / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.setColor(1f, 1f, 1f, alpha);
        graphics.blit(COMPASS_ICON, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.setColor(1f, 1f, 1f, 1f);

        // 距离文本
        Font font = Minecraft.getInstance().font;
        String distanceText = String.format("%.1f M", distance);
        int textWidth = font.width(distanceText);
        int textX = x - textWidth / 2;
        int textY = y + ICON_SIZE / 2 + 5;

        graphics.drawString(font, Component.literal(distanceText), textX, textY, 0xFFFFFF, true);
        graphics.pose().popPose();

        RenderSystem.disableBlend();
    }

    private static float[] projectToScreen(Vec3 worldPos, Vec3 cameraPos, float yaw, float pitch,
                                           int screenWidth, int screenHeight) {
        var relative = worldPos.subtract(cameraPos);
        var yawRad = Math.toRadians(yaw);
        var pitchRad = Math.toRadians(pitch);

        var x1 = relative.x * Math.cos(yawRad) + relative.z * Math.sin(yawRad);
        var z1 = -relative.x * Math.sin(yawRad) + relative.z * Math.cos(yawRad);
        var y1 = relative.y;

        var y2 = y1 * Math.cos(pitchRad) + z1 * Math.sin(pitchRad);
        var z2 = -y1 * Math.sin(pitchRad) + z1 * Math.cos(pitchRad);

        var mc = Minecraft.getInstance();
        var fov = mc.options.fov().get().floatValue();
        var tanHalfFov = Math.tan(Math.toRadians(fov / 2.0));
        var aspect = (double) screenWidth / screenHeight;

        if (Math.abs(z2) < 0.01) z2 = 0.01;

        var screenX = (-x1 / (z2 * tanHalfFov * aspect)) * (screenWidth / 2.0) + (screenWidth / 2.0);
        var screenY = (-y2 / (z2 * tanHalfFov)) * (screenHeight / 2.0) + (screenHeight / 2.0);

        return new float[]{(float) screenX, (float) screenY, (float) z2};
    }
}