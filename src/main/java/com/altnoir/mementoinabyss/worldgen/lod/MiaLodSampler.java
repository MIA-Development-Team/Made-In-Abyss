package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.ArrayList;
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

/** Streams stored cross-dimension LOD chunks through a player-centered moving window. */
public final class MiaLodSampler {
    private static final int MAX_CHUNKS_PER_TICK = 16;
    private static final int MAX_RESULTS_PER_TICK = 64;
    private static final int MAX_BYTES_PER_TICK = 128 * 1024;
    private static final int MAX_IN_FLIGHT_PER_TASK = 32;
    private static final long MISSING_RETRY_TICKS = 1200L;
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
        TASKS.put(player.getUUID(), new Task(player, link, radius, player.level().getGameTime()));
        MementoInAbyss.LOGGER.info("Queued {} cross-dimension LOD chunks for link {} (radius {}) for {}",
                TASKS.get(player.getUUID()).chunks.size(), link.id(), radius, player.getGameProfile().name());
    }

    public static void tick(MinecraftServer server) {
        for (Task original : TASKS.values()) {
            Task task = original;
            ServerLevel source = server.getLevel(task.link.source());
            if (source == null || !task.player.isAlive()
                    || !task.player.level().dimension().equals(task.link.target())) {
                TASKS.remove(task.player.getUUID(), task);
                continue;
            }
            ChunkPos playerChunk = task.player.chunkPosition();
            int configuredRadius = CrossDimensionLodLinks.radius(task.link);
            int configuredDetailRadius = CrossDimensionLodLinks.detailRadius(task.link);
            long gameTime = task.player.level().getGameTime();
            if (task.centerChunkX != playerChunk.x() || task.centerChunkZ != playerChunk.z()
                    || task.radius != configuredRadius || task.detailRadius != configuredDetailRadius
                    || task.isIdle() && task.nextMissingRetry <= gameTime) {
                Task replacement = new Task(task, playerChunk.x(), playerChunk.z(),
                        configuredRadius, configuredDetailRadius, gameTime);
                if (!TASKS.replace(task.player.getUUID(), task, replacement)) continue;
                task = replacement;
            }
            sendCompleted(task);
            if (task.isPassComplete()) {
                task.chunks = List.of();
                task.scheduleCursor = 0;
                task.sendCursor = 0;
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
                    MiaLodStorage.StoredChunk selected = null;
                    try {
                        Optional<MiaLodStorage.StoredChunk> stored =
                                MiaLodStorage.read(source, pos);
                        if (stored.isPresent()) selected = MiaLodStorage.coarsen(stored.get(), cellSize);
                    } catch (Throwable throwable) {
                        MementoInAbyss.LOGGER.warn("Unable to prepare cross-dimension LOD chunk {}", pos, throwable);
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
                long key = CrossDimensionLodKey.pack(prepared.pos.x(), prepared.pos.z());
                long retryAt = task.player.level().getGameTime() + MISSING_RETRY_TICKS;
                task.missingUntil.put(key, retryAt);
                task.nextMissingRetry = Math.min(task.nextMissingRetry, retryAt);
                processed++;
                continue;
            }

            int estimatedBytes = estimatedPayloadBytes(task, prepared.chunk);
            if (sent > 0 && bytes + estimatedBytes > MAX_BYTES_PER_TICK) break;
            if (!task.completed.remove(task.sendCursor, prepared)) continue;
            task.sendCursor++;
            processed++;
            CrossDimensionLodPayload payload = new CrossDimensionLodPayload(
                    task.link.id().toString(), task.link.displayYOffset(), task.radius, task.firstPayload,
                    prepared.chunk.chunkX(), prepared.chunk.chunkZ(), prepared.chunk.cellSize(),
                    prepared.chunk.minY(), prepared.chunk.yCells(), prepared.chunk.palette(), prepared.chunk.voxels());
            task.firstPayload = false;
            PacketDistributor.sendToPlayer(task.player, payload);
            long key = CrossDimensionLodKey.pack(prepared.pos.x(), prepared.pos.z());
            task.knownCellSizes.put(key, prepared.chunk.cellSize());
            task.missingUntil.remove(key);
            bytes += estimatedBytes;
            sent++;
        }
    }

    private static int estimatedPayloadBytes(Task task, MiaLodStorage.StoredChunk chunk) {
        return 48 + task.link.id().toString().length() * 3
                + chunk.palette().length * 5 + chunk.voxels().length * Short.BYTES;
    }

    public static void remove(ServerPlayer player) { TASKS.remove(player.getUUID()); }

    /** Makes a newly written lazy-generated chunk eligible for immediate delivery. Server thread only. */
    public static void notifyAvailable(CrossDimensionLodLink link, ChunkPos pos) {
        long key = CrossDimensionLodKey.pack(pos.x(), pos.z());
        for (Task task : TASKS.values()) {
            if (!task.link.equals(link)) continue;
            task.missingUntil.remove(key);
            task.nextMissingRetry = Math.min(task.nextMissingRetry, task.player.level().getGameTime());
        }
    }

    /** Forces a newly captured complete chunk to replace a provisional payload on clients. */
    public static void notifyReplaced(CrossDimensionLodLink link, ChunkPos pos) {
        long key = CrossDimensionLodKey.pack(pos.x(), pos.z());
        for (Task task : TASKS.values()) {
            if (!task.link.equals(link)) continue;
            task.knownCellSizes.remove(key);
            task.missingUntil.remove(key);
            task.nextMissingRetry = Math.min(task.nextMissingRetry, task.player.level().getGameTime());
        }
    }

    public static void clear() { TASKS.clear(); }

    public static DebugSnapshot debugSnapshot(ServerPlayer player) {
        Task task = TASKS.get(player.getUUID());
        if (task == null) return new DebugSnapshot(0, 0, 0, 0, 0, 0, 0);
        return new DebugSnapshot(task.chunks.size(), task.scheduleCursor, task.sendCursor,
                task.inFlight.get(), task.completed.size(), task.knownCellSizes.size(), task.missingUntil.size());
    }

    public record DebugSnapshot(int queued, int scheduled, int sent, int loading,
                                int ready, int known, int missing) {}

    private static final class Task {
        private final ServerPlayer player;
        private final CrossDimensionLodLink link;
        private final int radius;
        private final int detailRadius;
        private final int centerChunkX;
        private final int centerChunkZ;
        private final Long2IntOpenHashMap knownCellSizes;
        private final Long2LongOpenHashMap missingUntil;
        private final long planningTime;
        private List<ChunkPos> chunks;
        private int scheduleCursor;
        private int sendCursor;
        private final AtomicInteger inFlight = new AtomicInteger();
        private final ConcurrentMap<Integer, PreparedChunk> completed = new ConcurrentHashMap<>();
        private boolean firstPayload = true;
        private long nextMissingRetry = Long.MAX_VALUE;

        private Task(ServerPlayer player, CrossDimensionLodLink link, int radius, long planningTime) {
            this(player, link, player.chunkPosition().x(), player.chunkPosition().z(), radius,
                    CrossDimensionLodLinks.detailRadius(link), new Long2IntOpenHashMap(),
                    new Long2LongOpenHashMap(), true, planningTime);
        }

        private Task(Task previous, int centerChunkX, int centerChunkZ, int radius,
                     int detailRadius, long planningTime) {
            this(previous.player, previous.link, centerChunkX, centerChunkZ, radius, detailRadius,
                    previous.knownCellSizes, previous.missingUntil, previous.firstPayload, planningTime);
            retainNearby(centerChunkX, centerChunkZ, radius);
        }

        private Task(ServerPlayer player, CrossDimensionLodLink link, int centerChunkX, int centerChunkZ,
                     int radius, int detailRadius, Long2IntOpenHashMap knownCellSizes,
                     Long2LongOpenHashMap missingUntil, boolean firstPayload, long planningTime) {
            this.player = player;
            this.link = link;
            this.radius = radius;
            this.detailRadius = detailRadius;
            this.centerChunkX = centerChunkX;
            this.centerChunkZ = centerChunkZ;
            this.knownCellSizes = knownCellSizes;
            this.missingUntil = missingUntil;
            this.firstPayload = firstPayload;
            this.planningTime = planningTime;
            int chunkRadius = Mth.ceil(radius / 16.0);
            this.chunks = new ArrayList<>();
            queueIfNeeded(centerChunkX, centerChunkZ);
            // Concentric square rings give stable near-to-far ordering without allocating and sorting the whole disc.
            for (int ring = 1; ring <= chunkRadius; ring++) {
                for (int x = -ring; x <= ring; x++) {
                    queueIfNeeded(centerChunkX + x, centerChunkZ - ring);
                    queueIfNeeded(centerChunkX + x, centerChunkZ + ring);
                }
                for (int z = -ring + 1; z < ring; z++) {
                    queueIfNeeded(centerChunkX - ring, centerChunkZ + z);
                    queueIfNeeded(centerChunkX + ring, centerChunkZ + z);
                }
            }
        }

        private int cellSize(ChunkPos pos) {
            double dx = pos.getMiddleBlockX() - windowCenterX();
            double dz = pos.getMiddleBlockZ() - windowCenterZ();
            return cellSize(dx * dx + dz * dz);
        }

        private int cellSize(double distanceSquared) {
            double fineEnd = detailRadius * FINE_LEVEL_END;
            double mediumEnd = detailRadius * MEDIUM_LEVEL_END;
            if (distanceSquared <= fineEnd * fineEnd) return 4;
            if (distanceSquared <= mediumEnd * mediumEnd) return 8;
            return 16;
        }

        private void queueIfNeeded(int chunkX, int chunkZ) {
            double dx = chunkX * 16.0 + 8.0 - windowCenterX();
            double dz = chunkZ * 16.0 + 8.0 - windowCenterZ();
            double limit = radius + 12.0;
            if (dx * dx + dz * dz > limit * limit) return;
            long key = CrossDimensionLodKey.pack(chunkX, chunkZ);
            long retryAt = missingUntil.getOrDefault(key, Long.MIN_VALUE);
            if (retryAt > planningTime) {
                nextMissingRetry = Math.min(nextMissingRetry, retryAt);
                return;
            }
            if (retryAt != Long.MIN_VALUE) missingUntil.remove(key);
            int wantedCellSize = cellSize(dx * dx + dz * dz);
            if (knownCellSizes.getOrDefault(key, -1) != wantedCellSize) {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }

        private double windowCenterX() { return centerChunkX * 16.0 + 8.0; }
        private double windowCenterZ() { return centerChunkZ * 16.0 + 8.0; }

        private void retainNearby(int centerChunkX, int centerChunkZ, int radius) {
            int retentionChunks = Mth.ceil(radius / 16.0) + 8;
            long maxDistanceSquared = (long) retentionChunks * retentionChunks;
            var knownIterator = knownCellSizes.keySet().iterator();
            while (knownIterator.hasNext()) {
                long key = knownIterator.nextLong();
                long dx = (long) CrossDimensionLodKey.x(key) - centerChunkX;
                long dz = (long) CrossDimensionLodKey.z(key) - centerChunkZ;
                if (dx * dx + dz * dz > maxDistanceSquared) knownIterator.remove();
            }
            var missingIterator = missingUntil.keySet().iterator();
            while (missingIterator.hasNext()) {
                long key = missingIterator.nextLong();
                long dx = (long) CrossDimensionLodKey.x(key) - centerChunkX;
                long dz = (long) CrossDimensionLodKey.z(key) - centerChunkZ;
                if (dx * dx + dz * dz > maxDistanceSquared) missingIterator.remove();
            }
        }

        private boolean isPassComplete() {
            return scheduleCursor == chunks.size() && sendCursor == chunks.size()
                    && inFlight.get() == 0 && completed.isEmpty();
        }

        private boolean isIdle() { return chunks.isEmpty() && inFlight.get() == 0 && completed.isEmpty(); }

    }

    private record PreparedChunk(ChunkPos pos, MiaLodStorage.StoredChunk chunk) {}

    private MiaLodSampler() {}
}
