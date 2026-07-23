package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/** Persistent server-owned LOD storage, shared by every link that uses the same source dimension. */
final class MiaLodStorage {
    private static final int BASE_CELL_SIZE = 4;
    private static final int MAGIC = 0x4D49414C; // MIAL
    private static final int VERSION = 5;
    private static final int MAX_PENDING_WRITES = 128;
    private static final int MAX_CAPTURE_SUBMISSIONS_PER_TICK = 2;
    private static final int MAX_CAPTURES_IN_FLIGHT = 16;
    private static final ThreadPoolExecutor WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING_WRITES), r -> {
                Thread thread = new Thread(r, "MIA cross-dimension LOD writer");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
    private static final java.util.Set<Path> PENDING_WRITES = ConcurrentHashMap.newKeySet();
    private static final java.util.Set<Path> READY_DIRECTORIES = ConcurrentHashMap.newKeySet();
    /** Chunk events may run off-thread under C2ME; entries retain chunks only weakly until selected. */
    private static final ConcurrentLinkedDeque<PendingCapture> PENDING_CAPTURES = new ConcurrentLinkedDeque<>();
    private static final java.util.Set<CaptureKey> PENDING_CAPTURE_KEYS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger PENDING_CAPTURE_COUNT = new AtomicInteger();
    private static final AtomicInteger CAPTURES_IN_FLIGHT = new AtomicInteger();
    private static final AtomicLong CAPTURE_EPOCH = new AtomicLong();

    static void enqueueIfMissing(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        if (!lodEnabled()) return;
        int queueLimit = MementoInAbyss.CONFIGS.guiSection.crossDimensionLodCaptureQueueLimit.get();
        if (PENDING_CAPTURE_COUNT.get() >= queueLimit) return;
        CaptureKey key = new CaptureKey(level.dimension(),
                ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()));
        if (!PENDING_CAPTURE_KEYS.add(key)) return;
        PENDING_CAPTURE_COUNT.incrementAndGet();
        PENDING_CAPTURES.addLast(new PendingCapture(
                key, link, new WeakReference<>(chunk)));
    }

    static void processPendingCapture(MinecraftServer server) {
        if (!lodEnabled()) return;
        int submitted = 0;
        for (int checked = 0; checked < 32 && submitted < MAX_CAPTURE_SUBMISSIONS_PER_TICK
                && CAPTURES_IN_FLIGHT.get() < MAX_CAPTURES_IN_FLIGHT; checked++) {
            PendingCapture pending = PENDING_CAPTURES.pollFirst();
            if (pending == null) return;
            PENDING_CAPTURE_COUNT.decrementAndGet();

            ServerLevel level = server.getLevel(pending.key.dimension);
            if (level == null) {
                PENDING_CAPTURE_KEYS.remove(pending.key);
                continue;
            }
            ChunkPos pos = new ChunkPos(
                    ChunkPos.getX(pending.key.chunkPos), ChunkPos.getZ(pending.key.chunkPos));
            Path destination = chunkPath(level, pos);
            if (isComplete(destination)) {
                PENDING_CAPTURE_KEYS.remove(pending.key);
                continue;
            }
            if (PENDING_WRITES.contains(destination)) {
                PENDING_CAPTURE_COUNT.incrementAndGet();
                PENDING_CAPTURES.addLast(pending);
                continue;
            }

            ChunkAccess chunk = pending.chunk.get();
            if (chunk == null || !chunk.getPos().equals(pos)) {
                chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            }
            if (chunk == null) {
                PENDING_CAPTURE_KEYS.remove(pending.key);
                continue;
            }

            // PalettedContainer.copy() duplicates compact palette/bit-storage data without scanning
            // every block. Workers can then read this immutable snapshot without touching the level.
            ChunkSnapshot snapshot = snapshot(chunk, true);
            long captureEpoch = CAPTURE_EPOCH.get();
            CAPTURES_IN_FLIGHT.incrementAndGet();
            try {
                MiaExecutors.execute(MiaExecutors.Priority.REAL_CHUNK_CAPTURE, () -> {
                    try {
                        if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                        StoredChunk stored = voxelize(pending.link, snapshot, false);
                        if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                        persist(destination, stored).thenRun(() -> server.execute(() ->
                                MiaLodSampler.notifyReplaced(pending.link, pos)));
                    } catch (Throwable throwable) {
                        MementoInAbyss.LOGGER.warn("Unable to capture real chunk LOD {}", pos, throwable);
                    } finally {
                        PENDING_CAPTURE_KEYS.remove(pending.key);
                        if (captureEpoch == CAPTURE_EPOCH.get()) CAPTURES_IN_FLIGHT.decrementAndGet();
                    }
                });
                submitted++;
            } catch (RejectedExecutionException ignored) {
                if (captureEpoch == CAPTURE_EPOCH.get()) {
                    CAPTURES_IN_FLIGHT.decrementAndGet();
                    if (lodEnabled()) {
                        PENDING_CAPTURE_COUNT.incrementAndGet();
                        PENDING_CAPTURES.addFirst(pending);
                    }
                }
                return;
            }
        }
    }

    static void clearPendingCaptures() {
        CAPTURE_EPOCH.incrementAndGet();
        PENDING_CAPTURES.clear();
        PENDING_CAPTURE_KEYS.clear();
        PENDING_CAPTURE_COUNT.set(0);
        CAPTURES_IN_FLIGHT.set(0);
        cancelPendingWrites();
        READY_DIRECTORIES.clear();
    }

    static CompletableFuture<Void> ingest(CrossDimensionLodLink link, ServerLevel level,
                                          ChunkAccess chunk, boolean provisional) {
        if (!lodEnabled()) return CompletableFuture.completedFuture(null);
        Path destination = chunkPath(level, chunk.getPos());
        return persist(destination, voxelize(link, chunk, provisional));
    }

    private static CompletableFuture<Void> persist(Path destination, StoredChunk snapshot) {
        if (!lodEnabled()) return CompletableFuture.completedFuture(null);
        if (!PENDING_WRITES.add(destination)) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> completion = new CompletableFuture<>();
        byte[] encoded;
        try {
            // persist is reached from a prioritized CPU task. Keep Deflate work there and leave
            // the single writer responsible only for ordered filesystem operations.
            encoded = encode(snapshot);
        } catch (Throwable throwable) {
            PENDING_WRITES.remove(destination);
            completion.completeExceptionally(throwable);
            return completion;
        }
        try {
            WRITER.execute(new PendingWrite(destination, snapshot.provisional, encoded, completion));
        } catch (RejectedExecutionException ignored) {
            // A later load/unload retries this chunk if the bounded I/O queue is full.
            PENDING_WRITES.remove(destination);
            completion.completeExceptionally(ignored);
        }
        return completion;
    }

    static Optional<StoredChunk> read(ServerLevel level, ChunkPos pos) {
        if (!lodEnabled()) return Optional.empty();
        Path path = chunkPath(level, pos);
        if (!isFile(path)) return Optional.empty();
        try (DataInputStream input = new DataInputStream(new InflaterInputStream(
                new BufferedInputStream(Files.newInputStream(path))))) {
            if (input.readInt() != MAGIC || input.readInt() != VERSION) return Optional.empty();
            boolean provisional = input.readBoolean();
            int chunkX = input.readInt();
            int chunkZ = input.readInt();
            int cellSize = input.readUnsignedByte();
            int minY = input.readInt();
            int yCells = input.readInt();
            int paletteSize = input.readUnsignedShort();
            if (cellSize < 1 || cellSize > 16 || 16 % cellSize != 0
                    || yCells < 1 || yCells > 1024 || paletteSize < 1 || paletteSize > 4096) {
                return Optional.empty();
            }
            int[] palette = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) palette[i] = input.readInt();
            int horizontalCells = 16 / cellSize;
            short[] voxels = new short[horizontalCells * horizontalCells * yCells];
            for (int i = 0; i < voxels.length; i++) {
                voxels[i] = input.readShort();
                if (Short.toUnsignedInt(voxels[i]) >= paletteSize) return Optional.empty();
            }
            StoredChunk stored = new StoredChunk(chunkX, chunkZ, cellSize, minY, yCells,
                    palette, voxels, provisional);
            return Optional.of(stored);
        } catch (IOException | RuntimeException exception) {
            MementoInAbyss.LOGGER.warn("Unable to read cross-dimension LOD {}", path, exception);
            return Optional.empty();
        }
    }

    /** Cheap presence check used by the lazy source-dimension generator. */
    static boolean contains(ServerLevel level, ChunkPos pos) {
        return isFile(chunkPath(level, pos));
    }

    /** Derives a coarser level from the persisted base level without duplicating files on disk. */
    static StoredChunk coarsen(StoredChunk source, int targetCellSize) {
        if (targetCellSize == source.cellSize) return source;
        if (targetCellSize < source.cellSize || targetCellSize > 16
                || targetCellSize % source.cellSize != 0 || 16 % targetCellSize != 0) {
            throw new IllegalArgumentException("Invalid LOD cell size " + targetCellSize);
        }
        int ratio = targetCellSize / source.cellSize;
        if (source.yCells % ratio != 0) throw new IllegalArgumentException("LOD height is not divisible");
        int sourceHorizontal = 16 / source.cellSize;
        int horizontal = 16 / targetCellSize;
        int yCells = source.yCells / ratio;
        short[] voxels = new short[horizontal * horizontal * yCells];
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Short> paletteLookup = new HashMap<>();
        int airId = source.palette[0];
        palette.add(airId);
        paletteLookup.put(airId, (short) 0);
        int[] ids = new int[ratio * ratio * ratio];
        int[] uniqueIds = new int[ids.length];
        int[] uniqueCounts = new int[ids.length];

        for (int z = 0; z < horizontal; z++) {
            for (int x = 0; x < horizontal; x++) {
                for (int y = 0; y < yCells; y++) {
                    int count = 0;
                    for (int dz = 0; dz < ratio; dz++) {
                        for (int dx = 0; dx < ratio; dx++) {
                            for (int dy = 0; dy < ratio; dy++) {
                                short paletteIndex = source.voxels[index(x * ratio + dx, y * ratio + dy,
                                        z * ratio + dz, sourceHorizontal, source.yCells)];
                                if (paletteIndex != 0) ids[count++] = source.palette[paletteIndex];
                            }
                        }
                    }
                    int stateId = count == 0 ? airId
                            : mostFrequent(ids, count, uniqueIds, uniqueCounts);
                    Short existingIndex = paletteLookup.get(stateId);
                    short paletteIndex;
                    if (existingIndex == null) {
                        paletteIndex = (short) palette.size();
                        palette.add(stateId);
                        paletteLookup.put(stateId, paletteIndex);
                    } else {
                        paletteIndex = existingIndex;
                    }
                    voxels[index(x, y, z, horizontal, yCells)] = paletteIndex;
                }
            }
        }
        return new StoredChunk(source.chunkX, source.chunkZ, targetCellSize, source.minY, yCells,
                palette.stream().mapToInt(Integer::intValue).toArray(), voxels, source.provisional);
    }

    private static StoredChunk voxelize(CrossDimensionLodLink link, ChunkAccess chunk, boolean provisional) {
        return voxelize(link, snapshot(chunk, false), provisional);
    }

    private static StoredChunk voxelize(CrossDimensionLodLink link, ChunkSnapshot snapshot, boolean provisional) {
        return new Voxelizer(link, snapshot).run(provisional);
    }

    @SuppressWarnings("unchecked")
    private static ChunkSnapshot snapshot(ChunkAccess chunk, boolean copy) {
        LevelChunkSection[] source = chunk.getSections();
        PalettedContainer<BlockState>[] states = (PalettedContainer<BlockState>[]) new PalettedContainer<?>[source.length];
        for (int i = 0; i < source.length; i++) {
            LevelChunkSection section = source[i];
            if (!section.hasOnlyAir()) states[i] = copy ? section.getStates().copy() : section.getStates();
        }
        return new ChunkSnapshot(chunk.getPos(), chunk.getMinY(), states);
    }

    private static final class Voxelizer {
        private static final Map<BlockState, Boolean> SOLID_STATE_CACHE = new ConcurrentHashMap<>();
        private final ChunkSnapshot chunk;
        private final int horizontalCells = 16 / BASE_CELL_SIZE;
        private final int minY;
        private final int yCells;
        private final int totalCells;
        private final short[] voxels;
        private final List<Integer> palette = new ArrayList<>();
        private final Map<Integer, Short> paletteLookup = new HashMap<>();
        private final int airId = Block.getId(Blocks.AIR.defaultBlockState());
        private final int[] ids = new int[BASE_CELL_SIZE * BASE_CELL_SIZE * BASE_CELL_SIZE];
        private final int[] uniqueIds = new int[this.ids.length];
        private final int[] uniqueCounts = new int[this.ids.length];
        private final PalettedContainer<BlockState>[] sections;

        private Voxelizer(CrossDimensionLodLink link, ChunkSnapshot chunk) {
            this.chunk = chunk;
            MiaHeight sourceHeight = link.sourceHeight();
            this.minY = sourceHeight.minY();
            this.yCells = sourceHeight.height() / BASE_CELL_SIZE;
            this.totalCells = this.horizontalCells * this.horizontalCells * this.yCells;
            this.voxels = new short[this.totalCells];
            this.sections = chunk.sections;
            this.palette.add(this.airId);
            this.paletteLookup.put(this.airId, (short) 0);
        }

        private StoredChunk run(boolean provisional) {
            for (int cell = 0; cell < this.totalCells; cell++) sampleCell(cell);
            return new StoredChunk(this.chunk.pos.x(), this.chunk.pos.z(), BASE_CELL_SIZE,
                    this.minY, this.yCells, this.palette.stream().mapToInt(Integer::intValue).toArray(),
                    this.voxels, provisional);
        }

        private void sampleCell(int cellIndex) {
            int y = cellIndex % this.yCells;
            int horizontalIndex = cellIndex / this.yCells;
            int x = horizontalIndex % this.horizontalCells;
            int z = horizontalIndex / this.horizontalCells;
            int baseY = this.minY + y * BASE_CELL_SIZE;
            int sectionIndex = (baseY - this.chunk.minY) >> 4;
            if (sectionIndex < 0 || sectionIndex >= this.sections.length) return;
            PalettedContainer<BlockState> section = this.sections[sectionIndex];
            if (section == null) return;
            int count = 0;
            for (int dz = 0; dz < BASE_CELL_SIZE; dz++) {
                for (int dx = 0; dx < BASE_CELL_SIZE; dx++) {
                    for (int dy = 0; dy < BASE_CELL_SIZE; dy++) {
                        BlockState state = section.get(
                                x * BASE_CELL_SIZE + dx, (baseY + dy) & 15, z * BASE_CELL_SIZE + dz);
                        Boolean cachedSolid = SOLID_STATE_CACHE.get(state);
                        boolean solid;
                        if (cachedSolid == null) {
                            solid = isLodSolid(state);
                            Boolean raced = SOLID_STATE_CACHE.putIfAbsent(state, solid);
                            if (raced != null) solid = raced;
                        } else {
                            solid = cachedSolid;
                        }
                        if (solid) {
                            this.ids[count++] = Block.getId(state);
                        }
                    }
                }
            }
            int stateId = count == 0 ? this.airId
                    : mostFrequent(this.ids, count, this.uniqueIds, this.uniqueCounts);
            Short existingIndex = this.paletteLookup.get(stateId);
            short paletteIndex;
            if (existingIndex == null) {
                paletteIndex = (short) this.palette.size();
                this.palette.add(stateId);
                this.paletteLookup.put(stateId, paletteIndex);
            } else {
                paletteIndex = existingIndex;
            }
            this.voxels[cellIndex] = paletteIndex;
        }

        private static boolean isLodSolid(BlockState state) {
            return !state.isAir() && state.getLightEmission() == 0
                    && state.getFluidState().isEmpty()
                    && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        }
    }

    private static int mostFrequent(int[] ids, int length, int[] uniqueIds, int[] uniqueCounts) {
        int uniqueLength = 0;
        int bestId = ids[0];
        int bestCount = 0;
        for (int i = 0; i < length; i++) {
            int id = ids[i];
            int uniqueIndex = 0;
            while (uniqueIndex < uniqueLength && uniqueIds[uniqueIndex] != id) uniqueIndex++;
            int count;
            if (uniqueIndex == uniqueLength) {
                uniqueIds[uniqueLength] = id;
                uniqueCounts[uniqueLength] = 1;
                uniqueLength++;
                count = 1;
            } else {
                count = ++uniqueCounts[uniqueIndex];
            }
            if (count > bestCount) {
                bestId = id;
                bestCount = count;
            }
        }
        return bestId;
    }

    private static byte[] encode(StoredChunk chunk) throws IOException {
        int estimatedSize = 32 + chunk.palette.length * Integer.BYTES
                + chunk.voxels.length * Short.BYTES;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(estimatedSize);
        try (DataOutputStream output = new DataOutputStream(new DeflaterOutputStream(bytes))) {
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            output.writeBoolean(chunk.provisional);
            output.writeInt(chunk.chunkX);
            output.writeInt(chunk.chunkZ);
            output.writeByte(chunk.cellSize);
            output.writeInt(chunk.minY);
            output.writeInt(chunk.yCells);
            output.writeShort(chunk.palette.length);
            for (int stateId : chunk.palette) output.writeInt(stateId);
            for (short voxel : chunk.voxels) output.writeShort(voxel);
        }
        return bytes.toByteArray();
    }

    private static void write(Path destination, boolean provisional, byte[] encoded) throws IOException {
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        ensureDirectory(destination.getParent());
        Files.write(temporary, encoded);
        try {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        Path marker = completeMarker(destination);
        if (provisional) {
            if (marker.toFile().exists()) Files.delete(marker);
        }
        else Files.write(marker, new byte[0]);
    }

    private static Path chunkPath(ServerLevel level, ChunkPos pos) {
        var dimension = level.dimension().identifier();
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MementoInAbyss.ID).resolve("lod").resolve("dimensions")
                .resolve(dimension.getNamespace()).resolve(dimension.getPath())
                .resolve("r." + Math.floorDiv(pos.x(), 32) + "." + Math.floorDiv(pos.z(), 32))
                .resolve("c." + pos.x() + "." + pos.z() + ".v" + VERSION + ".mialod");
    }

    private static boolean isComplete(Path path) {
        return isFile(path) && isFile(completeMarker(path));
    }

    /** Default world paths can use File's non-throwing native attribute probe on missing files. */
    private static boolean isFile(Path path) {
        return path.toFile().isFile();
    }

    /**
     * Creates only the missing directory segments. Files.createDirectories first attempts the
     * leaf and catches FileAlreadyExistsException as normal control flow, which becomes visible
     * and allocates heavily when JFR exception events are enabled.
     */
    private static synchronized void ensureDirectory(Path directory) throws IOException {
        if (READY_DIRECTORIES.contains(directory)) return;

        ArrayDeque<Path> missing = new ArrayDeque<>();
        Path current = directory;
        while (current != null && !current.toFile().isDirectory()) {
            missing.push(current);
            current = current.getParent();
        }
        while (!missing.isEmpty()) Files.createDirectory(missing.pop());
        READY_DIRECTORIES.add(directory);
    }

    private static Path completeMarker(Path path) {
        return path.resolveSibling(path.getFileName() + ".complete");
    }

    private static int index(int x, int y, int z, int horizontalCells, int yCells) {
        return (z * horizontalCells + x) * yCells + y;
    }

    record StoredChunk(int chunkX, int chunkZ, int cellSize, int minY,
                       int yCells, int[] palette, short[] voxels, boolean provisional) {}
    private record CaptureKey(ResourceKey<Level> dimension, long chunkPos) {}
    private record PendingCapture(CaptureKey key, CrossDimensionLodLink link,
                                  WeakReference<ChunkAccess> chunk) {}
    private record ChunkSnapshot(ChunkPos pos, int minY, PalettedContainer<BlockState>[] sections) {}

    private static void cancelPendingWrites() {
        List<Runnable> cancelled = new ArrayList<>();
        WRITER.getQueue().drainTo(cancelled);
        for (Runnable runnable : cancelled) {
            if (runnable instanceof PendingWrite write) write.cancel();
        }
    }

    private static boolean lodEnabled() {
        return MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get();
    }

    private static final class PendingWrite implements Runnable {
        private final Path destination;
        private final boolean provisional;
        private final byte[] encoded;
        private final CompletableFuture<Void> completion;

        private PendingWrite(Path destination, boolean provisional, byte[] encoded,
                             CompletableFuture<Void> completion) {
            this.destination = destination;
            this.provisional = provisional;
            this.encoded = encoded;
            this.completion = completion;
        }

        @Override
        public void run() {
            if (!lodEnabled()) {
                cancel();
                return;
            }
            try {
                write(destination, provisional, encoded);
                completion.complete(null);
            } catch (Throwable throwable) {
                MementoInAbyss.LOGGER.warn("Unable to write cross-dimension LOD {}", destination, throwable);
                completion.completeExceptionally(throwable);
            } finally {
                PENDING_WRITES.remove(destination);
            }
        }

        private void cancel() {
            PENDING_WRITES.remove(destination);
            completion.completeExceptionally(new CancellationException("Cross-dimension LOD disabled"));
        }
    }

    private MiaLodStorage() {}
}
