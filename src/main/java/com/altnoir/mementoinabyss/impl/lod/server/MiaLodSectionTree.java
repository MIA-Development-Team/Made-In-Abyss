package com.altnoir.mementoinabyss.impl.lod.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable column snapshots with structurally shared physical 16³ sections.
 * Only parent memoization is mutable (and synchronized); old worker snapshots stay valid.
 */
final class MiaLodSectionTree {
    static final byte DIRTY_NEG_X = 1;
    static final byte DIRTY_POS_X = 1 << 1;
    static final byte DIRTY_NEG_Y = 1 << 2;
    static final byte DIRTY_POS_Y = 1 << 3;
    static final byte DIRTY_NEG_Z = 1 << 4;
    static final byte DIRTY_POS_Z = 1 << 5;
    static final byte DIRTY_ALL_FACES = 63;

    private final int chunkX, chunkZ, minY, sourceCellSize, airId;
    private final long revision;
    private final Section[] leaves;

    /** Only these source baselines were actually compared; gaps/eviction require conservative sends. */
    private final long[] comparedRevisions;

    private MiaLodSectionTree(
            MiaLodStorage.StoredChunk source, Section[] leaves, long[] comparedRevisions) {
        this.chunkX = source.chunkX();
        this.chunkZ = source.chunkZ();
        this.minY = source.minY();
        this.sourceCellSize = source.cellSize();
        this.airId = source.palette()[0];
        this.revision = source.revision();
        this.leaves = leaves;
        this.comparedRevisions = comparedRevisions;
    }

    static MiaLodSectionTree from(MiaLodStorage.StoredChunk source) {
        validate(source);
        int count = source.yCells() * source.cellSize() / 16;
        Section[] leaves = new Section[count];
        for (int i = 0; i < count; i++) {
            long[] faceRevisions = new long[6];
            Arrays.fill(faceRevisions, source.revision());
            leaves[i] =
                    new Section(
                            16 / source.cellSize(),
                            source.palette()[0],
                            extract(source, i),
                            source.revision(),
                            faceRevisions);
        }
        return new MiaLodSectionTree(source, leaves, new long[] {source.revision()});
    }

    /** Compare state IDs, not palette indices. Unchanged sections and parents retain identity. */
    MiaLodSectionTree update(MiaLodStorage.StoredChunk source) {
        validate(source);
        if (source.revision() <= revision) {
            throw new IllegalArgumentException(
                    "Section tree update must advance its source revision");
        }
        if (!matchesLayout(source)) return from(source);
        Section[] next = leaves.clone();
        for (int i = 0; i < next.length; i++) {
            next[i] = leaves[i].update(extract(source, i), source.revision());
        }
        int retained = Math.min(63, comparedRevisions.length);
        long[] history = new long[retained + 1];
        System.arraycopy(
                comparedRevisions, comparedRevisions.length - retained, history, 0, retained);
        history[retained] = source.revision();
        return new MiaLodSectionTree(source, next, history);
    }

    boolean matchesLayout(MiaLodStorage.StoredChunk source) {
        return chunkX == source.chunkX()
                && chunkZ == source.chunkZ()
                && minY == source.minY()
                && sourceCellSize == source.cellSize()
                && airId == source.palette()[0]
                && leaves.length * 16 == source.cellSize() * source.yCells();
    }

    int chunkX() {
        return chunkX;
    }

    int chunkZ() {
        return chunkZ;
    }

    int minY() {
        return minY;
    }

    int sectionCount() {
        return leaves.length;
    }

    long revision() {
        return revision;
    }

    Section section(int cellSize, int index) {
        if (cellSize < sourceCellSize
                || cellSize > 16
                || 16 % cellSize != 0
                || Integer.bitCount(cellSize) != 1) {
            throw new IllegalArgumentException("Invalid LOD section level " + cellSize);
        }
        Section section = leaves[index];
        for (int size = sourceCellSize; size < cellSize; size *= 2) section = section.parent();
        return section;
    }

    /** No coarse whole-column allocation: only changed sections are palettized for the wire. */
    List<SectionSnapshot> changes(int cellSize, long sinceRevision) {
        List<SectionSnapshot> result = new ArrayList<>();
        // Concurrent loads can skip an intermediate source revision (including an A→B→A
        // revert). A client on that unobserved baseline must not get a false no-op result.
        boolean unknownBaseline =
                sinceRevision > 0 && Arrays.binarySearch(comparedRevisions, sinceRevision) < 0;
        for (int i = 0; i < leaves.length; i++) {
            Section section = section(cellSize, i);
            if (unknownBaseline || section.revision() > sinceRevision) {
                SectionSnapshot snapshot = section.snapshot(i, sinceRevision);
                result.add(
                        unknownBaseline
                                ? new SectionSnapshot(
                                        i,
                                        revision,
                                        DIRTY_ALL_FACES,
                                        snapshot.palette(),
                                        snapshot.voxels())
                                : snapshot);
            }
        }
        return List.copyOf(result);
    }

    private static void validate(MiaLodStorage.StoredChunk source) {
        int cell = source.cellSize();
        if (cell < 1
                || cell > 16
                || Integer.bitCount(cell) != 1
                || source.revision() <= 0
                || source.yCells() <= 0
                || source.yCells() > 1024
                || source.yCells() * cell % 16 != 0
                || source.minY() % 16 != 0
                || source.palette().length == 0
                || source.palette().length > 4096
                || source.voxels().length != (16 / cell) * (16 / cell) * source.yCells()) {
            throw new IllegalArgumentException("Invalid stored LOD section dimensions");
        }
    }

    private static int[] extract(MiaLodStorage.StoredChunk source, int section) {
        int size = 16 / source.cellSize();
        int[] states = new int[size * size * size];
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int local = (z * size + x) * size + y;
                    int global = (z * size + x) * source.yCells() + section * size + y;
                    states[local] = source.palette()[Short.toUnsignedInt(source.voxels()[global])];
                }
            }
        }
        return states;
    }

    static final class Section {
        private final int size, airId;
        private final int[] states;
        private final long revision;

        /** Per-face history watermark; coalescing multiple source updates cannot lose a face. */
        private final long[] faceRevisions;

        private volatile Section parent;

        private Section(int size, int airId, int[] states, long revision, long[] faceRevisions) {
            this.size = size;
            this.airId = airId;
            this.states = states;
            this.revision = revision;
            this.faceRevisions = faceRevisions;
        }

        long revision() {
            return revision;
        }

        int state(int x, int y, int z) {
            return states[(z * size + x) * size + y];
        }

        byte dirtyFacesSince(long sinceRevision) {
            int mask = 0;
            for (int i = 0; i < 6; i++) if (faceRevisions[i] > sinceRevision) mask |= 1 << i;
            return (byte) mask;
        }

        private Section update(int[] nextStates, long nextRevision) {
            if (Arrays.equals(states, nextStates)) return this;
            long[] faces = faceRevisions.clone();
            for (int z = 0; z < size; z++) {
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        int index = (z * size + x) * size + y;
                        if (states[index] == nextStates[index]) continue;
                        if (x == 0) faces[0] = nextRevision;
                        if (x == size - 1) faces[1] = nextRevision;
                        if (y == 0) faces[2] = nextRevision;
                        if (y == size - 1) faces[3] = nextRevision;
                        if (z == 0) faces[4] = nextRevision;
                        if (z == size - 1) faces[5] = nextRevision;
                    }
                }
            }
            Section next = new Section(size, airId, nextStates, nextRevision, faces);
            if (size > 1) {
                // Materialize this section's old chain if necessary, not all sections in the
                // column.
                // update() returns the old parent immediately when downsampling erases the change.
                next.parent = parent().update(next.downsample(), nextRevision);
            }
            return next;
        }

        private Section parent() {
            Section existing = parent;
            if (existing != null) return existing;
            synchronized (this) {
                if (parent == null) {
                    long[] faces = new long[6];
                    Arrays.fill(faces, revision);
                    parent = new Section(size / 2, airId, downsample(), revision, faces);
                }
                return parent;
            }
        }

        private int[] downsample() {
            int nextSize = size / 2;
            int[] result = new int[nextSize * nextSize * nextSize];
            int[] ids = new int[8];
            for (int z = 0; z < nextSize; z++) {
                for (int x = 0; x < nextSize; x++) {
                    for (int y = 0; y < nextSize; y++) {
                        int count = 0;
                        for (int dz = 0; dz < 2; dz++) {
                            for (int dx = 0; dx < 2; dx++) {
                                for (int dy = 0; dy < 2; dy++)
                                    ids[count++] = state(x * 2 + dx, y * 2 + dy, z * 2 + dz);
                            }
                        }
                        result[(z * nextSize + x) * nextSize + y] = mostFrequentNonAir(ids, airId);
                    }
                }
            }
            return result;
        }

        private SectionSnapshot snapshot(int index, long sinceRevision) {
            List<Integer> palette = new ArrayList<>();
            Map<Integer, Short> lookup = new HashMap<>();
            palette.add(airId);
            lookup.put(airId, (short) 0);
            short[] voxels = new short[states.length];
            for (int i = 0; i < states.length; i++) {
                int state = states[i];
                Short entry = lookup.get(state);
                if (entry == null) {
                    if (palette.size() == 4096)
                        throw new IllegalArgumentException("LOD palette exceeds limit");
                    entry = (short) palette.size();
                    palette.add(state);
                    lookup.put(state, entry);
                }
                voxels[i] = entry;
            }
            return new SectionSnapshot(
                    index,
                    revision,
                    dirtyFacesSince(sinceRevision),
                    palette.stream().mapToInt(Integer::intValue).toArray(),
                    voxels);
        }
    }

    record SectionSnapshot(
            int index, long revision, byte dirtyFaces, int[] palette, short[] voxels) {}

    private static int mostFrequentNonAir(int[] ids, int airId) {
        int bestId = airId, bestCount = 0;
        for (int candidate : ids) {
            if (candidate == airId) continue;
            int count = 0;
            for (int id : ids) if (id == candidate) count++;
            if (count > bestCount) {
                bestId = candidate;
                bestCount = count;
            }
        }
        return bestId;
    }
}
