package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class VerticalDimensionTeleporterTest {
    @Test
    void downwardEntryIncludesConnectionEdgeButRequiresCrossingBuildBoundary() {
        assertTrue(canEnter(200, -64.01, 0));
        assertTrue(canEnter(120, -65, 160));
        assertFalse(canEnter(200.01, -65, 0));
        assertFalse(canEnter(0, -64, 0));
        assertFalse(canEnter(0, -63, 0));
    }

    @Test
    void disabledFeatureAndOtherDimensionsRetainTheirVoidBehavior() {
        assertFalse(
                VerticalDimensionTeleporter.canEnterOverworldAbyss(
                        Level.OVERWORLD, false, 0, -65, 0, -64, 100));
        assertFalse(
                VerticalDimensionTeleporter.canEnterOverworldAbyss(
                        Level.NETHER, true, 0, -65, 0, -64, 100));
        assertFalse(
                VerticalDimensionTeleporter.canEnterOverworldAbyss(
                        MiaDimensions.THE_ABYSS_LEVEL, true, 0, -65, 0, -64, 100));
    }

    @Test
    void destinationInvertsDefaultLodShiftAndPreservesFractionalFallDistance() {
        assertEquals(383.75, VerticalDimensionTeleporter.abyssDestinationY(-64.25, -64, 384));
        assertEquals(378.5, VerticalDimensionTeleporter.abyssDestinationY(-69.5, -64, 384));
    }

    @Test
    void destinationUsesActualDimensionBounds() {
        assertEquals(509.5, VerticalDimensionTeleporter.abyssDestinationY(-130.5, -128, 512));
    }

    @Test
    void upwardReturnUsesExactInverseCoordinatesAndExclusiveTopBoundary() {
        assertFalse(
                VerticalDimensionTeleporter.canReturnFromAbyss(
                        MiaDimensions.THE_ABYSS_LEVEL, 383.99, 384));
        assertTrue(
                VerticalDimensionTeleporter.canReturnFromAbyss(
                        MiaDimensions.THE_ABYSS_LEVEL, 384, 384));
        assertFalse(VerticalDimensionTeleporter.canReturnFromAbyss(Level.NETHER, 500, 384));
        assertEquals(-64, VerticalDimensionTeleporter.overworldDestinationY(384, 384, -64));
        assertEquals(-58.5, VerticalDimensionTeleporter.overworldDestinationY(389.5, 384, -64));
        double original = -69.5;
        double abyss = VerticalDimensionTeleporter.abyssDestinationY(original, -64, 384);
        assertEquals(original, VerticalDimensionTeleporter.overworldDestinationY(abyss, 384, -64));
        assertEquals(-125.75, VerticalDimensionTeleporter.overworldDestinationY(514.25, 512, -128));
    }

    @Test
    void declaredCentralReturnInvertsTheLodShiftAndKeepsFractionalHeight() {
        assertEquals(
                -63.5,
                VerticalDimensionTeleporter.aboveDestinationY(
                        MiaDimensions.THE_ABYSS_LEVEL,
                        new VerticalBoundary(Level.OVERWORLD, 0),
                        384.5,
                        384,
                        -64));
        assertEquals(
                -248,
                VerticalDimensionTeleporter.aboveDestinationY(
                        MiaDimensions.GREAT_FAULT_LEVEL,
                        new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 8),
                        384,
                        384,
                        -256));
    }

    private static boolean canEnter(double x, double y, double z) {
        return VerticalDimensionTeleporter.canEnterOverworldAbyss(
                Level.OVERWORLD, true, x, y, z, -64, 100);
    }
}
