package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VerticalBoundaryChunks {
    private static final DeferredRegister<TicketType> TYPES =
            DeferredRegister.create(Registries.TICKET_TYPE, MementoInAbyss.ID);
    private static final DeferredHolder<TicketType, TicketType> PRELOAD =
            TYPES.register("vertical_boundary", () -> new TicketType(0, TicketType.FLAG_LOADING));
    private static final int RADIUS = 1;
    private static final int RETAIN_TICKS = 20;
    private static final int MAX_GENERATION_LEAD = 128;
    private static final Map<Target, Long> REQUESTS = new HashMap<>();

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    public static void tick(MinecraftServer server) {
        long now = server.getTickCount();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || !MiaDimensions.isMiaDimension(player.level().dimension()))
                continue;
            var window = VerticalSectionRange.around(player.getY());
            for (var link : CrossDimensionLodLinks.forTargets(player.level())) {
                if (!MiaDimensions.isMiaDimension(link.source())) continue;
                int minY = link.sourceHeight().minY() + link.displayYOffset();
                int maxY = link.sourceHeight().maxY() + link.displayYOffset();
                double toward = player.getKnownMovement().y * (link.displayYOffset() < 0 ? -1 : 1);
                int generationLead =
                        (int) Math.ceil(Math.clamp(toward * 60, 0, MAX_GENERATION_LEAD));
                // Generation leads the transmitted window; cold terrain must finish before it is
                // visible.
                if ((long) window.maxYExclusive() + generationLead <= minY
                        || (long) window.minY() - generationLead >= maxY) continue;
                ServerLevel source = server.getLevel(link.source());
                if (source == null) continue;
                Target target = new Target(link.source(), player.chunkPosition());
                if (REQUESTS.put(target, now) == null) {
                    // Adding a loading-only ticket queues generation; do not call getChunkFuture
                    // here because its main-thread path blocks until generation completes.
                    source.getChunkSource()
                            .addTicketWithRadius(PRELOAD.get(), target.center(), RADIUS);
                }
            }
        }
        var iterator = REQUESTS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now - entry.getValue() <= RETAIN_TICKS) continue;
            ServerLevel level = server.getLevel(entry.getKey().dimension());
            if (level != null)
                level.getChunkSource()
                        .removeTicketWithRadius(PRELOAD.get(), entry.getKey().center(), RADIUS);
            iterator.remove();
        }
    }

    public static boolean ready(ServerLevel level, ChunkPos center) {
        for (int x = center.x() - RADIUS; x <= center.x() + RADIUS; x++) {
            for (int z = center.z() - RADIUS; z <= center.z() + RADIUS; z++) {
                if (level.getChunkSource().getChunkNow(x, z) == null) return false;
            }
        }
        return true;
    }

    public static void clear() {
        REQUESTS.clear();
    }

    private record Target(ResourceKey<Level> dimension, ChunkPos center) {}

    private VerticalBoundaryChunks() {}
}
