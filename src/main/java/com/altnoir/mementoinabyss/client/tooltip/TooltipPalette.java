package com.altnoir.mementoinabyss.client.tooltip;

import net.minecraft.network.chat.Style;

public record TooltipPalette(Style muted, Style primary, Style highlight) {
    public static final TooltipPalette MIA = new TooltipPalette(
            Style.EMPTY.withColor(0x64843A),
            Style.EMPTY.withColor(0x8CBA51),
            Style.EMPTY.withColor(0xB7D986)
    );
}
