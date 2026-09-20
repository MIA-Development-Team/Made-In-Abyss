package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.network.CrossDimensionLodBatchPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodBatchPayload.Section;
import com.altnoir.mementoinabyss.network.CrossDimensionLodCacheScope;
import com.altnoir.mementoinabyss.network.CrossDimensionLodReceiptPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodTransfer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongPredicate;

/** Client-thread transactional section store. No Minecraft, model or GPU access. */
final class CrossDimensionLodSectionStore {
    private static final int MAX_STAGING = 64;
    private static final long STAGING_TIMEOUT_TICKS = 200;
    private final Map<Long, Column> columns = new HashMap<>();
    private long epoch;
    private int stagingCount;
    private String link;
    private CrossDimensionLodCacheScope scope;

    private boolean selectStream(CrossDimensionLodTransfer t) {
        if (t.streamEpoch() < epoch) return false;
        if (t.streamEpoch() > epoch) {
            clear();
            epoch = t.streamEpoch();
            link = t.linkId();
            scope = t.cacheScope();
        }
        return t.linkId().equals(link) && t.cacheScope().equals(scope);
    }

    boolean offer(CrossDimensionLodTransfer t) {
        if (!selectStream(t)) return false;
        Column column = columns.computeIfAbsent(t.chunkKey(), ignored -> new Column());
        if (t.transferId() < Math.max(column.latestId, column.offeredId)) return false;
        column.offeredId = t.transferId();
        return true;
    }

    boolean alreadyApplied(CrossDimensionLodTransfer t) {
        Column column = columns.get(t.chunkKey());
        return column != null && t.equals(column.applied);
    }

    List<Section> snapshot(CrossDimensionLodTransfer t) {
        Column column = columns.get(t.chunkKey());
        return column != null && t.equals(column.applied) ? List.of(column.sections) : List.of();
    }

    Result restore(CrossDimensionLodTransfer t, List<Section> sections, long now) {
        Column offered = columns.get(t.chunkKey());
        if (epoch != t.streamEpoch()
                || !t.cacheScope().equals(scope)
                || !t.linkId().equals(link)
                || offered == null
                || offered.offeredId != t.transferId()) return Result.NONE;
        if (!t.replacement() || sections.size() != t.sectionCount())
            throw new IllegalArgumentException("Invalid cache restore");
        int size = 16 / t.cellSize();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (section.sectionY() != i
                    || section.revision() != t.targetRevision()
                    || section.voxels().length != size * size * size) {
                throw new IllegalArgumentException("Invalid cached section");
            }
        }
        return apply(t, sections, true, now);
    }

    Result accept(CrossDimensionLodBatchPayload payload, long now) {
        return apply(payload.transfer(), payload.sections(), payload.commit(), now);
    }

    private Result apply(
            CrossDimensionLodTransfer t, List<Section> updates, boolean commit, long now) {
        if (!selectStream(t)) return Result.NONE;
        Column column = columns.computeIfAbsent(t.chunkKey(), ignored -> new Column());
        if (t.transferId() < Math.max(column.latestId, column.offeredId)) return Result.NONE;
        if (column.applied != null && t.transferId() == column.applied.transferId()) {
            return commit && t.equals(column.applied)
                    ? new Result(
                            null,
                            (byte) 0,
                            CrossDimensionLodReceiptPayload.of(
                                    t, CrossDimensionLodReceiptPayload.APPLIED))
                    : Result.NONE;
        }
        if (t.transferId() == column.latestId && column.blocked) return Result.NONE;
        if (t.transferId() > column.latestId) {
            column.latestId = t.transferId();
            column.blocked = false;
            if (column.pending != null) stagingCount--;
            column.pending = null;
            if (column.applied != null && t.targetRevision() < column.applied.targetRevision())
                return reject(column, t);
            if (!t.replacement()
                    && (column.applied == null
                            || column.applied.targetRevision() != t.baseRevision()
                            || !sameLayout(t, column.applied))) return reject(column, t);
            if (stagingCount >= MAX_STAGING) return reject(column, t);
            column.pending = new Pending(t, now);
            stagingCount++;
        }
        Pending pending = column.pending;
        if (pending == null || !pending.transfer.equals(t)) return reject(column, t);
        pending.commit |= commit;
        for (var section : updates) {
            var previous = pending.sections[section.sectionY()];
            if (previous != null && !previous.sameContent(section)) return reject(column, t);
            if (previous == null) {
                pending.sections[section.sectionY()] = section;
                pending.received++;
                pending.dirtyFaces |= section.dirtyFaces();
            }
        }
        if (pending.received > t.updateCount()) return reject(column, t);
        if (!pending.commit || pending.received != t.updateCount()) return Result.NONE;
        var sections = t.replacement() ? new Section[t.sectionCount()] : column.sections.clone();
        for (int i = 0; i < sections.length; i++) {
            var update = pending.sections[i];
            if (update != null) {
                if (!t.replacement() && sections[i].revision() > update.revision())
                    return reject(column, t);
                sections[i] = update;
            }
            if (sections[i] == null) return reject(column, t);
        }
        CrossDimensionLodColumn assembled;
        try {
            assembled = t.updateCount() == 0 ? null : combine(t, sections);
        } catch (IllegalArgumentException failure) {
            return reject(column, t);
        }
        column.sections = sections;
        column.applied = t;
        column.pending = null;
        stagingCount--;
        byte faces =
                t.replacement()
                        ? CrossDimensionLodBatchPayload.DIRTY_ALL_FACES
                        : pending.dirtyFaces;
        // Empty checkpoints only advance the accepted baseline; they must not trigger remeshing.
        return new Result(
                t.updateCount() == 0 ? null : assembled,
                faces,
                CrossDimensionLodReceiptPayload.of(t, CrossDimensionLodReceiptPayload.APPLIED));
    }

    private static boolean sameLayout(CrossDimensionLodTransfer a, CrossDimensionLodTransfer b) {
        return a.cacheScope().equals(b.cacheScope())
                && a.cellSize() == b.cellSize()
                && a.minY() == b.minY()
                && a.sectionCount() == b.sectionCount()
                && a.displayYOffset() == b.displayYOffset();
    }

    private Result reject(Column column, CrossDimensionLodTransfer transfer) {
        if (column.pending != null) stagingCount--;
        column.pending = null;
        column.blocked = true;
        return new Result(
                null,
                (byte) 0,
                CrossDimensionLodReceiptPayload.of(
                        transfer, CrossDimensionLodReceiptPayload.RESYNC));
    }

    List<CrossDimensionLodReceiptPayload> expire(long now) {
        List<CrossDimensionLodReceiptPayload> receipts = new ArrayList<>();
        for (Column column : columns.values()) {
            if (column.pending != null && now - column.pending.started >= STAGING_TIMEOUT_TICKS) {
                receipts.add(reject(column, column.pending.transfer).receipt());
            }
        }
        return receipts;
    }

    void retain(LongPredicate keep) {
        var iterator = columns.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!keep.test(entry.getKey())) {
                if (entry.getValue().pending != null) stagingCount--;
                iterator.remove();
            }
        }
    }

    void evict(long key) {
        Column column = columns.remove(key);
        if (column != null && column.pending != null) stagingCount--;
    }

    void clear() {
        columns.clear();
        stagingCount = 0;
        epoch = 0;
        link = null;
        scope = null;
    }

    private static CrossDimensionLodColumn combine(
            CrossDimensionLodTransfer t, Section[] sections) {
        int size = 16 / t.cellSize();
        int height = sections.length * size;
        int air = sections[0].palette()[0];
        List<Integer> palette = new ArrayList<>();
        Map<Integer, Short> lookup = new HashMap<>();
        palette.add(air);
        lookup.put(air, (short) 0);
        short[] voxels = new short[size * size * height];
        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            var section = sections[sectionY];
            if (section.palette()[0] != air)
                throw new IllegalArgumentException("Inconsistent air state");
            short[] indices = new short[section.palette().length];
            for (int i = 0; i < indices.length; i++) {
                int state = section.palette()[i];
                Short index = lookup.get(state);
                if (index == null) {
                    if (palette.size() >= 4096)
                        throw new IllegalArgumentException("LOD column palette exceeds limit");
                    index = (short) palette.size();
                    palette.add(state);
                    lookup.put(state, index);
                }
                indices[i] = index;
            }
            for (int z = 0; z < size; z++)
                for (int x = 0; x < size; x++)
                    for (int y = 0; y < size; y++) {
                        short local = section.voxels()[(z * size + x) * size + y];
                        voxels[(z * size + x) * height + sectionY * size + y] =
                                indices[Short.toUnsignedInt(local)];
                    }
        }
        return new CrossDimensionLodColumn(
                t.linkId(),
                t.displayYOffset(),
                t.radius(),
                t.chunkX(),
                t.chunkZ(),
                t.cellSize(),
                t.minY(),
                height,
                palette.stream().mapToInt(Integer::intValue).toArray(),
                voxels);
    }

    record Result(
            CrossDimensionLodColumn column,
            byte dirtyFaces,
            CrossDimensionLodReceiptPayload receipt) {
        private static final Result NONE = new Result(null, (byte) 0, null);
    }

    private static final class Column {
        long latestId, offeredId;
        boolean blocked;
        CrossDimensionLodTransfer applied;
        Section[] sections;
        Pending pending;
    }

    private static final class Pending {
        final CrossDimensionLodTransfer transfer;
        final Section[] sections;
        final long started;
        int received;
        byte dirtyFaces;
        boolean commit;

        Pending(CrossDimensionLodTransfer transfer, long now) {
            this.transfer = transfer;
            this.started = now;
            this.sections = new Section[transfer.sectionCount()];
        }
    }
}
