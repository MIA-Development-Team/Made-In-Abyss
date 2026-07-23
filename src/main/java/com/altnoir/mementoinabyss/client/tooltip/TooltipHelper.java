package com.altnoir.mementoinabyss.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TooltipHelper {
    public static final int MAX_WIDTH_PER_LINE = 200;

    public static List<Component> wrap(String text, TooltipPalette palette) {
        return wrap(text, palette, 0);
    }

    public static List<Component> wrap(String text, TooltipPalette palette, int indent) {
        Minecraft minecraft = Minecraft.getInstance();
        Locale locale = minecraft.getLocale();
        BreakIterator iterator = BreakIterator.getLineInstance(locale);
        iterator.setText(text);

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int currentWidth = 0;
        for (int start = iterator.first(), end = iterator.next();
                end != BreakIterator.DONE;
                start = end, end = iterator.next()) {
            String word = text.substring(start, end);
            int wordWidth = minecraft.font.width(word.replace("_", ""));
            if (currentWidth > 0 && currentWidth + wordWidth > MAX_WIDTH_PER_LINE) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentWidth = 0;
            }
            currentLine.append(word);
            currentWidth += wordWidth;
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        List<Component> result = new ArrayList<>(lines.size());
        String prefix = " ".repeat(Math.max(0, indent));
        for (String line : lines) {
            MutableComponent formatted = Component.literal(prefix).withStyle(palette.primary());
            boolean highlighted = false;
            String[] sections = line.split("_", -1);
            for (int i = 0; i < sections.length; i++) {
                formatted.append(Component.literal(sections[i])
                        .withStyle(highlighted ? palette.highlight() : palette.primary()));
                if (i < sections.length - 1) {
                    highlighted = !highlighted;
                }
            }
            result.add(formatted);
        }
        return result;
    }

    private TooltipHelper() {}
}
