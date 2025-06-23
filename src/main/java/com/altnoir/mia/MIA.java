package com.altnoir.mia;

import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.core.curse.CurseManager;
import com.altnoir.mia.init.*;
import com.altnoir.mia.init.MiaDensityFunctionTypes;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(MIA.MOD_ID)
public class MIA {
    public static final String MOD_ID = "mia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CurseManager CURSE_MANAGER = new CurseManager();

    public MIA(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        MiaItems.register(modEventBus);
        MiaBlocks.register(modEventBus);
        MiaItemGroups.register(modEventBus);

        MiaEffects.register(modEventBus);
        MiaComponents.register(modEventBus);
        MiaDensityFunctionTypes.register(modEventBus);

        MiaRecipes.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, MiaConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    private void reload(final AddReloadListenerEvent event){
        event.addListener(CURSE_MANAGER);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
