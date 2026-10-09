package com.altnoir.mementoinabyss.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;

public class SeamlessLevelLoadingScreen extends LevelLoadingScreen {
    public SeamlessLevelLoadingScreen(
            LevelLoadTracker loadTracker, LevelLoadingScreen.Reason reason) {
        super(loadTracker, reason);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {}
}
