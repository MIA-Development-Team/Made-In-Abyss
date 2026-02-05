package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.datagen.DataGenerators;
import com.altnoir.mia.event.common.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class MiaEvents {
    // 模组事件-注册事件
    public static void modLoad(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading) {
            MiaConfig.onLoad(event.getConfig());
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        CapabilityRegister.register(event);
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        MiaNetworking.register(event.registrar(MIA.MOD_ID));
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerators.gatherData(event.getGenerator(), event.getExistingFileHelper(), event.getLookupProvider(), event.includeServer(), event.includeClient());
    }

    public static void addPackFinders(AddPackFindersEvent event) {
        PackFindersEvent.packSetup(event);
    }

    // 游戏事件-持续事件
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        CurseEvent.onDimensionChange(event.getEntity());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        CurseEvent.onPlayerTick(event.getEntity());
    }

    public static void onBonemealUse(BonemealEvent event) {
        BonemealUseEvent.onBonemealUse(event.getState(), event.getPos(), event.getStack(), event.getLevel(), event.getPlayer());
    }

    public static void onBrewingRecipe(RegisterBrewingRecipesEvent event) {
        BrewingRecipesEvent.onBrewingRecipe(event.getBuilder());
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MiaCommandsEvent.onRegisterCommands(event.getDispatcher());
    }

    // 实体事件
    public static void onLivingDeath(LivingDeathEvent event) {
        KillCountEvent.onLivingDeath(event.getEntity(), event.getSource());
    }

    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel().isClientSide()) return;
        AbyssMobEvent.onCheckSpawn(event.getEntity(), event.getLevel(), event.getSpawnType());
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        AbyssMobEvent.onLivingDrops(event.getEntity(), event.getDrops(), event.getSource());
    }
}
