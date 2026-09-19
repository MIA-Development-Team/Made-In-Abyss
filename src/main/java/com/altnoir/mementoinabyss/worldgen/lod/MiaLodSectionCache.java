package com.altnoir.mementoinabyss.worldgen.lod;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded, revision-aware cache. Workers build outside the lock and never mutate published trees. */
final class MiaLodSectionCache {
    private final int capacity;
    private final Map<Path, MiaLodSectionTree> trees = new LinkedHashMap<>(16, 0.75F, true);
    private long generation;

    MiaLodSectionCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Nonpositive section cache capacity");
        this.capacity = capacity;
    }

    MiaLodSectionTree get(MiaLodStorage.StoredChunk source) {
        Path path = source.sourcePath();
        if (path == null) return MiaLodSectionTree.from(source);
        MiaLodSectionTree previous;
        long startedGeneration;
        synchronized (this) {
            startedGeneration = generation;
            previous = trees.get(path);
            if (previous != null && previous.revision() == source.revision()
                    && previous.matchesLayout(source)) return previous;
        }
        MiaLodSectionTree result = previous != null && previous.revision() < source.revision()
                ? previous.update(source) : MiaLodSectionTree.from(source);
        synchronized (this) {
            MiaLodSectionTree current = trees.get(path);
            if (generation == startedGeneration && (current == null || current.revision() < result.revision())) {
                trees.put(path, result);
                while (trees.size() > capacity) trees.remove(trees.keySet().iterator().next());
            }
        }
        return result;
    }

    synchronized void clear() {
        generation++;
        trees.clear();
    }
}
