package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/** Persistent server-owned LOD storage, shared by every link that uses the same source dimension. */
final class MiaLodStorage {
    /** One stored voxel represents one source block, matching DH/Voxy's finest level. */
    private static final int BASE_CELL_SIZE = 1;
    private static final int MAGIC = 0x4D49414C; // MIAL
    /** Version 10 adds a length and CRC32 for the uncompressed payload body. */
    private static final int VERSION = 10;
    private static final int MAX_CACHE_ENTRIES = 256;
    private static final long MAX_COMPRESSED_FILE_BYTES = 8L * 1024L * 1024L;
    private static final long MAX_DECOMPRESSED_FILE_BYTES = 32L * 1024L * 1024L;
    private static final int MAX_PENDING_WRITES = 128;
    private static final int MAX_CAPTURE_SUBMISSIONS_PER_TICK = 2;
    private static final int MAX_CAPTURES_IN_FLIGHT = 16;
    private static final int MAX_DIRTY_CHUNKS_PER_TICK = 64;
    private static final ThreadPoolExecutor WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING_WRITES), r -> {
                Thread thread = new Thread(r, "MIA cross-dimension LOD writer");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
    private static final ConcurrentHashMap<Path, PendingWrite> PENDING_WRITES = new ConcurrentHashMap<>();
    private static final MiaLodSectionCache SECTION_CACHE = new MiaLodSectionCache(64);
    /** Fixed stripes avoid unbounded per-path locks and serialize read-cache publication with commit. */
    private static final Object[] FILE_LOCKS = java.util.stream.IntStream.range(0, 64)
            .mapToObj(ignored -> new Object()).toArray();
    private static final Map<Path, StoredChunk> CHUNK_CACHE = new LinkedHashMap<>(64, 0.75F, true);
    private static final java.util.Set<Path> READY_DIRECTORIES = ConcurrentHashMap.newKeySet();
    /** Chunk events may run off-thread under C2ME; entries retain chunks only weakly until selected. */
    private static final ConcurrentLinkedDeque<PendingCapture> PENDING_CAPTURES = new ConcurrentLinkedDeque<>();
    private static final java.util.Set<CaptureKey> PENDING_CAPTURE_KEYS = ConcurrentHashMap.newKeySet();
    private static final Object DIRTY_SECTION_LOCK = new Object();
    private static final Map<CaptureKey, java.util.Set<Integer>> DIRTY_SECTIONS = new HashMap<>();
    private static final AtomicInteger PENDING_CAPTURE_COUNT = new AtomicInteger();
    private static final AtomicInteger CAPTURES_IN_FLIGHT = new AtomicInteger();
    private static final AtomicLong CAPTURE_EPOCH = new AtomicLong();
    private static final AtomicLong NEXT_REVISION = new AtomicLong(
            Math.max(1L, System.currentTimeMillis() << 20));
    private static final ConcurrentHashMap<CaptureKey, Long> CAPTURE_VERSIONS = new ConcurrentHashMap<>();

    static void enqueueIfMissing(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        enqueueCapture(link, level, chunk, false, null);
    }

    static void markSectionDirty(ServerLevel level, BlockPos pos) {
        if (!lodEnabled()) return;
        CaptureKey key = new CaptureKey(level.dimension(), ChunkPos.pack(
                Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16)));
        int section = Math.floorDiv(pos.getY() - level.getMinY(), 16);
        synchronized (DIRTY_SECTION_LOCK) {
            DIRTY_SECTIONS.computeIfAbsent(key, ignored -> new java.util.HashSet<>()).add(section);
        }
    }

    static void processDirtySections(MinecraftServer server) {
        if (!lodEnabled()) return;
        List<DirtyCapture> dirty = new ArrayList<>(MAX_DIRTY_CHUNKS_PER_TICK);
        synchronized (DIRTY_SECTION_LOCK) {
            var iterator = DIRTY_SECTIONS.entrySet().iterator();
            while (iterator.hasNext() && dirty.size() < MAX_DIRTY_CHUNKS_PER_TICK) {
                var entry = iterator.next();
                dirty.add(new DirtyCapture(entry.getKey(), entry.getValue().stream()
                        .mapToInt(Integer::intValue).sorted().toArray()));
                iterator.remove();
            }
        }
        for (DirtyCapture dirtyCapture : dirty) {
            CaptureKey key = dirtyCapture.key;
            ServerLevel level = server.getLevel(key.dimension);
            if (level == null) {
                retainDirtyChunk(key);
                continue;
            }
            ChunkPos pos = new ChunkPos(ChunkPos.getX(key.chunkPos), ChunkPos.getZ(key.chunkPos));
            ChunkAccess chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            if (chunk == null) {
                retainDirtyChunk(key);
                continue;
            }
            CrossDimensionLodLinks.fromSource(level.dimension())
                    .forEach(link -> {
                        if (!enqueueCapture(link, level, chunk, true, dirtyCapture.sections)) {
                            retainDirtyChunk(key);
                        }
                    });
        }
    }

    private static void retainDirtyChunk(CaptureKey key) {
        synchronized (DIRTY_SECTION_LOCK) {
            DIRTY_SECTIONS.computeIfAbsent(key, ignored -> new java.util.HashSet<>()).add(-1);
        }
    }

    static void enqueueChanged(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        if (!enqueueCapture(link, level, chunk, true, null)) {
            retainDirtyChunk(new CaptureKey(level.dimension(), ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z())));
        }
    }

    private static boolean enqueueCapture(CrossDimensionLodLink link, ServerLevel level,
                                         ChunkAccess chunk, boolean changed, int[] dirtySections) {
        if (!lodEnabled()) return false;
        Path destination = chunkPath(level, chunk.getPos());
        // Loads reuse stable real data. Save events explicitly bypass this check.
        if (!changed && isRealStored(destination) && !chunk.isUnsaved()) return true;
        int queueLimit = MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodCaptureQueueLimit.get();
        if (PENDING_CAPTURE_COUNT.get() >= queueLimit) return false;
        CaptureKey key = new CaptureKey(level.dimension(),
                ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()));
        long version = CAPTURE_VERSIONS.compute(key, (ignored, previous) ->
                nextRevision(previous == null ? 0L : previous));
        if (!PENDING_CAPTURE_KEYS.add(key)) return true;
        PENDING_CAPTURE_COUNT.incrementAndGet();
        PENDING_CAPTURES.addLast(new PendingCapture(
                key, link, new WeakReference<>(chunk), version,
                dirtySections == null ? null : dirtySections.clone()));
        return true;
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
            ChunkAccess chunk = pending.chunk.get();
            if (chunk == null || !chunk.getPos().equals(pos)) {
                chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            }
            if (chunk == null) {
                PENDING_CAPTURE_KEYS.remove(pending.key);
                continue;
            }

            // Copy compact containers on the server thread. Retain untouched containers in the
            // snapshot as well: the worker may need a full rebuild if its base is missing/incompatible.
            ChunkSnapshot snapshot = snapshot(chunk, true);
            long captureEpoch = CAPTURE_EPOCH.get();
            CAPTURES_IN_FLIGHT.incrementAndGet();
            try {
                MiaExecutors.execute(MiaExecutors.Priority.REAL_CHUNK_CAPTURE, () -> {
                    try {
                        if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                        StoredChunk stored = null;
                        if (pending.dirtySections != null && !containsFullCapture(pending.dirtySections)) {
                            StoredChunk previous = read(level, pos).orElse(null);
                            if (previous != null && !previous.provisional()) {
                                StoredChunk patch = new Voxelizer(pending.link, snapshot)
                                        .run(false, pending.version, pending.dirtySections);
                                stored = mergeDirtySections(previous, patch, pending.dirtySections,
                                        snapshot.minY, pending.version);
                            }
                        }
                        if (stored == null) stored = voxelize(pending.link, snapshot, false, pending.version);
                        if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                        persist(destination, stored).whenComplete((ignored, failure) -> server.execute(() -> {
                            if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                            PENDING_CAPTURE_KEYS.remove(pending.key);
                            if (failure != null) {
                                MementoInAbyss.LOGGER.warn("Unable to commit cross-dimension LOD {}",
                                        destination, failure);
                                requeueCaptureAfterFailure(pending);
                            } else {
                                MiaLodSampler.notifyReplaced(pending.link, pos, pending.version);
                                requeueIfCaptureChanged(server, pending);
                            }
                        }));
                    } catch (Throwable throwable) {
                        MementoInAbyss.LOGGER.warn("Unable to capture real chunk LOD {}", pos, throwable);
                        server.execute(() -> {
                            if (captureEpoch != CAPTURE_EPOCH.get() || !lodEnabled()) return;
                            PENDING_CAPTURE_KEYS.remove(pending.key);
                            requeueCaptureAfterFailure(pending);
                        });
                    } finally {
                        // Keep the per-column key until commit: two partial captures must never
                        // both merge against the same disk base and overwrite each other's changes.
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
        CAPTURE_VERSIONS.clear();
        synchronized (DIRTY_SECTION_LOCK) {
            DIRTY_SECTIONS.clear();
        }
        cancelPendingWrites();
        SECTION_CACHE.clear();
        synchronized (CHUNK_CACHE) {
            CHUNK_CACHE.clear();
        }
        READY_DIRECTORIES.clear();
    }

    static CompletableFuture<Void> ingest(CrossDimensionLodLink link, ServerLevel level,
                                          ChunkAccess chunk, boolean provisional) {
        if (!lodEnabled()) return CompletableFuture.completedFuture(null);
        Path destination = chunkPath(level, chunk.getPos());
        return persist(destination, voxelize(link, chunk, provisional, nextRevision(0L)));
    }

    private static CompletableFuture<Void> persist(Path destination, StoredChunk snapshot) {
        if (!lodEnabled()) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> completion = new CompletableFuture<>();
        byte[] encoded;
        try {
            // persist is reached from a prioritized CPU task. Keep Deflate work there and leave
            // the single writer responsible only for ordered filesystem operations.
            encoded = encode(snapshot);
        } catch (Throwable throwable) {
            completion.completeExceptionally(throwable);
            return completion;
        }
        if (snapshot.provisional && isRealStored(destination)) {
            completion.complete(null);
            return completion;
        }
        PendingWrite write = new PendingWrite(destination, snapshot.provisional, encoded, completion);
        PendingWrite previous = PENDING_WRITES.putIfAbsent(destination, write);
        if (previous != null) {
            // Real captures always win over provisional captures. Otherwise latest-wins
            // coalescing is safe because the writer is single-threaded.
            if (!snapshot.provisional || previous.provisional) {
                PENDING_WRITES.put(destination, write);
                previous.cancel();
            } else {
                completion.complete(null);
                return completion;
            }
        }
        try {
            WRITER.execute(write);
        } catch (RejectedExecutionException ignored) {
            // A later load/unload retries this chunk if the bounded I/O queue is full.
            PENDING_WRITES.remove(destination, write);
            completion.completeExceptionally(ignored);
        }
        return completion;
    }

    static Optional<StoredChunk> read(ServerLevel level, ChunkPos pos) {
        if (!lodEnabled()) return Optional.empty();
        Path path = chunkPath(level, pos);
        synchronized (fileLock(path)) {
            return read(path);
        }
    }

    private static Optional<StoredChunk> read(Path path) {
        long readEpoch = CAPTURE_EPOCH.get();
        synchronized (CHUNK_CACHE) {
            StoredChunk cached = CHUNK_CACHE.get(path);
            if (cached != null) return Optional.of(cached);
        }
        try {
            if (!isFile(path) || Files.size(path) > MAX_COMPRESSED_FILE_BYTES) return Optional.empty();
        } catch (IOException exception) {
            return Optional.empty();
        }
        try (DataInputStream input = new DataInputStream(new LimitedInputStream(
                new InflaterInputStream(new BufferedInputStream(Files.newInputStream(path))),
                MAX_DECOMPRESSED_FILE_BYTES))) {
            if (input.readInt() != MAGIC || input.readInt() != VERSION) return Optional.empty();
            boolean provisional = input.readBoolean();
            long revision = input.readLong();
            int chunkX = input.readInt();
            int chunkZ = input.readInt();
            int cellSize = input.readUnsignedByte();
            int minY = input.readInt();
            int yCells = input.readInt();
            int bodyLength = input.readInt();
            long expectedChecksum = Integer.toUnsignedLong(input.readInt());
            if (revision <= 0 || cellSize < 1 || cellSize > 16 || 16 % cellSize != 0
                    || minY < -65_536 || minY > 65_536
                    || yCells < 1 || yCells > 1024
                    || bodyLength <= 0 || bodyLength > MAX_DECOMPRESSED_FILE_BYTES) {
                return Optional.empty();
            }
            byte[] body = new byte[bodyLength];
            input.readFully(body);
            CRC32 checksum = new CRC32();
            checksum.update(body);
            if (checksum.getValue() != expectedChecksum) return Optional.empty();

            int[] palette;
            short[] voxels;
            try (DataInputStream bodyInput = new DataInputStream(new ByteArrayInputStream(body))) {
                int paletteSize = bodyInput.readUnsignedShort();
                if (paletteSize < 1 || paletteSize > 4096) return Optional.empty();
                palette = new int[paletteSize];
                for (int i = 0; i < paletteSize; i++) {
                    BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, NbtIo.read(bodyInput));
                    palette[i] = Block.getId(state);
                }
                int horizontalCells = 16 / cellSize;
                voxels = new short[horizontalCells * horizontalCells * yCells];
                for (int i = 0; i < voxels.length; i++) {
                    voxels[i] = bodyInput.readShort();
                    if (Short.toUnsignedInt(voxels[i]) >= paletteSize) return Optional.empty();
                }
                if (bodyInput.available() != 0) return Optional.empty();
            }
            StoredChunk stored = new StoredChunk(chunkX, chunkZ, cellSize, minY, yCells,
                    palette, voxels, provisional, revision, path);
            if (cellSize == 1) {
                synchronized (CHUNK_CACHE) {
                    if (readEpoch == CAPTURE_EPOCH.get()) CHUNK_CACHE.put(path, stored);
                    trimCache();
                }
            }
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

    /** Keep trees across disk replacements so the next load can propagate only changed sections. */
    static MiaLodSectionTree sectionTree(StoredChunk source) {
        return SECTION_CACHE.get(source);
    }

    private static StoredChunk voxelize(CrossDimensionLodLink link, ChunkAccess chunk,
                                        boolean provisional, long revision) {
        return voxelize(link, snapshot(chunk, false), provisional, revision);
    }

    private static StoredChunk voxelize(CrossDimensionLodLink link, ChunkSnapshot snapshot,
                                        boolean provisional, long revision) {
        return new Voxelizer(link, snapshot).run(provisional, revision, null);
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

    private static boolean containsSection(int[] sections, int section) {
        for (int selected : sections) {
            if (selected < 0 || selected == section) return true;
        }
        return false;
    }

    private static boolean containsFullCapture(int[] sections) {
        for (int section : sections) if (section < 0) return true;
        return false;
    }

    private static StoredChunk mergeDirtySections(StoredChunk previous, StoredChunk patch,
                                                   int[] dirtySections, int captureMinY, long revision) {
        if (previous.cellSize() != BASE_CELL_SIZE || patch.cellSize() != BASE_CELL_SIZE
                || previous.minY() != patch.minY() || previous.chunkX() != patch.chunkX()
                || previous.chunkZ() != patch.chunkZ() || previous.yCells() != patch.yCells()) return null;
        int[] palette = previous.palette().clone();
        short[] voxels = previous.voxels().clone();
        Map<Integer, Short> paletteLookup = new HashMap<>();
        for (short i = 0; i < palette.length; i++) paletteLookup.put(palette[i], i);
        for (int section : dirtySections) {
            if (section < 0) return null;
            int baseY = captureMinY + section * 16 - previous.minY();
            if (baseY < 0 || baseY >= previous.yCells()) continue;
            int sectionHeight = Math.min(16, previous.yCells() - baseY);
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < sectionHeight; y++) {
                        short patchIndex = patch.voxels()[index(x, baseY + y, z, 16, patch.yCells())];
                        int stateId = patch.palette()[Short.toUnsignedInt(patchIndex)];
                        Short mergedIndex = paletteLookup.get(stateId);
                        if (mergedIndex == null) {
                            if (palette.length >= 4096) return null;
                            mergedIndex = (short) palette.length;
                            palette = java.util.Arrays.copyOf(palette, palette.length + 1);
                            palette[mergedIndex] = stateId;
                            paletteLookup.put(stateId, mergedIndex);
                        }
                        voxels[index(x, baseY + y, z, 16, previous.yCells())] = mergedIndex;
                    }
                }
            }
        }
        return new StoredChunk(previous.chunkX(), previous.chunkZ(), previous.cellSize(), previous.minY(),
                previous.yCells(), palette, voxels, false, revision, previous.sourcePath());
    }

    private static int index(int x, int y, int z, int horizontalCells, int yCells) {
        return (z * horizontalCells + x) * yCells + y;
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

        private StoredChunk run(boolean provisional, long revision, int[] selectedSections) {
            for (int section = 0; section < sections.length; section++) {
                if (selectedSections != null && !containsSection(selectedSections, section)) continue;
                int startY = Math.max(0, chunk.minY + section * 16 - minY);
                int endY = Math.min(yCells, chunk.minY + (section + 1) * 16 - minY);
                for (int z = 0; z < horizontalCells; z++) {
                    for (int x = 0; x < horizontalCells; x++) {
                        for (int y = startY; y < endY; y++) sampleCell(index(x, y, z, horizontalCells, yCells));
                    }
                }
            }
            int[] paletteData = this.palette.stream().mapToInt(Integer::intValue).toArray();
            return new StoredChunk(this.chunk.pos.x(), this.chunk.pos.z(), BASE_CELL_SIZE,
                    this.minY, this.yCells, paletteData, this.voxels, provisional, revision, null);
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
            // Emissive full blocks remain part of the voxel stream. Their final light
            // response is handled by the client material/shader path; dropping them here
            // makes glowstone and other emissive terrain disappear from LOD entirely.
            return !state.isAir()
                    && state.getFluidState().isEmpty()
                    && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        }
    }

    private static long nextRevision(long previous) {
        return NEXT_REVISION.updateAndGet(current -> Math.max(current, previous) + 1L);
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
        int estimatedBodySize = 2 + chunk.palette.length * 64
                + chunk.voxels.length * Short.BYTES;
        ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream(estimatedBodySize);
        try (DataOutputStream body = new DataOutputStream(bodyBytes)) {
            body.writeShort(chunk.palette.length);
            for (int stateId : chunk.palette) {
                NbtIo.write(NbtUtils.writeBlockState(Block.stateById(stateId)), body);
            }
            for (short voxel : chunk.voxels) body.writeShort(voxel);
        }
        byte[] body = bodyBytes.toByteArray();
        CRC32 checksum = new CRC32();
        checksum.update(body);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(40 + body.length);
        try (DataOutputStream output = new DataOutputStream(new DeflaterOutputStream(bytes))) {
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            output.writeBoolean(chunk.provisional);
            output.writeLong(chunk.revision);
            output.writeInt(chunk.chunkX);
            output.writeInt(chunk.chunkZ);
            output.writeByte(chunk.cellSize);
            output.writeInt(chunk.minY);
            output.writeInt(chunk.yCells);
            output.writeInt(body.length);
            output.writeInt((int) checksum.getValue());
            output.write(body);
        }
        return bytes.toByteArray();
    }

    private static Object fileLock(Path path) {
        return FILE_LOCKS[Math.floorMod(path.hashCode(), FILE_LOCKS.length)];
    }

    private static void write(Path destination, boolean provisional, byte[] encoded) throws IOException {
        synchronized (fileLock(destination)) {
            // A real commit may have won since the provisional worker encoded its snapshot.
            if (provisional && isRealStored(destination)) return;
            Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
            ensureDirectory(destination.getParent());
            Files.write(temporary, encoded);
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            invalidateCache(destination);
        }
    }

    private static Path chunkPath(ServerLevel level, ChunkPos pos) {
        var dimension = level.dimension().identifier();
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MementoInAbyss.ID).resolve("lod").resolve("dimensions")
                .resolve(dimension.getNamespace()).resolve(dimension.getPath())
                .resolve("r." + Math.floorDiv(pos.x(), 32) + "." + Math.floorDiv(pos.z(), 32))
                .resolve("c." + pos.x() + "." + pos.z() + ".v" + VERSION + ".mialod");
    }

    private static boolean isRealStored(Path path) {
        if (!isFile(path)) return false;
        try (DataInputStream input = new DataInputStream(new LimitedInputStream(
                new InflaterInputStream(new BufferedInputStream(Files.newInputStream(path))),
                MAX_DECOMPRESSED_FILE_BYTES))) {
            if (input.readInt() != MAGIC || input.readInt() != VERSION) return false;
            if (input.readBoolean()) return false;
            long revision = input.readLong();
            input.readInt(); // chunk X
            input.readInt(); // chunk Z
            int cellSize = input.readUnsignedByte();
            int minY = input.readInt();
            int yCells = input.readInt();
            if (revision <= 0 || cellSize < 1 || cellSize > 16 || 16 % cellSize != 0
                    || minY < -65_536 || minY > 65_536 || yCells < 1 || yCells > 1024) {
                return false;
            }
            int bodyLength = input.readInt();
            long expectedChecksum = Integer.toUnsignedLong(input.readInt());
            if (bodyLength <= 0 || bodyLength > MAX_DECOMPRESSED_FILE_BYTES) return false;
            byte[] body = new byte[bodyLength];
            input.readFully(body);
            CRC32 checksum = new CRC32();
            checksum.update(body);
            if (checksum.getValue() != expectedChecksum) return false;
            try (DataInputStream bodyInput = new DataInputStream(new ByteArrayInputStream(body))) {
                int paletteSize = bodyInput.readUnsignedShort();
                if (paletteSize < 1 || paletteSize > 4096) return false;
                for (int i = 0; i < paletteSize; i++) {
                    NbtUtils.readBlockState(BuiltInRegistries.BLOCK, NbtIo.read(bodyInput));
                }
                int voxelCount = Math.multiplyExact(Math.multiplyExact(16 / cellSize, 16 / cellSize), yCells);
                for (int i = 0; i < voxelCount; i++) {
                    if (Short.toUnsignedInt(bodyInput.readShort()) >= paletteSize) return false;
                }
                return bodyInput.available() == 0;
            }
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    private static void invalidateCache(Path path) {
        synchronized (CHUNK_CACHE) {
            CHUNK_CACHE.remove(path);
        }
    }

    private static void trimCache() {
        while (CHUNK_CACHE.size() > MAX_CACHE_ENTRIES) {
            CHUNK_CACHE.remove(CHUNK_CACHE.entrySet().iterator().next().getKey());
        }
    }

    /** Default world paths can use File's non-throwing native attribute probe on missing files. */
    private static boolean isFile(Path path) {
        return path.toFile().isFile();
    }

    /**
     * Creates only the missing directory segments. Files.createDirectories first attempts the
     * leaf and catches FileAlreadyExistsException as normal control flow, which creates avoidable
     * exceptions and allocations on the I/O path.
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

    record StoredChunk(int chunkX, int chunkZ, int cellSize, int minY,
                       int yCells, int[] palette, short[] voxels, boolean provisional,
                       long revision, Path sourcePath) {}
    private record CaptureKey(ResourceKey<Level> dimension, long chunkPos) {}
    private record PendingCapture(CaptureKey key, CrossDimensionLodLink link,
                                  WeakReference<ChunkAccess> chunk, long version,
                                  int[] dirtySections) {}
    private record DirtyCapture(CaptureKey key, int[] sections) {}
    private record ChunkSnapshot(ChunkPos pos, int minY, PalettedContainer<BlockState>[] sections) {}

    private static void requeueIfCaptureChanged(MinecraftServer server, PendingCapture pending) {
        long latest = CAPTURE_VERSIONS.getOrDefault(pending.key, pending.version);
        if (!lodEnabled()) return;
        if (latest <= pending.version) {
            CAPTURE_VERSIONS.remove(pending.key, latest);
            return;
        }
        if (!PENDING_CAPTURE_KEYS.add(pending.key)) return;
        PENDING_CAPTURE_COUNT.incrementAndGet();
        PENDING_CAPTURES.addLast(new PendingCapture(pending.key, pending.link,
                pending.chunk, latest, null));
    }

    private static void requeueCaptureAfterFailure(PendingCapture pending) {
        if (!lodEnabled()) return;
        long latest = CAPTURE_VERSIONS.compute(pending.key, (ignored, previous) ->
                nextRevision(Math.max(previous == null ? 0L : previous, pending.version)));
        if (!PENDING_CAPTURE_KEYS.add(pending.key)) return;
        PENDING_CAPTURE_COUNT.incrementAndGet();
        PENDING_CAPTURES.addLast(new PendingCapture(pending.key, pending.link,
                pending.chunk, latest, null));
    }

    private static final class LimitedInputStream extends InputStream {
        private final InputStream delegate;
        private final long limit;
        private long count;

        private LimitedInputStream(InputStream delegate, long limit) {
            this.delegate = delegate;
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            if (count >= limit) throw new IOException("Cross-dimension LOD file exceeds decode limit");
            int value = delegate.read();
            if (value >= 0) count++;
            return value;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (count >= limit) throw new IOException("Cross-dimension LOD file exceeds decode limit");
            int allowed = (int) Math.min(length, limit - count);
            int read = delegate.read(bytes, offset, allowed);
            if (read > 0) count += read;
            return read;
        }

        @Override
        public void close() throws IOException { delegate.close(); }
    }

    private static void cancelPendingWrites() {
        List<Runnable> cancelled = new ArrayList<>();
        WRITER.getQueue().drainTo(cancelled);
        for (Runnable runnable : cancelled) {
            if (runnable instanceof PendingWrite write) write.cancel();
        }
    }

    private static boolean lodEnabled() {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodEnabled.get();
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
                if (!PENDING_WRITES.replace(destination, this, this)) {
                    cancel();
                    return;
                }
                write(destination, provisional, encoded);
                completion.complete(null);
            } catch (Throwable throwable) {
                MementoInAbyss.LOGGER.warn("Unable to write cross-dimension LOD {}", destination, throwable);
                completion.completeExceptionally(throwable);
            } finally {
                PENDING_WRITES.remove(destination, this);
            }
        }

        private void cancel() {
            PENDING_WRITES.remove(destination, this);
            completion.completeExceptionally(new CancellationException("Cross-dimension LOD disabled"));
        }
    }

    private MiaLodStorage() {}
}
