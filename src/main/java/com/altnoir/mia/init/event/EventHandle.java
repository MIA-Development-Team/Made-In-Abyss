package com.altnoir.mia.init.event;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.datagen.DataGenerators;
import com.altnoir.mia.event.server.*;
import com.altnoir.mia.init.MiaNetworking;
import net.neoforged.bus.api.IEventBus;

public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    // 模组事件-注册事件
    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(CurseEvent::attachEntityCapabilities);
        modEventBus.addListener(CapabilityRegister::RegisterCapabilitiesEvent);

        modEventBus.addListener(MiaConfig::loadEvent);
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(MiaNetworking::register);
        modEventBus.addListener(PlayerAttributeEvent::addPlayerAttributes);

        modEventBus.addListener(PackFindersEvent::packSetup);
    }

    // 游戏事件-持续事件
    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(CurseEvent::onDimensionChange);
        gameEventBus.addListener(CurseEvent::onPlayerTick);

        gameEventBus.addListener(BonemealUseEvent::onBonemealUse);
        gameEventBus.addListener(KillCountEvent::onLivingDeath);
        gameEventBus.addListener(BrewingRecipesEvent::onBrewingRecipe);
        gameEventBus.addListener(MiaCommandsEvent::onRegisterCommands);
    }
}