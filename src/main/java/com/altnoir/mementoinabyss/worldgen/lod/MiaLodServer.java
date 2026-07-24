package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodDebugPayload;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

/** Owns the server-side cross-dimension LOD lifecycle. Called only from server events. */
public final class MiaLodServer {
    private static final int DEBUG_SYNC_INTERVAL_TICKS = 20;
    private static boolean enabledLastTick;
    private static boolean interestedLastTick;
    private static long nextDebugSyncTick;

    public static void captureIfNeeded(ServerLevel level, LevelChunk chunk) {
        if (!isEnabled()) return;
        CrossDimensionLodLinks.fromSource(level.dimension())
                .forEach(link -> MiaLodStorage.enqueueIfMissing(link, level, chunk));
    }

    public static void tick(MinecraftServer server) {
        MiaExecutors.refreshThreadLimit();
        boolean enabled = isEnabled();
        if (enabled != enabledLastTick) {
            enabledLastTick = enabled;
            if (enabled) requestForEligiblePlayers(server);
            else stopWork();
        }
        if (!enabled) return;

        boolean interested = MiaLodSampler.hasInterestedPlayer(server);
        if (interested != interestedLastTick) {
            interestedLastTick = interested;
            if (!interested) suspendConsumerWork();
        }
        if (interested) {
            MiaLodStorage.processPendingCapture(server);
            CrossDimensionLazyChunkGenerator.tick(server);
        }
        MiaLodSampler.tick(server);

        long gameTime = server.overworld().getGameTime();
        if (interested && gameTime >= nextDebugSyncTick) {
            nextDebugSyncTick = gameTime + DEBUG_SYNC_INTERVAL_TICKS;
            sendDebug(server);
        }
    }

    public static void onPlayerJoined(ServerPlayer player) {
        refreshPlayer(player);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        refreshPlayer(player);
    }

    public static void onPlayerLeft(ServerPlayer player) {
        MiaLodSampler.forget(player);
    }

    public static void setClientActive(ServerPlayer player, boolean active) {
        MiaLodSampler.setClientEnabled(player, active);
    }

    public static void stop() {
        stopWork();
        MiaLodSampler.clearClientPreferences();
        enabledLastTick = false;
    }

    private static void requestForEligiblePlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshPlayer(player);
        }
    }

    private static void refreshPlayer(ServerPlayer player) {
        if (isEnabled() && CrossDimensionLodLinks.forTarget(player.level().dimension()).isPresent()) {
            MiaLodSampler.request(player);
        } else {
            MiaLodSampler.remove(player);
        }
    }

    private static void sendDebug(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            var link = CrossDimensionLodLinks.forTarget(player.level().dimension()).orElse(null);
            if (link == null || !MiaLodSampler.wantsLod(player)) continue;
            var lazy = CrossDimensionLazyChunkGenerator.debugSnapshot(link);
            var stream = MiaLodSampler.debugSnapshot(player);
            PacketDistributor.sendToPlayer(player, new CrossDimensionLodDebugPayload(
                    link.id().toString(), lazy.phase(), lazy.generating(),
                    lazy.centralCursor(), lazy.centralTotal(), lazy.requested(), lazy.generated(), lazy.failed(),
                    lazy.activeX(), lazy.activeZ(), lazy.lastX(), lazy.lastZ(), lazy.elapsedMillis(), lazy.lastResult(),
                    stream.queued(), stream.scheduled(), stream.sent(), stream.loading(), stream.ready(),
                    stream.known(), stream.missing(), MiaExecutors.threadCount(),
                    MiaExecutors.activeTaskCount(), MiaExecutors.queuedTaskCount()));
        }
    }

    private static void stopWork() {
        MiaLodStorage.clearPendingCaptures();
        CrossDimensionLazyChunkGenerator.clear();
        MiaLodSampler.clearTasks();
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.REAL_CHUNK_CAPTURE);
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_LOAD);
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_PREGEN);
        interestedLastTick = false;
        nextDebugSyncTick = 0L;
    }

    private static void suspendConsumerWork() {
        CrossDimensionLazyChunkGenerator.clear();
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_LOAD);
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_PREGEN);
    }

    private static boolean isEnabled() {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodEnabled.get();
    }

    private MiaLodServer() {}
}
