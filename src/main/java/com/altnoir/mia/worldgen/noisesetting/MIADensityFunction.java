package com.altnoir.mia.worldgen.noisesetting;

import com.altnoir.mia.MIA;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MIADensityFunction {
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_ABYSS_BRINK = createKey("abyss_brink/base_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_HOLE = createKey("abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_NOODLE = createKey("abyss_brink/caves/noodle");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, location));
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> holdergetter = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = context.lookup(Registries.DENSITY_FUNCTION);

        context.register(ABYSS_HOLE, DensityFunctions.add(MIADensityFunctionTypes.abyssHole(0L), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_BRINK)));
        context.register(BASE_3D_NOISE_ABYSS_BRINK, BlendedNoise.createUnseeded(0.25, 0.25, 80.0, 160.0, 8.0));
        return context.register(ABYSS_BRINK_NOODLE, noodle(holdergetter1, holdergetter));
    }

    private static DensityFunction noodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction = getFunction(densityFunctions, MIANoiseRouterData.Y);
        int minY = 5;
        int maxY = 320;
        double d0 = 2.6666666666666665;
        DensityFunction densityfunction1 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE), 0.1, 1.0), minY, maxY, -1
        );
        DensityFunction densityfunction2 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.1, -0.15), minY, maxY, 0
        );
        DensityFunction densityfunction3 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0), minY, maxY, 0
        );
        DensityFunction densityfunction4 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0), minY, maxY, 0
        );
        DensityFunction densityfunction5 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), DensityFunctions.max(densityfunction3.abs(), densityfunction4.abs())
        );
        return DensityFunctions.rangeChoice(
                densityfunction1, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(densityfunction2, densityfunction5)
        );
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }
    private static DensityFunction yLimitedInterpolatable(DensityFunction input, DensityFunction whenInRange, int minY, int maxY, int whenOutOfRange) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(input, (double)minY, (double)(maxY + 1), whenInRange, DensityFunctions.constant((double)whenOutOfRange))
        );
    }
}
