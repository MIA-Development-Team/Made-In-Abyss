package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamAquifer;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AbyssSeamRuntimeTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static NoiseRouter router(DensityFunction finalDensity) {
        var zero = DensityFunctions.zero();
        return new NoiseRouter(
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                finalDensity,
                zero,
                zero,
                zero);
    }

    @Test
    void bindingUsesCurrentSeedAndLayoutWithoutChangingSerializedPlaceholder() {
        var placeholder = SharedAbyssWall.unbound(320);
        var raw = router(AbyssSeamDensity.abyss(placeholder, DensityFunctions.zero(), 384));
        var first = (AbyssSeamDensity) AbyssSeamRuntime.bindRouter(raw, 42, 640).finalDensity();
        var second = (AbyssSeamDensity) AbyssSeamRuntime.bindRouter(raw, 43, 480).finalDensity();
        assertTrue(first.wall().bound());
        assertEquals(42, first.wall().seed());
        assertEquals(640, first.wall().layoutRadius());
        assertEquals(43, second.wall().seed());
        assertEquals(480, second.wall().layoutRadius());
        assertFalse(placeholder.bound());
        assertEquals(0, placeholder.seed());
        assertEquals(320, placeholder.layoutRadius());
        assertSame(
                first.wall(),
                ((AbyssSeamDensity)
                                AbyssSeamRuntime.bindRouter(router(first), 99, 160).finalDensity())
                        .wall());
    }

    @Test
    void roofAndConnectionGuardsUseTheirOwnNondefaultBoundaries() {
        var wall = SharedAbyssWall.forAbyss(320, 42);
        var abyss = AbyssSeamDensity.abyss(wall, DensityFunctions.zero(), 1024);
        // Entire exterior roof is protected, including custom dimensions far taller than 384.
        assertTrue(AbyssSeamRuntime.protectedFromCarver(abyss, 10000, 1023, 0));
        assertTrue(AbyssSeamRuntime.protectedFromCarver(abyss, 10000, 1008, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(abyss, 10000, 1007, 0));
        assertTrue(AbyssSeamRuntime.protectedFromCarver(abyss, 340, 976, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(abyss, 340, 975, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(abyss, 0, 1024, 0));
        var overworld =
                AbyssSeamDensity.overworld(
                        SharedAbyssWall.forOverworld(320, 42, 1152), DensityFunctions.zero(), -128);
        assertTrue(AbyssSeamRuntime.protectedFromCarver(overworld, 340, -128, 0));
        assertTrue(AbyssSeamRuntime.protectedFromCarver(overworld, 340, -81, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(overworld, 340, -80, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(overworld, 10000, -128, 0));
        assertFalse(AbyssSeamRuntime.protectedFromCarver(overworld, 0, -129, 0));
    }

    @Test
    void aquiferKeepsOpeningDryAndOnlySuppressesItsFluidUpdates() {
        var seam =
                AbyssSeamDensity.abyss(
                        SharedAbyssWall.forAbyss(320, 42), DensityFunctions.constant(1), 384);
        int[] calls = {0};
        Aquifer original =
                new Aquifer() {
                    @Override
                    public BlockState computeSubstance(
                            DensityFunction.FunctionContext context, double density) {
                        calls[0]++;
                        return density > 0 ? null : Blocks.WATER.defaultBlockState();
                    }

                    @Override
                    public boolean shouldScheduleFluidUpdate() {
                        return true;
                    }
                };
        var aquifer = new AbyssSeamAquifer(original, seam);
        assertTrue(
                aquifer.computeSubstance(new DensityFunction.SinglePointContext(0, 383, 0), -1)
                        .isAir());
        assertFalse(aquifer.shouldScheduleFluidUpdate());
        assertEquals(0, calls[0]);
        assertEquals(
                Blocks.WATER.defaultBlockState(),
                aquifer.computeSubstance(new DensityFunction.SinglePointContext(1000, 383, 0), -1));
        assertTrue(aquifer.shouldScheduleFluidUpdate());
        assertNull(
                aquifer.computeSubstance(new DensityFunction.SinglePointContext(1000, 383, 0), 1));
        assertEquals(2, calls[0]);
    }

    @Test
    void surfaceOpeningDecisionStopsAtSeamAndNeverOpensExteriorRoof() {
        var seam =
                AbyssSeamDensity.abyss(
                        SharedAbyssWall.forAbyss(320, 42), DensityFunctions.constant(1), 384);
        assertTrue(
                AbyssSeamRuntime.isOpening(
                        seam, new DensityFunction.SinglePointContext(0, 383, 0)));
        assertFalse(
                AbyssSeamRuntime.isOpening(
                        seam, new DensityFunction.SinglePointContext(1000, 383, 0)));
        assertFalse(
                AbyssSeamRuntime.isOpening(
                        seam, new DensityFunction.SinglePointContext(0, 335, 0)));
        assertFalse(
                AbyssSeamRuntime.isOpening(
                        seam, new DensityFunction.SinglePointContext(0, 384, 0)));
    }
}
