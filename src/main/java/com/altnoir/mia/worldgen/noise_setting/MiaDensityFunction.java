package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.worldgen.MiaDensityFunctionTypes;
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
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_ABYSS_EDGE = abyssEdgeKey("base_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_DEPTH = abyssEdgeKey("depth");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_HOLE = abyssEdgeKey("abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_BIG_HOLE = abyssEdgeKey("big_abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_INSIDE_HOLE = abyssEdgeKey("inside_abyss_hole");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_MIDDLE_BASE_3D = abyssEdgeKey("middle_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_OUTSIDE_BASE_3D = abyssEdgeKey("outside_abyss_3d_noise");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_PILLARS = abyssEdgeKey("caves/pillars");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_ABYSS_PILLARS = abyssEdgeKey("caves/abyss_pillars");
    public static final ResourceKey<DensityFunction> ABYSS_EDGE_NOODLE = abyssEdgeKey("caves/noodle");

    public static final ResourceKey<DensityFunction> TEMPTATION_FOREST_HOLE = temptationForestKey("abyss_hole");
    public static final ResourceKey<DensityFunction> TEMPTATION_FOREST_INSIDE_HOLE = temptationForestKey("inside_abyss_hole");
    public static final ResourceKey<DensityFunction> TEMPTATION_FOREST_NOODLE = temptationForestKey("caves/noodle");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, location));
    }

    private static ResourceKey<DensityFunction> abyssEdgeKey(String location) {
        return createKey("abyss_edge/" + location);
    }

    private static ResourceKey<DensityFunction> temptationForestKey(String location) {
        return createKey("temptation_forest/" + location);
    }

    public static Holder<? extends DensityFunction> bootstrap(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> holdergetter = context.lookup(Registries.NOISE);
        HolderGetter<DensityFunction> holdergetter1 = context.lookup(Registries.DENSITY_FUNCTION);

        context.register(ABYSS_EDGE_DEPTH, DensityFunctions.add(DensityFunctions.yClampedGradient(0, 512, 2.5, -1.5), getFunction(holdergetter1, MiaNoiseRouterData.OFFSET)));
        context.register(ABYSS_EDGE_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.hopperAbyssHole(0.0F), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_EDGE)));
        context.register(ABYSS_EDGE_BIG_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.generalAbyssHole(128.0F), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_EDGE)));
        context.register(ABYSS_EDGE_INSIDE_HOLE, insideAbyssEdgeHole(holdergetter1));
        context.register(ABYSS_EDGE_MIDDLE_BASE_3D, middleAbyssEdgeBASE3D(holdergetter1));
        context.register(ABYSS_EDGE_OUTSIDE_BASE_3D, outsideAbyssEdgeBASE3D(holdergetter1));
        context.register(ABYSS_EDGE_PILLARS, pillars(holdergetter));
        context.register(ABYSS_EDGE_ABYSS_PILLARS, abyssEdgePillars(holdergetter1));
        context.register(ABYSS_EDGE_NOODLE, abyssEdgeNoodle(holdergetter1, holdergetter));

        context.register(TEMPTATION_FOREST_HOLE, DensityFunctions.add(MiaDensityFunctionTypes.generalAbyssHole(0.0F), getFunction(holdergetter1, BASE_3D_NOISE_ABYSS_EDGE)));
        context.register(TEMPTATION_FOREST_INSIDE_HOLE, insideTempationForestHole(holdergetter1));
        context.register(TEMPTATION_FOREST_NOODLE, tempationForestNoodle(holdergetter1, holdergetter));

        return context.register(BASE_3D_NOISE_ABYSS_EDGE, BlendedNoise.createUnseeded(0.25, 0.25, 160.0, 160.0, 8.0));
    }

    private static DensityFunction abyssEdgeNoodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return noodle(5, 360, densityFunctions, noiseParameters, 0, 40);
    }

    private static DensityFunction tempationForestNoodle(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return noodle(-5, 220, densityFunctions, noiseParameters, -40, 0);
    }

    private static DensityFunction noodle(int minY, int maxY, HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters, int minYOffset, int maxYOffset) {
        DensityFunction densityfunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        double d0 = 2.6666666666666665;

        int FMinY = minY + minYOffset;
        int FMaxY = maxY + maxYOffset;

        DensityFunction densityFunction1 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.constant(0.0), FMinY, FMaxY, -1
        );

        DensityFunction densityFunction2 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS),
                        1.0, 1.0, -0.1, -0.15),
                FMinY, FMaxY, 0
        );

        DensityFunction densityFunction3 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0),
                minY, maxY, 0
        );

        DensityFunction densityFunction4 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0),
                minY, maxY, 0
        );

        DensityFunction densityFunction5 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction3.abs(), DensityFunctions.zero().abs())
        );

        return DensityFunctions.rangeChoice(
                densityFunction1, -1000000.0, 0.0, DensityFunctions.constant(64.0),
                DensityFunctions.add(densityFunction2, densityFunction5)
        );
    }

    private static DensityFunction noodle2(int minY, int maxY, HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction densityfunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        double d0 = 2.6666666666666665;
        DensityFunction densityFunction1 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.constant(0.0), minY - 40, maxY, -1
        );
        DensityFunction densityFunction2 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.mappedNoise(noiseParameters.getOrThrow(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.1, -0.15), minY - 40, maxY, 0
        );
        DensityFunction densityFunction3 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_A), d0, d0), minY, maxY, 0
        );
        DensityFunction densityFunction4 = yLimitedInterpolatable(
                densityfunction, DensityFunctions.noise(noiseParameters.getOrThrow(Noises.NOODLE_RIDGE_B), d0, d0), minY, maxY, 0
        );
        DensityFunction densityFunction5 = DensityFunctions.mul(
                DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction3.abs(), DensityFunctions.zero().abs())
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

    private static DensityFunction abyssEdgePillars(HolderGetter<DensityFunction> densityFunctions) {
        return DensityFunctions.rangeChoice(
                getFunction(densityFunctions, ABYSS_EDGE_PILLARS), -1000000, 0.03, DensityFunctions.constant(-1000000), getFunction(densityFunctions, ABYSS_EDGE_PILLARS)
        );
    }

    private static DensityFunction insideAbyssEdgeHole(HolderGetter<DensityFunction> densityFunctions) {
        return abyssEdgeDensityFunction(densityFunctions, ABYSS_EDGE_HOLE, 0, 300, -1.0);
    }

    private static DensityFunction middleAbyssEdgeBASE3D(HolderGetter<DensityFunction> densityFunctions) {
        return abyssEdgeDensityFunction(densityFunctions, BASE_3D_NOISE_ABYSS_EDGE, 0, 300, -1.0);
    }

    private static DensityFunction outsideAbyssEdgeBASE3D(HolderGetter<DensityFunction> densityFunctions) {
        return abyssEdgeDensityFunction(densityFunctions, BASE_3D_NOISE_ABYSS_EDGE, 24, 320, 1.0);
    }

    private static DensityFunction insideTempationForestHole(HolderGetter<DensityFunction> densityFunctions) {
        return abyssEdgeDensityFunction(densityFunctions, TEMPTATION_FOREST_HOLE, -64, 256, -1.0);
    }

    private static DensityFunction abyssEdgeDensityFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> mainFunctionKey, int minY, int maxY, double outOfRangeValue) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction rangeChoice = DensityFunctions.rangeChoice(yFunction,
                minY,
                maxY,
                getFunction(densityFunctions, mainFunctionKey),
                DensityFunctions.constant(outOfRangeValue)
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

    private static DensityFunction yLimitedInterpolatable(DensityFunction input, DensityFunction whenInRange, int minY, int maxY, int whenOutOfRange) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(input, (double) minY, (double) maxY, whenInRange, DensityFunctions.constant((double) whenOutOfRange))
        );
    }
}
