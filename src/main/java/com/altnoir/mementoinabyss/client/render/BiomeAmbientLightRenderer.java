package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.lighting.BiomeAmbientLightSampler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class BiomeAmbientLightRenderer {
    private static @Nullable ClientLevel cachedLevel;
    private static @Nullable BiomeAmbientLightSampler sampler;
    private static @Nullable Vector3fc nativeAmbient;
    private static boolean applied;

    public static void extract(ExtractLevelRenderStateEvent event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != event.getLevel()) return;
        var state = minecraft.gameRenderer.getGameRenderState().lightmapRenderState;
        // Vanilla refreshes this value once per tick; never feed our previous frame back into it.
        if (state.needsUpdate || nativeAmbient == null) {
            nativeAmbient = state.ambientColor;
            // Refresh on native lightmap ticks so newly received chunks cannot leave stale corners.
            if (sampler != null) sampler.invalidate();
        }
        if (cachedLevel != event.getLevel()) {
            cachedLevel = event.getLevel();
            var attribute = MiaEnvironmentAttributes.AMBIENT_LIGHT_BIOME;
            var settings =
                    cachedLevel
                            .dimensionType()
                            .attributes()
                            .applyModifier(attribute, attribute.defaultValue());
            sampler =
                    settings.values().isEmpty()
                            ? null
                            : new BiomeAmbientLightSampler(
                                    settings,
                                    cachedLevel.registryAccess().lookupOrThrow(Registries.BIOME),
                                    cachedLevel.getBiomeManager());
        }
        if (sampler == null || sampler.isEmpty()) {
            restore(state);
            return;
        }
        sampler.sample(event.getCamera().position());
        if (sampler.weight() <= 0.0F) {
            restore(state);
            return;
        }
        float brightness = sampler.weightedBrightness();
        float remaining = 1.0F - sampler.weight();
        float red = nativeAmbient.x() * remaining + brightness;
        float green = nativeAmbient.y() * remaining + brightness;
        float blue = nativeAmbient.z() * remaining + brightness;
        if (state.ambientColor.x() != red
                || state.ambientColor.y() != green
                || state.ambientColor.z() != blue) {
            state.ambientColor = new Vector3f(red, green, blue);
            state.needsUpdate = true;
        }
        applied = true;
    }

    private static void restore(LightmapRenderState state) {
        if (!applied) return;
        state.ambientColor = nativeAmbient;
        state.needsUpdate = true;
        applied = false;
    }

    public static void invalidateBiomes() {
        cachedLevel = null;
        sampler = null;
    }

    public static void clear() {
        invalidateBiomes();
        nativeAmbient = null;
        applied = false;
    }

    private BiomeAmbientLightRenderer() {}
}
