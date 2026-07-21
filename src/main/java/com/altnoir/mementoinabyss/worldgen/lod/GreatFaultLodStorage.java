package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/** Persistent, server-owned LOD storage derived only from fully generated Great Fault chunks. */
public final class GreatFaultLodStorage {
    public static final int BASE_CELL_SIZE = 4;
    private static final int MAGIC = 0x4D49414C; // MIAL
    private static final int VERSION = 2;
    private static final int MAX_PENDING_WRITES = 128;
    private static final ExecutorService WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_PENDING_WRITES), r -> {
                Thread thread = new Thread(r, "MIA Great Fault LOD writer");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.CallerRunsPolicy());
    private static final java.util.Set<Path> PENDING_WRITES = ConcurrentHashMap.newKeySet();

    public static void ingest(CrossDimensionLodLink link, ServerLevel level, ChunkAccess chunk) {
        Path destination = chunkPath(link, level, chunk.getPos());
        if (!PENDING_WRITES.add(destination)) return;
        StoredChunk snapshot;
        try {
            snapshot = voxelize(chunk);
        } catch (RuntimeException exception) {
            PENDING_WRITES.remove(destination);
            throw exception;
        }
        WRITER.execute(() -> {
            try {
                write(destination, snapshot);
            } finally {
                PENDING_WRITES.remove(destination);
            }
        });
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
            int[] palette = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) palette[i] = input.readInt();
            int horizontalCells = 16 / cellSize;
            short[] voxels = new short[horizontalCells * horizontalCells * yCells];
            for (int i = 0; i < voxels.length; i++) voxels[i] = input.readShort();
            return Optional.of(new StoredChunk(chunkX, chunkZ, cellSize, minY, yCells, palette, voxels));
        } catch (IOException | RuntimeException exception) {
            MementoInAbyss.LOGGER.warn("Unable to read Great Fault LOD {}", path, exception);
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

    private static StoredChunk voxelize(ChunkAccess chunk) {
        int horizontalCells = 16 / BASE_CELL_SIZE;
        int minY = MiaHeight.GREAT_FAULT.minY();
        int yCells = MiaHeight.GREAT_FAULT.height() / BASE_CELL_SIZE;
        short[] voxels = new short[horizontalCells * horizontalCells * yCells];
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Short> paletteLookup = new HashMap<>();
        int airId = Block.getId(Blocks.AIR.defaultBlockState());
        palette.add(airId);
        paletteLookup.put(airId, (short) 0);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int[] ids = new int[BASE_CELL_SIZE * BASE_CELL_SIZE * BASE_CELL_SIZE];

        for (int z = 0; z < horizontalCells; z++) {
            for (int x = 0; x < horizontalCells; x++) {
                for (int y = 0; y < yCells; y++) {
                    int count = 0;
                    for (int dz = 0; dz < BASE_CELL_SIZE; dz++) {
                        for (int dx = 0; dx < BASE_CELL_SIZE; dx++) {
                            for (int dy = 0; dy < BASE_CELL_SIZE; dy++) {
                                cursor.set(chunk.getPos().getMinBlockX() + x * BASE_CELL_SIZE + dx,
                                        minY + y * BASE_CELL_SIZE + dy,
                                        chunk.getPos().getMinBlockZ() + z * BASE_CELL_SIZE + dz);
                                var state = chunk.getBlockState(cursor);
                                int id = Block.getId(state);
                                if (id != airId && state.getLightEmission() == 0) ids[count++] = id;
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
            MementoInAbyss.LOGGER.warn("Unable to write Great Fault LOD {}", destination, exception);
        }
    }

    private static Path chunkPath(CrossDimensionLodLink link, ServerLevel level, ChunkPos pos) {
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MementoInAbyss.ID).resolve("lod").resolve(link.id().getPath())
                .resolve("r." + Math.floorDiv(pos.x(), 32) + "." + Math.floorDiv(pos.z(), 32))
                .resolve("c." + pos.x() + "." + pos.z() + ".mialod");
    }

    private static int index(int x, int y, int z, int horizontalCells, int yCells) {
        return (z * horizontalCells + x) * yCells + y;
    }

    public record StoredChunk(int chunkX, int chunkZ, int cellSize, int minY,
                              int yCells, int[] palette, short[] voxels) {}
    private GreatFaultLodStorage() {}
}
