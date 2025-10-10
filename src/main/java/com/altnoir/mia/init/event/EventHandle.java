package com.altnoir.mia.init.event;

import com.altnoir.mia.MiaClientConfig;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.client.event.KeyArrowEvent;
import com.altnoir.mia.init.MiaKeyBinding;
import com.altnoir.mia.datagen.DataGenerators;
import com.altnoir.mia.init.MiaNetworking;
import net.neoforged.bus.api.IEventBus;

public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(MiaConfig::onLoad);

        modEventBus.addListener(DataGenerators::gatherData);

        modEventBus.addListener(MiaNetworking::register);
        modEventBus.addListener(CurseEvent::attachEntityCapabilities);

        modEventBus.addListener(CapabilityRegister::RegisterCapabilitiesEvent);
        modEventBus.addListener(PlayerAttributeEvent::addPlayerAttributes);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(CurseEvent::onDimensionChange);
        gameEventBus.addListener(CurseEvent::onPlayerTick);

        gameEventBus.addListener(BrewingRecipesEvent::onBrewingRecipe);
    }
}
