package com.altnoir.mementoinabyss.infrastructure.worldgen.river;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

public final class AbyssRiverAquifer implements Aquifer {
    private final Aquifer original;
    private final AbyssRiverProfile profile;
    private boolean local;

    public AbyssRiverAquifer(Aquifer original, AbyssRiverProfile profile) {
        this.original = original;
        this.profile = profile;
    }

    @Override
    public @Nullable BlockState computeSubstance(
            DensityFunction.FunctionContext context, double density) {
        if (density > 0) {
            local = false;
            return original.computeSubstance(context, density);
        }
        local = profile.isRiver(context.blockX(), context.blockY(), context.blockZ());
        if (!local) return original.computeSubstance(context, density);
        return (context.blockY() < AbyssRiverProfile.SEA_LEVEL ? Blocks.WATER : Blocks.AIR)
                .defaultBlockState();
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return !local && original.shouldScheduleFluidUpdate();
    }
}
