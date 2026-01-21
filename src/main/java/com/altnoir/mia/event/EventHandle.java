package com.altnoir.mia.event;

import com.altnoir.mia.init.MiaEvents;
import net.neoforged.bus.api.IEventBus;

public class EventHandle {
    public static void addListener(IEventBus modEventBus, IEventBus gameEventBus) {
        EventHandle.addModEventBus(modEventBus);
        EventHandle.addGameEventBus(gameEventBus);
    }

    // 模组事件-注册事件
    public static void addModEventBus(IEventBus modEventBus) {
        modEventBus.addListener(MiaEvents::modLoad);
        modEventBus.addListener(MiaEvents::registerCapabilities);
        modEventBus.addListener(MiaEvents::registerPayloadHandlers);

        modEventBus.addListener(MiaEvents::gatherData);
        modEventBus.addListener(MiaEvents::addPackFinders);
    }

    // 游戏事件-持续事件
    public static void addGameEventBus(IEventBus gameEventBus) {
        gameEventBus.addListener(MiaEvents::onPlayerDimensionChange);
        gameEventBus.addListener(MiaEvents::onPlayerTick);
        gameEventBus.addListener(MiaEvents::onBonemealUse);
        gameEventBus.addListener(MiaEvents::onLivingDeath);
        gameEventBus.addListener(MiaEvents::onBrewingRecipe);
        gameEventBus.addListener(MiaEvents::onRegisterCommands);
    }
}