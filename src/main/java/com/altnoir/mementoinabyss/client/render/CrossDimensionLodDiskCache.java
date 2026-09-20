package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.network.CrossDimensionLodCacheScope;
import com.altnoir.mementoinabyss.network.CrossDimensionLodTransfer;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodCacheData;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Single IO-worker owned, disposable, globally size/count-bounded LRU cache. */
final class CrossDimensionLodDiskCache {
    private static final int MAGIC = 0x4d494143;
    private static final int MAX_BODY = 16 * 1024 * 1024;
    private final Path root;
    private final long capacity;
    private final int maxFiles;
    private final LinkedHashMap<Path, Long> files = new LinkedHashMap<>(16, .75f, true);
    private long bytes;
    private boolean initialized;

    CrossDimensionLodDiskCache(Path root, long capacity, int maxFiles) {
        if (capacity < 1 || maxFiles < 1)
            throw new IllegalArgumentException("Invalid disk cache capacity");
        this.root = root;
        this.capacity = capacity;
        this.maxFiles = maxFiles;
    }

    record Key(CrossDimensionLodCacheScope scope, int chunkX, int chunkZ, int cellSize) {
        static Key of(CrossDimensionLodTransfer t) {
            return new Key(t.cacheScope(), t.chunkX(), t.chunkZ(), t.cellSize());
        }

        String fileName() {
            String key =
                    MiaLodCacheData.FORMAT
                            + "\n"
                            + scope.worldId()
                            + "\n"
                            + scope.dimension()
                            + "\n"
                            + chunkX
                            + "\n"
                            + chunkZ
                            + "\n"
                            + cellSize;
            return HexFormat.of()
                            .formatHex(
                                    MiaLodCacheData.sha256()
                                            .digest(key.getBytes(StandardCharsets.UTF_8)))
                    + ".mialod";
        }
    }

    MiaLodCacheData read(Key key, String expectedDigest) throws IOException {
        initialize();
        Path file = root.resolve(key.fileName());
        if (!files.containsKey(file)) return null;
        try {
            if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(file) > MAX_BODY) throw new IOException("Invalid cache file");
            byte[] body;
            try (var input = new GZIPInputStream(Files.newInputStream(file))) {
                body = input.readNBytes(MAX_BODY + 1);
                if (body.length > MAX_BODY) throw new IOException("Oversized cache body");
            }
            try (var input = new DataInputStream(new ByteArrayInputStream(body))) {
                if (input.readInt() != MAGIC || input.readInt() != MiaLodCacheData.FORMAT)
                    throw new IOException("Invalid cache version");
                var scope =
                        new CrossDimensionLodCacheScope(
                                new UUID(input.readLong(), input.readLong()), input.readUTF());
                var storedKey = new Key(scope, input.readInt(), input.readInt(), input.readInt());
                if (!key.equals(storedKey)) throw new IOException("Wrong cache namespace/key");
                int minY = input.readInt(), count = input.readInt(), paletteSize = input.readInt();
                if (paletteSize < 1 || paletteSize > 4096)
                    throw new IOException("Invalid cache palette size");
                String[] palette = new String[paletteSize];
                for (int i = 0; i < paletteSize; i++) {
                    palette[i] = input.readUTF();
                    if (palette[i].length() > 1024)
                        throw new IOException("Oversized cached block state");
                }
                int length = input.readInt();
                if (length < 1 || length > 262144)
                    throw new IOException("Invalid cache voxel count");
                short[] voxels = new short[length];
                for (int i = 0; i < length; i++) voxels[i] = input.readShort();
                String savedDigest = input.readUTF();
                if (input.read() != -1) throw new IOException("Trailing cache data");
                var data = new MiaLodCacheData(key.cellSize, minY, count, palette, voxels);
                String actual = data.digest();
                if (!actual.equals(savedDigest))
                    throw new IOException("Cache content checksum mismatch");
                if (!actual.equals(expectedDigest))
                    return null; // Valid but outdated; not a confirmed baseline.
                files.get(file); // Touch the in-memory LRU only after validation.
                Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
                return data;
            }
        } catch (IOException | IllegalArgumentException failure) {
            remove(file);
            return null;
        }
    }

    void write(Key key, MiaLodCacheData data) throws IOException {
        if (key.cellSize != data.cellSize())
            throw new IllegalArgumentException("Mismatched cache key");
        initialize();
        prune(); // Do not keep adding files if an earlier eviction failed (e.g. a locked file).
        var bytesOut = new ByteArrayOutputStream();
        try (var out =
                new DataOutputStream(
                        new BufferedOutputStream(new GZIPOutputStream(bytesOut), 32 * 1024))) {
            out.writeInt(MAGIC);
            out.writeInt(MiaLodCacheData.FORMAT);
            out.writeLong(key.scope.worldId().getMostSignificantBits());
            out.writeLong(key.scope.worldId().getLeastSignificantBits());
            out.writeUTF(key.scope.dimension());
            out.writeInt(key.chunkX);
            out.writeInt(key.chunkZ);
            out.writeInt(key.cellSize);
            out.writeInt(data.minY());
            out.writeInt(data.sectionCount());
            out.writeInt(data.palette().length);
            for (String state : data.palette()) out.writeUTF(state);
            out.writeInt(data.voxels().length);
            for (short voxel : data.voxels()) out.writeShort(voxel);
            out.writeUTF(data.digest());
        }
        byte[] encoded = bytesOut.toByteArray();
        if (encoded.length > capacity || encoded.length > MAX_BODY) return;
        Path destination = root.resolve(key.fileName());
        Path temporary = Files.createTempFile(root, ".lod-", ".tmp");
        try {
            Files.write(temporary, encoded);
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            Long previous = files.put(destination, (long) encoded.length);
            bytes += encoded.length - (previous == null ? 0 : previous);
            prune();
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void initialize() throws IOException {
        if (initialized) return;
        Files.createDirectories(root);
        // Keep only the newest bounded set while scanning; never materialize an unbounded file
        // list.
        var recent =
                new TreeSet<DiskEntry>(
                        Comparator.comparingLong(DiskEntry::time)
                                .thenComparing(e -> e.path.toString()));
        long scannedBytes = 0;
        try (var directory = Files.newDirectoryStream(root)) {
            for (Path file : directory) {
                String name = file.getFileName().toString();
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                if (name.startsWith(".lod-") && name.endsWith(".tmp")) {
                    Files.deleteIfExists(file);
                    continue;
                }
                if (!name.matches("[0-9a-f]{64}\\.mialod")) continue;
                long size = Files.size(file);
                if (size > MAX_BODY || size > capacity) {
                    Files.deleteIfExists(file);
                    continue;
                }
                recent.add(new DiskEntry(file, size, Files.getLastModifiedTime(file).toMillis()));
                scannedBytes += size;
                while (recent.size() > maxFiles || scannedBytes > capacity) {
                    var oldest = recent.pollFirst();
                    Files.deleteIfExists(oldest.path);
                    scannedBytes -= oldest.size;
                }
            }
        }
        files.clear();
        for (var entry : recent) files.put(entry.path, entry.size);
        bytes = scannedBytes;
        initialized = true;
    }

    private void prune() throws IOException {
        while (bytes > capacity || files.size() > maxFiles)
            remove(files.keySet().iterator().next());
    }

    private void remove(Path file) throws IOException {
        Files.deleteIfExists(file);
        Long previous = files.remove(file);
        if (previous != null) bytes -= previous;
    }

    long bytes() {
        return bytes;
    }

    int size() {
        return files.size();
    }

    private record DiskEntry(Path path, long size, long time) {}
}
