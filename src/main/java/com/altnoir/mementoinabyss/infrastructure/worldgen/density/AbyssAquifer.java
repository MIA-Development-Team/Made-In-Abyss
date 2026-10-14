package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

/** Per-NoiseChunk wrapper, including the disabled-aquifer/global-sea-level path. */
public final class AbyssAquifer implements Aquifer {
    private final Aquifer original;
    private final OverworldAbyssHole hole;
    private boolean drySample;

    public AbyssAquifer(Aquifer original, OverworldAbyssHole hole) {
        this.original = original;
        this.hole = hole;
    }

    public AbyssAquifer(Aquifer original, OverworldAbyssShape shape) {
        this(
                original,
                new OverworldAbyssHole(
                        net.minecraft.world.level.levelgen.DensityFunctions.zero(),
                        net.minecraft.world.level.levelgen.DensityFunctions.zero(),
                        net.minecraft.world.level.levelgen.DensityFunctions.zero(),
                        shape,
                        false));
    }

    @Override
    public @Nullable BlockState computeSubstance(
            DensityFunction.FunctionContext context, double density) {
        // Test the explicit opening even when a structure beardifier adds positive density.
        drySample = hole.isOpening(context);
        return drySample
                ? Blocks.AIR.defaultBlockState()
                : original.computeSubstance(context, density);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return !drySample && original.shouldScheduleFluidUpdate();
    }
}
