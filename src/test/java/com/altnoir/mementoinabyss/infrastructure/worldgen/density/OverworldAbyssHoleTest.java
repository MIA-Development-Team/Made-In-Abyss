package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.concurrent.Executors;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssHoleTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(160, -64, 320, 12345L, 1.0, 1.25);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static OverworldAbyssHole hole(DensityFunction input, boolean surface) {
        var original = new PlainHeight(70);
        return new OverworldAbyssHole(input, original, original, SHAPE, surface);
    }

    private static DensityFunction.FunctionContext at(int x, int y, int z) {
        return new DensityFunction.SinglePointContext(x, y, z);
    }

    @Test
    void centerIsOpenThroughWholeWorldEvenForSolidOriginalTerrain() {
        var hole = hole(DensityFunctions.constant(20), false);
        for (int y = -64; y < 320; y++) {
            assertEquals(-1, hole.compute(at(0, y, 0)));
            assertEquals(-1, hole.compute(at(160, y, 0)));
        }
        assertEquals(-65, hole(DensityFunctions.constant(20), true).compute(at(0, 0, 0)));
    }

    @Test
    void outsideIsExactlyOriginalIncludingNegativeCoordinatesAndWorldBorder() {
        DensityFunction input = new Plane(70);
        var hole = hole(input, false);
        for (int[] point :
                new int[][] {
                    {521, 0}, {-521, 0}, {0, 521}, {0, -521}, {368, 368}, {29999984, -29999984}
                }) {
            for (int y = -64; y < 320; y += 13) {
                assertEquals(
                        input.compute(at(point[0], y, point[1])),
                        hole.compute(at(point[0], y, point[1])));
                assertEquals(70, hole(input, true).compute(at(point[0], y, point[1])));
            }
        }
    }

    @Test
    void outerBoundaryDoesNotEvaluateSurfaceOrExtraNoise() {
        DensityFunction forbidden =
                new DensityFunction.SimpleFunction() {
                    public double compute(FunctionContext context) {
                        throw new AssertionError("Outside must not sample surface");
                    }

                    public double minValue() {
                        return 0;
                    }

                    public double maxValue() {
                        return 0;
                    }

                    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
                        return DensityFunctions.constant(0).codec();
                    }
                };
        assertEquals(
                0.125,
                new OverworldAbyssHole(
                                DensityFunctions.constant(0.125),
                                forbidden,
                                forbidden,
                                SHAPE,
                                false)
                        .compute(at(521, 5, 0)));
    }

    @Test
    void outerSlopeSurfaceBlendsBackToOriginalEstimate() {
        double baseline = 70.0;
        var surface = plain(70.0, true);
        var density = plain(70.0, false);
        // Up to two radii the reported surface is the funnel wall itself, so the report names the
        // block the carved density stops at and the ridge crest is not reached until the wall has
        // climbed all the way to it.
        for (int x = 161; x <= 318; x += 3) {
            double report = surface.compute(at(x, 0, -13));
            assertEquals((double) densityHeight(density, x, -13), report, 1.0E-9, "inner x=" + x);
            int solid = (int) report;
            if (solid > SHAPE.minY()) {
                assertTrue(density.compute(at(x, solid, -13)) > 0, "rock at inner x=" + x);
            }
            assertTrue(density.compute(at(x, solid + 1, -13)) <= 0, "air at inner x=" + x);
        }
        // Past the funnel the outer weight is below one, so the composed density is already the
        // blend
        // and the report has to name exactly the block that density stops at.
        for (int x = 320; x < 520; x += 7) {
            if (SHAPE.outerWeight(Math.hypot(x, -13)) <= 0.0) continue;
            double scan = densityHeight(density, x, -13);
            assertEquals((double) scan, surface.compute(at(x, 0, -13)), 1.0E-9, "outer x=" + x);
        }
        // ... and in the steep part of the outer band that mixture still stands above the plain
        // terrain, because the ridge it is fading towards is high and the descent is short.
        for (int x = 321; x <= 360; x += 13) {
            assertTrue(surface.compute(at(x, 0, 0)) > baseline, "outer band x=" + x);
        }
        // The rounded shoulder approaches the crest smoothly; adjacent heights stay within three
        // blocks.
        // The report
        // agrees with the density on both sides of the crest.
        assertEquals(surface.compute(at(320, 0, 0)), surface.compute(at(321, 0, 0)), 3.0);
        for (int x : new int[] {319, 320, 321, 322}) {
            double reported = surface.compute(at(x, 0, 0));
            assertEquals((double) densityHeight(density, x, 0), reported, 1.0E-9, "seam x=" + x);
            assertTrue(
                    density.compute(at(x, (int) reported, 0)) > 0, "rock at the report at x=" + x);
            assertTrue(
                    density.compute(at(x, (int) reported + 1, 0)) <= 0,
                    "air above the report at x=" + x);
        }
        for (int[] point : new int[][] {{520, 0}, {0, 520}, {-521, 0}, {368, 368}}) {
            assertFalse(SHAPE.affects(point[0], point[1]));
            assertEquals(baseline, surface.compute(at(point[0], 0, point[1])));
        }
    }

    /** An abyss hole over flat terrain, with a constant pre-carve estimate. */
    private static OverworldAbyssHole plain(double baseline, boolean surface) {
        return new OverworldAbyssHole(
                new Plane((int) baseline),
                DensityFunctions.constant(baseline),
                new PlainHeight((int) baseline),
                SHAPE,
                surface);
    }

    /** @return the highest block Y the composed density still reports as rock, or {@code -65} */
    private static int densityHeight(DensityFunction density, int x, int z) {
        for (int y = 319; y >= -64; y--) {
            if (density.compute(at(x, y, z)) > 0) return y;
        }
        return -65;
    }

    @Test
    void ridgeRangeAndInnerSurfaceMatchDensity() {
        var density = hole(new Plane(70), false);
        var surface = hole(new Plane(70), true);
        // Inside two radii the carve is the funnel wall itself: the wall's floor is the block the
        // density stops at and the block above it is open, all the way down to bedrock near the rim
        // where the wall has not yet climbed out of the floor.
        for (int x = 161; x <= 320; x++) {
            double h = surface.compute(at(x, 0, 0));
            assertEquals(
                    (double) densityHeight(density, x, 0),
                    h,
                    1.0E-9,
                    "the report is the top of the funnel wall at x=" + x);
            if (h > SHAPE.minY()) {
                assertTrue(density.compute(at(x, (int) h, 0)) > 0, "rock at the report at x=" + x);
            }
            assertTrue(
                    density.compute(at(x, (int) h + 1, 0)) <= 0, "air above the report at x=" + x);
        }
        for (int x = -320; x <= 320; x++) {
            double h = SHAPE.ridgeHeight(x, 100);
            assertEquals(256.0, h);
        }
    }

    @Test
    void outerSurfaceTracksComposedDensityForPlainsAndHighModdedMountains() {
        for (int baseline : new int[] {30, 70, 290}) {
            var original = new PlainHeight((int) baseline);
            var density =
                    new OverworldAbyssHole(
                            new Plane(baseline),
                            new PlainHeight((int) baseline),
                            original,
                            SHAPE,
                            false);
            var surface =
                    new OverworldAbyssHole(
                            new Plane(baseline),
                            new PlainHeight((int) baseline),
                            original,
                            SHAPE,
                            true);
            for (int x = 321; x < 520; x += 7) {
                int actual = -65;
                for (int y = 319; y >= -64; y--) {
                    if (density.compute(at(x, y, 0)) > 0) {
                        actual = y;
                        break;
                    }
                }
                // However modded the original field is, the report has to name the block the
                // composed
                // density stops at, so a consumer that reads the heightmap and a consumer that
                // reads
                // the density always agree.
                assertEquals(
                        (double) actual,
                        surface.compute(at(x, 0, 0)),
                        "x=" + x + ", baseline=" + baseline);
            }
        }
    }

    @Test
    void originalEstimateIsReadInsideOuterLimitAndIgnoredOutside() {
        var density = plain(70.0, false);
        var surface = plain(70.0, true);
        int previous = Integer.MAX_VALUE;
        for (int x : new int[] {400, 440, 480}) {
            double report = surface.compute(at(x, 0, 0));
            // The equality is the whole contract: blending two densities moves the zero crossing
            // away
            // from the height the blend names, so the report must be the scan rather than the
            // blend.
            assertEquals(
                    (double) densityHeight(density, x, 0),
                    report,
                    1.0E-9,
                    "the report is exactly the block the composed density stops at, x=" + x);
            assertTrue(
                    densityHeight(density, x, 0) < previous,
                    "the carved column descends monotonically outwards, x=" + x);
            previous = densityHeight(density, x, 0);
        }
        // At the very edge the blend has all but landed on the estimate it started from, so the
        // report is the plain's own boundary there; one block further out the carve is gone.
        for (int x : new int[] {519, 520}) {
            assertEquals(
                    70.0,
                    surface.compute(at(x, 0, 0)),
                    1.0E-9,
                    "at the limit the slope has already landed on the plain at x=" + x);
        }
        // ... and past the outer limit it returns it unchanged, without touching the carved graph
        // at
        // all.
        CountingEstimate estimate = new CountingEstimate(11);
        DensityFunction forbidden =
                new DensityFunction.SimpleFunction() {
                    public double compute(FunctionContext context) {
                        throw new AssertionError("Outside must not scan the abyss");
                    }

                    public double minValue() {
                        return 0;
                    }

                    public double maxValue() {
                        return 0;
                    }

                    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
                        return DensityFunctions.constant(0).codec();
                    }
                };
        var far = new OverworldAbyssHole(forbidden, forbidden, estimate, SHAPE, true);
        // The untouched estimate is read verbatim - only floored to a block - while the carved
        // graph
        // next to it is not sampled at all.
        assertEquals(11.0, far.compute(at(521, 0, 0)));
        assertEquals(11.0, far.compute(at(0, 0, -521)));
        assertEquals(11.0, far.compute(at(369, 0, 369)));
        assertEquals(3, estimate.calls);
    }

    /**
     * The composed graph may only differ from the original one where the abyss actually reaches.
     * Just inside the limit the carved values are still blended in; one block further out - and at
     * the outer circle itself, where the outer weight is exactly zero - every sample has to come
     * back bit-identical to the original field.
     */
    @Test
    void outsideTheOuterLimitIsBitIdenticalToTheOriginalGraph() {
        var surface =
                new OverworldAbyssHole(
                        new Plane(70),
                        DensityFunctions.constant(70),
                        new PlainHeight(70),
                        SHAPE,
                        true);
        for (int[] point :
                new int[][] {
                    {521, 0},
                    {0, 521},
                    {-521, 0},
                    {0, -521},
                    {368, 368},
                    {-368, -368},
                    {700, 0},
                    {1234, -5678}
                }) {
            for (int y : new int[] {-64, -63, 0, 63, 70, 71, 160, 255, 319}) {
                double reference = new Plane(70).compute(at(point[0], y, point[1]));
                assertEquals(
                        reference,
                        hole(new Plane(70), false).compute(at(point[0], y, point[1])),
                        "density at " + point[0] + "," + point[1]);
                // Past the outer limit the surface report hands over to the untouched estimate
                // field, which reports the block coordinate directly - the carved graph is not
                // touched at all.
                assertEquals(
                        70.0,
                        surface.compute(at(point[0], y, point[1])),
                        "surface at " + point[0] + "," + point[1]);
            }
        }
    }

    /**
     * Inverse of the same contract: the corner of the search grid that is nearest to the outer
     * circle is still carved. If it were not, no test above would be able to tell "not affected"
     * apart from "the outer weight accidentally collapsed to zero everywhere". The weight there is a
     * rounding crumb - the smoothstep leaves the band flat, so the blend reaches the plain value and
     * the carve at once rather than in a step.
     */
    @Test
    void justInsideTheOuterLimitTheCarveIsStillVisible() {
        var density = plain(70.0, false);
        var surface = plain(70.0, true);
        double weight = SHAPE.outerWeight(Math.hypot(519, 0));
        assertTrue(
                weight > 0.0 && weight < 1.0E-4,
                "the circle itself is the seam, x=519 is just inside it");
        // The compatibility graph blends the uncapped local field into the original density.
        // Just inside the edge its small nonzero contribution must still be observable.
        for (int y : new int[] {0, 69, 70, 160, 255}) {
            double unmodified = new Plane(70).compute(at(519, y, 0));
            double carved = density.compute(at(519, y, 0));
            double target =
                    Math.min(
                            (519.0 - SHAPE.radius()) / 16.0,
                            (SHAPE.innerHeight(519, 0) - y) / 24.0);
            assertEquals(
                    unmodified,
                    carved,
                    10.0 * weight + 1.0E-5,
                    "The contribution converges on vanilla at y=" + y);
            assertEquals(OverworldAbyssShape.lerp(weight, unmodified, target), carved, 1.0E-9);
            assertNotEquals(unmodified, carved, "The last inside column remains affected");
        }
        // The last carved column is rock up to the block the plain itself stops at, so losing the
        // carve here would not change the column - only the weight bound above would notice.
        assertTrue(density.compute(at(519, 69, 0)) > 0.0, "rock below the seam at x=519");
        assertEquals(70, densityHeight(density, 519, 0));
        // ... and the surface report names exactly that block, which is what kelp, geodes and
        // structures read.
        assertEquals(
                (double) densityHeight(density, 519, 0), surface.compute(at(519, 0, 0)), 1.0E-9);
    }

    /**
     * The outer radius is an exclusive bound: the block at the circle itself is still carved, one
     * block further out is not. Without this pair, a weight that silently rounded to zero would make
     * the conservation tests pass for the wrong reason.
     */
    @Test
    void theOuterCircleIsTheExactEdgeOfTheCarve() {
        var plane = new Plane(70);
        var density = hole(plane, false);
        assertTrue(SHAPE.affects(519, 0));
        assertFalse(SHAPE.affects(520, 0));
        // At the last carved column the slope has already reached the plain's own boundary, so the
        // two columns are the same column and the last stage of the descent is continuous.
        assertTrue(
                density.compute(at(519, 69, 0)) > 0.0,
                "the last carved column still holds the plain's lowest rock");
        assertEquals(
                plane.compute(at(520, 70, 0)),
                density.compute(at(520, 70, 0)),
                "the circle itself is untouched");
        assertEquals(70.0, hole(plane, true).compute(at(520, 0, 0)));
    }

    /**
     * The surface report must follow the composed terrain even when the original field is not a
     * single height, and it must find a solid layer that sits between the samples of a coarser walk.
     */
    @Test
    void surfaceScanFindsThinLayersInTheOuterBandAndFollowsTheComposedDensity() {
        var shaped = new Layered(100, 3);
        var estimate = new PlainHeight(69);
        var surface =
                new OverworldAbyssHole(
                        shaped, DensityFunctions.constant(70), estimate, SHAPE, true);
        var density =
                new OverworldAbyssHole(
                        shaped, DensityFunctions.constant(70), estimate, SHAPE, false);
        double weight = SHAPE.outerWeight(480);
        assertTrue(weight > 0.0 && weight < 1.0);
        // The scan has to find the layer: only three blocks of rock around y=100, with air both far
        // above and far below, which a walk that skipped blocks would step straight over.
        assertEquals(100, densityHeight(density, 480, 0));
        assertTrue(density.compute(at(480, 100, 0)) > 0);
        assertTrue(density.compute(at(480, 99, 0)) > 0);
        assertTrue(density.compute(at(480, 97, 0)) <= 0);
        assertTrue(density.compute(at(480, 103, 0)) <= 0);
        // The report follows the composed density, so the thin layer decides it even though the
        // blend that fades the slope down to the plain estimate would otherwise put the boundary
        // near the ridge.
        assertEquals(100.0, surface.compute(at(480, 0, 0)), 1.0E-9);
    }

    /**
     * The composed density has to be continuous where the carve hands the column back to the
     * untouched terrain: at the outer limit, and across the opening's rim where the funnel wall meets
     * the ring. Sampled one block either side of each seam, because a weight that reached past its
     * own circle would only show up there.
     */
    @Test
    void seamsAtRimAndOuterLimitAreContinuous() {
        var plane = new Plane(70);
        var density = hole(plane, false);
        for (int y : new int[] {-64, 64, 160, 240, 256}) {
            // The last carved column approaches the untouched terrain instead of equalling it: the
            // weight one block inside the limit is the top of the smoothstep, a crumb measured at
            // 1.0E-5 of density here. The tolerance is two orders above that and far below the
            // defects it has to catch, which land in the tens.
            assertEquals(
                    plane.compute(at(520, y, 0)),
                    density.compute(at(519, y, 0)),
                    1.0E-3,
                    "the terrain at the outer limit matches the last carved column");
            assertTrue(
                    Math.abs(density.compute(at(319, y, 0)) - density.compute(at(321, y, 0)))
                            < 0.5);
        }
    }

    @Test
    void arrayAndConcurrentSamplingAreDeterministic() throws Exception {
        var density = hole(new Plane(70), false);
        var provider =
                new DensityFunction.ContextProvider() {
                    public DensityFunction.FunctionContext forIndex(int i) {
                        return at(i * 17 - 700, i % 300 - 64, 0);
                    }

                    public void fillAllDirectly(double[] output, DensityFunction function) {
                        for (int i = 0; i < output.length; i++)
                            output[i] = function.compute(forIndex(i));
                    }
                };
        double[] values = new double[100];
        density.fillArray(values, provider);
        try (var executor = Executors.newFixedThreadPool(4)) {
            for (int i = values.length - 1; i >= 0; i--) {
                final int index = i;
                assertEquals(
                        values[i],
                        executor.submit(() -> density.compute(provider.forIndex(index))).get());
            }
        }
        // The nominal macro crest is seed-independent; seeded detail belongs to the vanilla noise
        // graph.
        assertEquals(
                SHAPE.ridgeHeight(320, 0),
                new OverworldAbyssShape(160, -64, 320, 12345, 1.0, 1.25).ridgeHeight(320, 0));
        assertEquals(
                SHAPE.ridgeHeight(320, 0),
                new OverworldAbyssShape(160, -64, 320, 54321, 1.0, 1.25).ridgeHeight(320, 0));
    }

    @Test
    void codecRoundTripAndVisitorPreserveChildGraph() {
        // Only registered density functions go in: the codec stores children as holders, so a
        // local test record whose codec() hands back some other function's codec - which is all a
        // test type can do - cannot round trip. Constants stand in for the carved graph and a
        // clamped gradient for the pre-carve height, so the round trip is the real one the mod
        // performs on a world preset.
        var input = DensityFunctions.constant(0.25);
        // A flat pre-carve field, deliberately neither the carved value nor one of the targets the
        // slope blends towards, so a branch that reached for the wrong child would show up as a
        // number no other child could have produced.
        var original = DensityFunctions.constant(0.5);
        var composed = new OverworldAbyssHole(input, original, original, SHAPE, false);
        var encoded =
                OverworldAbyssHole.CODEC
                        .codec()
                        .codec()
                        .encodeStart(JsonOps.INSTANCE, composed)
                        .getOrThrow();
        var decoded =
                OverworldAbyssHole.CODEC
                        .codec()
                        .codec()
                        .parse(JsonOps.INSTANCE, encoded)
                        .getOrThrow();
        for (int x : new int[] {0, 200, 320, 400, 520})
            assertEquals(composed.compute(at(x, 150, 0)), decoded.compute(at(x, 150, 0)));
        // The visitor has to reach the carved graph. The sample sits in the outer slope and well
        // above the wall, where the blend hands the carved value the weight of the terrain it is
        // fading toward - one minus the outer weight - so replacing the child moves the result by
        // that share of the difference and not by the whole difference: past the circle the report
        // skips the carved graph entirely, and the two are equal when the weight reaches one.
        var visited =
                composed.mapAll(
                        function -> function == input ? DensityFunctions.constant(0.75) : function);
        var repaired =
                composed.mapAll(
                        function -> function == input ? DensityFunctions.constant(0.25) : function);
        double carvedShareAtSample = 1.0 - SHAPE.outerWeight(SHAPE.distance(400, 0));
        assertEquals(
                composed.compute(at(400, 150, 0)) + 0.5 * carvedShareAtSample,
                visited.compute(at(400, 150, 0)),
                1.0E-6,
                "the carved value's own share decides how far replacing it moves the result");
        assertEquals(
                composed.compute(at(400, 150, 0)),
                repaired.compute(at(400, 150, 0)),
                1.0E-6,
                "a visitor that puts the same function back changes nothing");
        // Outside the limit the carved graph is handed back, replaced or not, and the pre-carve
        // field is never consulted: it is one of the three children, so a texture that leaked into
        // the outside would still show up here.
        assertEquals(0.25, composed.compute(at(800, 50, 0)));
        assertEquals(0.75, visited.compute(at(800, 50, 0)));
        assertEquals(-1.0, visited.compute(at(0, 50, 0)));
    }

    @Test
    void legacyUndersideFlagStillDecodesButDoesNotChangeUndergroundDensity() {
        var input = DensityFunctions.constant(12.0);
        var original = DensityFunctions.constant(70.0);
        var decoded = new java.util.ArrayList<OverworldAbyssHole>();
        for (boolean legacyFlag : new boolean[] {false, true}) {
            var legacy =
                    new OverworldAbyssHole(
                            input,
                            original,
                            original,
                            DensityFunctions.zero(),
                            SHAPE,
                            false,
                            false,
                            DensityFunctions.zero(),
                            legacyFlag);
            var json =
                    OverworldAbyssHole.CODEC
                            .codec()
                            .codec()
                            .encodeStart(JsonOps.INSTANCE, legacy)
                            .getOrThrow()
                            .getAsJsonObject();
            // Presets written by either old and new writers may omit the default false value.
            // Supplying both forms here covers explicit legacy data and the current default.
            json.addProperty("sculpt_underside", legacyFlag);
            var restored =
                    OverworldAbyssHole.CODEC
                            .codec()
                            .codec()
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow();
            assertEquals(legacyFlag, restored.sculptUnderside());
            decoded.add(restored);
        }

        for (int x : new int[] {321, 400, 640})
            for (int y = -64; y < 0; y++)
                assertEquals(
                        decoded.get(0).compute(at(x, y, 0)),
                        decoded.get(1).compute(at(x, y, 0)),
                        "Legacy sculpt_underside value changed decoded underground density at x="
                                + x
                                + ", y="
                                + y);
    }

    @Test
    void declaredBoundsContainSamples() {
        for (boolean surface : new boolean[] {false, true}) {
            var hole = hole(DensityFunctions.constant(30), surface);
            for (int x = -700; x <= 700; x += 13) {
                for (int y = -64; y <= 320; y += 29) {
                    double value = hole.compute(at(x, y, 17));
                    assertTrue(value >= hole.minValue() && value <= hole.maxValue());
                }
            }
        }
    }

    @Test
    void aquiferKeepsVoidDryAndRestoresDelegateOutside() {
        Aquifer delegate =
                new Aquifer() {
                    public BlockState computeSubstance(
                            DensityFunction.FunctionContext context, double density) {
                        return density > 0 ? null : Blocks.LAVA.defaultBlockState();
                    }

                    public boolean shouldScheduleFluidUpdate() {
                        return true;
                    }
                };
        var aquifer = new AbyssAquifer(delegate, SHAPE);
        assertTrue(aquifer.computeSubstance(at(0, -64, 0), 100).isAir());
        assertFalse(aquifer.shouldScheduleFluidUpdate());
        assertTrue(aquifer.computeSubstance(at(240, 250, 0), -1).isAir());
        assertTrue(aquifer.computeSubstance(at(700, -64, 0), -1).is(Blocks.LAVA));
        assertTrue(aquifer.shouldScheduleFluidUpdate());
        assertNull(aquifer.computeSubstance(at(320, -64, 0), 1));
        var disabled =
                new AbyssAquifer(
                        Aquifer.createDisabled(
                                (x, y, z) ->
                                        new Aquifer.FluidStatus(
                                                63, Blocks.WATER.defaultBlockState())),
                        SHAPE);
        assertTrue(disabled.computeSubstance(at(0, 0, 0), -1).isAir());
        assertTrue(disabled.computeSubstance(at(700, 0, 0), -1).is(Blocks.WATER));
    }

    private record Plane(int height) implements DensityFunction.SimpleFunction {
        public double compute(FunctionContext context) {
            return (height - context.blockY()) / 32.0;
        }

        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.constant(0).codec();
        }
    }

    /**
     * The untouched pre-carve terrain. The real field is a {@code DensityFunctions.findTopSurface} that
     * reports a block Y, so this reports the same block coordinate for every sample - and, for the codec
     * round trip, serialises as the plain density that reports the same height at y=0.
     */
    private record PlainHeight(int height) implements DensityFunction.SimpleFunction {
        static final KeyDispatchDataCodec<PlainHeight> CODEC =
                KeyDispatchDataCodec.of(
                        Codec.INT.fieldOf("height").xmap(PlainHeight::new, PlainHeight::height));

        public double compute(FunctionContext context) {
            return height;
        }

        public double minValue() {
            return -64.0;
        }

        public double maxValue() {
            return 320.0;
        }

        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    /**
     * Rock only in {@code [height - thickness, height]}: a thin layer that a coarse downward scan can
     * step straight over. Column {@code height} lands on the exact top of the layer.
     */
    private record Layered(int height, int thickness) implements DensityFunction.SimpleFunction {
        public double compute(FunctionContext context) {
            int y = context.blockY();
            return y <= height && y > height - thickness ? 1.0 : -1.0;
        }

        public double minValue() {
            return -1.0;
        }

        public double maxValue() {
            return 1.0;
        }

        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.constant(0).codec();
        }
    }

    /** Counts how often the pre-carve estimate is reached, to prove it is not sampled outside. */
    private static final class CountingEstimate implements DensityFunction.SimpleFunction {
        private final int height;
        private int calls;

        private CountingEstimate(int height) {
            this.height = height;
        }

        @Override
        public double compute(FunctionContext context) {
            calls++;
            return height;
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return DensityFunctions.constant(0).codec();
        }
    }
}
