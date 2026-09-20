package com.altnoir.mementoinabyss.impl.lod.client;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodBatchPayload.Section;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodCacheOfferPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodTransfer;
import com.altnoir.mementoinabyss.impl.lod.server.MiaLodCacheData;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/** Client-thread coordinator; disk, compression and SHA work stay on one bounded IO worker. */
final class CrossDimensionLodClientCache implements AutoCloseable {
    private static final int MAX_PENDING = 64;
    private final CrossDimensionLodDiskCache disk;
    private final IntFunction<String> nameOf;
    private final ToIntFunction<String> resolveName;
    private final CrossDimensionLodCacheIo io = new CrossDimensionLodCacheIo(64);
    private final ArrayBlockingQueue<Read> completed = new ArrayBlockingQueue<>(MAX_PENDING);
    private final Map<CrossDimensionLodTransfer, Pending> pending = new LinkedHashMap<>();
    private final Map<Integer, String> names = new HashMap<>();
    private final Map<String, Integer> ids = new HashMap<>();
    private final AtomicLong errors = new AtomicLong();
    private volatile long generation;
    private volatile long diskBytes;
    private long hits, misses;

    CrossDimensionLodClientCache(
            Path root, IntFunction<String> nameOf, ToIntFunction<String> resolveName) {
        this.nameOf = nameOf;
        this.resolveName = resolveName;
        disk = new CrossDimensionLodDiskCache(root, 512L * 1024 * 1024, 32768);
    }

    boolean lookup(CrossDimensionLodCacheOfferPayload offer, long now) {
        if (pending.containsKey(offer.transfer())) return true;
        if (pending.size() >= MAX_PENDING) {
            misses++;
            return false;
        }
        long stamp = generation;
        pending.put(offer.transfer(), new Pending(offer, now + 80));
        if (io.submit(
                true,
                () -> {
                    if (stamp != generation) return;
                    MiaLodCacheData data = null;
                    try {
                        data =
                                disk.read(
                                        CrossDimensionLodDiskCache.Key.of(offer.transfer()),
                                        offer.digest());
                    } catch (Exception failure) {
                        errors.incrementAndGet();
                    }
                    diskBytes = disk.bytes();
                    if (stamp == generation) completed.offer(new Read(stamp, offer, data));
                    // If the mailbox fills, the client timeout below still produces CACHE_MISS.
                })) return true;
        pending.remove(offer.transfer());
        misses++;
        return false;
    }

    Read poll(long now) {
        Read result;
        while ((result = completed.poll()) != null) {
            if (result.generation == generation && pending.remove(result.offer.transfer()) != null)
                return result;
        }
        var iterator = pending.values().iterator();
        while (iterator.hasNext()) {
            Pending value = iterator.next();
            if (now >= value.deadline) {
                iterator.remove();
                return new Read(generation, value.offer, null);
            }
        }
        return null;
    }

    List<Section> resolve(Read read) {
        if (read.data == null || !read.data.matchesLayout(read.offer.transfer())) return null;
        try {
            return read.data.sections(
                    read.offer.transfer(),
                    name -> ids.computeIfAbsent(name, resolveName::applyAsInt));
        } catch (RuntimeException invalidState) {
            return null;
        }
    }

    void hit() {
        hits++;
    }

    void miss() {
        misses++;
    }

    void save(CrossDimensionLodTransfer transfer, List<Section> snapshot) {
        if (!MiaLodCacheData.worthCaching(snapshot) || !io.hasWriteCapacity()) return;
        Map<Integer, String> frozen = new HashMap<>();
        try {
            for (Section section : snapshot)
                for (int id : section.palette()) {
                    frozen.put(id, names.computeIfAbsent(id, nameOf::apply));
                }
            io.submit(
                    false,
                    () -> {
                        try {
                            var data =
                                    MiaLodCacheData.fromSections(
                                            transfer.cellSize(),
                                            transfer.minY(),
                                            snapshot,
                                            frozen::get);
                            disk.write(CrossDimensionLodDiskCache.Key.of(transfer), data);
                            diskBytes = disk.bytes();
                        } catch (Exception failure) {
                            errors.incrementAndGet();
                        }
                    });
        } catch (RuntimeException invalidState) {
            errors.incrementAndGet();
        }
    }

    void clearSession() {
        generation++;
        pending.clear();
        completed.clear();
        names.clear();
        ids.clear();
        // Already captured writes may finish. They own named data and the original world namespace.
    }

    @Override
    public void close() {
        clearSession();
        io.close();
    }

    Stats stats() {
        return new Stats(hits, misses, pending.size(), io.queued(), diskBytes, errors.get());
    }

    record Stats(long hits, long misses, int pending, int queued, long bytes, long errors) {
        static final Stats EMPTY = new Stats(0, 0, 0, 0, 0, 0);
    }

    record Read(long generation, CrossDimensionLodCacheOfferPayload offer, MiaLodCacheData data) {}

    private record Pending(CrossDimensionLodCacheOfferPayload offer, long deadline) {}
}
