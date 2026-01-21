package com.altnoir.mia.event;

import com.altnoir.mia.client.MiaClientEvents;
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
        modEventBus.addListener(MiaClientEvents::modLoad);
        modEventBus.addListener(MiaClientEvents::registerDimensionEffects);
        modEventBus.addListener(MiaClientEvents::registerParticles);
        modEventBus.addListener(MiaClientEvents::registerTooltipComponentFactories);
        modEventBus.addListener(MiaClientEvents::registerEntityRenderers);
        modEventBus.addListener(MiaClientEvents::registerScreens);
        modEventBus.addListener(MiaClientEvents::registerKeyBindings);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(MiaClientEvents::onClientTick);
        gameEventBus.addListener(MiaClientEvents::onRenderGui);
        gameEventBus.addListener(MiaClientEvents::onRenderOverlay);
        gameEventBus.addListener(MiaClientEvents::onScreenInitPost);
        gameEventBus.addListener(MiaClientEvents::onTooltip);
    }
}