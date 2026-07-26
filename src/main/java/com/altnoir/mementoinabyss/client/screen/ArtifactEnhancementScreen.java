package com.altnoir.mementoinabyss.client.screen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.ArtifactEnhancementClientRecipes;
import com.altnoir.mementoinabyss.client.tooltip.ArtifactEnhancementMaterialTooltip;
import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementMenu;
import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementRecipe;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class ArtifactEnhancementScreen
        extends AbstractContainerScreen<ArtifactEnhancementMenu> {
    private static final Identifier BACKGROUND =
            MementoInAbyss.asResource("textures/gui/container/artifact_smithing_table.png");
    private static final Identifier RECIPE_SELECTED =
            MementoInAbyss.asResource("container/artifact_smithing_table/recipe_selected");
    private static final Identifier RECIPE_HIGHLIGHTED =
            MementoInAbyss.asResource("container/artifact_smithing_table/recipe_highlighted");
    private static final Identifier RECIPE_AVAILABLE =
            MementoInAbyss.asResource("container/artifact_smithing_table/recipe_available");
    private static final Identifier RECIPE_UNAVAILABLE =
            MementoInAbyss.asResource("container/artifact_smithing_table/recipe_unavailable");
    private static final Identifier SCROLLER =
            MementoInAbyss.asResource("container/artifact_smithing_table/scroller");
    private static final Identifier SCROLLER_DISABLED =
            MementoInAbyss.asResource("container/artifact_smithing_table/scroller_disabled");

    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int VISIBLE_RECIPES = COLUMNS * ROWS;
    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 32;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int SCROLLER_X = 119;
    private static final int SCROLLER_Y = 33;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int SCROLLER_TRACK_HEIGHT = 54;

    private float scrollOffset;
    private boolean scrolling;
    private int startIndex;

    public ArtifactEnhancementScreen(
            ArtifactEnhancementMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, 176, 184);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 90;
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

        if (!displayRecipes()) {
            Component status = Component.translatable(statusKey(menu.status()));
            graphics.textWithWordWrap(font, status, leftPos + 52, topPos + 35, 64, 0xFF3F3F3F);
            return;
        }

        int scrollTravel = SCROLLER_TRACK_HEIGHT - SCROLLER_HEIGHT;
        Identifier scroller = isScrollBarActive() ? SCROLLER : SCROLLER_DISABLED;
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                scroller,
                leftPos + SCROLLER_X,
                topPos + SCROLLER_Y + Math.round(scrollTravel * scrollOffset),
                SCROLLER_WIDTH,
                SCROLLER_HEIGHT
        );
        extractRecipeButtons(graphics, mouseX, mouseY);
    }

    private void extractRecipeButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = recipes();
        int endIndex = Math.min(startIndex + VISIBLE_RECIPES, recipes.size());
        for (int index = startIndex; index < endIndex; index++) {
            int visibleIndex = index - startIndex;
            int x = leftPos + RECIPES_X + visibleIndex % COLUMNS * BUTTON_WIDTH;
            int y = topPos + RECIPES_Y + visibleIndex / COLUMNS * BUTTON_HEIGHT + 2;
            ArtifactEnhancementRecipe recipe = recipes.get(index).value();
            boolean owned = menu.hasMaterial(recipe);
            boolean hovered = mouseX >= x && mouseY >= y
                    && mouseX < x + BUTTON_WIDTH && mouseY < y + BUTTON_HEIGHT;

            Identifier sprite;
            if (index == menu.selectedRecipeIndex()) {
                sprite = RECIPE_SELECTED;
            } else if (owned && hovered) {
                sprite = RECIPE_HIGHLIGHTED;
            } else {
                sprite = owned ? RECIPE_AVAILABLE : RECIPE_UNAVAILABLE;
            }
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    sprite,
                    x,
                    y - 1,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT
            );

            ItemStack material = new ItemStack(recipe.material(), recipe.materialCount());
            graphics.item(material, x, y);
            graphics.itemDecorations(
                    font,
                    material,
                    x,
                    y,
                    recipe.materialCount() > 1 ? Integer.toString(recipe.materialCount()) : null
            );
            if (hovered) {
                graphics.requestCursor(owned ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (!displayRecipes()) {
            return;
        }
        List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = recipes();
        int endIndex = Math.min(startIndex + VISIBLE_RECIPES, recipes.size());
        for (int index = startIndex; index < endIndex; index++) {
            int visibleIndex = index - startIndex;
            int x = leftPos + RECIPES_X + visibleIndex % COLUMNS * BUTTON_WIDTH;
            int y = topPos + RECIPES_Y + visibleIndex / COLUMNS * BUTTON_HEIGHT + 2;
            if (mouseX < x || mouseY < y || mouseX >= x + BUTTON_WIDTH || mouseY >= y + BUTTON_HEIGHT) {
                continue;
            }
            ArtifactEnhancementRecipe recipe = recipes.get(index).value();
            ItemStack material = new ItemStack(recipe.material(), recipe.materialCount());
            graphics.setComponentTooltipForNextFrame(
                    font,
                    List.of(
                            material.getHoverName(),
                            ArtifactEnhancementMaterialTooltip.modifierLine(recipe)
                    ),
                    mouseX,
                    mouseY
            );
            return;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        scrolling = false;
        if (displayRecipes()) {
            List<RecipeHolder<ArtifactEnhancementRecipe>> recipes = recipes();
            int endIndex = Math.min(startIndex + VISIBLE_RECIPES, recipes.size());
            for (int index = startIndex; index < endIndex; index++) {
                int visibleIndex = index - startIndex;
                double relativeX = event.x()
                        - (leftPos + RECIPES_X + visibleIndex % COLUMNS * BUTTON_WIDTH);
                double relativeY = event.y()
                        - (topPos + RECIPES_Y + visibleIndex / COLUMNS * BUTTON_HEIGHT);
                ArtifactEnhancementRecipe recipe = recipes.get(index).value();
                if (relativeX >= 0.0 && relativeY >= 0.0
                        && relativeX < BUTTON_WIDTH && relativeY < BUTTON_HEIGHT
                        && menu.hasMaterial(recipe)
                        && menu.clickMenuButton(minecraft.player, index)) {
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
                    );
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                    return true;
                }
            }

            int scrollerX = leftPos + SCROLLER_X;
            int scrollerY = topPos + SCROLLER_Y;
            if (isScrollBarActive()
                    && event.x() >= scrollerX && event.x() < scrollerX + SCROLLER_WIDTH
                    && event.y() >= scrollerY && event.y() < scrollerY + SCROLLER_TRACK_HEIGHT) {
                scrolling = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (scrolling && isScrollBarActive()) {
            int trackTop = topPos + SCROLLER_Y;
            scrollOffset = ((float) event.y() - trackTop - SCROLLER_HEIGHT / 2.0F)
                    / (SCROLLER_TRACK_HEIGHT - SCROLLER_HEIGHT);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            startIndex = Math.round(scrollOffset * offscreenRows()) * COLUMNS;
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
        if (isScrollBarActive()) {
            int rows = offscreenRows();
            scrollOffset = Mth.clamp(scrollOffset - (float) scrollY / rows, 0.0F, 1.0F);
            startIndex = Math.round(scrollOffset * rows) * COLUMNS;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean displayRecipes() {
        return menu.hasEnhanceableArtifact() && !recipes().isEmpty();
    }

    private boolean isScrollBarActive() {
        return displayRecipes() && recipes().size() > VISIBLE_RECIPES;
    }

    private int offscreenRows() {
        return Math.max(1, (recipes().size() + COLUMNS - 1) / COLUMNS - ROWS);
    }

    private static List<RecipeHolder<ArtifactEnhancementRecipe>> recipes() {
        return ArtifactEnhancementClientRecipes.all();
    }

    private static String statusKey(int status) {
        return switch (status) {
            case ArtifactEnhancementMenu.STATUS_INSERT_MATERIAL ->
                    "container.mementoinabyss.artifact_enhancement.insert_material";
            case ArtifactEnhancementMenu.STATUS_INVALID_MATERIAL ->
                    "container.mementoinabyss.artifact_enhancement.invalid_material";
            case ArtifactEnhancementMenu.STATUS_READY ->
                    "container.mementoinabyss.artifact_enhancement.ready";
            case ArtifactEnhancementMenu.STATUS_MAX_LEVEL ->
                    "container.mementoinabyss.artifact_enhancement.max_level";
            default -> "container.mementoinabyss.artifact_enhancement.insert_artifact";
        };
    }
}
