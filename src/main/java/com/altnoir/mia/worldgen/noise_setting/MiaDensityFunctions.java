package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.worldgen.MiaDensityFunctionTypes;
import com.altnoir.mia.worldgen.MiaHeight;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MiaDensityFunctions {
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_THE_ABYSS = createKey("base_3d_noise_the_abyss");

    public static final ResourceKey<DensityFunction> OFFSET = theAbyssKey("caves/offset");
    public static final ResourceKey<DensityFunction> FACTOR = theAbyssKey("caves/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS = theAbyssKey("caves/jaggedness");
    public static final ResourceKey<DensityFunction> THE_ABYSS_DEPTH = theAbyssKey("caves/depth");
    public static final ResourceKey<DensityFunction> THE_ABYSS_HOLE_ABOVE = theAbyssKey("hole_above");
    public static final ResourceKey<DensityFunction> THE_ABYSS_HOLE_BELOW = theAbyssKey("hole_below");
    public static final ResourceKey<DensityFunction> THE_ABYSS_BIG_HOLE = theAbyssKey("big_abyss_hole");
    public static final ResourceKey<DensityFunction> THE_ABYSS_INSIDE_HOLE_BELOW = theAbyssKey("inside_hole_below");
    public static final ResourceKey<DensityFunction> THE_ABYSS_INSIDE_HOLE_ABOVE = theAbyssKey("inside_hole_above");
    public static final ResourceKey<DensityFunction> THE_ABYSS_MIDDLE_BASE_3D = theAbyssKey("middle_3d_noise");
    public static final ResourceKey<DensityFunction> THE_ABYSS_OUTSIDE_BASE_3D = theAbyssKey("outside_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> THE_ABYSS_GREAT_CAVE = theAbyssKey("great_cave");
    public static final ResourceKey<DensityFunction> THE_ABYSS_BASE_GREAT_CAVE_NOODLE = theAbyssKey("caves/base_great_cave_noodle");
    public static final ResourceKey<DensityFunction> THE_ABYSS_GREAT_CAVE_NOODLE = theAbyssKey("caves/great_cave_noodle");
    public static final ResourceKey<DensityFunction> THE_ABYSS_BASE_PILLARS = theAbyssKey("caves/base_pillars");
    public static final ResourceKey<DensityFunction> THE_ABYSS_PILLARS = theAbyssKey("caves/pillars");
    public static final ResourceKey<DensityFunction> THE_ABYSS_NOODLE = theAbyssKey("caves/noodle");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, location));
    }

    private static ResourceKey<DensityFunction> createVanillaKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.withDefaultNamespace(location));
    }

    private static ResourceKey<DensityFunction> theAbyssKey(String location) {
        return createKey("the_abyss/" + location);
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> holdergetter = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = context.lookup(Registries.DENSITY_FUNCTION);

        DensityFunction yFunction = getFunction(holdergetter1, MiaNoiseRouterData.Y);

        DensityFunction densityfunction = registerAndWrap(
                context, MiaNoiseRouterData.SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(holdergetter.getOrThrow(Noises.SHIFT))))
        );
        DensityFunction densityfunction1 = registerAndWrap(
                context, MiaNoiseRouterData.SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(holdergetter.getOrThrow(Noises.SHIFT))))
        );
        Holder<DensityFunction> holder = context.register(
                NoiseRouterData.CONTINENTS,
                DensityFunctions.flatCache(
                        DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25, holdergetter.getOrThrow(Noises.CONTINENTALNESS))
                )
        );
        Holder<DensityFunction> holder1 = context.register(
                NoiseRouterData.EROSION,
                DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25, holdergetter.getOrThrow(Noises.EROSION)))
        );

        context.register(BASE_3D_NOISE_THE_ABYSS, BlendedNoise.createUnseeded(0.25, 0.25, 160.0, 160.0, 8.0));

        context.register(THE_ABYSS_BASE_PILLARS, pillars(holdergetter));
        context.register(THE_ABYSS_PILLARS, DensityFunctions.rangeChoice(
                getFunction(holdergetter1, THE_ABYSS_BASE_PILLARS),
                -1000000, 0.03, DensityFunctions.constant(-1000000),
                getFunction(holdergetter1, THE_ABYSS_BASE_PILLARS)
        ));
        DensityFunction the_abyss_depth = DensityFunctions.add(DensityFunctions.yClampedGradient(MiaHeight.THE_ABYSS.midY(), 1200, 1.5, -2.5), getFunction(holdergetter1, MiaNoiseRouterData.OFFSET));
        context.register(THE_ABYSS_DEPTH, DensityFunctions.rangeChoice(yFunction, MiaHeight.THE_ABYSS.minY(), 0, DensityFunctions.constant(2.0), the_abyss_depth));

        context.register(THE_ABYSS_HOLE_ABOVE, DensityFunctions.add(MiaDensityFunctionTypes.hopperAbyssHole(), getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS)));
        context.register(THE_ABYSS_HOLE_BELOW, DensityFunctions.add(MiaDensityFunctionTypes.generalAbyssHole(0.0F, 0.3F), getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS)));
        context.register(THE_ABYSS_BIG_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.generalAbyssHole(128.0F), getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS)));
        context.register(THE_ABYSS_INSIDE_HOLE_BELOW, abyssDensityFunction(holdergetter1, THE_ABYSS_HOLE_BELOW, MiaHeight.THE_ABYSS.minY() + 128, MiaHeight.THE_ABYSS.midY()));
        context.register(THE_ABYSS_INSIDE_HOLE_ABOVE, abyssDensityFunction(holdergetter1, THE_ABYSS_HOLE_ABOVE, MiaHeight.THE_ABYSS.midY(), MiaHeight.THE_ABYSS.maxY() - 64));
        context.register(THE_ABYSS_MIDDLE_BASE_3D, abyssDensityFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS, MiaHeight.THE_ABYSS.minY() + 128, MiaHeight.THE_ABYSS.maxY() - 64));
        context.register(THE_ABYSS_OUTSIDE_BASE_3D, abyssDensityFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS));

        DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter.getOrThrow(Noises.JAGGED), 1500.0, 0.0);
        registerTerrainNoises(
                context, holdergetter1, densityfunction3, holder, holder1, OFFSET, FACTOR, JAGGEDNESS,
                THE_ABYSS_GREAT_CAVE
        );
        context.register(THE_ABYSS_BASE_GREAT_CAVE_NOODLE, theAbyssGreatCaveNoodle(holdergetter1, holdergetter));
        context.register(THE_ABYSS_GREAT_CAVE_NOODLE, DensityFunctions.rangeChoice(
                DensityFunctions.min(
                        getFunction(holdergetter1, THE_ABYSS_BASE_GREAT_CAVE_NOODLE),
                        MiaDensityFunctionTypes.noodleAbyssHole(0.0F, 0.2F)
                ),
                -1000000, 0.05, DensityFunctions.constant(-1000000),
                getFunction(holdergetter1, THE_ABYSS_BASE_GREAT_CAVE_NOODLE)
        ));

        return context.register(THE_ABYSS_NOODLE, theAbyssNoodle(holdergetter1, holdergetter));
    }

    private static DensityFunction theAbyssGreatCaveNoodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return noodle(MiaHeight.THE_ABYSS.minY() + 64, 5, densityFunctions, noiseParameters, 0, 0, true, true, false);
    }

    private static DensityFunction theAbyssNoodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return theAbyssNoodle(MiaHeight.THE_ABYSS.minY() + 128, MiaHeight.THE_ABYSS.maxY(), densityFunctions, noiseParameters, -40, 40, true);
    }

    private static DensityFunction noodle(
            int minY, int maxY,
            HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            int minYOffset, int maxYOffset, boolean ridgeB, boolean inverse, boolean full) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);

        int FMinY = minY + minYOffset;
        int FMaxY = maxY + maxYOffset;

        DensityFunction densityFunction1 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE), 0.5, 0.5), FMinY, FMaxY, -1
        );

        DensityFunction densityFunction2 = yLimitedInterpolatable(
                yFunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0,
                        inverse ? 0.3 : -0.25, inverse ? 0.2 : -0.25),
                FMinY, FMaxY, 0
        );

        double d0 = 2.6666666666666665;
        DensityFunction densityFunction3 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0), minY, maxY, inverse ? -1 : 0
        );

        DensityFunction densityFunction4 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0), minY, maxY, inverse ? -1 : 0
        );

        DensityFunction densityFunction5 = DensityFunctions.mul(
                DensityFunctions.constant(inverse ? -1.5 : 1.5), densityFunction3.abs()
        );
        DensityFunction densityFunction6 = DensityFunctions.mul(
                DensityFunctions.constant(inverse ? -1.5 : 1.5), DensityFunctions.max(densityFunction3.abs(), densityFunction4.abs())
        );

        DensityFunction densityFunction7 = DensityFunctions.rangeChoice(
                densityFunction1, -1000000.0, 0.0,
                DensityFunctions.constant(inverse ? -1.0 : 64.0),
                DensityFunctions.add(densityFunction2, ridgeB ? densityFunction6 : densityFunction5)
        );
        return full ? DensityFunctions.add(densityFunction2, ridgeB ? densityFunction6 : densityFunction5) : densityFunction7;
    }

    private static DensityFunction theAbyssNoodle(
            int minY, int maxY,
            HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            int minYOffset, int maxYOffset, boolean ridgeB) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);

        int FMinY = minY + minYOffset;
        int FMaxY = maxY + maxYOffset;

        DensityFunction densityFunction0 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE), 1.5, 1.5), FMinY, FMaxY, -2
        );

        DensityFunction densityFunction1 = DensityFunctions.add(
                DensityFunctions.rangeChoice(yFunction, minY + 16, maxY - 64,
                        DensityFunctions.constant(0), DensityFunctions.constant(1)),
                densityFunction0
        );

        DensityFunction densityFunction2 = yLimitedInterpolatable(
                yFunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0,
                        -0.5, -0.3),
                FMinY, FMaxY, 0
        );

        double d0 = 2.6666666666666665;
        DensityFunction densityFunction3 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0), minY, maxY, 0
        );

        DensityFunction densityFunction4 = yLimitedInterpolatable(
                yFunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0), minY, maxY, 0
        );

        DensityFunction densityFunction5 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), densityFunction3.abs()
        );
        DensityFunction densityFunction6 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction3.abs(), densityFunction4.abs())
        );

        return DensityFunctions.rangeChoice(
                densityFunction1, -1000000.0, 0.0,
                DensityFunctions.constant(64.0),
                DensityFunctions.add(densityFunction2, ridgeB ? densityFunction6 : densityFunction5)
        );
    }

    private static DensityFunction pillars(HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityFunction = DensityFunctions.noise(noiseParameters.getOrThrow(Noises.PILLAR), 25.0, 0.3);
        DensityFunction densityFunction1 = DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction densityFunction2 = DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction densityFunction3 = DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(3.0)), densityFunction1);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction3, densityFunction2.cube()));
    }


    private static DensityFunction abyssDensityFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> mainFunctionKey) {
        DensityFunction entranceFactor = DensityFunctions.mul(
                DensityFunctions.constant(5.0),
                getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES)
        );

        return DensityFunctions.min(getFunction(densityFunctions, mainFunctionKey), entranceFactor);
    }

    private static DensityFunction abyssDensityFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> mainFunctionKey, int minY, int maxY) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction rangeChoice = DensityFunctions.rangeChoice(yFunction,
                minY,
                maxY,
                getFunction(densityFunctions, mainFunctionKey),
                DensityFunctions.constant(-1)
        );
        DensityFunction entranceFactor = DensityFunctions.mul(
                DensityFunctions.constant(5.0),
                getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES)
        );

        return DensityFunctions.min(rangeChoice, entranceFactor);
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }

    private static void registerTerrainNoises(
            BootstrapContext<DensityFunction> context,
            HolderGetter<DensityFunction> densityFunctionGetter,
            DensityFunction jaggedNoise,
            Holder<DensityFunction> continentalness,
            Holder<DensityFunction> erosion,
            ResourceKey<DensityFunction> offsetKey,
            ResourceKey<DensityFunction> factorKey,
            ResourceKey<DensityFunction> jaggednessKey,
            ResourceKey<DensityFunction> slopedCheeseKey
    ) {
        DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate = new DensityFunctions.Spline.Coordinate(continentalness);
        DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate1 = new DensityFunctions.Spline.Coordinate(erosion);
        DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate2 = new DensityFunctions.Spline.Coordinate(
                densityFunctionGetter.getOrThrow(createVanillaKey("overworld/ridges"))
        );
        DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate3 = new DensityFunctions.Spline.Coordinate(
                densityFunctionGetter.getOrThrow(createVanillaKey("overworld/ridges_folded"))
        );
        DensityFunction offset = registerAndWrap(
                context,
                offsetKey,
                splineWithBlending(
                        DensityFunctions.add(
                                DensityFunctions.constant(-0.50375F),
                                DensityFunctions.spline(
                                        TerrainProvider.overworldOffset(
                                                densityfunctions$spline$coordinate, densityfunctions$spline$coordinate1, densityfunctions$spline$coordinate3, false
                                        )
                                )
                        ),
                        DensityFunctions.blendOffset()
                )
        );
        DensityFunction factor = registerAndWrap(
                context,
                factorKey,
                splineWithBlending(
                        DensityFunctions.spline(
                                TerrainProvider.overworldFactor(
                                        densityfunctions$spline$coordinate,
                                        densityfunctions$spline$coordinate1,
                                        densityfunctions$spline$coordinate2,
                                        densityfunctions$spline$coordinate3,
                                        false
                                )
                        ),
                        DensityFunctions.constant(10.0)
                )
        );
        DensityFunction depth1 = DensityFunctions.add(DensityFunctions.yClampedGradient(-16, 16, 0, 1.5), offset);
        DensityFunction depth = DensityFunctions.add(DensityFunctions.yClampedGradient(-128, -96, 1.5, 0), depth1);
        DensityFunction jaggedness = registerAndWrap(
                context,
                jaggednessKey,
                splineWithBlending(
                        DensityFunctions.spline(
                                TerrainProvider.overworldJaggedness(
                                        densityfunctions$spline$coordinate,
                                        densityfunctions$spline$coordinate1,
                                        densityfunctions$spline$coordinate2,
                                        densityfunctions$spline$coordinate3,
                                        false
                                )
                        ),
                        DensityFunctions.zero()
                )
        );
        DensityFunction densityfunction4 = DensityFunctions.max(jaggedness, jaggedNoise.halfNegative());
        DensityFunction densityfunction5 = noiseGradientDensity(factor, DensityFunctions.add(depth, densityfunction4));
        context.register(slopedCheeseKey, DensityFunctions.add(densityfunction5, getFunction(densityFunctionGetter, BASE_3D_NOISE_THE_ABYSS)));
    }

    private static DensityFunction registerAndWrap(
            BootstrapContext<DensityFunction> context, ResourceKey<DensityFunction> key, DensityFunction value
    ) {
        return new DensityFunctions.HolderHolder(context.register(key, value));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction minFunction, DensityFunction maxFunction) {
        DensityFunction densityfunction = DensityFunctions.mul(maxFunction, minFunction);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), densityfunction.quarterNegative());
    }

    private static DensityFunction splineWithBlending(DensityFunction minFunction, DensityFunction maxFunction) {
        DensityFunction densityfunction = DensityFunctions.lerp(DensityFunctions.blendAlpha(), maxFunction, minFunction);
        return DensityFunctions.flatCache(DensityFunctions.cache2d(densityfunction));
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction input, DensityFunction whenInRange, int minY, int maxY, int whenOutOfRange) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(input, (double) minY, (double) maxY, whenInRange, DensityFunctions.constant((double) whenOutOfRange))
        );
    }
}
