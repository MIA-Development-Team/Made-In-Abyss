package com.altnoir.mia.init.event;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.datagen.DataGenerators;
import com.altnoir.mia.init.MiaNetworking;
import net.neoforged.bus.api.IEventBus;

public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(MiaConfig::loadEvent);
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(MiaNetworking::register);
        modEventBus.addListener(CurseEvent::attachEntityCapabilities);
        modEventBus.addListener(CapabilityRegister::RegisterCapabilitiesEvent);
        modEventBus.addListener(PlayerAttributeEvent::addPlayerAttributes);

        modEventBus.addListener(PackFindersEvent::packSetup);
    }

    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(CurseEvent::onDimensionChange);
        gameEventBus.addListener(CurseEvent::onPlayerTick);

        gameEventBus.addListener(BonemealUseEvent::onBonemealUse);
        gameEventBus.addListener(BrewingRecipesEvent::onBrewingRecipe);
    }
}
