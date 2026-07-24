package com.altnoir.mementoinabyss.compat.sodium;

import com.altnoir.mementoinabyss.compat.MiaMods;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class SodiumLodCompat {
    public static boolean isLoaded() {
        return MiaMods.SODIUM.isLoaded();
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (isLoaded()) SodiumApi.markSpriteActive(sprite);
    }

    private static final class SodiumApi {
        private static void markSpriteActive(TextureAtlasSprite sprite) {
            net.caffeinemc.mods.sodium.api.texture.SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }
    }

    private SodiumLodCompat() {}
}
