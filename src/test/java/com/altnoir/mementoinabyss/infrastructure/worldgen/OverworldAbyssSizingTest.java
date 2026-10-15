package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import org.junit.jupiter.api.Test;

class OverworldAbyssSizingTest {
    @Test
    void expandedRadiusDrivesAllOverworldBoundaries() {
        int radius = OverworldAbyss.overworldRadius(160);
        var shape = new OverworldAbyssShape(radius, -64, 320, 1, 1, 1.25);
        assertEquals(320, radius);
        assertEquals(640, radius * 2);
        assertEquals(1040, shape.outerLimit());
        assertTrue(OverworldAbyss.withinConnectionRange(640, 0, radius));
        assertTrue(OverworldAbyss.withinConnectionRange(-384, 512, radius));
        assertFalse(OverworldAbyss.withinConnectionRange(640.01, 0, radius));
        assertFalse(OverworldAbyss.withinConnectionRange(800, 800, radius));
        assertFalse(OverworldAbyss.withinConnectionRange(Double.NaN, 0, radius));
        assertFalse(OverworldAbyss.withinConnectionRange(Double.POSITIVE_INFINITY, 0, radius));
    }

    @Test
    void fullConfigRangeAndOddRadiiRemainRepresentable() {
        assertEquals(322, OverworldAbyss.overworldRadius(161));
        assertEquals(128, OverworldAbyss.overworldRadius(64));
        assertDoesNotThrow(
                () ->
                        new OverworldAbyssShape(
                                OverworldAbyss.overworldRadius(10240), -64, 320, 1, 1, 1.25));
    }
}
