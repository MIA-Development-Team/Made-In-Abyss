package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.impl.lod.LodTerrainReadiness;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;

/** Server-thread snapshots of real, already-loaded and sent player terrain. */
final class CurrentDimensionPriority {
    private static final Map<ServerPlayer, State> STATES = new WeakHashMap<>();
    private static volatile boolean backgroundAllowed;

    private static final class State {
        ServerLevel level;
        long checked = Long.MIN_VALUE;
        boolean coverage, ready;
        final LodTerrainReadiness.Gate gate = new LodTerrainReadiness.Gate();
    }

    static void tick(MinecraftServer server) {
        var players = server.getPlayerList().getPlayers();
        STATES.keySet().removeIf(player -> !players.contains(player));
        long tick = server.getTickCount();
        for (var player : players) {
            var state = STATES.computeIfAbsent(player, ignored -> new State());
            if (state.level != player.level()) {
                state.level = player.level();
                state.gate.reset();
                state.checked = Long.MIN_VALUE;
            }
            if (state.checked == Long.MIN_VALUE || tick - state.checked >= 5) {
                state.checked = tick;
                if (player.getChunkTrackingView() instanceof ChunkTrackingView.Positioned view) {
                    var center = player.chunkPosition();
                    var source = player.level().getChunkSource();
                    state.coverage =
                            LodTerrainReadiness.sample(
                                            center.x(),
                                            center.z(),
                                            view.viewDistance(),
                                            (x, z) ->
                                                    source.getChunkNow(x, z) != null
                                                            && source.chunkMap.isChunkTracked(
                                                                    player, x, z))
                                    .mostlyReady();
                } else state.coverage = false;
            }
            state.ready = state.gate.update(tick, state.coverage);
        }
        backgroundAllowed =
                players.stream()
                        .filter(ServerPlayer::isAlive)
                        .allMatch(CurrentDimensionPriority::ready);
    }

    static boolean ready(ServerPlayer player) {
        var state = STATES.get(player);
        return state != null && state.level == player.level() && state.ready;
    }

    static boolean allReady(MinecraftServer server) {
        return backgroundAllowed;
    }

    static boolean backgroundAllowed() {
        return backgroundAllowed;
    }

    static void clear() {
        STATES.clear();
        backgroundAllowed = false;
    }

    private CurrentDimensionPriority() {}
}
