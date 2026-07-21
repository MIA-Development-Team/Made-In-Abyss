package com.altnoir.mementoinabyss.compat;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Optional hooks exposed by Sodium for custom renderers. */
public final class SodiumLodCompat {
    public static boolean isLoaded() {
        return MiaMods.SODIUM.isLoaded();
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (isLoaded()) SodiumApi.markSpriteActive(sprite);
    }

    /** Kept separate so Sodium classes are never resolved when the mod is absent. */
    private static final class SodiumApi {
        private static void markSpriteActive(TextureAtlasSprite sprite) {
            net.caffeinemc.mods.sodium.api.texture.SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }
    }

    private SodiumLodCompat() {}
}
