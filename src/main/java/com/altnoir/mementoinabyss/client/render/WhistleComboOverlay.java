package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.client.WhistleComboHandler;
import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import com.altnoir.mementoinabyss.impl.whistle.skill.WhistleNote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class WhistleComboOverlay {
    private static final int LEFT = 10;
    private static final int TOP = 10;
    private static final int ROW_HEIGHT = 19;
    private static final int WIDTH = 126;

    private static final int BACKGROUND = 0xD8182115;
    private static final int BORDER = 0xFF64843A;
    private static final int PRIMARY = 0xFF8CBA51;
    private static final int HIGHLIGHT = 0xFFB7D986;
    private static final int MUTED = 0xFF66755A;

    public static void render(GuiGraphicsExtractor graphics) {
        if (!WhistleComboHandler.isActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        List<WhistleApi.SkillActivation> skills = WhistleComboHandler.equippedSkills();
        int height = 17 + skills.size() * ROW_HEIGHT;
        graphics.fill(LEFT, TOP, LEFT + WIDTH, TOP + height, BACKGROUND);
        graphics.outline(LEFT, TOP, WIDTH, height, BORDER);
        graphics.text(
                minecraft.font,
                Component.translatable("gui.mementoinabyss.whistle.skills"),
                LEFT + 5,
                TOP + 5,
                HIGHLIGHT,
                false
        );

        for (int i = 0; i < skills.size(); i++) {
            WhistleApi.SkillActivation activation = skills.get(i);
            int y = TOP + 17 + i * ROW_HEIGHT;
            graphics.item(activation.stack(), LEFT + 3, y + 1);

            boolean cooldown = minecraft.player.getCooldowns().isOnCooldown(activation.stack());
            if (cooldown) {
                float fraction = minecraft.player.getCooldowns()
                        .getCooldownPercent(activation.stack(), 0.0F);
                int seconds = Math.max(
                        1,
                        (int) Math.ceil(fraction * activation.cooldownTicks() / 20.0)
                );
                graphics.text(
                        minecraft.font,
                        Component.translatable(
                                "gui.mementoinabyss.whistle.cooldown",
                                seconds
                        ),
                        LEFT + 23,
                        y + 5,
                        MUTED,
                        false
                );
                continue;
            }

            boolean matches = WhistleComboHandler.canStillMatch(activation);
            List<WhistleNote> input = WhistleComboHandler.input();
            int x = LEFT + 23;
            List<WhistleNote> sequence = activation.skill().getDefinition().sequence();
            for (int noteIndex = 0; noteIndex < sequence.size(); noteIndex++) {
                int color;
                if (!matches) {
                    color = MUTED;
                } else if (noteIndex < input.size()) {
                    color = PRIMARY;
                } else {
                    color = HIGHLIGHT;
                }
                String symbol = sequence.get(noteIndex).symbol();
                graphics.text(minecraft.font, symbol, x, y + 5, color, false);
                x += minecraft.font.width(symbol) + 3;
            }
        }
    }

    private WhistleComboOverlay() {}
}
