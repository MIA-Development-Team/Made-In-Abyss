package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyssAquiferRouter;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssAquifer;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssOriginalSurface;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssInterpolated;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin implements AbyssOriginalSurface {
    @Shadow @Final @Mutable private Aquifer aquifer;

    @Unique private @Nullable DensityFunction mia$originalPreliminarySurface;

    @Inject(method = "wrapNew", at = @At("HEAD"), cancellable = true)
    private void mia$bindSurfaceBlender(
            DensityFunction function, CallbackInfoReturnable<DensityFunction> ci) {
        if (function instanceof OverworldAbyssInterpolated interpolated)
            ci.setReturnValue(interpolated.withBlender(((NoiseChunk) (Object) this).getBlender()));
    }

    /**
     * Runs before said aquifer is built by {@code NoiseChunk}'s own constructor body, because the
     * aquifer constructor samples the preliminary surface level of neighbouring columns.
     */
    @Inject(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/Aquifer;create(Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/NoiseRouter;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;IILnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"))
    private void mia$captureOriginalSurface(
            int cellCountXZ,
            RandomState randomState,
            int minX,
            int minZ,
            NoiseSettings noiseSettings,
            DensityFunctions.BeardifierOrMarker beardifier,
            NoiseGeneratorSettings settings,
            Aquifer.FluidPicker fluidPicker,
            Blender blender,
            CallbackInfo ci) {
        OverworldAbyssHole hole = OverworldAbyss.holeOf(randomState);
        if (hole != null) {
            // mapAll installs this chunk's own Cache2D, so the sampling below stays chunk-local and
            // the shared per-level instance is never touched from two generation threads at once.
            mia$originalPreliminarySurface =
                    hole.originalSurface().mapAll(((AbyssOriginalSurface) this)::mia$wrap);
        }
    }

    @ModifyArg(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/Aquifer;create(Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/NoiseRouter;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;IILnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"),
            index = 2)
    private NoiseRouter mia$useOriginalClimateForAquifer(NoiseRouter router) {
        return OverworldAbyssAquiferRouter.forAquifer(router);
    }

    @ModifyArg(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/Aquifer;create(Lnet/minecraft/world/level/levelgen/NoiseChunk;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/NoiseRouter;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;IILnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)Lnet/minecraft/world/level/levelgen/Aquifer;"),
            index = 6)
    private Aquifer.FluidPicker mia$layer2Water(
            Aquifer.FluidPicker original, @Local(argsOnly = true) RandomState state) {
        var layer2 = Layer2Runtime.state(state);
        return layer2 == null ? original : layer2.picker(original);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mia$keepOpeningDry(
            int cellCountXZ,
            RandomState randomState,
            int minX,
            int minZ,
            NoiseSettings noiseSettings,
            DensityFunctions.BeardifierOrMarker beardifier,
            NoiseGeneratorSettings settings,
            Aquifer.FluidPicker fluidPicker,
            Blender blender,
            CallbackInfo ci) {
        var seam =
                com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime.seamOf(
                        randomState);
        if (seam != null)
            aquifer =
                    new com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamAquifer(
                            aquifer, seam, Layer2Runtime.state(randomState));
        OverworldAbyssHole hole = OverworldAbyss.holeOf(randomState);
        if (hole != null) {
            int span = cellCountXZ * noiseSettings.getCellWidth();
            int closestX = Math.clamp(0, minX, minX + span);
            int closestZ = Math.clamp(0, minZ, minZ + span);
            if (hole.shape().distance(closestX, closestZ) < 2.0 * hole.shape().radius()) {
                aquifer =
                        new AbyssAquifer(
                                aquifer,
                                (OverworldAbyssHole)
                                        hole.mapAll(((AbyssOriginalSurface) this)::mia$wrap));
            }
        }
    }

    @Override
    public @Nullable DensityFunction mia$originalPreliminarySurface() {
        return mia$originalPreliminarySurface;
    }

    /**
     * {@code NoiseChunk.wrap} is protected, so the injected body cannot reference it directly. The
     * invoker keeps the interface name so the call site resolves to this bridge.
     */
    @Invoker("wrap")
    public abstract DensityFunction mia$wrap(DensityFunction function);
}
