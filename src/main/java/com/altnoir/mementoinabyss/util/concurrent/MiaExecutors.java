package com.altnoir.mementoinabyss.util.concurrent;

import com.altnoir.mementoinabyss.MementoInAbyss;

import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Shared CPU pool for MIA work. Lower priority values are executed first. */
public final class MiaExecutors {
    private static final int RESERVED_PROCESSORS = 6;
    private static final int CPU_THREADS = Math.max(
            1, Runtime.getRuntime().availableProcessors() - RESERVED_PROCESSORS);
    private static final AtomicLong NEXT_SEQUENCE = new AtomicLong();
    private static final ThreadPoolExecutor CPU = new ThreadPoolExecutor(
            CPU_THREADS, CPU_THREADS, 0L, TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<>(), runnable -> {
                Thread thread = new Thread(runnable, "MIA CPU worker");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());

    static {
        // Force every submission through the priority queue, including the first burst.
        CPU.prestartAllCoreThreads();
    }

    public enum Priority {
        REAL_CHUNK_CAPTURE(0),
        LOD_MESH(1),
        LOD_LOAD(2),
        LOD_PREGEN(2);

        private final int order;

        Priority(int order) {
            this.order = order;
        }
    }

    public static void execute(Priority priority, Runnable task) {
        CPU.execute(new PrioritizedTask(priority, NEXT_SEQUENCE.getAndIncrement(), task));
    }

    public static Executor executor(Priority priority) {
        return task -> execute(priority, task);
    }

    public static int threadCount() {
        return CPU.getCorePoolSize();
    }

    public static int activeTaskCount() {
        return CPU.getActiveCount();
    }

    public static int queuedTaskCount() {
        return CPU.getQueue().size();
    }

    /** Applies the live config without rebuilding the queue or interrupting active tasks. */
    public static void refreshThreadLimit() {
        int requested = MementoInAbyss.CONFIGS.guiSection.miaCpuThreadLimit.get();
        int limit = Math.clamp(requested, 1, Runtime.getRuntime().availableProcessors());
        if (CPU.getCorePoolSize() == limit && CPU.getMaximumPoolSize() == limit) return;
        synchronized (CPU) {
            if (limit > CPU.getMaximumPoolSize()) {
                CPU.setMaximumPoolSize(limit);
                CPU.setCorePoolSize(limit);
            } else {
                CPU.setCorePoolSize(limit);
                CPU.setMaximumPoolSize(limit);
            }
            CPU.prestartAllCoreThreads();
        }
    }

    /** Drops queued work of one kind without disturbing integrated-server tasks in the same JVM. */
    public static int discardQueuedTasks(Priority priority) {
        int before = CPU.getQueue().size();
        CPU.getQueue().removeIf(runnable ->
                runnable instanceof PrioritizedTask task && task.priority == priority);
        return before - CPU.getQueue().size();
    }

    private record PrioritizedTask(Priority priority, long sequence, Runnable task)
            implements Runnable, Comparable<PrioritizedTask> {
        @Override
        public void run() {
            task.run();
        }

        @Override
        public int compareTo(PrioritizedTask other) {
            int priorityComparison = Integer.compare(this.priority.order, other.priority.order);
            return priorityComparison != 0
                    ? priorityComparison
                    : Long.compare(this.sequence, other.sequence);
        }
    }

    private MiaExecutors() {}
}
