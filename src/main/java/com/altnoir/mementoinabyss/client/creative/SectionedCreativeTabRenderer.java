package com.altnoir.mementoinabyss.client.creative;

import com.altnoir.mementoinabyss.impl.creative.SectionedCreativeModeTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;

public final class SectionedCreativeTabRenderer {
    private static final int VISIBLE_ROWS = 5;
    private static final int GRID_LEFT = 8;
    private static final int GRID_TOP = 17;
    private static final int GRID_WIDTH = 162;
    private static final int ROW_HEIGHT = 18;

    private static final int BACKGROUND = 0xFF182115;
    private static final int BORDER_MUTED = 0xFF64843A;
    private static final int BORDER_PRIMARY = 0xFF8CBA51;
    private static final int TEXT_HIGHLIGHT = 0xFFB7D986;

    private static int visibleHeadingRows;

    public static void extract(
            CreativeModeInventoryScreen screen,
            GuiGraphicsExtractor graphics,
            SectionedCreativeModeTab tab,
            float scrollOffset
    ) {
        int firstVisibleRow = tab.visibleStartRow(scrollOffset);
        int left = screen.getLeftPos() + GRID_LEFT;
        int top = screen.getTopPos() + GRID_TOP;
        int headingRows = 0;

        graphics.nextStratum();
        for (SectionedCreativeModeTab.SectionLayout section : tab.sectionLayouts()) {
            int visibleRow = section.headingRow() - firstVisibleRow;
            if (visibleRow < 0 || visibleRow >= VISIBLE_ROWS) {
                continue;
            }
            headingRows |= 1 << visibleRow;

            int y = top + visibleRow * ROW_HEIGHT;
            if (section.bannerSprite() != null) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        section.bannerSprite(),
                        left,
                        y,
                        GRID_WIDTH,
                        ROW_HEIGHT
                );
            } else {
                extractDefaultBanner(graphics, left, y);
            }
            graphics.text(
                    Minecraft.getInstance().font,
                    section.title(),
                    left + 7,
                    y + 5,
                    TEXT_HIGHLIGHT,
                    false
            );
        }
        visibleHeadingRows = headingRows;
    }

    public static boolean isHeadingSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= VISIBLE_ROWS * 9) {
            return false;
        }
        return (visibleHeadingRows & 1 << (slotIndex / 9)) != 0;
    }

    public static void clearHeadingSlots() {
        visibleHeadingRows = 0;
    }

    private static void extractDefaultBanner(GuiGraphicsExtractor graphics, int left, int top) {
        graphics.fill(left, top, left + GRID_WIDTH, top + ROW_HEIGHT, BACKGROUND);
        graphics.fill(left, top, left + 1, top + ROW_HEIGHT, BORDER_MUTED);
        graphics.fill(left + GRID_WIDTH - 1, top, left + GRID_WIDTH, top + ROW_HEIGHT, BORDER_MUTED);
        graphics.fill(left + 1, top, left + GRID_WIDTH, top + 1, BORDER_PRIMARY);
        graphics.fill(
                left + 1,
                top + ROW_HEIGHT - 1,
                left + GRID_WIDTH,
                top + ROW_HEIGHT,
                BORDER_MUTED
        );
    }

    private SectionedCreativeTabRenderer() {
    }
}
