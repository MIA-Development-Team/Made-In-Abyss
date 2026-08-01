package com.altnoir.mementoinabyss.impl.rope;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Owner-controlled collection of ropes.
 *
 * <p>This class deliberately has no global singleton and no networking policy.
 * A future item, projectile, entity, or level attachment can own a system and
 * decide when ropes are created, synchronized, ticked, rendered, and removed.</p>
 */
public final class RopeSystem {
    private final Map<Long, RopeSimulation> ropes = new LinkedHashMap<>();
    private final Collection<RopeSimulation> ropesView = Collections.unmodifiableCollection(this.ropes.values());
    private long nextId = 1L;

    public long add(RopeSimulation rope) {
        Objects.requireNonNull(rope, "rope");
        long id = this.nextId++;
        this.ropes.put(id, rope);
        return id;
    }

    public RopeSimulation get(long id) {
        return this.ropes.get(id);
    }

    public RopeSimulation remove(long id) {
        return this.ropes.remove(id);
    }

    public Collection<RopeSimulation> ropes() {
        return this.ropesView;
    }

    public void tick(double seconds) {
        for (RopeSimulation rope : this.ropes.values()) {
            rope.tick(seconds);
        }
    }

    public void clear() {
        this.ropes.clear();
    }

    public int size() {
        return this.ropes.size();
    }
}
