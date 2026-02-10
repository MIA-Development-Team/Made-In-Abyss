package com.altnoir.mia;

import com.altnoir.mia.core.curse.CurseManager;
import com.altnoir.mia.core.spawner.AbyssTrialSpawnerManager;
import com.altnoir.mia.init.*;
import com.altnoir.mia.core.event.EventHandle;
import com.altnoir.mia.init.worldgen.*;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(MIA.MOD_ID)
public class MIA {
    public static final String MOD_ID = "mia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CurseManager CURSE_MANAGER = new CurseManager();
    public static final AbyssTrialSpawnerManager SPAWNER_MANAGER = new AbyssTrialSpawnerManager();

    public MIA(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        MiaItems.register(modEventBus);
        MiaBlocks.register(modEventBus);
        MiaBlockEntities.register(modEventBus);
        MiaItemGroups.register(modEventBus);

        MiaAttributes.register(modEventBus);
        MiaEffects.register(modEventBus);
        MiaPotions.register(modEventBus);
        MiaComponents.register(modEventBus);
        MiaSounds.register(modEventBus);

        MiaBiomeSources.register(modEventBus);
        MiaDensityFunctionTypes.register(modEventBus);
        MiaFeatures.register(modEventBus);
        MiaPlacements.register(modEventBus);
        MiaFoliagePlacerTypes.register(modEventBus);
        MiaTrunkPlacerTypes.register(modEventBus);

        MiaRecipes.register(modEventBus);
        MiaAttachments.register(modEventBus);
        MiaParticles.register(modEventBus);
        MiaEntities.register(modEventBus);
        MiaMenus.register(modEventBus);
        MiaStats.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, MiaConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, MiaConfig.SERVER_SPEC);

        var gameEventBus = NeoForge.EVENT_BUS;
        gameEventBus.register(this);
        EventHandle.addListener(modEventBus, gameEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        MiaStats.init();
    }

    @SubscribeEvent
    private void reload(final AddReloadListenerEvent event) {
        event.addListener(CURSE_MANAGER);
        event.addListener(SPAWNER_MANAGER);
    }
}