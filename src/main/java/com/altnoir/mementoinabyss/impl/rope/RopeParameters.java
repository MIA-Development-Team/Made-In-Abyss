package com.altnoir.mementoinabyss.impl.rope;

/**
 * Immutable tuning parameters for an XPBD rope.
 *
 * @param segmentLength       target distance between adjacent nodes
 * @param collisionRadius     radius presented to the collision resolver
 * @param gravityX            acceleration on the X axis
 * @param gravityY            acceleration on the Y axis
 * @param gravityZ            acceleration on the Z axis
 * @param velocityRetention   fraction of velocity retained per second
 * @param compliance          inverse distance-constraint stiffness; zero is rigid
 * @param substeps            integration substeps per tick
 * @param constraintIterations constraint passes per substep
 */
public record RopeParameters(
        double segmentLength,
        double collisionRadius,
        double gravityX,
        double gravityY,
        double gravityZ,
        double velocityRetention,
        double compliance,
        int substeps,
        int constraintIterations
) {
    public static final RopeParameters DEFAULT = new RopeParameters(
            1.0,
            0.0625,
            0.0,
            -9.81,
            0.0,
            0.96,
            0.0,
            2,
            8
    );

    public RopeParameters {
        if (!Double.isFinite(segmentLength) || segmentLength <= 0.0) {
            throw new IllegalArgumentException("segmentLength must be finite and positive");
        }
        if (!Double.isFinite(collisionRadius) || collisionRadius < 0.0) {
            throw new IllegalArgumentException("collisionRadius must be finite and non-negative");
        }
        if (!Double.isFinite(gravityX) || !Double.isFinite(gravityY) || !Double.isFinite(gravityZ)) {
            throw new IllegalArgumentException("gravity must be finite");
        }
        if (!Double.isFinite(velocityRetention) || velocityRetention < 0.0 || velocityRetention > 1.0) {
            throw new IllegalArgumentException("velocityRetention must be in [0, 1]");
        }
        if (!Double.isFinite(compliance) || compliance < 0.0) {
            throw new IllegalArgumentException("compliance must be finite and non-negative");
        }
        if (substeps < 1) {
            throw new IllegalArgumentException("substeps must be at least one");
        }
        if (constraintIterations < 1) {
            throw new IllegalArgumentException("constraintIterations must be at least one");
        }
    }

    public RopeParameters withSegmentLength(double value) {
        return new RopeParameters(value, this.collisionRadius, this.gravityX, this.gravityY, this.gravityZ,
                this.velocityRetention, this.compliance, this.substeps, this.constraintIterations);
    }

    public RopeParameters withCollisionRadius(double value) {
        return new RopeParameters(this.segmentLength, value, this.gravityX, this.gravityY, this.gravityZ,
                this.velocityRetention, this.compliance, this.substeps, this.constraintIterations);
    }

    public RopeParameters withGravity(double x, double y, double z) {
        return new RopeParameters(this.segmentLength, this.collisionRadius, x, y, z,
                this.velocityRetention, this.compliance, this.substeps, this.constraintIterations);
    }

    public RopeParameters withSolver(int substeps, int constraintIterations) {
        return new RopeParameters(this.segmentLength, this.collisionRadius,
                this.gravityX, this.gravityY, this.gravityZ,
                this.velocityRetention, this.compliance, substeps, constraintIterations);
    }
}
