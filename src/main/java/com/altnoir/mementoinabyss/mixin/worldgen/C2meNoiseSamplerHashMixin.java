package com.altnoir.mementoinabyss.mixin.worldgen;

import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = {NormalNoise.class, PerlinNoise.class, ImprovedNoise.class, BlendedNoise.class},
        priority = 1100)
public abstract class C2meNoiseSamplerHashMixin {
    @Unique private int mia$noiseHash;
    @Unique private volatile boolean mia$noiseHashReady;

    // C2ME adds structural hashes to these fixed samplers. Cache that exact result rather than
    // rescanning octave/permutation arrays whenever a chunk maps the shared density graph.
    // Vanilla inherits Object.hashCode, so there is no target method when C2ME is absent.
    @Inject(method = "hashCode()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void mia$reuseNoiseHash(CallbackInfoReturnable<Integer> ci) {
        if (mia$noiseHashReady) ci.setReturnValue(mia$noiseHash);
    }

    @Inject(method = "hashCode()I", at = @At("RETURN"), require = 0)
    private void mia$rememberNoiseHash(CallbackInfoReturnable<Integer> ci) {
        if (!mia$noiseHashReady) {
            mia$noiseHash = ci.getReturnValueI();
            mia$noiseHashReady = true;
        }
    }
}
