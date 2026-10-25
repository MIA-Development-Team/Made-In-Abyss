package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssNoodleTaper;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.GreatFaultProfile;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import com.altnoir.mementoinabyss.init.MiaDensityFunctionTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MiaDensityFunctions {
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_THE_ABYSS =
            createKey("base_3d_noise_the_abyss");

    public static final ResourceKey<DensityFunction> THE_ABYSS_DEPTH = theAbyssKey("caves/depth");
    public static final ResourceKey<DensityFunction> THE_ABYSS_HOLE_ABOVE =
            theAbyssKey("hole_above");
    public static final ResourceKey<DensityFunction> THE_ABYSS_HOLE_BELOW =
            theAbyssKey("hole_below");
    public static final ResourceKey<DensityFunction> THE_ABYSS_BIG_HOLE =
            theAbyssKey("big_abyss_hole");
    public static final ResourceKey<DensityFunction> THE_ABYSS_INSIDE_HOLE_BELOW =
            theAbyssKey("inside_hole_below");
    public static final ResourceKey<DensityFunction> THE_ABYSS_INSIDE_HOLE_ABOVE =
            theAbyssKey("inside_hole_above");
    public static final ResourceKey<DensityFunction> THE_ABYSS_MIDDLE_BASE_3D =
            theAbyssKey("middle_3d_noise");
    public static final ResourceKey<DensityFunction> THE_ABYSS_OUTSIDE_BASE_3D =
            theAbyssKey("outside_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> THE_ABYSS_LOWER_LAYER_SHIFT =
            theAbyssKey("lower_layer_shift");
    public static final ResourceKey<DensityFunction> THE_ABYSS_LOWER_LAYER_BLEND =
            theAbyssKey("lower_layer_blend");
    public static final ResourceKey<DensityFunction> THE_ABYSS_RAW_TERRAIN =
            theAbyssKey("raw_terrain");
    public static final ResourceKey<DensityFunction> THE_ABYSS_TONGUE_SUPPORT =
            theAbyssKey("tongue_support");
    static final int LOWER_TRANSITION_MIN = -176;
    static final int LOWER_TRANSITION_MAX = -80;
    public static final ResourceKey<DensityFunction> THE_ABYSS_BASE_GREAT_CAVE_NOODLE =
            theAbyssKey("caves/base_great_cave_noodle");
    public static final ResourceKey<DensityFunction> THE_ABYSS_GREAT_CAVE_NOODLE =
            theAbyssKey("caves/great_cave_noodle");
    public static final ResourceKey<DensityFunction> THE_ABYSS_BASE_PILLARS =
            theAbyssKey("caves/base_pillars");
    public static final ResourceKey<DensityFunction> THE_ABYSS_PILLARS =
            theAbyssKey("caves/pillars");
    public static final ResourceKey<DensityFunction> THE_ABYSS_NOODLE = theAbyssKey("caves/noodle");
    public static final ResourceKey<DensityFunction> THE_ABYSS_LOWER_NOODLE =
            theAbyssKey("caves/lower_noodle");

    public static final ResourceKey<DensityFunction> GREAT_FAULT_TERRAIN = greatFaultKey("terrain");
    public static final ResourceKey<DensityFunction> GREAT_FAULT_NOODLE =
            greatFaultKey("caves/noodle");
    public static final ResourceKey<DensityFunction> GREAT_FAULT_BIG_CAVE =
            greatFaultKey("caves/big_cave");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, MementoInAbyss.asResource(location));
    }

    private static ResourceKey<DensityFunction> createVanillaKey(String location) {
        return ResourceKey.create(
                Registries.DENSITY_FUNCTION, Identifier.withDefaultNamespace(location));
    }

    private static ResourceKey<DensityFunction> theAbyssKey(String location) {
        return createKey("the_abyss/" + location);
    }

    private static ResourceKey<DensityFunction> greatFaultKey(String location) {
        return createKey("great_fault/" + location);
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> holdergetter = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = context.lookup(Registries.DENSITY_FUNCTION);

        DensityFunction yFunction = getFunction(holdergetter1, MiaNoiseRouterData.Y);

        DensityFunction densityfunction =
                registerAndWrap(
                        context,
                        MiaNoiseRouterData.SHIFT_X,
                        DensityFunctions.flatCache(
                                DensityFunctions.cache2d(
                                        DensityFunctions.shiftA(
                                                holdergetter.getOrThrow(Noises.SHIFT)))));
        DensityFunction densityfunction1 =
                registerAndWrap(
                        context,
                        MiaNoiseRouterData.SHIFT_Z,
                        DensityFunctions.flatCache(
                                DensityFunctions.cache2d(
                                        DensityFunctions.shiftB(
                                                holdergetter.getOrThrow(Noises.SHIFT)))));
        context.register(
                NoiseRouterData.CONTINENTS,
                DensityFunctions.flatCache(
                        DensityFunctions.shiftedNoise2d(
                                densityfunction,
                                densityfunction1,
                                0.25,
                                holdergetter.getOrThrow(Noises.CONTINENTALNESS))));
        context.register(
                NoiseRouterData.EROSION,
                DensityFunctions.flatCache(
                        DensityFunctions.shiftedNoise2d(
                                densityfunction,
                                densityfunction1,
                                0.25,
                                holdergetter.getOrThrow(Noises.EROSION))));

        // Layer 1-2
        context.register(
                BASE_3D_NOISE_THE_ABYSS,
                DensityFunctions.cacheOnce(
                        BlendedNoise.createUnseeded(0.25, 0.25, 160.0, 160.0, 8.0)));
        context.register(
                THE_ABYSS_LOWER_LAYER_SHIFT,
                DensityFunctions.cache2d(
                        DensityFunctions.mul(
                                        DensityFunctions.noise(
                                                holdergetter.getOrThrow(Noises.SURFACE), 1, 0),
                                        DensityFunctions.constant(14))
                                .clamp(-14, 14)));
        DensityFunction lowerRamp =
                DensityFunctions.cacheOnce(lowerLayerRamp(holdergetter1, -152, -104));
        context.register(
                THE_ABYSS_LOWER_LAYER_BLEND,
                DensityFunctions.cacheOnce(
                        DensityFunctions.mul(
                                DensityFunctions.mul(lowerRamp, lowerRamp),
                                DensityFunctions.add(
                                        DensityFunctions.constant(3),
                                        DensityFunctions.mul(
                                                DensityFunctions.constant(-2), lowerRamp)))));

        context.register(THE_ABYSS_BASE_PILLARS, pillars(holdergetter));
        context.register(
                THE_ABYSS_PILLARS,
                DensityFunctions.rangeChoice(
                        getFunction(holdergetter1, THE_ABYSS_BASE_PILLARS),
                        -1000000,
                        0.06,
                        DensityFunctions.constant(-1000000),
                        getFunction(holdergetter1, THE_ABYSS_BASE_PILLARS)));
        context.register(
                THE_ABYSS_DEPTH,
                DensityFunctions.rangeChoice(
                        yFunction,
                        MiaHeight.THE_ABYSS.minY(),
                        0,
                        DensityFunctions.rangeChoice(
                                yFunction,
                                MiaHeight.THE_ABYSS.minY(),
                                -112,
                                DensityFunctions.constant(3.0),
                                DensityFunctions.constant(2.0)),
                        DensityFunctions.yClampedGradient(
                                MiaHeight.THE_ABYSS.middleY(), 832, 1.5, -2.5)));

        context.register(
                THE_ABYSS_HOLE_ABOVE,
                DensityFunctions.cacheOnce(
                        DensityFunctions.add(
                                MiaDensityFunctionTypes.hopper(),
                                getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS))));
        context.register(
                THE_ABYSS_HOLE_BELOW,
                DensityFunctions.add(
                        MiaDensityFunctionTypes.general(0.0F, 0.3F),
                        getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS)));
        context.register(
                THE_ABYSS_BIG_HOLE, SharedAbyssWall.unbound(HopperAbyssHole.abyssRadius()));
        context.register(
                THE_ABYSS_INSIDE_HOLE_BELOW,
                abyssDensityFunction(
                        holdergetter1,
                        THE_ABYSS_HOLE_BELOW,
                        MiaHeight.THE_ABYSS.minY() + 128,
                        MiaHeight.THE_ABYSS.middleY()));
        context.register(
                THE_ABYSS_INSIDE_HOLE_ABOVE,
                abyssDensityFunction(
                        holdergetter1,
                        THE_ABYSS_HOLE_ABOVE,
                        MiaHeight.THE_ABYSS.middleY(),
                        MiaHeight.THE_ABYSS.maxY() - 64));
        context.register(
                THE_ABYSS_MIDDLE_BASE_3D,
                abyssDensityFunction(
                        holdergetter1,
                        BASE_3D_NOISE_THE_ABYSS,
                        MiaHeight.THE_ABYSS.minY() + 128,
                        MiaHeight.THE_ABYSS.maxY() - 64));
        context.register(
                THE_ABYSS_OUTSIDE_BASE_3D,
                abyssDensityFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS));

        context.register(
                THE_ABYSS_BASE_GREAT_CAVE_NOODLE,
                theAbyssGreatCaveNoodle(holdergetter1, holdergetter));
        context.register(
                THE_ABYSS_GREAT_CAVE_NOODLE,
                new AbyssNoodleTaper(
                        getFunction(holdergetter1, THE_ABYSS_BASE_GREAT_CAVE_NOODLE),
                        HopperAbyssHole.abyssRadius()));
        context.register(THE_ABYSS_NOODLE, abyssNoodle(holdergetter1, holdergetter));
        context.register(
                THE_ABYSS_LOWER_NOODLE,
                abyssNoodleBase(
                        MiaHeight.THE_ABYSS.minY() + 128,
                        MiaHeight.THE_ABYSS.maxY(),
                        holdergetter1,
                        holdergetter,
                        1.5,
                        1.5,
                        2.6666666666666665,
                        -0.5,
                        -0.3,
                        40,
                        true,
                        false,
                        false,
                        true));
        Layer2DensityFunctions.bootstrap(context);
        context.register(
                THE_ABYSS_RAW_TERRAIN, MiaNoiseRouterData.theAbyssRawDensity(holdergetter1));
        context.register(
                THE_ABYSS_TONGUE_SUPPORT, MiaNoiseRouterData.theAbyssTongueSupport(holdergetter1));

        // Layer 3
        context.register(
                GREAT_FAULT_TERRAIN,
                new GreatFaultProfile(
                        getFunction(holdergetter1, BASE_3D_NOISE_THE_ABYSS),
                        MiaHeight.GREAT_FAULT.minY(),
                        MiaHeight.GREAT_FAULT.maxY()));
        context.register(GREAT_FAULT_NOODLE, greatFaultNoodle(holdergetter1, holdergetter));
        context.register(GREAT_FAULT_BIG_CAVE, bigCaves(holdergetter));
    }

    private static DensityFunction theAbyssGreatCaveNoodle(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return abyssNoodle(
                MiaHeight.THE_ABYSS.minY() + 64,
                5,
                densityFunctions,
                noiseParameters,
                0.5,
                0.3,
                0.2,
                true);
    }

    private static DensityFunction abyssNoodle(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return abyssNoodle(
                MiaHeight.THE_ABYSS.minY() + 128,
                MiaHeight.THE_ABYSS.maxY(),
                densityFunctions,
                noiseParameters,
                1.5,
                -0.5,
                -0.3,
                40,
                false);
    }

    private static DensityFunction greatFaultNoodle(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return abyssNoodle(
                MiaHeight.GREAT_FAULT.minY(),
                MiaHeight.GREAT_FAULT.maxY(),
                densityFunctions,
                noiseParameters,
                2.0,
                1.0,
                -0.1,
                -0.15,
                false);
    }

    private static DensityFunction abyssNoodle(
            int minY,
            int maxY,
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            double noiseScale,
            double fromY,
            double toY,
            boolean inverse) {
        return abyssNoodle(
                minY, maxY, densityFunctions, noiseParameters, noiseScale, fromY, toY, 0, inverse);
    }

    private static DensityFunction abyssNoodle(
            int minY,
            int maxY,
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            double noiseScale,
            double fromY,
            double toY,
            int YOffset,
            boolean inverse) {
        return abyssNoodleBase(
                minY,
                maxY,
                densityFunctions,
                noiseParameters,
                noiseScale,
                noiseScale,
                2.6666666666666665,
                fromY,
                toY,
                YOffset,
                true,
                inverse,
                false,
                false);
    }

    private static DensityFunction abyssNoodle(
            int minY,
            int maxY,
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            double noiseScaleX,
            double noiseScaleY,
            double fromY,
            double toY,
            boolean inverse) {
        return abyssNoodleBase(
                minY,
                maxY,
                densityFunctions,
                noiseParameters,
                noiseScaleX,
                noiseScaleY,
                0.5,
                fromY,
                toY,
                0,
                true,
                inverse,
                true,
                false);
    }

    private static DensityFunction abyssNoodleBase(
            int minY,
            int maxY,
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            double noiseScaleX,
            double noiseScaleY,
            double d0,
            double fromY,
            double toY,
            int YOffset,
            boolean ridgeB,
            boolean inverse,
            boolean full,
            boolean extendLowerRidges) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);

        int FMinY = minY - YOffset;
        int FMaxY = maxY + YOffset;

        DensityFunction densityFunction0 =
                yLimitedInterpolatable(
                        yFunction,
                        DensityFunctions.noise(
                                noiseParameters.getOrThrow(Noises.NOODLE),
                                noiseScaleX,
                                noiseScaleY),
                        FMinY,
                        FMaxY,
                        -1);
        // 剃头（看情况再添加）
        DensityFunction densityFunction1 =
                DensityFunctions.add(
                        DensityFunctions.rangeChoice(
                                yFunction,
                                minY,
                                maxY - 64,
                                DensityFunctions.constant(0),
                                DensityFunctions.constant(1)),
                        densityFunction0);
        DensityFunction densityFunction2 =
                yLimitedInterpolatable(
                        yFunction,
                        DensityFunctions.mappedNoise(
                                noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS),
                                1.0,
                                1.0,
                                fromY,
                                toY),
                        FMinY,
                        FMaxY,
                        0);

        double d1;
        if (d0 != 2.6666666666666665) {
            d1 = d0 * 7.5;
        } else {
            d1 = d0;
        }

        DensityFunction densityFunction3 =
                yLimitedInterpolatable(
                        yFunction,
                        DensityFunctions.noise(
                                noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d1, d0),
                        extendLowerRidges ? FMinY : minY,
                        maxY,
                        inverse ? -1 : 0);

        DensityFunction densityFunction4 =
                yLimitedInterpolatable(
                        yFunction,
                        DensityFunctions.noise(
                                noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d1, d0),
                        extendLowerRidges ? FMinY : minY,
                        maxY,
                        inverse ? -1 : 0);

        DensityFunction densityFunction5 =
                DensityFunctions.mul(
                        DensityFunctions.constant(inverse ? -1.5 : 1.5), densityFunction3.abs());
        DensityFunction densityFunction6 =
                DensityFunctions.mul(
                        DensityFunctions.constant(inverse ? -1.5 : 1.5),
                        DensityFunctions.max(densityFunction3.abs(), densityFunction4.abs()));

        DensityFunction densityFunction7 =
                DensityFunctions.rangeChoice(
                        densityFunction0,
                        -1000000.0,
                        0.0,
                        DensityFunctions.constant(inverse ? -1.0 : 64.0),
                        DensityFunctions.add(
                                densityFunction2, ridgeB ? densityFunction6 : densityFunction5));
        return full
                ? DensityFunctions.add(
                        densityFunction2, ridgeB ? densityFunction6 : densityFunction5)
                : densityFunction7;
    }

    private static DensityFunction pillars(
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityFunction =
                DensityFunctions.noise(noiseParameters.getOrThrow(Noises.PILLAR), 25.0, 0.3);
        DensityFunction densityFunction1 =
                DensityFunctions.mappedNoise(
                        noiseParameters.getOrThrow(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction densityFunction2 =
                DensityFunctions.mappedNoise(
                        noiseParameters.getOrThrow(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction densityFunction3 =
                DensityFunctions.add(
                        DensityFunctions.mul(densityFunction, DensityFunctions.constant(3.0)),
                        densityFunction1);
        return DensityFunctions.cacheOnce(
                DensityFunctions.mul(densityFunction3, densityFunction2.cube()));
    }

    private static DensityFunction bigCaves(
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction =
                DensityFunctions.yClampedGradient(
                        MiaHeight.GREAT_FAULT.maxY(), MiaHeight.GREAT_FAULT.maxY() - 64, 1.5, 0.1);
        DensityFunction densityfunction1 =
                DensityFunctions.add(
                        DensityFunctions.noise(
                                noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 7.5),
                        DensityFunctions.mul(
                                DensityFunctions.noise(
                                        noiseParameters.getOrThrow(Noises.PATCH), 0.0, 1.0),
                                DensityFunctions.constant(0.5).cube()));
        DensityFunction densityfunction2 =
                DensityFunctions.cacheOnce(DensityFunctions.add(densityfunction, densityfunction1));
        return DensityFunctions.add(densityfunction2, DensityFunctions.constant(0.3));
    }

    private static DensityFunction abyssDensityFunction(
            HolderGetter<DensityFunction> densityFunctions,
            ResourceKey<DensityFunction> mainFunctionKey) {
        DensityFunction entranceFactor =
                DensityFunctions.mul(
                        DensityFunctions.constant(5.0),
                        getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES));

        return DensityFunctions.min(getFunction(densityFunctions, mainFunctionKey), entranceFactor);
    }

    private static DensityFunction abyssDensityFunction(
            HolderGetter<DensityFunction> densityFunctions,
            ResourceKey<DensityFunction> mainFunctionKey,
            int minY,
            int maxY) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction rangeChoice =
                DensityFunctions.rangeChoice(
                        yFunction,
                        minY,
                        maxY,
                        getFunction(densityFunctions, mainFunctionKey),
                        DensityFunctions.constant(-1));
        DensityFunction entranceFactor =
                DensityFunctions.mul(
                        DensityFunctions.constant(5.0),
                        getFunction(densityFunctions, MiaNoiseRouterData.ENTRANCES));

        return DensityFunctions.min(rangeChoice, entranceFactor);
    }

    static DensityFunction lowerLayerRamp(
            HolderGetter<DensityFunction> functions, int fromY, int toY) {
        DensityFunction shiftedY =
                DensityFunctions.add(
                        getFunction(functions, MiaNoiseRouterData.Y),
                        getFunction(functions, THE_ABYSS_LOWER_LAYER_SHIFT));
        return DensityFunctions.mul(
                        DensityFunctions.add(shiftedY, DensityFunctions.constant(-fromY)),
                        DensityFunctions.constant(1.0 / (toY - fromY)))
                .clamp(0, 1);
    }

    static DensityFunction lowerLayerDensity(
            HolderGetter<DensityFunction> functions,
            ResourceKey<DensityFunction> original,
            ResourceKey<DensityFunction> main) {
        DensityFunction fading =
                lerpWithEndpoints(
                        getFunction(functions, THE_ABYSS_LOWER_LAYER_BLEND),
                        DensityFunctions.constant(-1),
                        getFunction(functions, main));
        DensityFunction entrances =
                DensityFunctions.mul(
                        DensityFunctions.constant(5),
                        getFunction(functions, MiaNoiseRouterData.ENTRANCES));
        return DensityFunctions.rangeChoice(
                getFunction(functions, MiaNoiseRouterData.Y),
                LOWER_TRANSITION_MIN,
                LOWER_TRANSITION_MAX,
                DensityFunctions.min(fading, entrances),
                getFunction(functions, original));
    }

    static DensityFunction lerpWithEndpoints(
            DensityFunction weight, DensityFunction lower, DensityFunction upper) {
        DensityFunction cached = DensityFunctions.cacheOnce(weight);
        DensityFunction inverse =
                DensityFunctions.add(
                        DensityFunctions.mul(cached, DensityFunctions.constant(-1)),
                        DensityFunctions.constant(1));
        DensityFunction mixed =
                DensityFunctions.add(
                        DensityFunctions.mul(lower, inverse), DensityFunctions.mul(upper, cached));
        // Isolate exactly 0 and 1; retain vanilla arithmetic, including positive zero at endpoints.
        return DensityFunctions.rangeChoice(
                cached,
                1,
                Math.nextUp(1.0),
                DensityFunctions.add(upper, DensityFunctions.zero()),
                DensityFunctions.rangeChoice(
                        cached,
                        0,
                        Math.nextUp(0.0),
                        DensityFunctions.add(lower, DensityFunctions.zero()),
                        mixed));
    }

    static DensityFunction lerpDirect(
            DensityFunction weight, DensityFunction lower, DensityFunction upper) {
        // Vanilla lerp inserts CacheOnce. Under a RangeChoice's per-point array fallback,
        // that scalar cache can reuse one point's weight throughout the noise cell.
        return DensityFunctions.add(
                DensityFunctions.mul(
                        DensityFunctions.add(
                                DensityFunctions.constant(1),
                                DensityFunctions.mul(DensityFunctions.constant(-1), weight)),
                        lower),
                DensityFunctions.mul(weight, upper));
    }

    private static DensityFunction smoothstep(DensityFunction value) {
        return DensityFunctions.mul(
                DensityFunctions.mul(value, value),
                DensityFunctions.add(
                        DensityFunctions.constant(3),
                        DensityFunctions.mul(DensityFunctions.constant(-2), value)));
    }

    private static DensityFunction getFunction(
            HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }

    private static DensityFunction registerAndWrap(
            BootstrapContext<DensityFunction> context,
            ResourceKey<DensityFunction> key,
            DensityFunction value) {
        return new DensityFunctions.HolderHolder(context.register(key, value));
    }

    private static DensityFunction yLimitedInterpolatable(
            DensityFunction input,
            DensityFunction whenInRange,
            int minY,
            int maxY,
            int whenOutOfRange) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(
                        input,
                        (double) minY,
                        (double) maxY,
                        whenInRange,
                        DensityFunctions.constant((double) whenOutOfRange)));
    }
}
