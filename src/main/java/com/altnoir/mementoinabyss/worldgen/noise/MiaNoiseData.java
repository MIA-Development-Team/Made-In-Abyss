package com.altnoir.mementoinabyss.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class MiaNoiseData {
    public static final ResourceKey<NormalNoise.NoiseParameters> STRIPEY = ResourceKey.create(
            Registries.NOISE, MementoInAbyss.asResource("stripey"));

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(STRIPEY, new NormalNoise.NoiseParameters(-3, 1.1D, 1.0D));
    }

    private MiaNoiseData() {
    }
}
