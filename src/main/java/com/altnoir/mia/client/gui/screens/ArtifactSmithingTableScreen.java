package com.altnoir.mia.client.gui.screens;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.event.client.ClientTooltipEvent;
import com.altnoir.mia.common.inventory.ArtifactSmithingTableMenu;
import com.altnoir.mia.common.recipe.ArtifactSmithingRecipe;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class ArtifactSmithingTableScreen extends AbstractContainerScreen<ArtifactSmithingTableMenu> {
    private static final ResourceLocation BACKGROUND = MiaUtil
            .miaId("textures/gui/container/artifact_smithing_table.png");

    private static final ResourceLocation RECIPE_SELECTED_SPRITE = MiaUtil
            .miaId("container/artifact_smithing_table/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = MiaUtil
            .miaId("container/artifact_smithing_table/recipe_highlighted");
    private static final ResourceLocation RECIPE_AVAILABLE_SPRITE = MiaUtil
            .miaId("container/artifact_smithing_table/recipe_available");
    private static final ResourceLocation RECIPE_UNAVAILABLE_SPRITE = MiaUtil
            .miaId("container/artifact_smithing_table/recipe_unavailable");

    private static final ResourceLocation SCROLLER_SPRITE = MiaUtil.miaId("container/artifact_smithing_table/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = MiaUtil
            .miaId("container/artifact_smithing_table/scroller_disabled");

    // 每行槽间隔高度为18像素
    private static final int RECIPES_COLUMNS = 4; // 配方显示网格列数
    private static final int RECIPES_ROWS = 3; // 配方显示网格行数
    private static final int RECIPES_X = 52; // 配方显示网格X
    private static final int RECIPES_Y = 32; // 配方显示网格Y
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
    private static final int SCROLLER_WIDTH = 12; // 配方滚动条宽度
    private static final int SCROLLER_HEIGHT = 15; // 配方滚动条高度
    private static final int SCROLLER_X = 119; // 配方滚动条X
    private static final int SCROLLER_Y_START = 33; // 配方滚动条Y起始位置
    private static final int SCROLLER_FULL_HEIGHT = 72 - 18; // 配方滚动条完整高度

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public ArtifactSmithingTableScreen(ArtifactSmithingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.imageWidth = 176;
        this.imageHeight = 184;

        this.inventoryLabelY = this.imageHeight - 94;

        menu.registerUpdateListener(this::containerChanged);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        // render background
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);
        // render scrollbar
        int k = (int) ((SCROLLER_FULL_HEIGHT - SCROLLER_HEIGHT + 1) * this.scrollOffs);
        ResourceLocation resourcelocation = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(resourcelocation, i + SCROLLER_X, j + SCROLLER_Y_START + k, SCROLLER_WIDTH,
                SCROLLER_HEIGHT);
        // render buttons
        int l = this.leftPos + RECIPES_X;
        int i1 = this.topPos + RECIPES_Y;
        int j1 = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;
        this.renderButtons(guiGraphics, mouseX, mouseY, l, i1, j1);
        // render recipes
        this.renderRecipes(guiGraphics, l, i1, j1);
        // render text
        var recipeSelected = ((ArtifactSmithingTableMenu) this.menu).getSelectedRecipe();
        if (recipeSelected != null) {
            Component text = ClientTooltipEvent.formatAttributeModifier(recipeSelected.value().getAttribute().value(),
                    recipeSelected.value().getAttributeAmount(),
                    recipeSelected.value().getAttributeOperation());

            guiGraphics.drawString(this.font, text, this.leftPos + RECIPES_X, this.topPos + RECIPES_Y - 10, 0x000000,
                    false);
        }
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y,
                               int lastVisibleElementIndex) {
        int availableRecipeCount = ((ArtifactSmithingTableMenu) this.menu).getAvailableRecipes().size();
        int unavailableRecipeCount = ((ArtifactSmithingTableMenu) this.menu).getUnavailableRecipes().size();
        for (int i = this.startIndex; i < lastVisibleElementIndex
                && i < availableRecipeCount + unavailableRecipeCount; ++i) {
            int j = i - this.startIndex;
            int k = x + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            int l = j / RECIPES_COLUMNS;
            int i1 = y + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            ResourceLocation resourcelocation;

            if (i == ((ArtifactSmithingTableMenu) this.menu).getSelectedRecipeIndex()) {
                MIA.LOGGER.debug("selected " + i);

                resourcelocation = RECIPE_SELECTED_SPRITE;
            } else if (i < availableRecipeCount) {
                if (mouseX >= k && mouseY >= i1 && mouseX < k + RECIPES_IMAGE_SIZE_WIDTH
                        && mouseY < i1 + RECIPES_IMAGE_SIZE_HEIGHT) {
                    resourcelocation = RECIPE_HIGHLIGHTED_SPRITE;
                } else {
                    resourcelocation = RECIPE_AVAILABLE_SPRITE;
                }
            } else {
                resourcelocation = RECIPE_UNAVAILABLE_SPRITE;
            }

            guiGraphics.blitSprite(resourcelocation, k, i1 - 1, RECIPES_IMAGE_SIZE_WIDTH, RECIPES_IMAGE_SIZE_HEIGHT);
        }

    }

    private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int startIndex) {
        List<RecipeHolder<ArtifactSmithingRecipe>> availableRecipes = ((ArtifactSmithingTableMenu) this.menu)
                .getAvailableRecipes();
        List<RecipeHolder<ArtifactSmithingRecipe>> unavailableRecipes = ((ArtifactSmithingTableMenu) this.menu)
                .getUnavailableRecipes();

        for (int i = this.startIndex; i < startIndex
                && i < (availableRecipes.size() + unavailableRecipes.size()); ++i) {
            int j = i - this.startIndex;
            int k = x + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            int l = j / RECIPES_COLUMNS;
            int i1 = y + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            if (i < availableRecipes.size()) {
                ItemStack material = availableRecipes.get(i).value().getMaterial();
                guiGraphics.renderItem(material, k, i1);
                guiGraphics.renderItemDecorations(this.font, material, k, i1,
                        material.getCount() > 1 ? Integer.toString(material.getCount()) : null);
            } else if ((i - availableRecipes.size()) < unavailableRecipes.size()) {
                ItemStack material = unavailableRecipes.get(i - availableRecipes.size()).value().getMaterial();
                guiGraphics.renderItem(material, k, i1);
                guiGraphics.renderItemDecorations(this.font, material, k, i1,
                        material.getCount() > 1 ? Integer.toString(material.getCount()) : null);
            }
        }

    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        if (this.displayRecipes) {
            int i = this.leftPos + RECIPES_X;
            int j = this.topPos + RECIPES_Y;
            int k = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;
            List<RecipeHolder<ArtifactSmithingRecipe>> list = new ArrayList<>();
            list.addAll(((ArtifactSmithingTableMenu) this.menu).getAvailableRecipes());
            list.addAll(((ArtifactSmithingTableMenu) this.menu).getUnavailableRecipes());

            for (int l = this.startIndex; l < k
                    && l < ((ArtifactSmithingTableMenu) this.menu).getNumRecipes(); ++l) {
                int i1 = l - this.startIndex;
                int j1 = i + i1 % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
                int k1 = j + i1 / RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_HEIGHT + 2;
                if (x >= j1 && x < j1 + RECIPES_IMAGE_SIZE_WIDTH && y >= k1 && y < k1 + RECIPES_IMAGE_SIZE_HEIGHT) {
                    guiGraphics.renderTooltip(this.font, list.get(l).value().getMaterial(), x, y);
                }
            }
        }

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int i = this.leftPos + RECIPES_X;
            int j = this.topPos + RECIPES_Y;
            int k = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;
            for (int l = this.startIndex; l < k; ++l) {
                int i1 = l - this.startIndex;
                double d0 = mouseX - (double) (i + i1 % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH);
                double d1 = mouseY - (double) (j + i1 / RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_HEIGHT);
                if (d0 >= 0.0 && d1 >= 0.0 && d0 < (double) RECIPES_IMAGE_SIZE_WIDTH
                        && d1 < (double) RECIPES_IMAGE_SIZE_HEIGHT
                        && ((ArtifactSmithingTableMenu) this.menu).clickMenuButton(this.minecraft.player, l)) {
                    // play sound
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode
                            .handleInventoryButtonClick(((ArtifactSmithingTableMenu) this.menu).containerId, l);
                    return true;
                }
            }

            i = this.leftPos + SCROLLER_X;
            j = this.topPos + 9 + 18;
            if (mouseX >= (double) i && mouseX < (double) (i + SCROLLER_WIDTH) && mouseY >= (double) j
                    && mouseY < (double) (j + SCROLLER_FULL_HEIGHT)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + RECIPES_Y;
            int j = i + SCROLLER_FULL_HEIGHT;
            this.scrollOffs = ((float) mouseY - (float) i - ((float) SCROLLER_HEIGHT) / 2)
                    / ((float) (j - i) - SCROLLER_HEIGHT);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) this.getOffscreenRows()) + 0.5)
                    * RECIPES_COLUMNS;
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            float f = (float) scrollY / (float) i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) i) + 0.5) * RECIPES_COLUMNS;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes
                && ((ArtifactSmithingTableMenu) this.menu).getNumRecipes() > RECIPES_COLUMNS * RECIPES_ROWS;
    }

    protected int getOffscreenRows() {
        return (((ArtifactSmithingTableMenu) this.menu).getNumRecipes() + RECIPES_COLUMNS - 1) / RECIPES_COLUMNS
                - RECIPES_ROWS;
    }

    private void containerChanged() {
        this.displayRecipes = ((ArtifactSmithingTableMenu) this.menu).inputHasSmithingRecipe();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
    }

}
