package com.altnoir.mementoinabyss.impl.rope;

/**
 * Allocation-free mutable position used at the boundary of the rope API.
 */
public final class MutableRopePoint {
    private double x;
    private double y;
    private double z;

    public MutableRopePoint() {
    }

    public MutableRopePoint(double x, double y, double z) {
        this.set(x, y, z);
    }

    public MutableRopePoint set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public MutableRopePoint set(MutableRopePoint other) {
        return this.set(other.x, other.y, other.z);
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }
}
