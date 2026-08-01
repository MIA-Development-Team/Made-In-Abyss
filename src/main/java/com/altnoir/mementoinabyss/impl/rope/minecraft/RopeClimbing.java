package com.altnoir.mementoinabyss.impl.rope.minecraft;

import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Applies ladder-like movement while a player bounding box overlaps a
 * simulated rope segment.
 */
public final class RopeClimbing {
    private static final double ROPE_CONTACT_RADIUS = 0.22;
    private static final double MAX_HORIZONTAL_SPEED = 0.15;
    private static final double MAX_FALL_SPEED = -0.15;
    private static final double CLIMB_SPEED = 0.2;

    public static void tick(
            Player player,
            Iterable<RopeConnectorBlockEntity> connectors,
            boolean jump,
            boolean shift
    ) {
        if (player.isSpectator()
                || player.getAbilities().flying
                || player.isPassenger()
                || !touchesRope(player, connectors)) {
            return;
        }

        player.resetFallDistance();
        Vec3 movement = player.getDeltaMovement();
        double vertical = Math.max(movement.y(), MAX_FALL_SPEED);
        if (jump) {
            vertical = CLIMB_SPEED;
        } else if (shift && vertical < 0.0) {
            vertical = 0.0;
        }
        player.setDeltaMovement(
                Math.clamp(movement.x(), -MAX_HORIZONTAL_SPEED, MAX_HORIZONTAL_SPEED),
                vertical,
                Math.clamp(movement.z(), -MAX_HORIZONTAL_SPEED, MAX_HORIZONTAL_SPEED)
        );
    }

    private static boolean touchesRope(
            Player player,
            Iterable<RopeConnectorBlockEntity> connectors
    ) {
        AABB contactBox = player.getBoundingBox().inflate(
                ROPE_CONTACT_RADIUS, 0.08, ROPE_CONTACT_RADIUS);
        for (RopeConnectorBlockEntity connector : connectors) {
            if (connector.isRemoved() || connector.getLevel() != player.level()) {
                continue;
            }
            RopeSimulation rope = connector.getActiveRope();
            if (rope == null) {
                continue;
            }
            for (int segment = 0; segment < rope.pointCount() - 1; segment++) {
                if (intersects(
                        contactBox,
                        rope.x(segment), rope.y(segment), rope.z(segment),
                        rope.x(segment + 1), rope.y(segment + 1), rope.z(segment + 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean intersects(
            AABB box,
            double ax, double ay, double az,
            double bx, double by, double bz
    ) {
        double minimum = 0.0;
        double maximum = 1.0;
        double[] start = {ax, ay, az};
        double[] delta = {bx - ax, by - ay, bz - az};
        double[] lower = {box.minX, box.minY, box.minZ};
        double[] upper = {box.maxX, box.maxY, box.maxZ};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(delta[axis]) < 1.0E-9) {
                if (start[axis] < lower[axis] || start[axis] > upper[axis]) {
                    return false;
                }
                continue;
            }
            double first = (lower[axis] - start[axis]) / delta[axis];
            double second = (upper[axis] - start[axis]) / delta[axis];
            if (first > second) {
                double swap = first;
                first = second;
                second = swap;
            }
            minimum = Math.max(minimum, first);
            maximum = Math.min(maximum, second);
            if (minimum > maximum) {
                return false;
            }
        }
        return true;
    }

    private RopeClimbing() {
    }
}
