package com.altnoir.mia.worldgen.noise;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MiaNoiseData {
    public static final ResourceKey<NormalNoise.NoiseParameters> STRIPEY = createKey("stripey");

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        register(context, STRIPEY, -3, 1.1, 1.0);
    }

    private static ResourceKey<NormalNoise.NoiseParameters> createKey(String key) {
        return ResourceKey.create(Registries.NOISE, MiaUtil.miaId(key));
    }

    private static void register(
            BootstrapContext<NormalNoise.NoiseParameters> context,
            ResourceKey<NormalNoise.NoiseParameters> key,
            int firstOctave,
            double amplitude,
            double... otherAmplitudes
    ) {
        context.register(key, new NormalNoise.NoiseParameters(firstOctave, amplitude, otherAmplitudes));
    }
}
