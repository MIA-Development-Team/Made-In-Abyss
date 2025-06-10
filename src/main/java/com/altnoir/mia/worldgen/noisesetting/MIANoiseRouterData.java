package com.altnoir.mia.worldgen.noisesetting;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MIANoiseRouterData extends NoiseRouterData {
    public static final ResourceKey<DensityFunction> Y = createKey("y");
    public static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    public static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = createKey("end/base_3d_noise");
    public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
    public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
    public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED = createKey("overworld/ridges_folded");
    public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
    public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
    public static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
    public static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
    public static final ResourceKey<DensityFunction> OFFSET_LARGE = createKey("overworld_large_biomes/offset");
    public static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
    public static final ResourceKey<DensityFunction> OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
    public static final ResourceKey<DensityFunction> FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
    public static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
    public static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
    public static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
    public static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
    public static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    public static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.withDefaultNamespace(location));
    }
    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }
    private static DensityFunction postProcess(DensityFunction densityFunction) {
        DensityFunction densityfunction = DensityFunctions.blendDensity(densityFunction);
        return DensityFunctions.mul(DensityFunctions.interpolated(densityfunction), DensityFunctions.constant(0.64)).squeeze();
    }

    private static DensityFunction abyssBrinkDensity(HolderGetter<DensityFunction> densityFunctions) {
        DensityFunction baseNoise = DensityFunctions.add(
            DensityFunctions.constant(-36.8975),
            DensityFunctions.mul(
                DensityFunctions.yClampedGradient(250, 320, 1, 0),
                DensityFunctions.add(
                    DensityFunctions.constant(37),
                    getFunction(densityFunctions, MIADensityFunction.ABYSS_HOLE)
                )
            )
        );

        DensityFunction blended = DensityFunctions.add(
            DensityFunctions.constant(0.4),
            DensityFunctions.mul(
                DensityFunctions.yClampedGradient(0, 32, 0, 1),
                DensityFunctions.add(
                    DensityFunctions.constant(-0.4),
                    baseNoise
                )
            )
        );

        return DensityFunctions.mul(
            DensityFunctions.constant(0.64),
            DensityFunctions.interpolated(DensityFunctions.blendDensity(blended))
        ).squeeze();
    }

    private static DensityFunction underground(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters, DensityFunction p_256658_) {
        DensityFunction densityfunction = getFunction(densityFunctions, SPAGHETTI_2D);
        DensityFunction densityfunction1 = getFunction(densityFunctions, SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction densityfunction2 = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.CAVE_LAYER), 8.0);
        DensityFunction densityfunction3 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityfunction2.square());
        DensityFunction densityfunction4 = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction densityfunction5 = DensityFunctions.add(
                DensityFunctions.add(DensityFunctions.constant(0.27), densityfunction4).clamp(-1.0, 1.0),
                DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), p_256658_)).clamp(0.0, 0.5)
        );
        DensityFunction densityfunction6 = DensityFunctions.add(densityfunction3, densityfunction5);
        DensityFunction densityfunction7 = DensityFunctions.min(
                DensityFunctions.min(densityfunction6, getFunction(densityFunctions, ENTRANCES)), DensityFunctions.add(densityfunction, densityfunction1)
        );
        DensityFunction densityfunction8 = getFunction(densityFunctions, PILLARS);
        DensityFunction densityfunction9 = DensityFunctions.rangeChoice(
                densityfunction8, -1000000.0, 0.03, DensityFunctions.constant(-1000000.0), densityfunction8
        );
        return DensityFunctions.max(densityfunction7, densityfunction9);
    }

    private static NoiseRouter abyssBrinkRouter(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction = getFunction(densityFunctions, SHIFT_X);
        DensityFunction densityfunction1 = getFunction(densityFunctions, SHIFT_Z);
        DensityFunction densityfunction2 = DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25, noiseParameters.getOrThrow(Noises.TEMPERATURE));
        DensityFunction densityfunction3 = DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25, noiseParameters.getOrThrow(Noises.VEGETATION));
        DensityFunction densityfunction4 = DensityFunctions.min(abyssBrinkDensity(densityFunctions), getFunction(densityFunctions, MIADensityFunction.ABYSS_BRINK_NOODLE));

        DensityFunction densityfunction5 = getFunction(densityFunctions, FACTOR);
        DensityFunction densityfunction6 = getFunction(densityFunctions, DEPTH);
        DensityFunction densityfunction7 = noiseGradientDensity(DensityFunctions.cache2d(densityfunction5), densityfunction6);

        DensityFunction densityfunction8 = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        DensityFunction densityfunction9 = postProcess(slideEnd(getFunction(densityFunctions, MIADensityFunction.ABYSS_HOLE)));

        DensityFunction densityfunction11 = getFunction(densityFunctions, SLOPED_CHEESE);
        DensityFunction densityfunction12 = DensityFunctions.min(
                densityfunction11, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(densityFunctions, ENTRANCES))
        );
        DensityFunction densityfunction13 = DensityFunctions.rangeChoice(
                densityfunction11, -1000000.0, 1.5625, densityfunction12, underground(densityFunctions, noiseParameters, densityfunction11)
        );
        DensityFunction densityfunction14 = DensityFunctions.min(postProcess(slideAbyssBrink(false, densityfunction13)), getFunction(densityFunctions, MIADensityFunction.ABYSS_BRINK_NOODLE));

        return new NoiseRouter(
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                densityfunction2, // temperature
                densityfunction3, // vegetation
                getFunction(densityFunctions, CONTINENTS), // continents
                getFunction(densityFunctions, EROSION), // erosion
                densityfunction6, // depth
                getFunction(densityFunctions, RIDGES), // ridges
                slideAbyssBrink(false, DensityFunctions.add(densityfunction7, DensityFunctions.constant(-0.703125)).clamp(-64.0, 64.0)), // initial_density_without_jaggedness
                //slideEnd(DensityFunctions.add(densityfunction8, DensityFunctions.constant(-0.703125))), // initial_density_without_jaggedness
                densityfunction4, // final_density
                //densityfunction9, // final_density
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero()
        ); // 前面四个和后面三个我还没搞明白是干什么的
    }

    private static DensityFunction noiseGradientDensity(DensityFunction minFunction, DensityFunction maxFunction) {
        DensityFunction densityfunction = DensityFunctions.mul(maxFunction, minFunction);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), densityfunction.quarterNegative());
    }
    private static DensityFunction slideAbyssBrink(boolean amplified, DensityFunction densityFunction) {
        return slide(densityFunction, 0, 320, amplified ? 16 : 80, amplified ? 0 : 64, -0.078125, 0, 24, amplified ? 0.4 : 0.1171875);
    }

    private static DensityFunction slideEnd(DensityFunction densityFunction) {
        return slideEndLike(densityFunction, 0, 250);
    }
    private static DensityFunction slideEndLike(DensityFunction densityFunction, int minY, int maxY) {
        return slide(densityFunction, minY, maxY, 72, -184, -23.4375, 4, 32, -0.234375);
    }
    private static DensityFunction slide(DensityFunction input, int minY, int maxY, int i1, int i2, double v1, int i3, int i4, double v2) {
        DensityFunction densityFunction1 = DensityFunctions.yClampedGradient(minY + maxY - i1, minY + maxY - i2, 1.0, 0.0);
        DensityFunction lerped = DensityFunctions.lerp(densityFunction1, v1, input);
        DensityFunction densityFunction2 = DensityFunctions.yClampedGradient(minY + i3, minY + i4, 0.0, 1.0);
        return DensityFunctions.lerp(densityFunction2, v2, lerped);
    }

    protected static NoiseRouter abyssBrink(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return abyssBrinkRouter(densityFunctions, noiseParameters);
    }
}
