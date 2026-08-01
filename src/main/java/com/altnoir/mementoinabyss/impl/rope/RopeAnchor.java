package com.altnoir.mementoinabyss.impl.rope;

/**
 * Supplies the current world-space position of a rigid rope attachment.
 *
 * <p>The rope follows the supplied position but never applies force or movement
 * back to the attachment. Implementations may therefore read an entity, block
 * entity, projectile, or any other externally controlled object.</p>
 */
@FunctionalInterface
public interface RopeAnchor {
    /**
     * Writes the current attachment position into {@code destination}.
     */
    void sample(MutableRopePoint destination);

    /**
     * Creates an immovable attachment at a fixed world-space position.
     */
    static RopeAnchor fixed(double x, double y, double z) {
        return destination -> destination.set(x, y, z);
    }
}
