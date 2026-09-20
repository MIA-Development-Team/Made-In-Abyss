package com.altnoir.mementoinabyss.worldgen.lod;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.*;
import com.altnoir.mementoinabyss.network.CrossDimensionLodBatchPayload.Section;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

/** Persistent camera-driven stream. Replanning changes demand/priority, never the stream or useful in-flight work. */
final class MiaLodSampler {
    private static final int MAX_BATCHES_PER_TICK = 16;
    private static final int MAX_BYTES_PER_TICK = 128 * 1024;
    private static final int MAX_IN_FLIGHT_PER_TASK = 32;
    private static final int MAX_LOADS_PER_TICK = 16;
    private static final AtomicLong NEXT_STREAM_EPOCH = new AtomicLong();
    private static final Map<UUID, Task> TASKS = new ConcurrentHashMap<>();
    private static final Set<UUID> CLIENT_DISABLED = ConcurrentHashMap.newKeySet();

    static void request(ServerPlayer player) {
        if (!wantsLod(player)) return;
        var link = CrossDimensionLodLinks.forTarget(player.level().dimension()).orElse(null);
        if (link == null) {
            remove(player);
            return;
        }
        TASKS.compute(
                player.getUUID(),
                (key, previous) ->
                        previous != null && previous.player == player && previous.link.equals(link)
                                ? previous
                                : new Task(player, link));
    }

    static void receiveView(ServerPlayer player, CrossDimensionLodViewPayload payload) {
        Task task = TASKS.get(player.getUUID());
        if (task == null
                || !isCurrent(task)
                || !payload.linkId().equals(task.link.id().toString())
                || !player.level().dimension().equals(task.link.target())
                || payload.sequence() <= task.viewSequence) return;
        long now = player.level().getGameTime();
        if (now - task.lastViewTick < 2 || !validCamera(player, payload.view())) return;
        task.lastViewTick = now;
        task.viewSequence = payload.sequence();
        task.replan |=
                payload.view().materiallyDifferent(task.view)
                        || task.requestedRadius != payload.radius();
        task.requestedRadius = payload.radius();
        task.view = payload.view();
    }

    private static boolean validCamera(ServerPlayer player, MiaLodView view) {
        var anchor = player.getCamera().position();
        // Third-person/spectator cameras are supported, but a client cannot request an unrelated
        // distant area.
        return view.near(anchor.x, anchor.y, anchor.z, 64);
    }

    static void tick(MinecraftServer server) {
        if (!lodEnabled()) {
            TASKS.clear();
            return;
        }
        for (Task task : TASKS.values()) {
            ServerLevel source = server.getLevel(task.link.source());
            if (source == null
                    || !task.player.isAlive()
                    || !task.player.level().dimension().equals(task.link.target())) {
                TASKS.remove(task.player.getUUID(), task);
                continue;
            }
            long now = task.player.level().getGameTime();
            if (!task.delivery.retries(now).isEmpty()) task.scan = true;
            drainPrepared(task);
            if (task.view == null || !validCamera(task.player, task.view)) continue;
            int radius = Math.min(CrossDimensionLodLinks.radius(task.link), task.requestedRadius);
            if (task.replan || task.radius != radius) task.plan(radius);
            if (task.availability.retryReady(now)) task.scan = true;
            sendCompleted(task, now);
            scheduleLoads(task, source, now);
        }
    }

    private static void scheduleLoads(Task task, ServerLevel source, long now) {
        if (!task.scan) return;
        int scheduled = 0;
        for (var demand : task.demands.values()) {
            long key = demand.key();
            if (task.work.containsKey(key)
                    || task.delivery.waiting(key)
                    || task.delivery.coolingDown(key, now)
                    || task.availability.blocked(key, now)) continue;
            long since = task.delivery.baseline(key, demand.cellSize());
            long requested = task.pendingRevisions.getOrDefault(key, 0L);
            if (since > 0 && since >= requested) continue;
            long sending =
                    task.work.values().stream()
                            .filter(
                                    work ->
                                            work.transfer != null
                                                    && task.delivery.active(work.transfer))
                            .count();
            int limit =
                    MiaLodViewPlanner.workLimit(
                            demand, task.delivery.confirmedCellSize(key), MAX_IN_FLIGHT_PER_TASK);
            // Reserve credits for newly visible coarse cover instead of filling every slot with
            // old-view refinements.
            if (scheduled >= MAX_LOADS_PER_TICK || task.activeLoads.get() >= MAX_IN_FLIGHT_PER_TASK)
                return;
            if (task.work.size() + task.delivery.outstandingCount() - sending >= limit) continue;
            var work =
                    new Work(
                            key,
                            demand.cellSize(),
                            since,
                            requested,
                            task.availability.version(key));
            task.work.put(key, work);
            scheduled++;
            PreparedChunk fallback = task.cacheFallbacks.remove(key);
            if (fallback != null
                    && since == 0
                    && fallback.cellSize == work.cellSize
                    && fallback.tree.revision() >= requested) {
                work.prepared = fallback;
                continue;
            }
            boolean probe = since == 0 && task.delivery.cacheCandidate(key, work.cellSize);
            task.activeLoads.incrementAndGet();
            try {
                MiaExecutors.execute(
                        MiaExecutors.Priority.LOD_LOAD, () -> prepare(task, source, work, probe));
            } catch (RejectedExecutionException ignored) {
                task.activeLoads.decrementAndGet();
                task.work.remove(key);
                return;
            }
        }
        task.scan = false;
    }

    private static void prepare(Task task, ServerLevel source, Work work, boolean probe) {
        PreparedChunk prepared = null;
        ChunkPos pos =
                new ChunkPos(CrossDimensionLodKey.x(work.key), CrossDimensionLodKey.z(work.key));
        try {
            if (!isCurrent(task) || work.cancelled) return;
            var stored = MiaLodStorage.read(source, pos);
            if (work.cancelled || !isCurrent(task)) return;
            if (stored.isPresent()
                    && stored.get().revision() >= Math.max(work.requested, work.base)) {
                var tree = MiaLodStorage.sectionTree(stored.get());
                var sections =
                        tree.changes(work.cellSize, work.base).stream()
                                .map(
                                        section ->
                                                new Section(
                                                        section.index(),
                                                        section.revision(),
                                                        section.dirtyFaces(),
                                                        section.palette(),
                                                        section.voxels()))
                                .toList();
                var scope =
                        new CrossDimensionLodCacheScope(
                                MiaLodWorldIdentity.get(
                                        source.getServer(),
                                        source.getServer().getWorldPath(LevelResource.ROOT)),
                                source.dimension().identifier().toString());
                String digest = null;
                // A tiny coarse preview is cheaper than a cache round trip and a full-column
                // canonical hash.
                if (probe && MiaLodCacheData.worthCaching(sections)) {
                    try {
                        Map<Integer, String> names = new HashMap<>();
                        digest =
                                MiaLodCacheData.fromSections(
                                                work.cellSize,
                                                tree.minY(),
                                                sections,
                                                id ->
                                                        names.computeIfAbsent(
                                                                id, MiaLodStateNames::name))
                                        .digest();
                    } catch (RuntimeException failure) {
                        MementoInAbyss.LOGGER.debug(
                                "Cannot offer cached LOD reuse for {}", pos, failure);
                    }
                }
                prepared =
                        new PreparedChunk(tree, work.cellSize, work.base, sections, scope, digest);
            }
        } catch (Throwable failure) {
            MementoInAbyss.LOGGER.warn(
                    "Unable to prepare cross-dimension LOD chunk {}", pos, failure);
        } finally {
            if (isCurrent(task)) task.completed.add(new PreparedResult(work, prepared));
            task.activeLoads.decrementAndGet();
        }
    }

    private static void drainPrepared(Task task) {
        for (int i = 0; i < 64; i++) {
            var result = task.completed.poll();
            if (result == null) break;
            if (task.work.get(result.work.key) != result.work) continue;
            if (result.prepared == null) {
                task.work.remove(result.work.key);
                task.availability.missing(
                        result.work.key,
                        result.work.availability,
                        task.player.level().getGameTime());
                task.scan = true;
            } else result.work.prepared = result.prepared;
        }
    }

    private static void sendCompleted(Task task, long now) {
        int sent = 0, bytes = 0;
        var ready =
                new ArrayList<>(
                        task.work.values().stream().filter(work -> work.prepared != null).toList());
        ready.sort(Comparator.comparingDouble(work -> task.priority(work.key)));
        // Each column owns its cursor. Partial fine transfers cannot force small visible previews
        // to wait.
        while (sent < MAX_BATCHES_PER_TICK && bytes < MAX_BYTES_PER_TICK && !ready.isEmpty()) {
            boolean progress = false;
            for (var iterator = ready.iterator();
                    iterator.hasNext() && sent < MAX_BATCHES_PER_TICK; ) {
                Work work = iterator.next();
                var prepared = work.prepared;
                if (work.transfer != null && !task.delivery.active(work.transfer)) {
                    task.work.remove(work.key);
                    iterator.remove();
                    task.scan = true;
                    continue;
                }
                if (work.transfer == null) {
                    var tree = prepared.tree;
                    var transfer =
                            new CrossDimensionLodTransfer(
                                    task.link.id().toString(),
                                    task.link.displayYOffset(),
                                    task.radius,
                                    task.streamEpoch,
                                    task.delivery.nextId(),
                                    prepared.baseRevision,
                                    tree.revision(),
                                    tree.chunkX(),
                                    tree.chunkZ(),
                                    prepared.cellSize,
                                    tree.minY(),
                                    tree.sectionCount(),
                                    prepared.sections.size(),
                                    prepared.scope);
                    if (!task.delivery.begin(transfer, now)) continue;
                    work.transfer = transfer;
                }
                if (work.section == 0
                        && prepared.digest != null
                        && task.delivery.canOfferCache(work.transfer)) {
                    var offer =
                            new CrossDimensionLodCacheOfferPayload(work.transfer, prepared.digest);
                    if (bytes + offer.encodedSize() > MAX_BYTES_PER_TICK) continue;
                    PacketDistributor.sendToPlayer(task.player, offer);
                    task.delivery.offered(work.transfer, now);
                    task.cacheFallbacks.put(work.key, prepared);
                    if (task.cacheFallbacks.size() > MAX_IN_FLIGHT_PER_TASK)
                        task.cacheFallbacks.remove(task.cacheFallbacks.keySet().iterator().next());
                    bytes += offer.encodedSize();
                    task.work.remove(work.key);
                    iterator.remove();
                } else {
                    var packet =
                            CrossDimensionLodBatchPayload.pack(
                                    work.transfer,
                                    prepared.sections,
                                    work.section,
                                    MAX_BYTES_PER_TICK - bytes);
                    if (packet == null) continue;
                    PacketDistributor.sendToPlayer(task.player, packet);
                    bytes += packet.encodedSize();
                    work.section += packet.sections().size();
                    if (packet.commit()) {
                        task.delivery.sent(work.transfer, now);
                        task.work.remove(work.key);
                        iterator.remove();
                    }
                }
                sent++;
                progress = true;
                task.scan = true;
            }
            if (!progress) break;
        }
    }

    static void receive(ServerPlayer player, CrossDimensionLodReceiptPayload receipt) {
        Task task = TASKS.get(player.getUUID());
        if (task == null
                || !isCurrent(task)
                || !player.level().dimension().equals(task.link.target())
                || !receipt.linkId().equals(task.link.id().toString())
                || receipt.streamEpoch() != task.streamEpoch) return;
        long now = player.level().getGameTime();
        if (task.feedbackTick != now) {
            task.feedbackTick = now;
            task.feedbackCount = 0;
        }
        long key = CrossDimensionLodKey.pack(receipt.chunkX(), receipt.chunkZ());
        int previousLevel = task.delivery.confirmedCellSize(key);
        if (++task.feedbackCount > 64 || !task.delivery.receive(receipt, now)) return;
        if (receipt.status() == CrossDimensionLodReceiptPayload.APPLIED) {
            task.cacheFallbacks.remove(key);
            task.pendingRevisions.computeIfPresent(
                    key,
                    (ignored, revision) -> revision <= receipt.targetRevision() ? null : revision);
            task.replan |= previousLevel != receipt.cellSize() || !task.demands.containsKey(key);
        }
        task.scan = true;
    }

    static void remove(ServerPlayer player) {
        TASKS.remove(player.getUUID());
    }

    static void forget(ServerPlayer player) {
        remove(player);
        CLIENT_DISABLED.remove(player.getUUID());
    }

    static void setClientEnabled(ServerPlayer player, boolean enabled) {
        if (enabled) {
            CLIENT_DISABLED.remove(player.getUUID());
            request(player);
        } else {
            CLIENT_DISABLED.add(player.getUUID());
            remove(player);
        }
    }

    static boolean wantsLod(ServerPlayer player) {
        return lodEnabled() && !CLIENT_DISABLED.contains(player.getUUID());
    }

    static boolean hasInterestedPlayer(MinecraftServer server) {
        return lodEnabled()
                && server.getPlayerList().getPlayers().stream()
                        .anyMatch(
                                player ->
                                        player.isAlive()
                                                && CrossDimensionLodLinks.forTarget(
                                                                player.level().dimension())
                                                        .isPresent()
                                                && wantsLod(player));
    }

    private static boolean isCurrent(Task task) {
        return wantsLod(task.player) && TASKS.get(task.player.getUUID()) == task;
    }

    static void notifyAvailable(CrossDimensionLodLink link, ChunkPos pos) {
        long key = CrossDimensionLodKey.pack(pos.x(), pos.z());
        for (Task task : TASKS.values())
            if (task.link.equals(link) && task.demands.containsKey(key)) {
                task.availability.available(key);
                task.scan = true;
            }
    }

    static void notifyReplaced(CrossDimensionLodLink link, ChunkPos pos, long revision) {
        long key = CrossDimensionLodKey.pack(pos.x(), pos.z());
        for (Task task : TASKS.values())
            if (task.link.equals(link)
                    && (task.demands.containsKey(key)
                            || task.delivery.confirmedCellSize(key) != 0
                            || task.delivery.waiting(key))) {
                task.pendingRevisions.merge(key, revision, Math::max);
                task.availability.available(key);
                task.scan = true;
            }
    }

    static ChunkPos generationCandidate(
            CrossDimensionLodLink link, java.util.function.Predicate<ChunkPos> missing) {
        var order =
                Comparator.comparingDouble(MiaLodViewPlanner.Demand::priority)
                        .thenComparingLong(MiaLodViewPlanner.Demand::key);
        var best = new PriorityQueue<MiaLodViewPlanner.Demand>(order.reversed());
        Map<Long, MiaLodViewPlanner.Demand> selected = new HashMap<>();
        for (Task task : TASKS.values())
            if (task.link.equals(link)
                    && task.view != null
                    && validCamera(task.player, task.view)) {
                long now = task.player.level().getGameTime();
                for (var demand : task.demands.values()) {
                    if (demand.band() > 1) break;
                    if (best.size() == 64 && order.compare(demand, best.peek()) >= 0) break;
                    if (!task.availability.blocked(demand.key(), now)) continue;
                    var previous = selected.get(demand.key());
                    if (previous != null) {
                        if (order.compare(previous, demand) <= 0) continue;
                        best.remove(previous);
                    } else if (best.size() == 64) selected.remove(best.remove().key());
                    selected.put(demand.key(), demand);
                    best.add(demand);
                }
            }
        var candidates = new ArrayList<>(best);
        candidates.sort(order);
        for (var candidate : candidates) {
            var pos =
                    new ChunkPos(
                            CrossDimensionLodKey.x(candidate.key()),
                            CrossDimensionLodKey.z(candidate.key()));
            if (missing.test(pos)) return pos;
        }
        return null;
    }

    static void clearTasks() {
        TASKS.clear();
    }

    static void clearClientPreferences() {
        CLIENT_DISABLED.clear();
    }

    static DebugSnapshot debugSnapshot(ServerPlayer player) {
        Task task = TASKS.get(player.getUUID());
        if (task == null) return new DebugSnapshot(0, 0, 0, 0, 0, 0, 0);
        int loading =
                (int) task.work.values().stream().filter(work -> work.prepared == null).count();
        return new DebugSnapshot(
                task.demands.size(),
                task.work.size(),
                task.delivery.outstandingCount(),
                loading,
                task.work.size() - loading,
                task.delivery.confirmedCount(),
                task.availability.missingCount());
    }

    record DebugSnapshot(
            int candidates,
            int pending,
            int outstanding,
            int loading,
            int ready,
            int known,
            int missing) {}

    private static final class Task {
        final ServerPlayer player;
        final CrossDimensionLodLink link;
        final long streamEpoch = NEXT_STREAM_EPOCH.incrementAndGet();
        final MiaLodDeliveryState delivery = new MiaLodDeliveryState();
        final Map<Long, Long> pendingRevisions = new HashMap<>();
        final MiaLodAvailability availability = new MiaLodAvailability();
        final AtomicInteger activeLoads = new AtomicInteger();
        final Map<Long, PreparedChunk> cacheFallbacks = new LinkedHashMap<>();
        final Map<Long, Work> work = new HashMap<>();
        final ConcurrentLinkedQueue<PreparedResult> completed = new ConcurrentLinkedQueue<>();
        Map<Long, MiaLodViewPlanner.Demand> demands = new LinkedHashMap<>();
        MiaLodView view;
        int radius, requestedRadius;
        long viewSequence, lastViewTick = Long.MIN_VALUE / 2;
        long feedbackTick = Long.MIN_VALUE;
        int feedbackCount;
        boolean replan = true, scan = true;

        Task(ServerPlayer player, CrossDimensionLodLink link) {
            this.player = player;
            this.link = link;
        }

        void plan(int radius) {
            this.radius = radius;
            int minY = link.sourceHeight().minY() + link.displayYOffset();
            int maxY = link.sourceHeight().maxY() + link.displayYOffset();
            var selected =
                    MiaLodViewPlanner.plan(view, radius, minY, maxY, delivery::confirmedCellSize);
            var previousDemands = demands;
            demands = new LinkedHashMap<>();
            for (var demand : selected) demands.put(demand.key(), demand);
            var iterator = work.values().iterator();
            while (iterator.hasNext()) {
                Work old = iterator.next();
                var demand = demands.get(old.key);
                var previous = previousDemands.get(old.key);
                // Keep partially sent transactions intact; only work with no client-side staging is
                // preempted.
                if (MiaLodViewPlanner.preemptUnsent(old.section, old.cellSize, previous, demand)) {
                    old.cancelled = true;
                    if (old.transfer != null) delivery.abandonSending(old.transfer);
                    iterator.remove();
                }
            }
            double retention = radius + 128.0;
            java.util.function.LongPredicate keep =
                    key ->
                            view.distanceSquared(
                                            CrossDimensionLodKey.x(key) * 16.0,
                                            minY,
                                            CrossDimensionLodKey.z(key) * 16.0,
                                            CrossDimensionLodKey.x(key) * 16.0 + 16,
                                            maxY,
                                            CrossDimensionLodKey.z(key) * 16.0 + 16)
                                    <= retention * retention;
            delivery.retain(key -> keep.test(key) || work.containsKey(key));
            pendingRevisions.keySet().removeIf(key -> !keep.test(key));
            availability.retain(keep);
            cacheFallbacks.keySet().removeIf(key -> !keep.test(key));
            replan = false;
            scan = true;
        }

        double priority(long key) {
            var demand = demands.get(key);
            return demand == null ? Double.MAX_VALUE : demand.priority();
        }
    }

    private static final class Work {
        final long key, base, requested, availability;
        volatile boolean cancelled;
        final int cellSize;
        PreparedChunk prepared;
        CrossDimensionLodTransfer transfer;
        int section;

        Work(long key, int cellSize, long base, long requested, long availability) {
            this.key = key;
            this.cellSize = cellSize;
            this.base = base;
            this.requested = requested;
            this.availability = availability;
        }
    }

    private record PreparedResult(Work work, PreparedChunk prepared) {}

    private record PreparedChunk(
            MiaLodSectionTree tree,
            int cellSize,
            long baseRevision,
            List<Section> sections,
            CrossDimensionLodCacheScope scope,
            String digest) {}

    private static boolean lodEnabled() {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodEnabled.get();
    }

    private MiaLodSampler() {}
}
