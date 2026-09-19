package com.altnoir.mementoinabyss.worldgen.lod;

import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.UUID;

/** Accessed by LOD load workers. A replaced/corrupt identity deliberately invalidates client caches. */
final class MiaLodWorldIdentity {
    private static final Map<Object, UUID> IDS = new WeakHashMap<>();

    static synchronized UUID get(Object server, Path worldRoot) {
        Path file = worldRoot.toAbsolutePath().normalize().resolve("data/mementoinabyss/lod/world-id");
        return IDS.computeIfAbsent(server, ignored -> load(file));
    }

    private static UUID load(Path file) {
        try {
            if (Files.isRegularFile(file) && Files.size(file) <= 128) {
                try { return UUID.fromString(Files.readString(file).strip()); }
                catch (IllegalArgumentException ignored) { /* Rotate an invalid identity. */ }
            }
            UUID id = UUID.randomUUID();
            Files.createDirectories(file.getParent());
            Path temporary = Files.createTempFile(file.getParent(), "world-id-", ".tmp");
            try {
                Files.writeString(temporary, id.toString());
                try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
            } finally { Files.deleteIfExists(temporary); }
            return id;
        } catch (IOException failure) {
            LogUtils.getLogger().warn("Cannot persist LOD world identity; using a session-local cache namespace", failure);
            return UUID.randomUUID();
        }
    }

    static synchronized void clear() { IDS.clear(); }
    private MiaLodWorldIdentity() {}
}
