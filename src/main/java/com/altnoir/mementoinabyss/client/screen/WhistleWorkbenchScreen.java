package com.altnoir.mementoinabyss.client.screen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import com.altnoir.mementoinabyss.impl.whistle.component.PlacedWhistleFragment;
import com.altnoir.mementoinabyss.content.item.whistle.fragment.WhistleFragmentItem;
import com.altnoir.mementoinabyss.impl.whistle.grid.WhistleGrid;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridCell;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridRectangle;
import com.altnoir.mementoinabyss.impl.whistle.grid.GridRotation;
import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;
import com.altnoir.mementoinabyss.impl.whistle.workbench.WhistleWorkbenchMenu;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class WhistleWorkbenchScreen
        extends AbstractContainerScreen<WhistleWorkbenchMenu> {
    private static final Identifier BACKGROUND =
            MementoInAbyss.asResource("textures/gui/container/whistle_workbench.png");

    private static final int GRID_X = 58;
    private static final int GRID_Y = 32;
    private static final int CELL_SIZE = 18;
    private static final int SLOT_SHADOW = 0xFF373737;
    private static final int SLOT_FACE = 0xFF8B8B8B;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_HOVERED = 0x80FFFFFF;
    private static final int SLOT_BLOCKED = 0x88000000;
    private static final int BORDER_BRIGHT = 0xFF8CBA51;
    private static final int VALID_PREVIEW = 0x668CBA51;
    private static final int INVALID_PREVIEW = 0x66C85A5A;

    private GridRotation rotation = GridRotation.NONE;

    public WhistleWorkbenchScreen(
            WhistleWorkbenchMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, 176, 192);
        this.titleLabelX = 4;
        this.titleLabelY = 2;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 94;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
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
                256
        );

        Optional<WhistleGrid> grid = WhistleApi.grid(menu.whistle());
        grid.ifPresent(value -> {
            drawGrid(graphics, value, mouseX, mouseY);
            drawInstalledFragments(graphics);
            drawPlacementPreview(graphics, value, mouseX, mouseY);
        });
    }

    private void drawGrid(
            GuiGraphicsExtractor graphics,
            WhistleGrid grid,
            int mouseX,
            int mouseY
    ) {
        GridCell hovered = hoveredCell(mouseX, mouseY).orElse(null);
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                GridCell cell = new GridCell(x, y);
                int cellX = leftPos + GRID_X + x * CELL_SIZE;
                int cellY = topPos + GRID_Y + y * CELL_SIZE;
                boolean blocked = !grid.accepts(cell);
                drawVanillaSlot(graphics, cellX, cellY);
                if (blocked) {
                    graphics.fill(
                            cellX + 1,
                            cellY + 1,
                            cellX + CELL_SIZE - 1,
                            cellY + CELL_SIZE - 1,
                            SLOT_BLOCKED
                    );
                } else if (cell.equals(hovered)) {
                    graphics.fill(
                            cellX + 1,
                            cellY + 1,
                            cellX + CELL_SIZE - 1,
                            cellY + CELL_SIZE - 1,
                            SLOT_HOVERED
                    );
                }
            }
        }
    }

    private static void drawVanillaSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, SLOT_SHADOW);
        graphics.fill(x + 1, y + 1, x + CELL_SIZE, y + CELL_SIZE, SLOT_LIGHT);
        graphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, SLOT_FACE);
    }

    private void drawInstalledFragments(GuiGraphicsExtractor graphics) {
        for (PlacedWhistleFragment placed : WhistleApi.loadout(menu.whistle()).fragments()) {
            ItemStack stack = placed.createStack();
            if (!(stack.getItem() instanceof WhistleFragmentItem<?> fragment)) {
                continue;
            }
            int primary = fragment.getDefinition().color();
            drawConnectedShape(
                    graphics,
                    placed.occupiedCells(),
                    0x66000000 | primary & 0x00FFFFFF,
                    primary,
                    darken(primary)
            );
            SkillShape rotatedShape = fragment.getDefinition().shape().rotate(placed.rotation());
            GridRectangle iconArea = rotatedShape.largestRectangle();
            graphics.item(
                    stack,
                    leftPos + GRID_X
                            + (placed.x() + iconArea.x()) * CELL_SIZE
                            + (iconArea.width() * CELL_SIZE - 16) / 2,
                    topPos + GRID_Y
                            + (placed.y() + iconArea.y()) * CELL_SIZE
                            + (iconArea.height() * CELL_SIZE - 16) / 2
            );
        }
    }

    private void drawPlacementPreview(
            GuiGraphicsExtractor graphics,
            WhistleGrid grid,
            int mouseX,
            int mouseY
    ) {
        ItemStack held = menu.heldFragment();
        Optional<GridCell> hovered = hoveredCell(mouseX, mouseY);
        if (grid == null || held.isEmpty() || hovered.isEmpty()
                || !(held.getItem() instanceof WhistleFragmentItem<?> fragment)) {
            return;
        }

        GridCell anchor = hovered.get();
        boolean valid = WhistleApi.canInstall(
                menu.whistle(),
                held,
                anchor.x(),
                anchor.y(),
                rotation
        );
        Set<GridCell> previewCells = fragment.getDefinition().shape().rotate(rotation).cells().stream()
                .map(relative -> relative.offset(anchor.x(), anchor.y()))
                .filter(cell -> cell.x() >= 0 && cell.y() >= 0
                        && cell.x() < grid.width() && cell.y() < grid.height())
                .collect(Collectors.toUnmodifiableSet());
        int color = valid ? VALID_PREVIEW : INVALID_PREVIEW;
        int border = valid ? BORDER_BRIGHT : 0xFFC85A5A;
        drawConnectedShape(graphics, previewCells, color, border, darken(border));
    }

    private void drawConnectedShape(
            GuiGraphicsExtractor graphics,
            Set<GridCell> cells,
            int fill,
            int primaryBorder,
            int mutedBorder
    ) {
        for (GridCell cell : cells) {
            int x = leftPos + GRID_X + cell.x() * CELL_SIZE;
            int y = topPos + GRID_Y + cell.y() * CELL_SIZE;
            graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, fill);
        }
        for (GridCell cell : cells) {
            int x = leftPos + GRID_X + cell.x() * CELL_SIZE;
            int y = topPos + GRID_Y + cell.y() * CELL_SIZE;
            if (!cells.contains(cell.offset(0, -1))) {
                graphics.fill(x, y, x + CELL_SIZE, y + 1, primaryBorder);
            }
            if (!cells.contains(cell.offset(-1, 0))) {
                graphics.fill(x, y, x + 1, y + CELL_SIZE, mutedBorder);
            }
            if (!cells.contains(cell.offset(1, 0))) {
                graphics.fill(x + CELL_SIZE - 1, y, x + CELL_SIZE, y + CELL_SIZE, mutedBorder);
            }
            if (!cells.contains(cell.offset(0, 1))) {
                graphics.fill(x, y + CELL_SIZE - 1, x + CELL_SIZE, y + CELL_SIZE, mutedBorder);
            }
        }
    }

    private static int darken(int color) {
        int red = (color >> 16 & 0xFF) * 2 / 3;
        int green = (color >> 8 & 0xFF) * 2 / 3;
        int blue = (color & 0xFF) * 2 / 3;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        hoveredCell(mouseX, mouseY)
                .flatMap(cell -> WhistleApi.fragmentAt(menu.whistle(), cell))
                .ifPresent(fragment -> graphics.setTooltipForNextFrame(
                        font,
                        fragment.createStack(),
                        mouseX,
                        mouseY
                ));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Optional<GridCell> hovered = hoveredCell((int) event.x(), (int) event.y());
        if (hovered.isPresent()) {
            GridCell cell = hovered.get();
            int buttonId = -1;
            if (event.button() == 0 && !menu.heldFragment().isEmpty()) {
                buttonId = WhistleWorkbenchMenu.placeButton(cell.x(), cell.y(), rotation);
            } else if ((event.button() == 0 || event.button() == 1)
                    && menu.getCarried().isEmpty()
                    && WhistleApi.fragmentAt(menu.whistle(), cell).isPresent()) {
                buttonId = WhistleWorkbenchMenu.removeButton(cell.x(), cell.y());
            }

            if (buttonId >= 0 && sendButton(buttonId)) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
                );
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_R && !menu.heldFragment().isEmpty()) {
            rotation = rotation.next();
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
            );
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean sendButton(int buttonId) {
        if (minecraft.player == null || minecraft.gameMode == null || !menu.clickMenuButton(minecraft.player, buttonId)) {
            return false;
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        return true;
    }

    private Optional<GridCell> hoveredCell(int mouseX, int mouseY) {
        int x = mouseX - leftPos - GRID_X;
        int y = mouseY - topPos - GRID_Y;
        if (x < 0 || y < 0) {
            return Optional.empty();
        }
        int cellX = x / CELL_SIZE;
        int cellY = y / CELL_SIZE;
        return WhistleApi.grid(menu.whistle())
                .filter(grid -> cellX < grid.width() && cellY < grid.height())
                .map(grid -> new GridCell(cellX, cellY));
    }

}
