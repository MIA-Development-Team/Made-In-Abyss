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
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodSampler;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodStorage;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLazyChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerPlayer;

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
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            CrossDimensionLodLinks.fromSource(level.dimension())
                    .forEach(link -> MiaLodStorage.enqueueIfMissing(link, level, chunk));
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            CrossDimensionLodLinks.fromSource(level.dimension())
                    .forEach(link -> MiaLodStorage.enqueueIfMissing(link, level, chunk));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DelayedCavePillarGenerator.onServerTick(event);
        MiaLodStorage.processPendingCapture();
        CrossDimensionLazyChunkGenerator.tick(event.getServer());
        MiaLodSampler.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DelayedCavePillarGenerator.clearPending();
        MiaLodStorage.clearPendingCaptures();
        CrossDimensionLazyChunkGenerator.clear();
        MiaLodSampler.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && CrossDimensionLodLinks.forTarget(player.level().dimension()).isPresent()) {
            MiaLodSampler.request(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (CrossDimensionLodLinks.forTarget(event.getTo()).isPresent()) MiaLodSampler.request(player);
            else MiaLodSampler.remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MiaLodSampler.remove(player);
    }

    @EventBusSubscriber
    public static class ModBusEvents {
    }
}
