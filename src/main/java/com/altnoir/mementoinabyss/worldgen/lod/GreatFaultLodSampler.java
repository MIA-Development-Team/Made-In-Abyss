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
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Builds coarse 3D chunks directly from the Great Fault noise generator, without generating chunks. */
public final class GreatFaultLodSampler {
    private static final int MAX_CHUNKS_PER_TICK = 16;
    private static final int MAX_RESULTS_PER_TICK = 64;
    private static final int MAX_BYTES_PER_TICK = 128 * 1024;
    private static final int MAX_IN_FLIGHT_PER_TASK = 32;
    private static final ThreadPoolExecutor LOADER = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(256), runnable -> {
                Thread thread = new Thread(runnable, "MIA LOD loader");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
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
            sendCompleted(task);
            if (task.isPassComplete()) {
                if (!task.missing.isEmpty() && task.pass++ < 2) {
                    task.chunks = new ArrayList<>(task.missing);
                    task.missing.clear();
                    task.scheduleCursor = 0;
                    task.sendCursor = 0;
                } else {
                    TASKS.remove(task.player.getUUID(), task);
                    continue;
                }
            }
            scheduleLoads(task, source);
        }
    }

    private static void scheduleLoads(Task task, ServerLevel source) {
        while (task.scheduleCursor < task.chunks.size()
                && task.scheduleCursor - task.sendCursor < MAX_IN_FLIGHT_PER_TASK) {
            int sequence = task.scheduleCursor;
            ChunkPos pos = task.chunks.get(sequence);
            int cellSize = task.cellSize(pos);
            task.scheduleCursor++;
            task.inFlight.incrementAndGet();
            try {
                LOADER.execute(() -> {
                    GreatFaultLodStorage.StoredChunk selected = null;
                    try {
                        Optional<GreatFaultLodStorage.StoredChunk> stored =
                                GreatFaultLodStorage.read(task.link, source, pos);
                        if (stored.isPresent()) selected = GreatFaultLodStorage.coarsen(stored.get(), cellSize);
                    } catch (Throwable throwable) {
                        MementoInAbyss.LOGGER.warn("Unable to prepare Great Fault LOD chunk {}", pos, throwable);
                    } finally {
                        task.completed.put(sequence, new PreparedChunk(pos, selected));
                        task.inFlight.decrementAndGet();
                    }
                });
            } catch (RejectedExecutionException ignored) {
                task.inFlight.decrementAndGet();
                task.scheduleCursor--;
                break;
            }
        }
    }

    private static void sendCompleted(Task task) {
        int sent = 0;
        int processed = 0;
        int bytes = 0;
        while (sent < MAX_CHUNKS_PER_TICK && processed < MAX_RESULTS_PER_TICK) {
            PreparedChunk prepared = task.completed.get(task.sendCursor);
            if (prepared == null) break;
            if (prepared.chunk == null) {
                task.completed.remove(task.sendCursor, prepared);
                task.sendCursor++;
                task.missing.add(prepared.pos);
                processed++;
                continue;
            }

            int estimatedBytes = estimatedPayloadBytes(task, prepared.chunk);
            if (sent > 0 && bytes + estimatedBytes > MAX_BYTES_PER_TICK) break;
            if (!task.completed.remove(task.sendCursor, prepared)) continue;
            task.sendCursor++;
            processed++;
            CrossDimensionLodPayload payload = new CrossDimensionLodPayload(
                    task.link.id().toString(), task.link.displayYOffset(), task.link.outsidePlaneY(),
                    task.link.centerX(), task.link.centerZ(), task.radius, task.firstPayload,
                    prepared.chunk.chunkX(), prepared.chunk.chunkZ(), prepared.chunk.cellSize(),
                    prepared.chunk.minY(), prepared.chunk.yCells(), prepared.chunk.palette(), prepared.chunk.voxels());
            task.firstPayload = false;
            PacketDistributor.sendToPlayer(task.player, payload);
            bytes += estimatedBytes;
            sent++;
        }
    }

    private static int estimatedPayloadBytes(Task task, GreatFaultLodStorage.StoredChunk chunk) {
        return 64 + task.link.id().toString().length() * 3
                + chunk.palette().length * 5 + chunk.voxels().length * Short.BYTES;
    }

    public static void remove(ServerPlayer player) { TASKS.remove(player.getUUID()); }
    public static void clear() { TASKS.clear(); }

    private static final class Task {
        private final ServerPlayer player;
        private final CrossDimensionLodLink link;
        private final int radius;
        private List<ChunkPos> chunks;
        private final List<ChunkPos> missing = new ArrayList<>();
        private int scheduleCursor;
        private int sendCursor;
        private final AtomicInteger inFlight = new AtomicInteger();
        private final ConcurrentMap<Integer, PreparedChunk> completed = new ConcurrentHashMap<>();
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

        private boolean isPassComplete() {
            return scheduleCursor == chunks.size() && sendCursor == chunks.size()
                    && inFlight.get() == 0 && completed.isEmpty();
        }

    }

    private record PreparedChunk(ChunkPos pos, GreatFaultLodStorage.StoredChunk chunk) {}

    private GreatFaultLodSampler() {}
}
