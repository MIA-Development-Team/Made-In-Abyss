package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.LevelResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/** Persistent, server-owned LOD storage derived only from fully generated source chunks. */
public final class MiaLodStorage {
    public static final int BASE_CELL_SIZE = 4;
    private static final int MAGIC = 0x4D49414C; // MIAL
    private static final int VERSION = 3;
    private static final int MAX_PENDING_WRITES = 128;
    private static final int MAX_PENDING_CAPTURES = 64;
    private static final ExecutorService WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING_WRITES), r -> {
                Thread thread = new Thread(r, "MIA cross-dimension LOD writer");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
    private static final java.util.Set<Path> PENDING_WRITES = ConcurrentHashMap.newKeySet();
    /** Server-thread queue: bounds expensive chunk voxelization to one source chunk per tick. */
    private static final ArrayDeque<PendingCapture> PENDING_CAPTURES = new ArrayDeque<>();
    private static final java.util.Set<Path> PENDING_CAPTURE_PATHS = new HashSet<>();

    public static void enqueueIfMissing(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        Path destination = chunkPath(link, level, chunk.getPos());
        if (Files.isRegularFile(destination) || PENDING_WRITES.contains(destination)
                || PENDING_CAPTURE_PATHS.contains(destination) || PENDING_CAPTURES.size() >= MAX_PENDING_CAPTURES) return;
        PENDING_CAPTURES.addLast(new PendingCapture(link, level, chunk, destination));
        PENDING_CAPTURE_PATHS.add(destination);
    }

    public static void processPendingCapture() {
        PendingCapture pending = PENDING_CAPTURES.pollFirst();
        if (pending == null) return;
        PENDING_CAPTURE_PATHS.remove(pending.destination);
        if (!Files.isRegularFile(pending.destination)) ingest(pending.link, pending.level, pending.chunk);
    }

    public static void clearPendingCaptures() {
        PENDING_CAPTURES.clear();
        PENDING_CAPTURE_PATHS.clear();
    }

    public static void ingest(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        Path destination = chunkPath(link, level, chunk.getPos());
        if (!PENDING_WRITES.add(destination)) return;
        StoredChunk snapshot;
        try {
            snapshot = voxelize(link, chunk);
        } catch (RuntimeException exception) {
            PENDING_WRITES.remove(destination);
            throw exception;
        }
        try {
            WRITER.execute(() -> {
                try {
                    write(destination, snapshot);
                } finally {
                    PENDING_WRITES.remove(destination);
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Never compress on the server thread. A later load/unload retries this chunk.
            PENDING_WRITES.remove(destination);
        }
    }

    public static void ingestIfMissing(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        // Presence checks must not inflate and allocate the complete voxel payload on every source chunk load.
        if (!Files.isRegularFile(chunkPath(link, level, chunk.getPos()))) ingest(link, level, chunk);
    }

    public static Optional<StoredChunk> read(CrossDimensionLodLink link, ServerLevel level, ChunkPos pos) {
        Path path = chunkPath(link, level, pos);
        if (!Files.isRegularFile(path)) return Optional.empty();
        try (DataInputStream input = new DataInputStream(new InflaterInputStream(
                new BufferedInputStream(Files.newInputStream(path))))) {
            if (input.readInt() != MAGIC || input.readInt() != VERSION) return Optional.empty();
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
            return Optional.of(new StoredChunk(chunkX, chunkZ, cellSize, minY, yCells, palette, voxels));
        } catch (IOException | RuntimeException exception) {
            MementoInAbyss.LOGGER.warn("Unable to read cross-dimension LOD {}", path, exception);
            return Optional.empty();
        }
    }

    /** Derives a coarser level from the persisted base level without duplicating files on disk. */
    public static StoredChunk coarsen(StoredChunk source, int targetCellSize) {
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
                    int stateId = count == 0 ? airId : mostFrequent(ids, count);
                    short paletteIndex = paletteLookup.computeIfAbsent(stateId, id -> {
                        palette.add(id);
                        return (short) (palette.size() - 1);
                    });
                    voxels[index(x, y, z, horizontal, yCells)] = paletteIndex;
                }
            }
        }
        return new StoredChunk(source.chunkX, source.chunkZ, targetCellSize, source.minY, yCells,
                palette.stream().mapToInt(Integer::intValue).toArray(), voxels);
    }

    private static StoredChunk voxelize(CrossDimensionLodLink link, ChunkAccess chunk) {
        MiaHeight sourceHeight = link.sourceHeight();
        int horizontalCells = 16 / BASE_CELL_SIZE;
        int minY = sourceHeight.minY();
        int yCells = sourceHeight.height() / BASE_CELL_SIZE;
        short[] voxels = new short[horizontalCells * horizontalCells * yCells];
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Short> paletteLookup = new HashMap<>();
        int airId = Block.getId(Blocks.AIR.defaultBlockState());
        palette.add(airId);
        paletteLookup.put(airId, (short) 0);
        int[] ids = new int[BASE_CELL_SIZE * BASE_CELL_SIZE * BASE_CELL_SIZE];
        LevelChunkSection[] sections = chunk.getSections();
        int chunkMinY = chunk.getMinY();
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int z = 0; z < horizontalCells; z++) {
            for (int x = 0; x < horizontalCells; x++) {
                for (int y = 0; y < yCells; y++) {
                    int count = 0;
                    for (int dz = 0; dz < BASE_CELL_SIZE; dz++) {
                        for (int dx = 0; dx < BASE_CELL_SIZE; dx++) {
                            for (int dy = 0; dy < BASE_CELL_SIZE; dy++) {
                                int blockY = minY + y * BASE_CELL_SIZE + dy;
                                int sectionIndex = (blockY - chunkMinY) >> 4;
                                if (sectionIndex < 0 || sectionIndex >= sections.length) continue;
                                LevelChunkSection section = sections[sectionIndex];
                                if (section.hasOnlyAir()) continue;
                                var state = section.getBlockState(x * BASE_CELL_SIZE + dx,
                                        blockY & 15, z * BASE_CELL_SIZE + dz);
                                int id = Block.getId(state);
                                blockPos.set(chunkMinX + x * BASE_CELL_SIZE + dx, blockY,
                                        chunkMinZ + z * BASE_CELL_SIZE + dz);
                                if (id != airId && state.getLightEmission() == 0
                                        && state.getFluidState().isEmpty()
                                        && state.isCollisionShapeFullBlock(chunk, blockPos)) {
                                    ids[count++] = id;
                                }
                            }
                        }
                    }
                    int stateId = count == 0 ? airId : mostFrequent(ids, count);
                    short paletteIndex = paletteLookup.computeIfAbsent(stateId, id -> {
                        palette.add(id);
                        return (short) (palette.size() - 1);
                    });
                    voxels[index(x, y, z, horizontalCells, yCells)] = paletteIndex;
                }
            }
        }
        return new StoredChunk(chunk.getPos().x(), chunk.getPos().z(), BASE_CELL_SIZE,
                minY, yCells, palette.stream().mapToInt(Integer::intValue).toArray(), voxels);
    }

    private static int mostFrequent(int[] ids, int length) {
        Arrays.sort(ids, 0, length);
        int bestId = ids[0], bestCount = 1, runId = ids[0], runCount = 1;
        for (int i = 1; i < length; i++) {
            if (ids[i] == runId) runCount++;
            else {
                if (runCount > bestCount) { bestId = runId; bestCount = runCount; }
                runId = ids[i];
                runCount = 1;
            }
        }
        return runCount > bestCount ? runId : bestId;
    }

    private static void write(Path destination, StoredChunk chunk) {
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            Files.createDirectories(destination.getParent());
            try (DataOutputStream output = new DataOutputStream(new DeflaterOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporary))))) {
                output.writeInt(MAGIC);
                output.writeInt(VERSION);
                output.writeInt(chunk.chunkX);
                output.writeInt(chunk.chunkZ);
                output.writeByte(chunk.cellSize);
                output.writeInt(chunk.minY);
                output.writeInt(chunk.yCells);
                output.writeShort(chunk.palette.length);
                for (int stateId : chunk.palette) output.writeInt(stateId);
                for (short voxel : chunk.voxels) output.writeShort(voxel);
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            MementoInAbyss.LOGGER.warn("Unable to write cross-dimension LOD {}", destination, exception);
        }
    }

    private static Path chunkPath(CrossDimensionLodLink link, ServerLevel level, ChunkPos pos) {
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MementoInAbyss.ID).resolve("lod").resolve(link.id().getPath())
                .resolve("r." + Math.floorDiv(pos.x(), 32) + "." + Math.floorDiv(pos.z(), 32))
                .resolve("c." + pos.x() + "." + pos.z() + ".v" + VERSION + ".mialod");
    }

    private static int index(int x, int y, int z, int horizontalCells, int yCells) {
        return (z * horizontalCells + x) * yCells + y;
    }

    public record StoredChunk(int chunkX, int chunkZ, int cellSize, int minY,
                              int yCells, int[] palette, short[] voxels) {}
    private record PendingCapture(CrossDimensionLodLink link, ServerLevel level,
                                  ChunkAccess chunk, Path destination) {}
    private MiaLodStorage() {}
}
