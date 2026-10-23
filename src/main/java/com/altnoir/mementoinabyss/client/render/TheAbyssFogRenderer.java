package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jspecify.annotations.Nullable;

public final class TheAbyssFogRenderer {
    private static final double CLEAR_HEIGHT = 32.0;
    private static final double DENSE_HEIGHT = -128.0;

    public static void renderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.ATMOSPHERIC
                || !(event.getEnvironment() instanceof AtmosphericFogEnvironment)) return;

        applyFog(
                event.getFogData(),
                strength(
                        Minecraft.getInstance().level,
                        event.getCamera(),
                        event.getType(),
                        (float) event.getPartialTick()));
    }

    public static float strength(
            @Nullable ClientLevel level, Camera camera, FogType fogType, float partialTicks) {
        if (!MementoInAbyss.CONFIGS.graphsSection.abyssFogEnabled.get()
                || level == null
                || !level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)
                || camera.position().y >= CLEAR_HEIGHT
                || fogType != FogType.NONE && fogType != FogType.ATMOSPHERIC
                || Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) return 0.0F;

        if (camera.entity() instanceof LivingEntity entity
                && (entity.hasEffect(MobEffects.BLINDNESS)
                        || entity.hasEffect(MobEffects.DARKNESS))) return 0.0F;

        float density =
                camera.attributeProbe()
                        .getValue(MiaEnvironmentAttributes.ABYSS_FOG_DENSITY, partialTicks);
        float heightFactor =
                (float)
                        Mth.clamp(
                                (CLEAR_HEIGHT - camera.position().y)
                                        / (CLEAR_HEIGHT - DENSE_HEIGHT),
                                0.0,
                                1.0);
        return heightFactor * density;
    }

    static void applyFog(FogData fog, float strength) {
        if (strength <= 0.0F) return;

        float denseEnd = Math.min(fog.renderDistanceEnd, 192.0F) * 0.5F;
        if (denseEnd <= 0.0F || fog.environmentalEnd <= denseEnd) return;

        // Blend extinction rather than distances so clear areas retain distant LOD visibility.
        float end = 1.0F / Mth.lerp(strength, 1.0F / fog.environmentalEnd, 1.0F / denseEnd);
        float denseStart = Math.min(fog.renderDistanceEnd * 0.05F, denseEnd - 1.0F);
        // AtmosphericFogEnvironment has already applied the rain offset to the incoming start.
        float start =
                Mth.lerp(
                        strength,
                        fog.environmentalStart,
                        Math.min(fog.environmentalStart, 0.0F) + denseStart);

        fog.environmentalStart = Math.min(start, end - 0.001F);
        fog.environmentalEnd = end;
        fog.skyEnd = Math.min(fog.skyEnd, end);
        fog.cloudEnd = Math.min(fog.cloudEnd, end);
    }

    private TheAbyssFogRenderer() {}
}
