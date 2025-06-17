package com.altnoir.mia.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.mixin.IPauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class PauseScreenConfig{
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof PauseScreen pauseScreen) {
            Minecraft.getInstance().execute(() -> {
                try {
                    Button disconnectButton = ((IPauseScreen) pauseScreen).getDisconnectButton();
                    if (disconnectButton != null) {
                        disconnectButton.visible = false;
                        disconnectButton.active = false;
                        disconnectButton.setMessage(Component.translatable("mia.pause.disconnect"));
                    }
                } catch (Exception e) {
                    MIA.LOGGER.warn("Error removing disconnect button:", e);
                }
            });
        }
    }
}