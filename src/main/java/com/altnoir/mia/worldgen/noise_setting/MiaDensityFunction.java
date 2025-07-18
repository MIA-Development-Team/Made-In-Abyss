package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaDensityFunctionTypes;
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

public class MiaDensityFunction {
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_ABYSS_BRINK = createKey("abyss_brink/base_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_DEPTH = createKey("abyss_brink/depth");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_HOLE = createKey("abyss_brink/abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_BIG_HOLE = createKey("abyss_brink/big_abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_INSIDE_HOLE = createKey("abyss_brink/inside_abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_MIDDLE_BASE_3D = createKey("abyss_brink/middle_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_OUTSIDE_BASE_3D = createKey("abyss_brink/outside_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_PILLARS = createKey("abyss_brink/caves/pillars");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_ABYSS_PILLARS = createKey("abyss_brink/caves/abyss_pillars");
    public static final ResourceKey<DensityFunction> ABYSS_BRINK_NOODLE = createKey("abyss_brink/caves/noodle");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, location));
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> holdergetter = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = context.lookup(Registries.DENSITY_FUNCTION);

        context.register(ABYSS_BRINK_DEPTH, DensityFunctions.add(DensityFunctions.yClampedGradient(0, 580, 2.5, -2.5), getFunction(holdergetter1, MiaNoiseRouterData.OFFSET)));
        context.register(ABYSS_BRINK_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.abyssHole(0L), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_BRINK)));
        context.register(ABYSS_BRINK_BIG_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.abyssBigHole(0L), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_BRINK)));
        context.register(BASE_3D_NOISE_ABYSS_BRINK, BlendedNoise.createUnseeded(0.25, 0.25, 160.0, 160.0, 8.0));
        context.register(ABYSS_BRINK_INSIDE_HOLE, insideAbyssBrinkHole(holdergetter1));
        context.register(ABYSS_BRINK_MIDDLE_BASE_3D, middleAbyssBrinkBASE3D(holdergetter1));
        context.register(ABYSS_BRINK_OUTSIDE_BASE_3D, outsideAbyssBrinkBASE3D(holdergetter1));
        context.register(ABYSS_BRINK_PILLARS, pillars(holdergetter));
        context.register(ABYSS_BRINK_ABYSS_PILLARS, abyssBrinkPillars(holdergetter1));
        return context.register(ABYSS_BRINK_NOODLE, noodle(holdergetter1, holdergetter));
    }

    private static DensityFunction noodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        int minY = 5;
        int maxY = 360;
        double d0 = 2.6666666666666665;
        DensityFunction densityFunction1 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.constant(0.0), minY, maxY, -1
        );
        DensityFunction densityFunction2 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.1, -0.15), minY, maxY, 0
        );
        DensityFunction densityFunction3 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0), minY, maxY, 0
        );
        DensityFunction densityFunction4 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0), minY, maxY, 0
        );
        DensityFunction densityFunction5 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction3.abs(), densityFunction4.abs())
        );
        return DensityFunctions.rangeChoice(
                densityFunction1, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(densityFunction2, densityFunction5)
        );
    }

    private static DensityFunction pillars(HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityFunction = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.PILLAR), 25.0, 0.3);
        DensityFunction densityFunction1 = DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction densityFunction2 = DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction densityFunction3 = DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(3.0)), densityFunction1);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction3, densityFunction2.cube()));
    }

    private static DensityFunction abyssBrinkPillars(HolderGetter<DensityFunction> densityFunctions) {
        return DensityFunctions.rangeChoice(
                getFunction(densityFunctions, ABYSS_BRINK_PILLARS), -1000000, 0.03, DensityFunctions.constant(-1000000), getFunction(densityFunctions, ABYSS_BRINK_PILLARS)
        );
    }

    private static DensityFunction insideAbyssBrinkHole(HolderGetter<DensityFunction> densityFunctions) {
        DensityFunction densityFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        int minY = 0;
        int maxY = 300;
        DensityFunction densityFunction1 = DensityFunctions.rangeChoice(
                densityFunction, minY, maxY, getFunction(densityFunctions, ABYSS_BRINK_HOLE), DensityFunctions.constant(-1.0)
        );
        DensityFunction densityFunction2 = DensityFunctions.mul(
                DensityFunctions.constant(5.0), getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES)
        );

        return DensityFunctions.min(densityFunction1, densityFunction2);
    }

    private static DensityFunction middleAbyssBrinkBASE3D(HolderGetter<DensityFunction> densityFunctions) {
        DensityFunction densityFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        int minY = 0;
        int maxY = 300;

        DensityFunction densityFunction1 = DensityFunctions.rangeChoice(
                densityFunction, minY, maxY, getFunction(densityFunctions, BASE_3D_NOISE_ABYSS_BRINK), DensityFunctions.constant(-1.0)
        );
        DensityFunction densityFunction2 = DensityFunctions.mul(
                DensityFunctions.constant(5.0), getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES)
        );
        return DensityFunctions.min(densityFunction1, densityFunction2);
    }

    private static DensityFunction outsideAbyssBrinkBASE3D(HolderGetter<DensityFunction> densityFunctions) {
        DensityFunction densityFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        int minY = 24;
        int maxY = 320;
        DensityFunction densityFunction1 = DensityFunctions.rangeChoice(
                densityFunction, minY, maxY, getFunction(densityFunctions, BASE_3D_NOISE_ABYSS_BRINK), DensityFunctions.constant(1.0)
        );
        DensityFunction densityFunction2 = DensityFunctions.mul(
                DensityFunctions.constant(5.0), getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES)
        );
        return DensityFunctions.min(densityFunction1, densityFunction2);
    }

    private static DensityFunction abyssBrinkBigCave(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        int minY = 24;
        int maxY = 300;

        DensityFunction densityFunction1 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE), 2, 4), minY, maxY, -1
        );
        DensityFunction densityFunction2 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE), 1, 2), minY, maxY, -1
        );
        return DensityFunctions.rangeChoice(
                densityFunction1, -1000000.0, 0.0, DensityFunctions.constant(1.0), densityFunction2
        );
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction input, DensityFunction whenInRange, int minY, int maxY, int whenOutOfRange) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(input, (double) minY, (double) maxY, whenInRange, DensityFunctions.constant((double) whenOutOfRange))
        );
    }
}
