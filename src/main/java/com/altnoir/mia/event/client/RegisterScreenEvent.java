package com.altnoir.mia.event.client;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.altnoir.mia.client.gui.screens.ArtifactSmithingTableScreen;
import com.altnoir.mia.init.MiaMenus;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class RegisterScreenEvent {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MiaMenus.ARTIFACT_ENHANCEMENT_TABLE.get(), ArtifactSmithingTableScreen::new);
    }
}
