package com.altnoir.mementoinabyss.impl.rope;

/**
 * Optional one-way collision hook for a rope node.
 *
 * <p>The resolver may move {@code position} out of colliders. No impulse is
 * reported back to the collided object. Implementations should not retain the
 * mutable position supplied to this method.</p>
 */
@FunctionalInterface
public interface RopeCollisionResolver {
    RopeCollisionResolver NONE = (position, radius) -> {
    };

    /**
     * Called once before a rope tick. Resolvers may use this to cache nearby
     * collision geometry before the iterative solver starts.
     */
    default void prepare(RopeSimulation rope) {
    }

    void resolve(MutableRopePoint position, double radius);

    /**
     * Resolves a moving point. Implementations may use {@code previousPosition}
     * for swept collision detection; the default retains discrete behavior.
     */
    default void resolve(
            MutableRopePoint position,
            MutableRopePoint previousPosition,
            double radius
    ) {
        this.resolve(position, radius);
    }
}
