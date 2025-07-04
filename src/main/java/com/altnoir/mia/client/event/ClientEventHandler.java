package com.altnoir.mia.client.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        ClientEventHandler.addModEventBus(modEventBus);
        ClientEventHandler.addGameEventBus(gameEventBus);
    }

    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(ClientDimEffects::registerDimensionEffects);
        modEventBus.addListener(RegisterParticlesEvent::registerParticles);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(ClientCurseEvent::ScreenEventInitPost);
        gameEventBus.addListener(ClientCurseEvent::onRenderOverlay);
        gameEventBus.addListener(ClientTooltipEvent::onTooltip);
    }
}
