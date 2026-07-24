package com.altnoir.mementoinabyss.client.screen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.enhancement.ArtifactEnhancementMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ArtifactEnhancementScreen
        extends AbstractContainerScreen<ArtifactEnhancementMenu> {
    private static final net.minecraft.resources.Identifier BACKGROUND =
            MementoInAbyss.asResource("textures/gui/container/artifact_smithing_table.png");

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
        Component status = Component.translatable(statusKey(menu.status()));
        graphics.textWithWordWrap(font, status, leftPos + 52, topPos + 35, 64, 0xFF3F3F3F);
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
