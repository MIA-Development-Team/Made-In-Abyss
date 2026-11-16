package com.altnoir.mementoinabyss.content.returnchest.client;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.returnchest.ReturnChestMenu;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ReturnChestScreen extends AbstractContainerScreen<ReturnChestMenu> {
    private static final Identifier BACKGROUND =
            MementoInAbyss.asResource("textures/gui/container/return_chest.png");
    private static final Identifier SCROLLER =
            MementoInAbyss.asResource("container/return_chest/scroller");
    private static final Identifier SCROLLER_DISABLED =
            MementoInAbyss.asResource("container/return_chest/scroller_disabled");
    private static final int SCROLLER_X = 156;
    private static final int SCROLLER_Y = 18;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int SCROLLER_TRACK_HEIGHT = 106;

    private boolean scrolling;

    public ReturnChestScreen(ReturnChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 126;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256);
        int maxRow = menu.maxRow();
        float offset = maxRow > 0 ? (float) menu.scrollRow() / maxRow : 0.0F;
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                maxRow > 0 ? SCROLLER : SCROLLER_DISABLED,
                leftPos + SCROLLER_X,
                topPos
                        + SCROLLER_Y
                        + Math.round((SCROLLER_TRACK_HEIGHT - SCROLLER_HEIGHT) * offset),
                SCROLLER_WIDTH,
                SCROLLER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        scrolling = false;
        if (menu.maxRow() > 0) {
            int x = leftPos + SCROLLER_X;
            int y = topPos + SCROLLER_Y;
            if (event.x() >= x
                    && event.x() < x + SCROLLER_WIDTH
                    && event.y() >= y
                    && event.y() < y + SCROLLER_TRACK_HEIGHT) {
                scrolling = true;
                scrollTo(rowAt(event.y()));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (scrolling && menu.maxRow() > 0) {
            scrollTo(rowAt(event.y()));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.maxRow() > 0) {
            scrollTo(menu.scrollRow() - (int) Math.signum(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderSlotContents(
            GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String countText) {
        super.renderSlotContents(
                graphics,
                stack,
                slot,
                slot.index < ReturnChestMenu.SLOTS ? countLabel(stack.getCount()) : countText);
    }

    private static @Nullable String countLabel(int count) {
        if (count < 1_000) return null;
        if (count < 1_000_000) return compact(count / 1_000.0, "k");
        if (count < 1_000_000_000) return compact(count / 1_000_000.0, "M");
        return compact(count / 1_000_000_000.0, "B");
    }

    private static String compact(double value, String suffix) {
        if (value < 10.0) return String.format(Locale.ROOT, "%.1f%s", value, suffix);
        return (long) value + suffix;
    }

    private int rowAt(double mouseY) {
        int travel = SCROLLER_TRACK_HEIGHT - SCROLLER_HEIGHT;
        float offset =
                Mth.clamp(
                        (float) (mouseY - topPos - SCROLLER_Y - SCROLLER_HEIGHT / 2.0) / travel,
                        0.0F,
                        1.0F);
        return Math.round(offset * menu.maxRow());
    }

    private void scrollTo(int row) {
        int clamped = Mth.clamp(row, 0, menu.maxRow());
        if (clamped == menu.scrollRow()) return;
        if (menu.clickMenuButton(minecraft.player, clamped)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, clamped);
        }
    }
}
