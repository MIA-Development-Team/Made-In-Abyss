package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class MiaNoiseData {
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_WARP_X = layer2("warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_WARP_Z = layer2("warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_RELIEF = layer2("relief");
    public static final ResourceKey<NormalNoise.NoiseParameters> LAYER2_PILLAR = layer2("pillar");
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
        context.register(LAYER2_WARP_X, new NormalNoise.NoiseParameters(-9, 1.0, .5, .25));
        context.register(LAYER2_WARP_Z, new NormalNoise.NoiseParameters(-9, 1.0, .5, .25));
        context.register(LAYER2_RELIEF, new NormalNoise.NoiseParameters(-7, 1.0));
        context.register(LAYER2_PILLAR, new NormalNoise.NoiseParameters(-7, 1.0));
        context.register(STRIPEY, new NormalNoise.NoiseParameters(-3, 1.1D, 1.0D));
        context.register(LAYER2_CAVE_A, new NormalNoise.NoiseParameters(-5, 1.0));
        context.register(LAYER2_CAVE_B, new NormalNoise.NoiseParameters(-5, 1.0));
        context.register(LAYER2_CAVE_THICKNESS, new NormalNoise.NoiseParameters(-4, 1.0));
    }

    private MiaNoiseData() {}

    private static ResourceKey<NormalNoise.NoiseParameters> layer2(String name) {
        return ResourceKey.create(
                Registries.NOISE, MementoInAbyss.asResource("the_abyss/layer2/" + name));
    }
}
