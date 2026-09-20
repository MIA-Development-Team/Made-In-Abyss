package com.altnoir.mementoinabyss.client.render;

import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** One disk owner; FIFO within each lane. Reads can displace queued best-effort writes, never other reads. */
final class CrossDimensionLodCacheIo implements AutoCloseable {
    private final Semaphore slots;
    private final ThreadPoolExecutor executor;
    private final AtomicLong sequence = new AtomicLong();

    CrossDimensionLodCacheIo(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Invalid IO queue capacity");
        slots = new Semaphore(capacity);
        var queue =
                new PriorityBlockingQueue<Runnable>(
                        capacity,
                        Comparator.comparingInt((Runnable r) -> ((Job) r).read ? 0 : 1)
                                .thenComparingLong(r -> ((Job) r).sequence));
        executor =
                new ThreadPoolExecutor(
                        1,
                        1,
                        0,
                        TimeUnit.MILLISECONDS,
                        queue,
                        runnable -> {
                            var thread = new Thread(runnable, "MIA LOD client cache");
                            thread.setDaemon(true);
                            thread.setPriority(Thread.NORM_PRIORITY - 1);
                            return thread;
                        },
                        new ThreadPoolExecutor.AbortPolicy());
    }

    boolean submit(boolean read, Runnable action) {
        if (!slots.tryAcquire()) {
            if (!read) return false;
            Job victim =
                    executor.getQueue().stream()
                            .map(r -> (Job) r)
                            .filter(job -> !job.read)
                            .max(Comparator.comparingLong(job -> job.sequence))
                            .orElse(null);
            if (victim != null && executor.remove(victim)) slots.release();
            if (!slots.tryAcquire()) return false;
        }
        try {
            executor.execute(new Job(read, sequence.incrementAndGet(), action));
            return true;
        } catch (RejectedExecutionException rejected) {
            slots.release();
            return false;
        }
    }

    int queued() {
        return executor.getQueue().size();
    }

    boolean hasWriteCapacity() {
        return slots.availablePermits() > 0;
    }

    @Override
    public void close() {
        for (Runnable ignored : executor.shutdownNow()) slots.release();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private final class Job implements Runnable {
        final boolean read;
        final long sequence;
        final Runnable action;

        Job(boolean read, long sequence, Runnable action) {
            this.read = read;
            this.sequence = sequence;
            this.action = action;
        }

        @Override
        public void run() {
            slots.release(); // Capacity bounds queued work; one additional task may be running.
            action.run();
        }
    }
}
