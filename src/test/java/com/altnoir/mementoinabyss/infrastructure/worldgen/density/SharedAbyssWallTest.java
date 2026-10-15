package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SharedAbyssWallTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static DensityFunction.FunctionContext at(int x, int y, int z) {
        return new DensityFunction.SinglePointContext(x, y, z);
    }

    @Test
    void canonicalPhaseMatchesExactlyAcrossDimensionsIncludingNegativeLatticeCells() {
        for (long seed : new long[] {0, 12345, -987654321L}) {
            var abyss = SharedAbyssWall.forAbyss(320, seed);
            var overworld = SharedAbyssWall.forOverworld(320, seed, 448);
            for (int y : new int[] {-65, -64, -63, -5, -4, -3, -1, 0, 1, 111, 112})
                for (int x : new int[] {-367, -345, -321, -5, -4, -3, 0, 3, 321, 345})
                    for (int z : new int[] {-347, -5, -4, -3, 0, 3, 344}) {
                        assertEquals(
                                abyss.boundaryRadius(at(x, y + 448, z)),
                                overworld.boundaryRadius(at(x, y, z)));
                        assertEquals(
                                abyss.compute(at(x, y + 448, z)), overworld.compute(at(x, y, z)));
                    }
        }
    }

    @Test
    void reliefIsBoundedAndSeedBindingIsImmutableAndIndependentOfCallOrder() {
        var placeholder = SharedAbyssWall.unbound(320);
        var a = placeholder.bind(12345);
        var b = placeholder.bind(-7);
        assertFalse(placeholder.bound());
        assertTrue(a.bound());
        assertSame(a, a.bind(12345));
        assertEquals(0, placeholder.seed());
        assertEquals(344, a.baseRadius());
        assertEquals(320, a.layoutRadius());
        var independent = SharedAbyssWall.forAbyss(320, 12345);
        boolean variation = false;
        for (int y = -257; y <= 448; y += 31)
            for (int x = -400; x <= 400; x += 29) {
                var point = at(x, y, 127);
                double radius = a.boundaryRadius(point);
                assertTrue(radius >= 320 && radius <= 368);
                assertTrue(a.compute(point) >= a.minValue() && a.compute(point) <= a.maxValue());
                variation |= radius != b.boundaryRadius(point);
                assertEquals(radius, independent.boundaryRadius(point));
            }
        assertTrue(variation);
        assertEquals(-5, a.compute(at(0, 383, 0)));
        assertEquals(6.25, a.compute(at(10000, 383, 0)));
    }

    @Test
    void parallelSamplingCannotContaminateOtherLatticeCellsOrSeeds() {
        var a = SharedAbyssWall.forAbyss(320, 12);
        var b = SharedAbyssWall.forAbyss(320, -56);
        double[] serial =
                IntStream.range(0, 512)
                        .mapToDouble(
                                i ->
                                        (i % 2 == 0 ? a : b)
                                                .compute(at(-400 + i, -257 + i, -351 + i % 31)))
                        .toArray();
        double[] parallel =
                IntStream.range(0, 512)
                        .parallel()
                        .mapToDouble(
                                i ->
                                        (i % 2 == 0 ? a : b)
                                                .compute(at(-400 + i, -257 + i, -351 + i % 31)))
                        .toArray();
        assertArrayEquals(serial, parallel);
    }

    @Test
    void registeredCodecPreservesSeedsPhaseAndPlaceholderBinding() {
        for (var wall :
                new SharedAbyssWall[] {
                    SharedAbyssWall.unbound(320),
                    SharedAbyssWall.forAbyss(320, -89),
                    SharedAbyssWall.forOverworld(320, 67, 448)
                }) {
            var json =
                    DensityFunction.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, wall).getOrThrow();
            var decoded =
                    (SharedAbyssWall)
                            DensityFunction.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            assertEquals(wall.bound(), decoded.bound());
            assertEquals(wall.seed(), decoded.seed());
            assertEquals(wall.phaseOffset(), decoded.phaseOffset());
            for (int y : new int[] {-65, -64, -1, 0, 383})
                assertEquals(wall.compute(at(-343, y, -17)), decoded.compute(at(-343, y, -17)));
            assertEquals(
                    wall.bind(123).compute(at(344, 383, -1)),
                    decoded.bind(123).compute(at(344, 383, -1)));
        }
    }

    @Test
    void generalHoleBoundsDescribeActualClampAndSurviveRegisteredCodec() {
        var general = new GeneralAbyssHole(24, 1);
        var json = DensityFunction.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, general).getOrThrow();
        var decoded = DensityFunction.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(-5, general.minValue());
        assertEquals(6.25, general.maxValue());
        for (int x : new int[] {0, 160, 320, 344, 400, 10000}) {
            double value = decoded.compute(at(x, 0, 0));
            assertTrue(value >= decoded.minValue() && value <= decoded.maxValue());
            assertEquals(general.compute(at(x, 0, 0)), value);
        }
    }

    @Test
    void completeRoofBandUsesExactSharedDensityEvenWithHugeOriginalDensity() {
        var wall = SharedAbyssWall.forAbyss(320, 123);
        var roof = AbyssSeamDensity.abyss(wall, DensityFunctions.constant(1e20), 384);
        for (int y = 368; y < 384; y++) {
            var point = at(344, y, 0);
            double t = Math.clamp(.64 * wall.compute(point), -1, 1);
            assertEquals(t / 2 - t * t * t / 24, roof.compute(point));
        }
        assertEquals(1e20, roof.compute(at(344, 335, 0)));
    }
}
