package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

/** Chunk-local wrapper covers both enabled aquifers and the global fluid picker. */
public final class AbyssSeamAquifer implements Aquifer {
    private final Aquifer original;
    private final AbyssSeamDensity seam;
    private boolean dry;

    public AbyssSeamAquifer(Aquifer original, AbyssSeamDensity seam) {
        this.original = original;
        this.seam = seam;
    }

    @Override
    public @Nullable BlockState computeSubstance(
            DensityFunction.FunctionContext context, double density) {
        dry = AbyssSeamRuntime.isOpening(seam, context);
        return dry ? Blocks.AIR.defaultBlockState() : original.computeSubstance(context, density);
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return !dry && original.shouldScheduleFluidUpdate();
    }
}
