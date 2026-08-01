package com.altnoir.mementoinabyss.impl.rope;

import java.util.Arrays;
import java.util.Objects;

/**
 * Pure Java, allocation-free-per-tick XPBD rope simulation.
 *
 * <p>Either, both, or neither endpoint may be attached. Attachments are
 * one-way rigid constraints: the endpoint follows the anchor, while the rope
 * never modifies the attached object.</p>
 */
public final class RopeSimulation {
    private static final double MIN_DISTANCE_SQUARED = 1.0E-12;
    private static final double MAX_TICK_SECONDS = 0.1;

    private final RopeParameters parameters;
    private final int pointCount;
    private final double[] x;
    private final double[] y;
    private final double[] z;
    private final double[] previousX;
    private final double[] previousY;
    private final double[] previousZ;
    private final double[] renderPreviousX;
    private final double[] renderPreviousY;
    private final double[] renderPreviousZ;
    private final double[] inverseMass;
    private final double[] restLength;
    private final double[] lambda;
    private final boolean[] collisionContact;
    private final MutableRopePoint anchorScratch = new MutableRopePoint();
    private final MutableRopePoint collisionScratch = new MutableRopePoint();
    private final MutableRopePoint collisionPreviousScratch = new MutableRopePoint();

    private RopeAnchor startAnchor;
    private RopeAnchor endAnchor;
    private int temporaryAnchorPoint = -1;
    private RopeAnchor temporaryAnchor;
    private RopeCollisionResolver collisionResolver = RopeCollisionResolver.NONE;

    private RopeSimulation(
            double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ,
            double ropeLength,
            RopeParameters parameters
    ) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (!Double.isFinite(ropeLength) || ropeLength <= 0.0) {
            throw new IllegalArgumentException("ropeLength must be finite and positive");
        }
        if (ropeLength + 1.0E-9 < distance) {
            throw new IllegalArgumentException("ropeLength cannot be shorter than the endpoint distance");
        }
        this.pointCount = Math.max(2, (int) Math.ceil(ropeLength / parameters.segmentLength()) + 1);

        this.x = new double[this.pointCount];
        this.y = new double[this.pointCount];
        this.z = new double[this.pointCount];
        this.previousX = new double[this.pointCount];
        this.previousY = new double[this.pointCount];
        this.previousZ = new double[this.pointCount];
        this.renderPreviousX = new double[this.pointCount];
        this.renderPreviousY = new double[this.pointCount];
        this.renderPreviousZ = new double[this.pointCount];
        this.inverseMass = new double[this.pointCount];
        this.restLength = new double[this.pointCount - 1];
        this.lambda = new double[this.pointCount - 1];
        this.collisionContact = new boolean[this.pointCount];

        Arrays.fill(this.inverseMass, 1.0);
        for (int i = 0; i < this.pointCount; i++) {
            double fraction = (double) i / (this.pointCount - 1);
            this.x[i] = startX + dx * fraction;
            this.y[i] = startY + dy * fraction;
            this.z[i] = startZ + dz * fraction;
        }
        System.arraycopy(this.x, 0, this.previousX, 0, this.pointCount);
        System.arraycopy(this.y, 0, this.previousY, 0, this.pointCount);
        System.arraycopy(this.z, 0, this.previousZ, 0, this.pointCount);
        System.arraycopy(this.x, 0, this.renderPreviousX, 0, this.pointCount);
        System.arraycopy(this.y, 0, this.renderPreviousY, 0, this.pointCount);
        System.arraycopy(this.z, 0, this.renderPreviousZ, 0, this.pointCount);

        Arrays.fill(this.restLength, ropeLength / (this.pointCount - 1));
    }

    /**
     * Creates a straight rope. Anchors are attached separately so this factory
     * also covers ropes with one free end.
     */
    public static RopeSimulation straight(
            double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ,
            RopeParameters parameters
    ) {
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return new RopeSimulation(startX, startY, startZ, endX, endY, endZ,
                Math.max(distance, parameters.segmentLength()), parameters);
    }

    /**
     * Creates a rope with an explicit total rest length. A length greater than
     * the endpoint distance produces slack and allows a two-anchor rope to sag.
     */
    public static RopeSimulation withLength(
            double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ,
            double ropeLength,
            RopeParameters parameters
    ) {
        return new RopeSimulation(startX, startY, startZ, endX, endY, endZ, ropeLength, parameters);
    }

    /**
     * Restores a rope from a synchronized node snapshot.
     */
    public static RopeSimulation fromPoints(
            double[] points,
            double ropeLength,
            RopeParameters parameters
    ) {
        Objects.requireNonNull(points, "points");
        if (points.length < 6 || points.length % 3 != 0) {
            throw new IllegalArgumentException("points must contain at least two xyz triples");
        }
        int last = points.length - 3;
        RopeSimulation rope = new RopeSimulation(
                points[0], points[1], points[2],
                points[last], points[last + 1], points[last + 2],
                ropeLength, parameters
        );
        if (rope.pointCount * 3 != points.length) {
            throw new IllegalArgumentException("point count does not match rope length");
        }
        for (int point = 0; point < rope.pointCount; point++) {
            int offset = point * 3;
            double x = points[offset];
            double y = points[offset + 1];
            double z = points[offset + 2];
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("points must be finite");
            }
            rope.x[point] = x;
            rope.y[point] = y;
            rope.z[point] = z;
            rope.previousX[point] = x;
            rope.previousY[point] = y;
            rope.previousZ[point] = z;
            rope.renderPreviousX[point] = x;
            rope.renderPreviousY[point] = y;
            rope.renderPreviousZ[point] = z;
        }
        return rope;
    }

    public RopeSimulation attachStart(RopeAnchor anchor) {
        this.startAnchor = Objects.requireNonNull(anchor, "anchor");
        this.pinAnchor(0, anchor);
        return this;
    }

    public RopeSimulation attachEnd(RopeAnchor anchor) {
        this.endAnchor = Objects.requireNonNull(anchor, "anchor");
        this.pinAnchor(this.pointCount - 1, anchor);
        return this;
    }

    public RopeSimulation detachStart() {
        this.startAnchor = null;
        return this;
    }

    public RopeSimulation detachEnd() {
        this.endAnchor = null;
        return this;
    }

    public RopeSimulation attachPoint(int point, RopeAnchor anchor) {
        this.checkPoint(point);
        this.temporaryAnchorPoint = point;
        this.temporaryAnchor = Objects.requireNonNull(anchor, "anchor");
        this.pinAnchor(point, anchor);
        return this;
    }

    public RopeSimulation detachPoint(int point) {
        if (this.temporaryAnchorPoint == point) {
            this.temporaryAnchorPoint = -1;
            this.temporaryAnchor = null;
        }
        return this;
    }

    public boolean hasStartAnchor() {
        return this.startAnchor != null;
    }

    public boolean hasEndAnchor() {
        return this.endAnchor != null;
    }

    public RopeSimulation collisionResolver(RopeCollisionResolver resolver) {
        this.collisionResolver = Objects.requireNonNull(resolver, "resolver");
        return this;
    }

    /**
     * Advances the simulation. Values above 100 ms are clamped to keep a
     * stalled game tick from destabilizing the rope.
     */
    public void tick(double seconds) {
        if (!Double.isFinite(seconds) || seconds <= 0.0) {
            return;
        }

        System.arraycopy(this.x, 0, this.renderPreviousX, 0, this.pointCount);
        System.arraycopy(this.y, 0, this.renderPreviousY, 0, this.pointCount);
        System.arraycopy(this.z, 0, this.renderPreviousZ, 0, this.pointCount);

        double tickSeconds = Math.min(seconds, MAX_TICK_SECONDS);
        double stepSeconds = tickSeconds / this.parameters.substeps();
        double stepSquared = stepSeconds * stepSeconds;
        double velocityScale = Math.pow(this.parameters.velocityRetention(), stepSeconds);

        this.pinAnchors();
        this.collisionResolver.prepare(this);
        for (int substep = 0; substep < this.parameters.substeps(); substep++) {
            Arrays.fill(this.collisionContact, false);
            this.pinAnchors();
            this.integrate(stepSquared, velocityScale);
            Arrays.fill(this.lambda, 0.0);

            for (int iteration = 0; iteration < this.parameters.constraintIterations(); iteration++) {
                this.solveDistanceConstraints(stepSquared);
                if ((iteration & 3) == 3
                        || iteration == this.parameters.constraintIterations() - 1) {
                    this.resolveCollisions();
                    this.resolveSegmentCollisions();
                }
                this.pinAnchors();
            }
            // Finish on distance constraints so collision corrections cannot
            // leave a visibly stretched one-block segment.
            for (int rigidityPass = 0; rigidityPass < 8; rigidityPass++) {
                this.pinAnchors();
                this.solveDistanceConstraints(stepSquared);
            }
            // Rigidity corrections after an impact can otherwise become
            // artificial Verlet velocity on the next substep. End outside
            // colliders, then keep contacted nodes at rest for this substep.
            this.resolveCollisions();
            this.resolveSegmentCollisions();
            this.pinAnchors();
            this.stabilizeCollisionContacts();
        }
    }

    private void stabilizeCollisionContacts() {
        for (int point = 0; point < this.pointCount; point++) {
            if (!this.collisionContact[point] || this.isAnchoredPoint(point)) {
                continue;
            }
            this.previousX[point] = this.x[point];
            this.previousY[point] = this.y[point];
            this.previousZ[point] = this.z[point];
        }
    }

    private void integrate(double stepSquared, double velocityScale) {
        for (int i = 0; i < this.pointCount; i++) {
            if (this.isAnchoredPoint(i)) {
                continue;
            }

            double currentX = this.x[i];
            double currentY = this.y[i];
            double currentZ = this.z[i];
            double velocityX = (currentX - this.previousX[i]) * velocityScale;
            double velocityY = (currentY - this.previousY[i]) * velocityScale;
            double velocityZ = (currentZ - this.previousZ[i]) * velocityScale;

            this.previousX[i] = currentX;
            this.previousY[i] = currentY;
            this.previousZ[i] = currentZ;
            this.x[i] = currentX + velocityX + this.parameters.gravityX() * stepSquared;
            this.y[i] = currentY + velocityY + this.parameters.gravityY() * stepSquared;
            this.z[i] = currentZ + velocityZ + this.parameters.gravityZ() * stepSquared;
        }
    }

    private void solveDistanceConstraints(double stepSquared) {
        double alpha = this.parameters.compliance() / stepSquared;

        for (int segment = 0; segment < this.pointCount - 1; segment++) {
            int a = segment;
            int b = segment + 1;
            double dx = this.x[b] - this.x[a];
            double dy = this.y[b] - this.y[a];
            double dz = this.z[b] - this.z[a];
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared < MIN_DISTANCE_SQUARED) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);
            double weightA = this.isAnchoredPoint(a) ? 0.0 : this.inverseMass[a];
            double weightB = this.isAnchoredPoint(b) ? 0.0 : this.inverseMass[b];
            double denominator = weightA + weightB + alpha;
            if (denominator == 0.0) {
                continue;
            }

            double constraint = distance - this.restLength[segment];
            double deltaLambda = (-constraint - alpha * this.lambda[segment]) / denominator;
            this.lambda[segment] += deltaLambda;

            double scale = deltaLambda / distance;
            double correctionX = dx * scale;
            double correctionY = dy * scale;
            double correctionZ = dz * scale;
            this.x[a] -= correctionX * weightA;
            this.y[a] -= correctionY * weightA;
            this.z[a] -= correctionZ * weightA;
            this.x[b] += correctionX * weightB;
            this.y[b] += correctionY * weightB;
            this.z[b] += correctionZ * weightB;
        }
    }

    private void resolveCollisions() {
        if (this.collisionResolver == RopeCollisionResolver.NONE) {
            return;
        }

        for (int i = 0; i < this.pointCount; i++) {
            if (this.isAnchoredPoint(i)) {
                continue;
            }

            double oldX = this.x[i];
            double oldY = this.y[i];
            double oldZ = this.z[i];
            this.collisionScratch.set(oldX, oldY, oldZ);
            this.collisionPreviousScratch.set(
                    this.previousX[i], this.previousY[i], this.previousZ[i]);
            this.collisionResolver.resolve(
                    this.collisionScratch,
                    this.collisionPreviousScratch,
                    this.parameters.collisionRadius()
            );

            double correctionX = this.collisionScratch.x() - oldX;
            double correctionY = this.collisionScratch.y() - oldY;
            double correctionZ = this.collisionScratch.z() - oldZ;
            this.x[i] += correctionX;
            this.y[i] += correctionY;
            this.z[i] += correctionZ;
            if (correctionX != 0.0 || correctionY != 0.0 || correctionZ != 0.0) {
                this.collisionContact[i] = true;
                this.previousX[i] = this.x[i];
                this.previousY[i] = this.y[i];
                this.previousZ[i] = this.z[i];
            }
        }
    }

    /**
     * Nodes are one block apart, so node-only collision can still let the
     * rendered span cut through a block. Three interior samples constrain the
     * span as well without adding simulation nodes.
     */
    private void resolveSegmentCollisions() {
        if (this.collisionResolver == RopeCollisionResolver.NONE) {
            return;
        }
        for (int segment = 0; segment < this.pointCount - 1; segment++) {
            for (int sample = 1; sample <= 3; sample++) {
                double fraction = sample * 0.25;
                double inverseFraction = 1.0 - fraction;
                double sampleX = this.x[segment] * inverseFraction + this.x[segment + 1] * fraction;
                double sampleY = this.y[segment] * inverseFraction + this.y[segment + 1] * fraction;
                double sampleZ = this.z[segment] * inverseFraction + this.z[segment + 1] * fraction;
                this.collisionScratch.set(sampleX, sampleY, sampleZ);
                this.collisionResolver.resolve(
                        this.collisionScratch, this.parameters.collisionRadius());

                double correctionX = this.collisionScratch.x() - sampleX;
                double correctionY = this.collisionScratch.y() - sampleY;
                double correctionZ = this.collisionScratch.z() - sampleZ;
                double correctionLength = Math.sqrt(
                        correctionX * correctionX
                                + correctionY * correctionY
                                + correctionZ * correctionZ);
                double maximumCorrection = this.parameters.collisionRadius();
                if (correctionLength > maximumCorrection && correctionLength > 1.0E-12) {
                    double scale = maximumCorrection / correctionLength;
                    correctionX *= scale;
                    correctionY *= scale;
                    correctionZ *= scale;
                }
                double weightA = this.isAnchoredPoint(segment) ? 0.0 : inverseFraction;
                double weightB = this.isAnchoredPoint(segment + 1) ? 0.0 : fraction;
                double denominator = weightA * inverseFraction + weightB * fraction;
                if (denominator <= 0.0) {
                    continue;
                }
                double scaleA = weightA / denominator;
                double scaleB = weightB / denominator;
                this.applyCollisionCorrection(
                        segment, correctionX * scaleA, correctionY * scaleA, correctionZ * scaleA);
                this.applyCollisionCorrection(
                        segment + 1, correctionX * scaleB, correctionY * scaleB, correctionZ * scaleB);
            }
        }
    }

    private void applyCollisionCorrection(int point, double x, double y, double z) {
        if (this.isAnchoredPoint(point)) {
            return;
        }
        this.x[point] += x;
        this.y[point] += y;
        this.z[point] += z;
        if (x != 0.0 || y != 0.0 || z != 0.0) {
            this.collisionContact[point] = true;
            this.previousX[point] = this.x[point];
            this.previousY[point] = this.y[point];
            this.previousZ[point] = this.z[point];
        }
    }

    private void pinAnchors() {
        if (this.startAnchor != null) {
            this.pinAnchor(0, this.startAnchor);
        }
        if (this.endAnchor != null) {
            this.pinAnchor(this.pointCount - 1, this.endAnchor);
        }
        if (this.temporaryAnchor != null) {
            this.pinAnchor(this.temporaryAnchorPoint, this.temporaryAnchor);
        }
    }

    private void pinAnchor(int index, RopeAnchor anchor) {
        anchor.sample(this.anchorScratch);
        double anchorX = this.anchorScratch.x();
        double anchorY = this.anchorScratch.y();
        double anchorZ = this.anchorScratch.z();
        if (!Double.isFinite(anchorX) || !Double.isFinite(anchorY) || !Double.isFinite(anchorZ)) {
            throw new IllegalStateException("Rope anchor supplied a non-finite position");
        }
        this.x[index] = anchorX;
        this.y[index] = anchorY;
        this.z[index] = anchorZ;
        this.previousX[index] = anchorX;
        this.previousY[index] = anchorY;
        this.previousZ[index] = anchorZ;
    }

    private boolean isAnchoredPoint(int point) {
        return point == 0 && this.startAnchor != null
                || point == this.pointCount - 1 && this.endAnchor != null
                || point == this.temporaryAnchorPoint && this.temporaryAnchor != null;
    }

    public RopeParameters parameters() {
        return this.parameters;
    }

    public int pointCount() {
        return this.pointCount;
    }

    public double lengthToPoint(int point) {
        this.checkPoint(point);
        double length = 0.0;
        for (int segment = 0; segment < point; segment++) {
            length += this.restLength[segment];
        }
        return length;
    }

    public double x(int point) {
        this.checkPoint(point);
        return this.x[point];
    }

    public double y(int point) {
        this.checkPoint(point);
        return this.y[point];
    }

    public double z(int point) {
        this.checkPoint(point);
        return this.z[point];
    }

    /**
     * Sets a point's initial velocity in blocks per second. The Verlet history
     * is offset by one solver substep so the value is independent of substeps.
     */
    public void setVelocity(int point, double velocityX, double velocityY, double velocityZ) {
        this.checkPoint(point);
        if (!Double.isFinite(velocityX)
                || !Double.isFinite(velocityY)
                || !Double.isFinite(velocityZ)) {
            throw new IllegalArgumentException("velocity must be finite");
        }
        double substepSeconds = 1.0 / (20.0 * this.parameters.substeps());
        this.previousX[point] = this.x[point] - velocityX * substepSeconds;
        this.previousY[point] = this.y[point] - velocityY * substepSeconds;
        this.previousZ[point] = this.z[point] - velocityZ * substepSeconds;
    }

    public double[] copyPoints() {
        double[] points = new double[this.pointCount * 3];
        for (int point = 0; point < this.pointCount; point++) {
            int offset = point * 3;
            points[offset] = this.x[point];
            points[offset + 1] = this.y[point];
            points[offset + 2] = this.z[point];
        }
        return points;
    }

    public boolean applyPoints(double[] points, double blend) {
        return this.applyPoints(points, blend, -1);
    }

    public boolean applyPoints(double[] points, double blend, int ignoredPoint) {
        if (points == null || points.length != this.pointCount * 3 || !Double.isFinite(blend)) {
            return false;
        }
        double clampedBlend = Math.max(0.0, Math.min(1.0, blend));
        for (int point = 0; point < this.pointCount; point++) {
            int offset = point * 3;
            if (!Double.isFinite(points[offset])
                    || !Double.isFinite(points[offset + 1])
                    || !Double.isFinite(points[offset + 2])) {
                return false;
            }
        }
        for (int point = 0; point < this.pointCount; point++) {
            if (point == ignoredPoint) {
                continue;
            }
            int offset = point * 3;
            double correctionX = (points[offset] - this.x[point]) * clampedBlend;
            double correctionY = (points[offset + 1] - this.y[point]) * clampedBlend;
            double correctionZ = (points[offset + 2] - this.z[point]) * clampedBlend;
            this.x[point] += correctionX;
            this.y[point] += correctionY;
            this.z[point] += correctionZ;
            this.previousX[point] += correctionX;
            this.previousY[point] += correctionY;
            this.previousZ[point] += correctionZ;
            this.renderPreviousX[point] += correctionX;
            this.renderPreviousY[point] += correctionY;
            this.renderPreviousZ[point] += correctionZ;
        }
        return true;
    }

    public double interpolatedX(int point, double partialTick) {
        this.checkPoint(point);
        return lerp(this.renderPreviousX[point], this.x[point], partialTick);
    }

    public double interpolatedY(int point, double partialTick) {
        this.checkPoint(point);
        return lerp(this.renderPreviousY[point], this.y[point], partialTick);
    }

    public double interpolatedZ(int point, double partialTick) {
        this.checkPoint(point);
        return lerp(this.renderPreviousZ[point], this.z[point], partialTick);
    }

    public void setInverseMass(int point, double value) {
        this.checkPoint(point);
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException("inverse mass must be finite and non-negative");
        }
        this.inverseMass[point] = value;
    }

    private void checkPoint(int point) {
        if (point < 0 || point >= this.pointCount) {
            throw new IndexOutOfBoundsException(point);
        }
    }

    private static double lerp(double start, double end, double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return start + (end - start) * clamped;
    }
}
