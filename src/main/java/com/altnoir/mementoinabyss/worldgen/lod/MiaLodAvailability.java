package com.altnoir.mementoinabyss.worldgen.lod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongPredicate;

/** Prevents a late failed read from hiding a file that was committed while that read was in flight. */
final class MiaLodAvailability {
    private static final long RETRY_TICKS = 1200;
    private final Map<Long, Long> versions = new HashMap<>();
    private final Map<Long, Long> missingUntil = new HashMap<>();

    long version(long key) {
        return versions.getOrDefault(key, 0L);
    }

    void available(long key) {
        versions.merge(key, 1L, Long::sum);
        missingUntil.remove(key);
    }

    void missing(long key, long readVersion, long now) {
        if (version(key) == readVersion) missingUntil.put(key, now + RETRY_TICKS);
    }

    boolean blocked(long key, long now) {
        return missingUntil.getOrDefault(key, 0L) > now;
    }

    boolean retryReady(long now) {
        return missingUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    int missingCount() {
        return missingUntil.size();
    }

    void retain(LongPredicate keep) {
        versions.keySet().removeIf(key -> !keep.test(key));
        missingUntil.keySet().removeIf(key -> !keep.test(key));
    }
}
