package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssOriginalSurface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The aquifer samples the preliminary surface level of the columns it may spread fluid into, out to
 * two chunks away, and keeps the highest of them to decide how far down it still has to look. The
 * abyss reports a carved height there, so without this the cut-off would drop for columns far outside
 * the affected radius and water, lava and cave entrances would move with it.
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.Aquifer$NoiseBasedAquifer")
public abstract class AquiferMixin {
    @Shadow @Final private NoiseChunk noiseChunk;

    @WrapOperation(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/NoiseChunk;maxPreliminarySurfaceLevel(IIII)I"))
    private int mia$keepOriginalMaxSurface(
            NoiseChunk chunk, int minX, int minZ, int maxX, int maxZ, Operation<Integer> original) {
        if (!mia$canResolve(chunk)) return original.call(chunk, minX, minZ, maxX, maxZ);
        // Mirrors NoiseChunk#maxPreliminarySurfaceLevel's own stepping over the four-block grid.
        int highest = Integer.MIN_VALUE;
        for (int z = minZ; z <= maxZ; z += 4) {
            for (int x = minX; x <= maxX; x += 4) {
                highest = Math.max(highest, mia$originalSurface(chunk, x, z));
            }
        }
        return highest == Integer.MIN_VALUE
                ? original.call(chunk, minX, minZ, maxX, maxZ)
                : highest;
    }

    @WrapOperation(
            method = "computeFluid",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/NoiseChunk;preliminarySurfaceLevel(II)I"))
    private int mia$keepOriginalFluidSurface(
            NoiseChunk chunk, int sampleX, int sampleZ, Operation<Integer> original) {
        return mia$canResolve(chunk)
                ? mia$originalSurface(chunk, sampleX, sampleZ)
                : original.call(chunk, sampleX, sampleZ);
    }

    @Unique private static boolean mia$canResolve(NoiseChunk chunk) {
        return chunk instanceof AbyssOriginalSurface abyss
                && abyss.mia$originalPreliminarySurface() != null;
    }

    @Unique private static int mia$originalSurface(NoiseChunk chunk, int x, int z) {
        DensityFunction surface = ((AbyssOriginalSurface) chunk).mia$originalPreliminarySurface();
        return Mth.floor(surface.compute(new DensityFunction.SinglePointContext(x, 0, z)));
    }
}
