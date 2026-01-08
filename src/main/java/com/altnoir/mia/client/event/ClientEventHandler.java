package com.altnoir.mia.client.event;

import com.altnoir.mia.client.MiaClientConfig;
import com.altnoir.mia.init.MiaKeyBinding;
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
        modEventBus.addListener(MiaClientConfig::onLoad);
        modEventBus.addListener(ClientDimEffects::registerDimensionEffects);
        modEventBus.addListener(RegisterParticlesEvent::registerParticles);
        modEventBus.addListener(ClientTooltipComponentEvent::onRegisterFactories);
        modEventBus.addListener(RegisterEntityRendererEvent::registerEntityRenderers);
        modEventBus.addListener(RegisterScreenEvent::registerScreens);
        modEventBus.addListener(MiaKeyBinding::registerKeyBindings);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(ClientCurseEvent::ScreenEventInitPost);
        gameEventBus.addListener(ClientCurseEvent::onRenderOverlay);
        gameEventBus.addListener(ClientTooltipEvent::onTooltip);
        gameEventBus.addListener(KeyArrowEvent::onClientTick);
        gameEventBus.addListener(KeyArrowEvent::onRenderGui);
        gameEventBus.addListener(CompassOverlayEvent::onRenderGui);
    }
}