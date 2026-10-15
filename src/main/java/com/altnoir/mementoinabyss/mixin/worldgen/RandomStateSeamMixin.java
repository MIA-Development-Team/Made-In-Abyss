package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomState.class)
public abstract class RandomStateSeamMixin {
    @Shadow @Final @Mutable private NoiseRouter router;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mia$bindSharedWall(
            NoiseGeneratorSettings settings,
            HolderGetter<NormalNoise.NoiseParameters> noises,
            long seed,
            CallbackInfo ci) {
        router = AbyssSeamRuntime.bindRouter(router, seed);
    }
}
