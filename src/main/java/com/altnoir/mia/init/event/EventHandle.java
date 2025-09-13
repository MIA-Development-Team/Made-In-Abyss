package com.altnoir.mia.init.event;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.datagen.DataGenerators;
import com.altnoir.mia.init.MiaNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(DataGenerators::gatherData);

        modEventBus.addListener(MiaNetworking::register);
        modEventBus.addListener(CurseEvent::attachEntityCapabilities);
        modEventBus.addListener(MiaConfig::onLoad);

        modEventBus.addListener(CapabilityRegister::RegisterCapabilitiesEvent);
        modEventBus.addListener(PlayerAttributeEvent::addPlayerAttributes);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(CurseEvent::onDimensionChange);
        gameEventBus.addListener(CurseEvent::onPlayerTick);

        gameEventBus.addListener(BrewingRecipesEvent::onBrewingRecipe);
    }
}
