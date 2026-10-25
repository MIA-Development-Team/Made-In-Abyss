package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class MiaNoiseData {
    public static final ResourceKey<NormalNoise.NoiseParameters> STRIPEY =
            ResourceKey.create(Registries.NOISE, MementoInAbyss.asResource("stripey"));
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_CAVE_A =
            ResourceKey.create(
                    Registries.NOISE, MementoInAbyss.asResource("the_abyss/layer2/cave_a"));
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_CAVE_B =
            ResourceKey.create(
                    Registries.NOISE, MementoInAbyss.asResource("the_abyss/layer2/cave_b"));
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_CAVE_THICKNESS =
            ResourceKey.create(
                    Registries.NOISE, MementoInAbyss.asResource("the_abyss/layer2/cave_thickness"));

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(STRIPEY, new NormalNoise.NoiseParameters(-3, 1.1D, 1.0D));
        context.register(LAYER2_CAVE_A, new NormalNoise.NoiseParameters(-5, 1.0));
        context.register(LAYER2_CAVE_B, new NormalNoise.NoiseParameters(-5, 1.0));
        context.register(LAYER2_CAVE_THICKNESS, new NormalNoise.NoiseParameters(-4, 1.0));
    }

    private MiaNoiseData() {}
}
