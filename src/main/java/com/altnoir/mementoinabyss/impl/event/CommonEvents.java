package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.impl.curse.CurseEvent;
import com.altnoir.mementoinabyss.impl.curse.CurseManager;
import com.altnoir.mementoinabyss.impl.strippable.StripEvent;
import com.altnoir.mementoinabyss.impl.tillable.TillEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import com.altnoir.mementoinabyss.worldgen.structure.DelayedCavePillarGenerator;

@EventBusSubscriber
public class CommonEvents {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event){
        CurseEvent.onEntityTick(event);
    }

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        CurseEvent.onNewRegistry(event);
    }

    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        CurseManager.init(event);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event){
        CurseEvent.onClone(event);
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        TillEvent.onRightClickBlock(event);
        StripEvent.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        DelayedCavePillarGenerator.onChunkLoad(event);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DelayedCavePillarGenerator.onServerTick(event);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DelayedCavePillarGenerator.clearPending();
    }

    @EventBusSubscriber
    public static class ModBusEvents {
    }
}
