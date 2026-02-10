package com.altnoir.mia.client;

import com.altnoir.mia.MIA;
import com.altnoir.mia.compat.ponder.TestPonder;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MIA.MOD_ID, dist = Dist.CLIENT)
public class MIAClient {
    public MIAClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, MiaClientConfig.CLIENT_SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        var gameEventBus = NeoForge.EVENT_BUS;
        MIAClient.addModEventBus(modEventBus);
        MIAClient.addGameEventBus(gameEventBus);
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
        gameEventBus.addListener(MiaClientEvents::onRenderNameTag);
        //gameEventBus.addListener(MiaClientEvents::onCameraAngles);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new TestPonder());
    }
}