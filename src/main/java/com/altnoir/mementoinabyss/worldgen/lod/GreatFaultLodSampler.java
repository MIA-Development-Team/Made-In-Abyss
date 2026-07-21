package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Builds coarse 3D chunks directly from the Great Fault noise generator, without generating chunks. */
public final class GreatFaultLodSampler {
    private static final int CHUNKS_PER_TICK = 2;
    private static final double FINE_LEVEL_END = 0.34;
    private static final double MEDIUM_LEVEL_END = 0.67;
    private static final Map<UUID, Task> TASKS = new ConcurrentHashMap<>();

    public static void request(ServerPlayer player) {
        var link = CrossDimensionLodLinks.forTarget(player.level().dimension()).orElse(null);
        if (link == null) return;
        int radius = CrossDimensionLodLinks.radius(link);
        TASKS.put(player.getUUID(), new Task(player, link, radius));
        MementoInAbyss.LOGGER.info("Queued {} cross-dimension LOD chunks for link {} (radius {}) for {}",
                TASKS.get(player.getUUID()).chunks.size(), link.id(), radius, player.getGameProfile().name());
    }

    public static void tick(MinecraftServer server) {
        for (Task task : TASKS.values()) {
            ServerLevel source = server.getLevel(task.link.source());
            if (source == null || !task.player.isAlive()
                    || !task.player.level().dimension().equals(task.link.target())) {
                TASKS.remove(task.player.getUUID(), task);
                continue;
            }
            for (int i = 0; i < CHUNKS_PER_TICK && task.cursor < task.chunks.size(); i++) {
                ChunkPos pos = task.chunks.get(task.cursor++);
                var storedChunk = GreatFaultLodStorage.read(task.link, source, pos);
                if (storedChunk.isEmpty()) {
                    task.missing.add(pos);
                    continue;
                }
                storedChunk.ifPresent(stored -> {
                    int cellSize = task.cellSize(pos);
                    var selectedLevel = GreatFaultLodStorage.coarsen(stored, cellSize);
                    CrossDimensionLodPayload payload = new CrossDimensionLodPayload(
                            task.link.id().toString(), task.link.displayYOffset(), task.link.outsidePlaneY(),
                            task.link.centerX(), task.link.centerZ(),
                            task.radius, task.firstPayload,
                            selectedLevel.chunkX(), selectedLevel.chunkZ(), selectedLevel.cellSize(),
                            selectedLevel.minY(), selectedLevel.yCells(), selectedLevel.palette(), selectedLevel.voxels());
                    task.firstPayload = false;
                    PacketDistributor.sendToPlayer(task.player, payload);
                });
            }
            if (task.cursor == task.chunks.size()) {
                if (!task.missing.isEmpty() && task.pass++ < 2) {
                    task.chunks = new ArrayList<>(task.missing);
                    task.missing.clear();
                    task.cursor = 0;
                } else {
                    TASKS.remove(task.player.getUUID(), task);
                }
            }
        }
    }

    public static void remove(ServerPlayer player) { TASKS.remove(player.getUUID()); }
    public static void clear() { TASKS.clear(); }

    private static final class Task {
        private final ServerPlayer player;
        private final CrossDimensionLodLink link;
        private final int radius;
        private List<ChunkPos> chunks;
        private final List<ChunkPos> missing = new ArrayList<>();
        private int cursor;
        private boolean firstPayload = true;
        private int pass;

        private Task(ServerPlayer player, CrossDimensionLodLink link, int radius) {
            this.player = player;
            this.link = link;
            this.radius = radius;
            int chunkRadius = Mth.ceil(radius / 16.0);
            this.chunks = new ArrayList<>();
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                for (int x = -chunkRadius; x <= chunkRadius; x++) {
                    int chunkX = Math.floorDiv(link.centerX(), 16) + x;
                    int chunkZ = Math.floorDiv(link.centerZ(), 16) + z;
                    double centerX = chunkX * 16.0 + 8.0 - link.centerX();
                    double centerZ = chunkZ * 16.0 + 8.0 - link.centerZ();
                    if (centerX * centerX + centerZ * centerZ <= (radius + 12.0) * (radius + 12.0)) {
                        chunks.add(new ChunkPos(chunkX, chunkZ));
                    }
                }
            }
            chunks.sort(Comparator.comparingDouble(pos -> {
                double dx = pos.getMiddleBlockX() - link.centerX();
                double dz = pos.getMiddleBlockZ() - link.centerZ();
                return dx * dx + dz * dz;
            }));
        }

        private int cellSize(ChunkPos pos) {
            double dx = pos.getMiddleBlockX() - link.centerX();
            double dz = pos.getMiddleBlockZ() - link.centerZ();
            double normalizedDistance = Math.sqrt(dx * dx + dz * dz) / radius;
            if (normalizedDistance <= FINE_LEVEL_END) return 4;
            if (normalizedDistance <= MEDIUM_LEVEL_END) return 8;
            return 16;
        }

    }

    private GreatFaultLodSampler() {}
}
