package com.altnoir.mementoinabyss.client.render;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

/** Fair, bounded main-thread application. Lane preference survives a budget exhausted by one large transaction. */
final class CrossDimensionLodReceivePump {
    private final LongSupplier clock;
    private boolean cacheNext;

    CrossDimensionLodReceivePump(LongSupplier clock) {
        this.clock = clock;
    }

    int drain(int maxItems, long budgetNanos, BooleanSupplier network, BooleanSupplier cache) {
        long started = clock.getAsLong();
        int processed = 0;
        while (processed < maxItems
                && (processed == 0 || clock.getAsLong() - started < budgetNanos)) {
            if (cacheNext && cache.getAsBoolean()) cacheNext = false;
            else if (network.getAsBoolean()) cacheNext = true;
            else if (!cacheNext && cache.getAsBoolean()) cacheNext = false;
            else break;
            processed++;
        }
        return processed;
    }
}
