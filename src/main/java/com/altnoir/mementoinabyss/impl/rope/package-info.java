/**
 * General-purpose, pure Java rope simulation.
 *
 * <p>A typical two-anchor rope can be created as follows:</p>
 *
 * <pre>{@code
 * RopeParameters parameters = RopeParameters.DEFAULT;
 * RopeSimulation rope = RopeSimulation.withLength(
 *         start.x, start.y, start.z,
 *         end.x, end.y, end.z,
 *         12.0,
 *         parameters
 * );
 * rope.attachStart(out -> out.set(start.x, start.y, start.z));
 * rope.attachEnd(out -> out.set(end.x, end.y, end.z));
 * rope.tick(1.0 / 20.0);
 * }</pre>
 *
 * <p>Omit either attachment to leave that endpoint free. Anchors and collision
 * are deliberately one-way and never receive forces from the rope.</p>
 */
package com.altnoir.mementoinabyss.impl.rope;
