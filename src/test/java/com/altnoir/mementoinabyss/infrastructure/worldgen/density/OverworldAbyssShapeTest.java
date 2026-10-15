package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OverworldAbyssShapeTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(10000, -64, 320, 12345L, 1.0, 1.25);

    @Test
    void innerRampKeepsShoulderAndCubicSteepeningBeforeRoundedCrest() {
        assertEquals(128, SHAPE.innerHeight(10000, 0));
        assertEquals(128, SHAPE.innerHeight(10024, 0));
        assertEquals(144, SHAPE.innerHeight(15012, 0));
        assertEquals(256, SHAPE.innerHeight(20000, 0));
        double previous = 128;
        for (int x = 10001; x <= 20000; x++) {
            double height = SHAPE.innerHeight(x, 0);
            assertTrue(height >= previous, "monotonic at " + x);
            assertTrue(height <= 256);
            previous = height;
        }
        double earlySlope = SHAPE.innerHeight(13001, 0) - SHAPE.innerHeight(13000, 0);
        double lateSlope = SHAPE.innerHeight(18001, 0) - SHAPE.innerHeight(18000, 0);
        assertTrue(lateSlope > earlySlope * 6);
        assertTrue(SHAPE.innerHeight(10001, 0) - 128 < 1.0E-6);
        assertTrue(256 - SHAPE.innerHeight(19999, 0) < 1.0E-4);
        int join = (int) Math.round(SHAPE.wallRadius() + 0.85 * (20000 - SHAPE.wallRadius()));
        double beforeJoin = SHAPE.innerHeight(join, 0) - SHAPE.innerHeight(join - 1, 0);
        double afterJoin = SHAPE.innerHeight(join + 1, 0) - SHAPE.innerHeight(join, 0);
        assertEquals(beforeJoin, afterJoin, 1.0E-4);
    }

    @Test
    void outerFallIsMonotonicAndTangentAtBothEndsForEveryExponent() {
        for (double exponent : new double[] {0.5, 1.0, 3.0}) {
            var shape = new OverworldAbyssShape(320, -64, 320, 1, exponent, 1.25);
            assertEquals(1, shape.outerWeight(640));
            assertEquals(0, shape.outerWeight(1040));
            double previous = 1;
            for (int distance = 641; distance <= 1040; distance++) {
                double weight = shape.outerWeight(distance);
                assertTrue(weight <= previous);
                assertTrue(weight >= 0);
                previous = weight;
            }
            double epsilon = 0.0001;
            assertTrue((1 - shape.outerWeight(640 + epsilon)) / epsilon < 1.0E-5);
            assertTrue(shape.outerWeight(1040 - epsilon) / epsilon < 1.0E-5);
        }
    }
}
