package com.altnoir.mementoinabyss.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;

public record TooltipPalette(Style muted, Style primary, Style highlight) {
    public static final TooltipPalette MIA =
            new TooltipPalette(
                    Style.EMPTY.withColor(0x64843A),
                    Style.EMPTY.withColor(0x8CBA51),
                    Style.EMPTY.withColor(0xB7D986));

    public static Style colored(int rgb) {
        return Style.EMPTY.withColor(rgb).withShadowColor(ARGB.opaque(ARGB.scaleRGB(rgb, 0.25F)));
    }

    public Component colored(Component text, int rgb) {
        return text.copy().withStyle(colored(rgb));
    }
}
