package com.altnoir.mia.client.event;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.altnoir.mia.client.gui.screens.WhistleEnhancementTableScreen;
import com.altnoir.mia.init.MiaMenus;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class RegisterScreenEvent {
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MiaMenus.WHISTLE_ENHANCEMENT_TABLE.get(), WhistleEnhancementTableScreen::new);
    }
}
