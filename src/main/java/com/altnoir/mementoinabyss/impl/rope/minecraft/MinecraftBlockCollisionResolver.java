package com.altnoir.mementoinabyss.impl.rope.minecraft;

import com.altnoir.mementoinabyss.impl.rope.MutableRopePoint;
import com.altnoir.mementoinabyss.impl.rope.RopeCollisionResolver;
import com.altnoir.mementoinabyss.impl.rope.RopeSimulation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One-way collision against Minecraft block shapes.
 *
 * <p>Nearby shape boxes are collected once per rope tick and reused by every
 * solver iteration. This is intentionally an approximation: rope nodes collide
 * as cubes, and blocks receive no force.</p>
 */
public final class MinecraftBlockCollisionResolver implements RopeCollisionResolver {
    private static final double CONTACT_EPSILON = 1.0E-4;
    private static final double NORMAL_TIE_EPSILON = 1.0 / 64.0;

    private final CollisionGetter level;
    private final double queryPadding;
    private final Set<Long> ignoredBlocks;
    private final List<AABB> colliders = new ArrayList<>();
    private final double[] sweepInterval = new double[2];

    public MinecraftBlockCollisionResolver(CollisionGetter level) {
        this(level, 1.0);
    }

    public MinecraftBlockCollisionResolver(CollisionGetter level, double queryPadding) {
        this(level, queryPadding, new BlockPos[0]);
    }

    /**
     * Creates a resolver that ignores collision shapes belonging to the given
     * blocks. This is useful for anchors embedded inside a solid connector.
     */
    public MinecraftBlockCollisionResolver(
            CollisionGetter level,
            double queryPadding,
            BlockPos... ignoredBlocks
    ) {
        this.level = Objects.requireNonNull(level, "level");
        if (!Double.isFinite(queryPadding) || queryPadding < 0.0) {
            throw new IllegalArgumentException("queryPadding must be finite and non-negative");
        }
        this.queryPadding = queryPadding;
        this.ignoredBlocks = new HashSet<>(ignoredBlocks.length);
        for (BlockPos ignoredBlock : ignoredBlocks) {
            this.ignoredBlocks.add(Objects.requireNonNull(ignoredBlock, "ignoredBlock").asLong());
        }
    }

    @Override
    public void prepare(RopeSimulation rope) {
        this.colliders.clear();
        if (rope.pointCount() == 0) {
            return;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < rope.pointCount(); i++) {
            minX = Math.min(minX, Math.min(rope.x(i), rope.interpolatedX(i, 0.0)));
            minY = Math.min(minY, Math.min(rope.y(i), rope.interpolatedY(i, 0.0)));
            minZ = Math.min(minZ, Math.min(rope.z(i), rope.interpolatedZ(i, 0.0)));
            maxX = Math.max(maxX, Math.max(rope.x(i), rope.interpolatedX(i, 0.0)));
            maxY = Math.max(maxY, Math.max(rope.y(i), rope.interpolatedY(i, 0.0)));
            maxZ = Math.max(maxZ, Math.max(rope.z(i), rope.interpolatedZ(i, 0.0)));
        }

        double inflation = rope.parameters().collisionRadius() + this.queryPadding;
        AABB query = new AABB(
                minX - inflation, minY - inflation, minZ - inflation,
                maxX + inflation, maxY + inflation, maxZ + inflation
        );
        for (VoxelShape shape : this.level.getBlockCollisions(null, query)) {
            for (AABB collider : shape.toAabbs()) {
                if (!this.belongsToIgnoredBlock(collider)) {
                    this.colliders.add(collider);
                }
            }
        }
    }

    private boolean belongsToIgnoredBlock(AABB collider) {
        if (this.ignoredBlocks.isEmpty()) {
            return false;
        }
        double centerX = (collider.minX + collider.maxX) * 0.5;
        double centerY = (collider.minY + collider.maxY) * 0.5;
        double centerZ = (collider.minZ + collider.maxZ) * 0.5;
        return this.ignoredBlocks.contains(BlockPos.containing(centerX, centerY, centerZ).asLong());
    }

    @Override
    public void resolve(MutableRopePoint position, double radius) {
        double x = position.x();
        double y = position.y();
        double z = position.z();

        for (AABB collider : this.colliders) {
            double closestX = Math.clamp(x, collider.minX, collider.maxX);
            double closestY = Math.clamp(y, collider.minY, collider.maxY);
            double closestZ = Math.clamp(z, collider.minZ, collider.maxZ);
            double dx = x - closestX;
            double dy = y - closestY;
            double dz = z - closestZ;
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared >= radius * radius) {
                continue;
            }

            if (distanceSquared > 1.0E-12) {
                double distance = Math.sqrt(distanceSquared);
                double correction = (radius + CONTACT_EPSILON - distance) / distance;
                x += dx * correction;
                y += dy * correction;
                z += dz * correction;
            } else {
                // The center is on or inside the shape. Prefer the previous
                // axis when distances are nearly tied by requiring a
                // meaningful improvement before changing axis.
                double correction = collider.minX - radius - x - CONTACT_EPSILON;
                int axis = 0;
                double candidate = collider.maxX + radius - x + CONTACT_EPSILON;
                if (betterCorrection(candidate, correction)) {
                    correction = candidate;
                }
                candidate = collider.minY - radius - y - CONTACT_EPSILON;
                if (betterCorrection(candidate, correction)) {
                    correction = candidate;
                    axis = 1;
                }
                candidate = collider.maxY + radius - y + CONTACT_EPSILON;
                if (betterCorrection(candidate, correction)) {
                    correction = candidate;
                    axis = 1;
                }
                candidate = collider.minZ - radius - z - CONTACT_EPSILON;
                if (betterCorrection(candidate, correction)) {
                    correction = candidate;
                    axis = 2;
                }
                candidate = collider.maxZ + radius - z + CONTACT_EPSILON;
                if (betterCorrection(candidate, correction)) {
                    correction = candidate;
                    axis = 2;
                }

                if (axis == 0) {
                    x += correction;
                } else if (axis == 1) {
                    y += correction;
                } else {
                    z += correction;
                }
            }
        }

        position.set(x, y, z);
    }

    private static boolean betterCorrection(double candidate, double current) {
        return Math.abs(candidate) + NORMAL_TIE_EPSILON < Math.abs(current);
    }

    @Override
    public void resolve(
            MutableRopePoint position,
            MutableRopePoint previousPosition,
            double radius
    ) {
        double startX = previousPosition.x();
        double startY = previousPosition.y();
        double startZ = previousPosition.z();
        double dx = position.x() - startX;
        double dy = position.y() - startY;
        double dz = position.z() - startZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double earliest = Double.POSITIVE_INFINITY;
        int sampleCount = Math.clamp(
                (int) Math.ceil(length / Math.max(radius * 0.4, 1.0E-3)),
                1,
                128
        );
        for (AABB collider : this.colliders) {
            AABB inflated = collider.inflate(radius);
            if (this.intersectionFraction(
                    inflated, startX, startY, startZ, dx, dy, dz)
                    == Double.POSITIVE_INFINITY) {
                continue;
            }
            double hit = this.sweptSphereIntersection(
                    collider, startX, startY, startZ,
                    dx, dy, dz, radius, sampleCount);
            if (hit < earliest) {
                earliest = hit;
            }
        }
        if (earliest != Double.POSITIVE_INFINITY) {
            if (length > 1.0E-9) {
                earliest = Math.max(0.0, earliest - CONTACT_EPSILON / length);
            }
            position.set(
                    startX + dx * earliest,
                    startY + dy * earliest,
                    startZ + dz * earliest
            );
        }
        this.resolve(position, radius);
    }

    private double sweptSphereIntersection(
            AABB collider,
            double startX,
            double startY,
            double startZ,
            double dx,
            double dy,
            double dz,
            double radius,
            int sampleCount
    ) {
        double outside = 0.0;
        for (int sample = 1; sample <= sampleCount; sample++) {
            double fraction = (double) sample / sampleCount;
            double x = startX + dx * fraction;
            double y = startY + dy * fraction;
            double z = startZ + dz * fraction;
            if (!sphereIntersects(collider, x, y, z, radius)) {
                outside = fraction;
                continue;
            }

            double inside = fraction;
            for (int iteration = 0; iteration < 10; iteration++) {
                double middle = (outside + inside) * 0.5;
                if (sphereIntersects(
                        collider,
                        startX + dx * middle,
                        startY + dy * middle,
                        startZ + dz * middle,
                        radius)) {
                    inside = middle;
                } else {
                    outside = middle;
                }
            }
            return outside;
        }
        return Double.POSITIVE_INFINITY;
    }

    private static boolean sphereIntersects(
            AABB collider,
            double x,
            double y,
            double z,
            double radius
    ) {
        double closestX = Math.clamp(x, collider.minX, collider.maxX);
        double closestY = Math.clamp(y, collider.minY, collider.maxY);
        double closestZ = Math.clamp(z, collider.minZ, collider.maxZ);
        double dx = x - closestX;
        double dy = y - closestY;
        double dz = z - closestZ;
        double penetratingRadius = Math.max(0.0, radius - CONTACT_EPSILON);
        return dx * dx + dy * dy + dz * dz
                < penetratingRadius * penetratingRadius;
    }

    private double intersectionFraction(
            AABB box,
            double startX,
            double startY,
            double startZ,
            double dx,
            double dy,
            double dz
    ) {
        this.sweepInterval[0] = 0.0;
        this.sweepInterval[1] = 1.0;
        return clipAxis(startX, dx, box.minX, box.maxX, this.sweepInterval)
                && clipAxis(startY, dy, box.minY, box.maxY, this.sweepInterval)
                && clipAxis(startZ, dz, box.minZ, box.maxZ, this.sweepInterval)
                ? this.sweepInterval[0]
                : Double.POSITIVE_INFINITY;
    }

    private static boolean clipAxis(
            double start,
            double delta,
            double boxMinimum,
            double boxMaximum,
            double[] interval
    ) {
        if (Math.abs(delta) < 1.0E-12) {
            return start >= boxMinimum && start <= boxMaximum;
        }
        double first = (boxMinimum - start) / delta;
        double second = (boxMaximum - start) / delta;
        if (first > second) {
            double swap = first;
            first = second;
            second = swap;
        }
        interval[0] = Math.max(interval[0], first);
        interval[1] = Math.min(interval[1], second);
        return interval[0] <= interval[1];
    }
}
