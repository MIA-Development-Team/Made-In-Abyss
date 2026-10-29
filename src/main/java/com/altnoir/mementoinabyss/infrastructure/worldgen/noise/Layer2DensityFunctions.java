package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.api.worldgen.Layer2TerrainKind;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.DensityGraphNode;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2TerrainParameters;
import com.altnoir.mementoinabyss.init.MiaDensityFunctionTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/** Datagen builds the whole terrain graph from ordinary density nodes. */
public final class Layer2DensityFunctions {
    public static final ResourceKey<DensityFunction> LAYOUT = key("layout");
    public static final ResourceKey<DensityFunction> WEIGHT = key("weight");
    public static final ResourceKey<DensityFunction> RIVER = key("river");
    public static final ResourceKey<DensityFunction> SURFACE_BASE = key("surface_base");
    public static final ResourceKey<DensityFunction> ROOF_BASE = key("roof_base");
    public static final ResourceKey<DensityFunction> SUPPORT = key("support_strength");
    public static final ResourceKey<DensityFunction> SURFACE = key("surface_height");
    public static final ResourceKey<DensityFunction> UNDERSIDE = key("underside_height");
    public static final ResourceKey<DensityFunction> ROOF = key("roof_height");
    public static final ResourceKey<DensityFunction> PILLAR = key("pillar_core");
    public static final ResourceKey<DensityFunction> CAVES = key("caves");
    public static final ResourceKey<DensityFunction> TERRAIN = key("terrain");
    public static final ResourceKey<DensityFunction> FLOODEDNESS = key("floodedness");
    private static final ResourceKey<DensityFunction> TERRAIN_KIND = key("terrain_kind");
    private static final ResourceKey<DensityFunction> PRIMARY_BLEND = key("primary_blend");
    private static final ResourceKey<DensityFunction> REGION_BLEND = key("region_blend");
    private static final ResourceKey<DensityFunction> RELIEF = key("relief");
    private static final ResourceKey<DensityFunction> OFFSET = key("offset");
    private static final ResourceKey<DensityFunction> RELIEF_AMPLITUDE = key("relief_amplitude");
    private static final ResourceKey<DensityFunction> FACTOR = key("factor");
    private static final ResourceKey<DensityFunction> JAGGEDNESS = key("jaggedness");
    private static final ResourceKey<DensityFunction> CLEARANCE = key("clearance");
    private static final ResourceKey<DensityFunction> PILLAR_NOISE = key("pillar_noise");
    private static final ResourceKey<DensityFunction> CLEAR_SPACE = key("clear_space");
    private static final ResourceKey<DensityFunction> END_BAND_INVERSE = key("end_band_inverse");
    private static final ResourceKey<DensityFunction> DETAIL_BUDGET = key("detail_budget");
    private static final ResourceKey<DensityFunction> DETAIL_NOISE = key("detail_noise");
    private static final ResourceKey<DensityFunction> FLOOR_AMPLITUDE = key("floor_amplitude");
    private static final ResourceKey<DensityFunction> ROOF_AMPLITUDE = key("roof_amplitude");
    private static final ResourceKey<DensityFunction> PILLAR_SHAPE = key("pillar_shape");

    private static void register(
            BootstrapContext<DensityFunction> context,
            ResourceKey<DensityFunction> key,
            DensityFunction function) {
        context.register(key, new DensityGraphNode(function));
    }

    private static ResourceKey<DensityFunction> key(String name) {
        return ResourceKey.create(
                Registries.DENSITY_FUNCTION, MementoInAbyss.asResource("the_abyss/layer2/" + name));
    }

    static DensityFunction get(
            HolderGetter<DensityFunction> functions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(functions.getOrThrow(key));
    }

    private static DensityFunction add(DensityFunction f, double value) {
        return DensityFunctions.add(f, DensityFunctions.constant(value));
    }

    private static DensityFunction scale(DensityFunction f, double value) {
        return DensityFunctions.mul(f, DensityFunctions.constant(value));
    }

    private static DensityFunction smooth(DensityFunction t) {
        return DensityFunctions.mul(DensityFunctions.mul(t, t), add(scale(t, -2), 3));
    }

    private static DensityFunction regional(
            DensityFunction blend, double outside, DensityFunction inside) {
        return add(DensityFunctions.mul(blend, add(inside, -outside)), outside);
    }

    private static DensityFunction kinds(
            DensityFunction kind,
            DensityFunction primary,
            DensityFunction woodland,
            DensityFunction wetland,
            DensityFunction highland,
            DensityFunction plateau,
            DensityFunction wetlandCore) {
        DensityFunction original =
                DensityFunctions.rangeChoice(
                        kind,
                        0,
                        1,
                        woodland,
                        DensityFunctions.rangeChoice(
                                kind,
                                1,
                                2,
                                wetland,
                                DensityFunctions.rangeChoice(kind, 2, 3, highland, plateau)));
        DensityFunction companion = DensityFunctions.rangeChoice(kind, 4, 5, woodland, highland);
        return DensityFunctions.rangeChoice(
                kind,
                4,
                6,
                MiaDensityFunctions.lerpDirect(primary, companion, wetlandCore),
                original);
    }

    private static DensityFunction oceanKinds(
            DensityFunction kind,
            DensityFunction primary,
            double woodland,
            double ocean,
            DensityFunction original) {
        DensityFunction sea = DensityFunctions.constant(ocean);
        DensityFunction land = DensityFunctions.constant(woodland);
        int oceanKind = Layer2TerrainKind.OCEAN.ordinal();
        int oceanWoodland = Layer2TerrainKind.OCEAN_WOODLAND.ordinal();
        int woodlandOcean = Layer2TerrainKind.WOODLAND_OCEAN.ordinal();
        return DensityFunctions.rangeChoice(
                kind,
                oceanKind,
                woodlandOcean + 1,
                DensityFunctions.rangeChoice(
                        kind,
                        oceanKind,
                        oceanWoodland,
                        sea,
                        DensityFunctions.rangeChoice(
                                kind,
                                oceanWoodland,
                                woodlandOcean,
                                MiaDensityFunctions.lerpDirect(primary, land, sea),
                                MiaDensityFunctions.lerpDirect(primary, sea, land))),
                original);
    }

    private static DensityFunction parameter(DensityFunction function) {
        return DensityFunctions.flatCache(DensityFunctions.cache2d(function));
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        var functions = context.lookup(Registries.DENSITY_FUNCTION);
        var noises = context.lookup(Registries.NOISE);
        DensityFunction y = get(functions, MiaNoiseRouterData.Y);
        register(
                context,
                LAYOUT,
                DensityFunctions.flatCache(
                        DensityFunctions.cache2d(
                                new Layer2Layout(
                                        1024,
                                        .24,
                                        .38,
                                        24,
                                        scale(
                                                DensityFunctions.noise(
                                                        noises.getOrThrow(
                                                                MiaNoiseData.LAYER2_WARP_X),
                                                        1,
                                                        0),
                                                224),
                                        scale(
                                                DensityFunctions.noise(
                                                        noises.getOrThrow(
                                                                MiaNoiseData.LAYER2_WARP_Z),
                                                        1,
                                                        0),
                                                224),
                                        -128,
                                        -112,
                                        .3F,
                                        32,
                                        0x48f2b971ac735de0L))));
        DensityFunction radial =
                DensityFunctions.cache2d(
                        smooth(
                                scale(add(MiaDensityFunctionTypes.general(32, .3F), -1), 1 / 3.0)
                                        .clamp(0, 1)));
        register(
                context,
                WEIGHT,
                DensityFunctions.rangeChoice(
                        y,
                        -176,
                        16,
                        DensityFunctions.mul(
                                radial,
                                DensityFunctions.mul(
                                        smooth(DensityFunctions.yClampedGradient(-176, -152, 0, 1)),
                                        smooth(DensityFunctions.yClampedGradient(0, 16, 1, 0)))),
                        DensityFunctions.zero()));
        DensityFunction edge = get(functions, LAYOUT);
        register(
                context,
                RIVER,
                DensityFunctions.cache2d(
                        DensityFunctions.mul(
                                smooth(
                                        scale(add(MiaDensityFunctionTypes.general(32, .3F), -4), .5)
                                                .clamp(0, 1)),
                                smooth(add(scale(edge, -1), 1).clamp(0, 1)))));
        register(
                context,
                TERRAIN_KIND,
                parameter(
                        new Layer2TerrainParameters(
                                edge, Layer2TerrainParameters.Parameter.TERRAIN_KIND)));
        register(
                context,
                PRIMARY_BLEND,
                parameter(
                        new Layer2TerrainParameters(
                                edge, Layer2TerrainParameters.Parameter.PRIMARY_BLEND)));
        register(
                context,
                REGION_BLEND,
                DensityFunctions.cache2d(smooth(scale(edge, 24 / 64.0).clamp(0, 1))));
        DensityFunction kind = get(functions, TERRAIN_KIND);
        DensityFunction primary = get(functions, PRIMARY_BLEND);
        DensityFunction region = get(functions, REGION_BLEND);
        DensityFunction wetlandOffset = add(scale(primary, 15), -115);
        DensityFunction wetlandCoreOffset = DensityFunctions.constant(-115);
        register(
                context,
                OFFSET,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                -106,
                                -121,
                                DensityFunctions.rangeChoice(
                                        kind,
                                        1,
                                        2,
                                        wetlandOffset,
                                        regional(
                                                region,
                                                -100,
                                                kinds(
                                                        kind,
                                                        primary,
                                                        DensityFunctions.constant(-106),
                                                        wetlandOffset,
                                                        DensityFunctions.constant(-88),
                                                        add(scale(primary, 16), -88),
                                                        wetlandCoreOffset))))));
        DensityFunction wetlandRelief = add(scale(primary, 2.2), .8);
        register(
                context,
                RELIEF_AMPLITUDE,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                6,
                                .25,
                                DensityFunctions.rangeChoice(
                                        kind,
                                        1,
                                        2,
                                        wetlandRelief,
                                        regional(
                                                region,
                                                6,
                                                kinds(
                                                        kind,
                                                        primary,
                                                        DensityFunctions.constant(6),
                                                        wetlandRelief,
                                                        DensityFunctions.constant(6),
                                                        add(scale(primary, -2), 6),
                                                        DensityFunctions.constant(.8)))))));
        register(
                context,
                FACTOR,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                0.0625,
                                0.0625,
                                regional(
                                        region,
                                        1 / 16.0,
                                        kinds(
                                                kind,
                                                primary,
                                                DensityFunctions.constant(1 / 16.0),
                                                DensityFunctions.constant(.05),
                                                DensityFunctions.constant(1 / 14.0),
                                                DensityFunctions.constant(1 / 16.0),
                                                DensityFunctions.constant(.05))))));
        register(
                context,
                JAGGEDNESS,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                0.025,
                                0.005,
                                regional(
                                        region,
                                        .025,
                                        kinds(
                                                kind,
                                                primary,
                                                DensityFunctions.constant(.025),
                                                DensityFunctions.constant(.01),
                                                DensityFunctions.constant(.04),
                                                add(scale(primary, -.025), .04),
                                                DensityFunctions.constant(.01))))));
        register(
                context,
                CLEARANCE,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                72,
                                72,
                                regional(
                                        region,
                                        72,
                                        kinds(
                                                kind,
                                                primary,
                                                DensityFunctions.constant(72),
                                                DensityFunctions.constant(64),
                                                DensityFunctions.constant(64),
                                                add(scale(primary, -8), 64),
                                                DensityFunctions.constant(64))))));
        register(
                context,
                RELIEF,
                DensityFunctions.cache2d(
                        DensityFunctions.add(
                                        scale(
                                                DensityFunctions.noise(
                                                        noises.getOrThrow(
                                                                MiaNoiseData.LAYER2_RELIEF),
                                                        1,
                                                        0),
                                                .8),
                                        scale(
                                                DensityFunctions.add(
                                                        get(
                                                                functions,
                                                                MiaNoiseRouterData.CONTINENTS),
                                                        get(functions, MiaNoiseRouterData.EROSION)),
                                                .1))
                                .clamp(-1, 1)));
        DensityFunction relief = get(functions, RELIEF);
        register(
                context,
                PILLAR_NOISE,
                DensityFunctions.cache2d(
                        DensityFunctions.noise(
                                noises.getOrThrow(MiaNoiseData.LAYER2_PILLAR), 1, 0)));
        DensityFunction pillarNoise = get(functions, PILLAR_NOISE);
        DensityFunction wetlandIslands =
                DensityFunctions.rangeChoice(
                        kind,
                        1,
                        2,
                        DensityFunctions.mul(
                                add(scale(primary, -1), 1),
                                scale(smooth(scale(pillarNoise, 4).clamp(0, 1)), 3.5)),
                        DensityFunctions.zero());
        DensityFunction islands =
                DensityFunctions.rangeChoice(
                        kind,
                        4,
                        6,
                        DensityFunctions.mul(
                                primary,
                                scale(smooth(scale(add(pillarNoise, -.3), 4).clamp(0, 1)), 3.5)),
                        wetlandIslands);
        register(
                context,
                SURFACE_BASE,
                DensityFunctions.cache2d(
                        MiaDensityFunctions.lerpDirect(
                                get(functions, RIVER),
                                DensityFunctions.add(
                                        get(functions, OFFSET),
                                        DensityFunctions.add(
                                                DensityFunctions.mul(
                                                        relief, get(functions, RELIEF_AMPLITUDE)),
                                                islands)),
                                add(scale(relief, 2), -120))));
        register(context, UNDERSIDE, DensityFunctions.cache2d(add(scale(relief, 6), -152)));
        register(
                context,
                ROOF_BASE,
                DensityFunctions.cache2d(
                        DensityFunctions.max(
                                DensityFunctions.add(
                                        get(functions, SURFACE_BASE), get(functions, CLEARANCE)),
                                add(
                                        DensityFunctions.add(
                                                scale(relief, 4),
                                                scale(add(pillarNoise, -.1).clamp(0, 1), -40)),
                                        -16))));
        // The same partition that lowers the riverbed also opens the space between floor and roof.
        DensityFunction land = smooth(scale(add(edge, -1), 24 / 64.0).clamp(0, 1));
        register(
                context,
                SUPPORT,
                DensityFunctions.cache2d(
                        DensityFunctions.mul(
                                land,
                                scale(
                                        smooth(scale(add(pillarNoise, -.1), 2.5).clamp(0, 1)),
                                        1.25))));
        register(
                context,
                CLEAR_SPACE,
                parameter(
                        DensityFunctions.add(
                                get(functions, ROOF_BASE),
                                scale(get(functions, SURFACE_BASE), -1))));
        register(context, SURFACE, DensityFunctions.cache2d(get(functions, SURFACE_BASE)));
        register(context, ROOF, DensityFunctions.cache2d(get(functions, ROOF_BASE)));
        DensityFunction surface = get(functions, SURFACE);
        DensityFunction roofHeight = get(functions, ROOF);
        DensityFunction factor = get(functions, FACTOR);
        DensityFunction gap = get(functions, CLEAR_SPACE);
        register(
                context,
                PILLAR,
                DensityFunctions.cache2d(scale(add(get(functions, SUPPORT), -1), 4)));
        // This reciprocal overestimates 5/gap for native gaps 56..80, keeping each end below 20%.
        register(
                context,
                END_BAND_INVERSE,
                parameter(
                        DensityFunctions.max(
                                DensityFunctions.constant(1 / 16.0),
                                add(scale(gap, -1 / 896.0), 17 / 112.0))));
        register(context, DETAIL_BUDGET, parameter(scale(add(gap, -48), 1 / 24.0).clamp(0, 1)));
        DensityFunction wetlandAmplitude = add(scale(primary, 5.5), .5);
        DensityFunction floorAmplitude =
                DensityFunctions.rangeChoice(
                        kind,
                        1,
                        2,
                        wetlandAmplitude,
                        regional(
                                region,
                                6,
                                kinds(
                                        kind,
                                        primary,
                                        DensityFunctions.constant(6),
                                        wetlandAmplitude,
                                        DensityFunctions.constant(8),
                                        DensityFunctions.constant(4),
                                        DensityFunctions.constant(.5))));
        floorAmplitude = oceanKinds(kind, primary, 6, .5, floorAmplitude);
        register(
                context,
                FLOOR_AMPLITUDE,
                parameter(
                        DensityFunctions.mul(
                                get(functions, DETAIL_BUDGET),
                                MiaDensityFunctions.lerpDirect(
                                        get(functions, RIVER),
                                        floorAmplitude,
                                        DensityFunctions.constant(.5)))));
        register(
                context,
                ROOF_AMPLITUDE,
                parameter(
                        oceanKinds(
                                kind,
                                primary,
                                12,
                                12,
                                DensityFunctions.mul(
                                        get(functions, DETAIL_BUDGET),
                                        regional(
                                                region,
                                                12,
                                                kinds(
                                                        kind,
                                                        primary,
                                                        DensityFunctions.constant(12),
                                                        DensityFunctions.constant(12),
                                                        DensityFunctions.constant(16),
                                                        DensityFunctions.constant(12),
                                                        DensityFunctions.constant(12)))))));
        var noiseCoordinate =
                new DensityFunctions.Spline.Coordinate(
                        functions.getOrThrow(MiaDensityFunctions.BASE_3D_NOISE_THE_ABYSS));
        register(
                context,
                DETAIL_NOISE,
                DensityFunctions.cacheOnce(
                        DensityFunctions.spline(
                                CubicSpline
                                        .<DensityFunctions.Spline.Point,
                                                DensityFunctions.Spline.Coordinate>
                                                builder(noiseCoordinate)
                                        .addPoint(-3, -1, 0)
                                        .addPoint(-1, -.8F, .25F)
                                        .addPoint(0, 0, 1)
                                        .addPoint(1, .8F, .25F)
                                        .addPoint(3, 1, 0)
                                        .build())));
        DensityFunction lowerDistance = DensityFunctions.add(y, scale(surface, -1));
        DensityFunction upperDistance = DensityFunctions.add(roofHeight, scale(y, -1));
        DensityFunction inverseBand = get(functions, END_BAND_INVERSE);
        DensityFunction ends =
                DensityFunctions.max(
                        smooth(
                                add(scale(DensityFunctions.mul(lowerDistance, inverseBand), -1), 1)
                                        .clamp(0, 1)),
                        smooth(
                                add(scale(DensityFunctions.mul(upperDistance, inverseBand), -1), 1)
                                        .clamp(0, 1)));
        DensityFunction pillarBounds =
                DensityFunctions.min(
                        DensityFunctions.mul(add(lowerDistance, 24), factor),
                        DensityFunctions.mul(add(upperDistance, 24), factor));
        register(
                context,
                PILLAR_SHAPE,
                DensityFunctions.min(
                        DensityFunctions.add(
                                get(functions, PILLAR),
                                DensityFunctions.mul(get(functions, SUPPORT), ends)),
                        pillarBounds));
        DensityFunction detailNoise = get(functions, DETAIL_NOISE);
        DensityFunction floorDetail =
                DensityFunctions.mul(
                        DensityFunctions.mul(detailNoise, get(functions, FLOOR_AMPLITUDE)), factor);
        DensityFunction roofDetail =
                DensityFunctions.mul(
                        DensityFunctions.mul(detailNoise, get(functions, ROOF_AMPLITUDE)), factor);
        DensityFunction undersideDetail =
                DensityFunctions.mul(
                        DensityFunctions.mul(
                                add(get(functions, MiaDensityFunctions.BASE_3D_NOISE_THE_ABYSS), 0)
                                        .clamp(-3, 3),
                                get(functions, JAGGEDNESS)),
                        scale(factor, 16));
        DensityFunction floor =
                DensityFunctions.min(
                        DensityFunctions.add(
                                DensityFunctions.mul(scale(lowerDistance, -1), factor),
                                floorDetail),
                        DensityFunctions.add(
                                DensityFunctions.mul(
                                        DensityFunctions.add(
                                                y, scale(get(functions, UNDERSIDE), -1)),
                                        factor),
                                undersideDetail));
        DensityFunction roof =
                DensityFunctions.add(
                        DensityFunctions.mul(scale(upperDistance, -1), factor), roofDetail);
        DensityFunction pillar =
                DensityFunctions.min(
                        DensityFunctions.add(get(functions, PILLAR_SHAPE), scale(detailNoise, .1)),
                        pillarBounds);
        register(context, TERRAIN, DensityFunctions.max(DensityFunctions.max(floor, roof), pillar));

        DensityFunction thickness =
                add(
                        scale(get(functions, MiaDensityFunctions.SHARED_NOODLE_THICKNESS), -.0225),
                        .12);
        DensityFunction upperFade = smooth(DensityFunctions.yClampedGradient(-24, -16, 1, 0));
        DensityFunction cave =
                DensityFunctions.add(
                        scale(get(functions, MiaDensityFunctions.SHARED_NOODLE_RIDGES), 1.5),
                        scale(DensityFunctions.mul(thickness, upperFade), -1));
        DensityFunction wet =
                DensityFunctions.cache2d(
                        DensityFunctions.max(
                                get(functions, RIVER),
                                smooth(scale(add(scale(surface, -1), -110), .25).clamp(0, 1))));
        DensityFunction bedGuard =
                DensityFunctions.rangeChoice(
                        wet,
                        0,
                        Math.nextUp(0.0),
                        DensityFunctions.constant(-1),
                        MiaDensityFunctions.lerpDirect(
                                wet,
                                DensityFunctions.constant(-1),
                                scale(
                                        DensityFunctions.min(
                                                add(DensityFunctions.add(y, scale(surface, -1)), 5),
                                                add(
                                                        DensityFunctions.add(surface, scale(y, -1)),
                                                        1)),
                                        1 / 8.0)));
        DensityFunction protectedCave =
                DensityFunctions.max(
                        DensityFunctions.max(
                                DensityFunctions.yClampedGradient(-168, -144, 1, -1), bedGuard),
                        cave);
        protectedCave = DensityFunctions.max(get(functions, PILLAR), protectedCave);
        // Positive samples already contain rock. Preserve the .03 tongue-root threshold as well.
        cave = DensityFunctions.rangeChoice(cave, .03, 1000000, cave, protectedCave);
        register(
                context,
                CAVES,
                DensityFunctions.rangeChoice(
                        y,
                        -168,
                        -16,
                        MiaDensityFunctions.lerpDirect(
                                get(functions, WEIGHT), DensityFunctions.constant(1), cave),
                        DensityFunctions.constant(1)));
        // Aquifer cell centres extend beyond the actual water band; the boundary adapter clips
        // blocks.
        register(
                context,
                FLOODEDNESS,
                DensityFunctions.rangeChoice(
                        y,
                        -152,
                        -88,
                        DensityFunctions.rangeChoice(
                                MiaDensityFunctionTypes.general(32, .3F),
                                -1000000,
                                0,
                                DensityFunctions.constant(-1),
                                DensityFunctions.constant(1)),
                        DensityFunctions.constant(-1)));
    }

    private Layer2DensityFunctions() {}
}
