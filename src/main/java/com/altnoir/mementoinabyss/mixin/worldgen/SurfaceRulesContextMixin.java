package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssOriginalSurface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code SurfaceRules.Context#getMinSurfaceLevel} averages the preliminary surface level of four
 * columns on a sixteen-block grid before deciding whether a column is "above the surface", and on a
 * chunk edge those corners live in the neighbouring chunk. Reading the carved abyss height there
 * would push the surface band, and with it grass, sand and snow, over terrain that is hundreds of
 * blocks away from the rim, so the untouched estimate is read instead.
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public abstract class SurfaceRulesContextMixin {
    /**
     * Wraps all four corner lookups. {@code @ModifyReturnValue} cannot be used here: the target of
     * its {@code @At} is a call to a method returning {@code int}, while a return-value modifier
     * wants the call whose own value becomes the method's return value, which in this method is none
     * of them.
     */
    @WrapOperation(
            method = "getMinSurfaceLevel",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/NoiseChunk;preliminarySurfaceLevel(II)I"))
    private int mia$keepOriginalMinSurface(
            NoiseChunk chunk, int sampleX, int sampleZ, Operation<Integer> original) {
        if (!(chunk instanceof AbyssOriginalSurface abyss))
            return original.call(chunk, sampleX, sampleZ);
        DensityFunction surface = abyss.mia$originalPreliminarySurface();
        return surface == null
                ? original.call(chunk, sampleX, sampleZ)
                : Mth.floor(
                        surface.compute(
                                new DensityFunction.SinglePointContext(sampleX, 0, sampleZ)));
    }
}
