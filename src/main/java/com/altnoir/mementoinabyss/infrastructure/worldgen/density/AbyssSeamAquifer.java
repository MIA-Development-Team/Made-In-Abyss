package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime.State;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

/** Chunk-local wrapper covers both enabled aquifers and the global fluid picker. */
public final class AbyssSeamAquifer implements Aquifer {
    private final Aquifer original;
    private final AbyssSeamDensity seam;
    private final @Nullable State layer2;
    private boolean dry;

    public AbyssSeamAquifer(Aquifer original, AbyssSeamDensity seam, @Nullable State layer2) {
        this.original = original;
        this.seam = seam;
        this.layer2 = layer2;
    }

    @Override
    public @Nullable BlockState computeSubstance(
            DensityFunction.FunctionContext context, double density) {
        int x = context.blockX(), y = context.blockY(), z = context.blockZ();
        dry =
                AbyssSeamRuntime.isOpening(seam, context)
                        || (density <= 0 && layer2 != null && y < 0 && layer2.opening(x, z));
        if (dry) return Blocks.AIR.defaultBlockState();
        BlockState substance = original.computeSubstance(context, density);
        dry = density <= 0 && layer2 != null && layer2.dry(x, y, z);
        // Keep aquifer pressure barriers instead of replacing them with air at the dry boundary.
        if (dry && substance != null && !substance.getFluidState().isEmpty())
            return y < layer2.layout().minWaterY() ? null : Blocks.AIR.defaultBlockState();
        return substance;
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return !dry && original.shouldScheduleFluidUpdate();
    }
}
