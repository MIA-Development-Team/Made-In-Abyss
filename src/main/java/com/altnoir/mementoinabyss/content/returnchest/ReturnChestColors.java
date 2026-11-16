package com.altnoir.mementoinabyss.content.returnchest;

import net.minecraft.world.item.DyeColor;

public final class ReturnChestColors {
    private static final float TARGET_MAX = 230.0F / 255.0F;
    private static final float MAX_BOOST = 2.2F;

    public static int tint(DyeColor color) {
        int rgb = color.getTextureDiffuseColor() & 0xFFFFFF;
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        int max = Math.max(red, Math.max(green, blue));
        if (max == 0) return 0;
        float scale = Math.clamp(TARGET_MAX * 255.0F / max, 1.0F, MAX_BOOST);
        return clamp(red * scale) << 16 | clamp(green * scale) << 8 | clamp(blue * scale);
    }

    private static int clamp(float value) {
        return Math.min(255, Math.round(value));
    }

    private ReturnChestColors() {}
}
