package com.altnoir.mia.client.event;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    public static void addModEventBus(IEventBus modEventBus) {

    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(EventHandle::ScreenEventInitPost);
    }

    public static void ScreenEventInitPost(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen pauseScreen) {
            Button disconnectButton = pauseScreen.disconnectButton;
            if (disconnectButton != null) {
                disconnectButton.active = false;
                disconnectButton.setMessage(Component.translatable("mia.pause.disconnect"));
            }
        }
    }
}
