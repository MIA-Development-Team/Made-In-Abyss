package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Client-owned, short-lived direction marker shown after using the Star Compass. */
public final class StarCompassOverlay {
    private static final Identifier ICON = MementoInAbyss.asResource("world/compass_icon");
    private static final int ICON_SIZE = 16;
    private static final long FADE_IN_MILLIS = 800;
    private static final long DISPLAY_MILLIS = 15_000;
    private static final long FADE_OUT_MILLIS = 3_000;

    private static @Nullable BlockPos target;
    private static long receivedAt;

    public static void accept(@Nullable BlockPos newTarget) {
        target = newTarget;
        receivedAt = Util.getMillis();
    }

    public static void clear() {
        target = null;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos currentTarget = target;
        if (minecraft.player == null || minecraft.level == null || currentTarget == null) {
            return;
        }

        float alpha = alphaAt(Util.getMillis() - receivedAt);
        if (alpha <= 0.0F) {
            clear();
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float partialTick = deltaTracker.getGameTimeDeltaTicks();
        float[] projected = project(
                Vec3.atCenterOf(currentTarget),
                minecraft.player.getEyePosition(partialTick),
                minecraft.player.getYRot(),
                minecraft.player.getXRot(),
                width,
                height,
                minecraft.options.fov().get()
        );

        float screenX = projected[0];
        float screenY = projected[1];
        boolean behindCamera = projected[2] < 0.0F;
        if (behindCamera || screenX < 0 || screenX > width || screenY < 0 || screenY > height) {
            float centerX = width / 2.0F;
            float centerY = height / 2.0F;
            float directionX = screenX - centerX;
            float directionY = screenY - centerY;
            if (behindCamera) {
                directionX = -directionX;
                directionY = -directionY;
            }

            float length = (float) Math.hypot(directionX, directionY);
            if (length < 1.0E-3F) {
                directionX = 0.0F;
                directionY = -1.0F;
            } else {
                directionX /= length;
                directionY /= length;
            }

            float margin = ICON_SIZE / 2.0F + 10.0F;
            float maxX = Math.abs(directionX) < 1.0E-3F
                    ? Float.POSITIVE_INFINITY
                    : (width / 2.0F - margin) / Math.abs(directionX);
            float maxY = Math.abs(directionY) < 1.0E-3F
                    ? Float.POSITIVE_INFINITY
                    : (height / 2.0F - margin) / Math.abs(directionY);
            float edgeDistance = Math.min(maxX, maxY);
            screenX = centerX + directionX * edgeDistance;
            screenY = centerY + directionY * edgeDistance;
        }

        double distance = Math.sqrt(currentTarget.distSqr(minecraft.player.blockPosition()));
        renderMarker(graphics, Math.round(screenX), Math.round(screenY), alpha, distance);
    }

    private static float alphaAt(long elapsedMillis) {
        if (elapsedMillis < FADE_IN_MILLIS) {
            return Math.clamp((float) elapsedMillis / FADE_IN_MILLIS, 0.0F, 1.0F);
        }
        if (elapsedMillis <= DISPLAY_MILLIS) {
            return 1.0F;
        }
        return Math.clamp(
                1.0F - (float) (elapsedMillis - DISPLAY_MILLIS) / FADE_OUT_MILLIS,
                0.0F,
                1.0F
        );
    }

    private static void renderMarker(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            float alpha,
            double distance
    ) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ICON,
                x - ICON_SIZE / 2,
                y - ICON_SIZE / 2,
                ICON_SIZE,
                ICON_SIZE,
                alpha
        );

        Minecraft minecraft = Minecraft.getInstance();
        String distanceText = "%.1f m".formatted(distance);
        int color = Math.round(alpha * 255.0F) << 24 | 0xFFFFFF;
        graphics.text(
                minecraft.font,
                distanceText,
                x - minecraft.font.width(distanceText) / 2,
                y + ICON_SIZE / 2 + 5,
                color,
                true
        );
    }

    private static float[] project(
            Vec3 worldPosition,
            Vec3 cameraPosition,
            float yaw,
            float pitch,
            int width,
            int height,
            float fov
    ) {
        Vec3 relative = worldPosition.subtract(cameraPosition);
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);

        double cameraX = relative.x * Math.cos(yawRadians) + relative.z * Math.sin(yawRadians);
        double yawDepth = -relative.x * Math.sin(yawRadians) + relative.z * Math.cos(yawRadians);
        double cameraY = relative.y * Math.cos(pitchRadians) + yawDepth * Math.sin(pitchRadians);
        double cameraDepth = -relative.y * Math.sin(pitchRadians) + yawDepth * Math.cos(pitchRadians);
        if (Math.abs(cameraDepth) < 0.01) {
            cameraDepth = Math.copySign(0.01, cameraDepth);
        }

        double tanHalfFov = Math.tan(Math.toRadians(fov / 2.0F));
        double aspectRatio = (double) width / height;
        float screenX = (float) ((-cameraX / (cameraDepth * tanHalfFov * aspectRatio) + 1.0) * width / 2.0);
        float screenY = (float) ((-cameraY / (cameraDepth * tanHalfFov) + 1.0) * height / 2.0);
        return new float[]{screenX, screenY, (float) cameraDepth};
    }

    private StarCompassOverlay() {}
}
