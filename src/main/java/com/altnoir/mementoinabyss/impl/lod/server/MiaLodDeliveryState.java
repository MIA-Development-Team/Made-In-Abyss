package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodReceiptPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodTransfer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongPredicate;

/** Server-thread delivery ledger. Only an exact ACK for a fully sent batch advances a baseline. */
final class MiaLodDeliveryState {
    static final int MAX_OUTSTANDING = 32;
    static final long ACK_TIMEOUT_TICKS = 100;
    private final Map<Long, Baseline> confirmed = new HashMap<>();
    private final Map<Long, Outstanding> outstanding = new HashMap<>();
    private final Map<Long, Long> retryAfter = new HashMap<>();
    private final Map<Long, Integer> failures = new HashMap<>();
    private final Set<CacheAttempt> cacheAttempts = new HashSet<>();
    private long nextId;

    long nextId() {
        return ++nextId;
    }

    int outstandingCount() {
        return outstanding.size();
    }

    int confirmedCount() {
        return confirmed.size();
    }

    int confirmedCellSize(long key) {
        Baseline value = confirmed.get(key);
        return value == null ? 0 : value.cellSize;
    }

    boolean waiting(long key) {
        return outstanding.containsKey(key);
    }

    boolean coolingDown(long key, long now) {
        return retryAfter.getOrDefault(key, 0L) > now;
    }

    long baseline(long key, int cellSize) {
        Baseline value = confirmed.get(key);
        return value != null && value.cellSize == cellSize ? value.revision : 0L;
    }

    boolean begin(CrossDimensionLodTransfer transfer, long now) {
        long key = transfer.chunkKey();
        if (outstanding.size() >= MAX_OUTSTANDING
                || waiting(key)
                || coolingDown(key, now)
                || !transfer.replacement()
                        && transfer.baseRevision() != baseline(key, transfer.cellSize()))
            return false;
        outstanding.put(key, new Outstanding(transfer, Phase.SENDING, Long.MAX_VALUE));
        retryAfter.remove(key);
        return true;
    }

    boolean active(CrossDimensionLodTransfer transfer) {
        Outstanding value = outstanding.get(transfer.chunkKey());
        return value != null && value.transfer.equals(transfer);
    }

    void sent(CrossDimensionLodTransfer transfer, long now) {
        if (active(transfer))
            outstanding.put(
                    transfer.chunkKey(),
                    new Outstanding(transfer, Phase.AWAITING_ACK, now + ACK_TIMEOUT_TICKS));
    }

    boolean cacheCandidate(long key, int cellSize) {
        return !cacheAttempts.contains(new CacheAttempt(key, cellSize));
    }

    boolean canOfferCache(CrossDimensionLodTransfer transfer) {
        return transfer.replacement() && cacheCandidate(transfer.chunkKey(), transfer.cellSize());
    }

    void offered(CrossDimensionLodTransfer transfer, long now) {
        if (!active(transfer) || !canOfferCache(transfer))
            throw new IllegalStateException("Invalid LOD cache probe");
        cacheAttempts.add(new CacheAttempt(transfer.chunkKey(), transfer.cellSize()));
        outstanding.put(
                transfer.chunkKey(),
                new Outstanding(transfer, Phase.PROBING, now + ACK_TIMEOUT_TICKS));
    }

    /** false also covers forged, late, duplicate and premature acknowledgements. */
    boolean receive(CrossDimensionLodReceiptPayload receipt, long now) {
        long key = CrossDimensionLodKey.pack(receipt.chunkX(), receipt.chunkZ());
        Outstanding value = outstanding.get(key);
        if (value == null || !receipt.matches(value.transfer)) return false;
        if (receipt.status() == CrossDimensionLodReceiptPayload.CACHE_MISS) {
            if (value.phase != Phase.PROBING) return false;
            outstanding.remove(key);
            retryAfter.remove(key);
            return true; // A miss does not invalidate a previously applied level of this column.
        }
        if (receipt.status() == CrossDimensionLodReceiptPayload.RESYNC) {
            fail(key, now);
            return true;
        }
        if (value.phase == Phase.SENDING) return false;
        outstanding.remove(key);
        confirmed.put(key, new Baseline(receipt.cellSize(), receipt.targetRevision()));
        failures.remove(key);
        cacheAttempts.remove(new CacheAttempt(key, receipt.cellSize()));
        retryAfter.remove(key);
        return true;
    }

    private void fail(long key, long now) {
        outstanding.remove(key);
        confirmed.remove(key);
        int attempts = failures.merge(key, 1, Integer::sum);
        retryAfter.put(key, now + Math.min(200L, 20L << Math.min(attempts - 1, 4)));
    }

    List<Long> retries(long now) {
        for (var entry : List.copyOf(outstanding.entrySet())) {
            if (entry.getValue().phase != Phase.SENDING && now >= entry.getValue().deadline)
                fail(entry.getKey(), now);
        }
        List<Long> ready = new ArrayList<>();
        var iterator = retryAfter.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now >= entry.getValue()) {
                ready.add(entry.getKey());
                iterator.remove();
            }
        }
        return ready;
    }

    /** Dropping an out-of-range partial send is selection, not a delivery failure. Keep the confirmed baseline. */
    void abandonSending(CrossDimensionLodTransfer transfer) {
        Outstanding value = outstanding.get(transfer.chunkKey());
        if (value != null && value.transfer.equals(transfer) && value.phase == Phase.SENDING) {
            outstanding.remove(transfer.chunkKey());
        }
    }

    void retain(LongPredicate keep) {
        confirmed.keySet().removeIf(key -> !keep.test(key));
        outstanding.keySet().removeIf(key -> !keep.test(key));
        retryAfter.keySet().removeIf(key -> !keep.test(key));
        failures.keySet().removeIf(key -> !keep.test(key));
        cacheAttempts.removeIf(attempt -> !keep.test(attempt.key));
    }

    private record Baseline(int cellSize, long revision) {}

    private enum Phase {
        SENDING,
        PROBING,
        AWAITING_ACK
    }

    private record CacheAttempt(long key, int cellSize) {}

    private record Outstanding(CrossDimensionLodTransfer transfer, Phase phase, long deadline) {}
}
