package com.altnoir.mementoinabyss.client;

import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import com.altnoir.mementoinabyss.network.RopeNodeGrabPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Client input state for dragging a selected node of a single-ended rope.
 */
public final class RopeFreeEndGrabHandler {
    private static final double GRAB_REACH = 5.0;
    private static final double GRAB_RADIUS_SQUARED = 0.65 * 0.65;
    private static final double MAX_NODE_MOVE_PER_TICK = 0.2;

    private static BlockPos connectorPos;
    private static int grabbedPoint = -1;
    private static Vec3 grabbedPosition;
    private static boolean wasUseDown;

    public static void begin(RopeConnectorBlockEntity connector) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        int point = findLookedAtNode(connector, minecraft);
        RopeSimulation rope = connector.getClientRope();
        if (point < 1 || rope == null) {
            return;
        }
        connectorPos = connector.getBlockPos().immutable();
        grabbedPoint = point;
        grabbedPosition = new Vec3(rope.x(point), rope.y(point), rope.z(point));
        attach(connector);
        send(RopeNodeGrabPayload.Action.BEGIN, heldPosition(
                minecraft.player.getEyePosition(), minecraft.player.getLookAngle()));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clear();
            return;
        }
        boolean useDown = minecraft.options.keyUse.isDown();
        if (connectorPos == null) {
            if (useDown && !wasUseDown
                    && minecraft.screen == null
                    && minecraft.player.getMainHandItem().isEmpty()) {
                GrabSelection hit = findLookedAtFreeRope(minecraft);
                if (hit != null) {
                    begin(hit.connector, hit.point);
                }
            }
            wasUseDown = useDown;
            return;
        }
        if (!(minecraft.level.getBlockEntity(connectorPos) instanceof RopeConnectorBlockEntity connector)
                || !connector.hasFreeEnd()
                || connector.getClientRope() == null) {
            clear();
            return;
        }
        if (useDown) {
            Vec3 desired = heldPosition(
                    minecraft.player.getEyePosition(), minecraft.player.getLookAngle());
            Vec3 movement = desired.subtract(grabbedPosition);
            double distance = movement.length();
            Vec3 nextPosition = distance > MAX_NODE_MOVE_PER_TICK
                    ? grabbedPosition.add(movement.scale(MAX_NODE_MOVE_PER_TICK / distance))
                    : desired;
            grabbedPosition = connector.clampClientGrabPosition(grabbedPoint, nextPosition);
            if (minecraft.player.getEyePosition().distanceToSqr(grabbedPosition)
                    > GRAB_REACH * GRAB_REACH) {
                connector.releaseClientPoint(grabbedPoint);
                send(RopeNodeGrabPayload.Action.RELEASE, grabbedPosition);
                resetState();
                return;
            }
            attach(connector);
            send(RopeNodeGrabPayload.Action.UPDATE, desired);
            wasUseDown = true;
            return;
        }

        connector.releaseClientPoint(grabbedPoint);
        send(RopeNodeGrabPayload.Action.RELEASE, grabbedPosition);
        resetState();
    }

    public static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        if (connectorPos != null
                && minecraft.level != null
                && minecraft.level.getBlockEntity(connectorPos) instanceof RopeConnectorBlockEntity connector) {
            connector.releaseClientPoint(grabbedPoint);
        }
        resetState();
    }

    private static void begin(RopeConnectorBlockEntity connector, int point) {
        RopeSimulation rope = connector.getClientRope();
        if (rope == null || point < 1 || point >= rope.pointCount()) {
            return;
        }
        connectorPos = connector.getBlockPos().immutable();
        grabbedPoint = point;
        grabbedPosition = new Vec3(rope.x(point), rope.y(point), rope.z(point));
        attach(connector);
        Minecraft minecraft = Minecraft.getInstance();
        send(RopeNodeGrabPayload.Action.BEGIN, heldPosition(
                minecraft.player.getEyePosition(), minecraft.player.getLookAngle()));
    }

    private static void attach(RopeConnectorBlockEntity connector) {
        connector.grabClientPoint(grabbedPoint, out -> out.set(
                grabbedPosition.x(), grabbedPosition.y(), grabbedPosition.z()));
    }

    private static void send(RopeNodeGrabPayload.Action action, Vec3 target) {
        ClientPacketDistributor.sendToServer(
                new RopeNodeGrabPayload(connectorPos, grabbedPoint, action, target));
    }

    private static void resetState() {
        connectorPos = null;
        grabbedPoint = -1;
        grabbedPosition = null;
        wasUseDown = false;
    }

    private static Vec3 heldPosition(Vec3 eyePosition, Vec3 lookDirection) {
        return eyePosition.add(lookDirection.scale(0.55)).add(0.0, -0.35, 0.0);
    }

    private static GrabSelection findLookedAtFreeRope(Minecraft minecraft) {
        GrabSelection closest = null;
        for (RopeConnectorBlockEntity connector : RopeConnectorBlockEntity.clientInstances()) {
            if (!connector.hasFreeEnd()
                    || connector.getClientRope() == null
                    || connector.getLevel() != minecraft.level) {
                continue;
            }
            int point = findLookedAtNode(connector, minecraft);
            if (point < 1) {
                continue;
            }
            RopeSimulation rope = connector.getClientRope();
            Vec3 node = new Vec3(rope.x(point), rope.y(point), rope.z(point));
            double distance = minecraft.player.getEyePosition().distanceToSqr(node);
            if (closest == null || distance < closest.distanceSquared) {
                closest = new GrabSelection(connector, point, distance);
            }
        }
        return closest;
    }

    private static int findLookedAtNode(
            RopeConnectorBlockEntity connector,
            Minecraft minecraft
    ) {
        RopeSimulation rope = connector.getClientRope();
        if (rope == null) {
            return -1;
        }
        Vec3 rayStart = minecraft.player.getEyePosition();
        Vec3 rayEnd = rayStart.add(minecraft.player.getLookAngle().scale(GRAB_REACH));
        int closestPoint = -1;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int point = 1; point < rope.pointCount(); point++) {
            Vec3 node = new Vec3(rope.x(point), rope.y(point), rope.z(point));
            double distance = pointSegmentDistanceSquared(node, rayStart, rayEnd);
            if (distance <= GRAB_RADIUS_SQUARED && distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }
        return closestPoint;
    }

    private static double pointSegmentDistanceSquared(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSquared = segment.lengthSqr();
        if (lengthSquared <= 1.0E-12) {
            return point.distanceToSqr(start);
        }
        double fraction = Math.clamp(point.subtract(start).dot(segment) / lengthSquared, 0.0, 1.0);
        return point.distanceToSqr(start.add(segment.scale(fraction)));
    }

    private record GrabSelection(
            RopeConnectorBlockEntity connector,
            int point,
            double distanceSquared
    ) {
    }

    private RopeFreeEndGrabHandler() {
    }
}
