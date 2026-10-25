package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.altnoir.mementoinabyss.init.MiaDensityFunctionTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Datagen builds the whole terrain graph from ordinary density nodes. */
public final class Layer2DensityFunctions {
    public static final ResourceKey<DensityFunction> LAYOUT = key("layout");
    public static final ResourceKey<DensityFunction> WEIGHT = key("weight");
    public static final ResourceKey<DensityFunction> RIVER = key("river");
    public static final ResourceKey<DensityFunction> SURFACE = key("surface_height");
    public static final ResourceKey<DensityFunction> UNDERSIDE = key("underside_height");
    public static final ResourceKey<DensityFunction> ROOF = key("roof_height");
    public static final ResourceKey<DensityFunction> PILLAR = key("pillar_core");
    public static final ResourceKey<DensityFunction> CAVES = key("caves");
    public static final ResourceKey<DensityFunction> TERRAIN = key("terrain");
    public static final ResourceKey<DensityFunction> FLOODEDNESS = key("floodedness");

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

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        var functions = context.lookup(Registries.DENSITY_FUNCTION);
        var noises = context.lookup(Registries.NOISE);
        DensityFunction y = get(functions, MiaNoiseRouterData.Y);
        context.register(
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
        context.register(
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
        context.register(
                RIVER,
                DensityFunctions.cache2d(
                        DensityFunctions.mul(
                                smooth(
                                        scale(add(MiaDensityFunctionTypes.general(32, .3F), -4), .5)
                                                .clamp(0, 1)),
                                smooth(add(scale(edge, -1), 1).clamp(0, 1)))));
        DensityFunction relief =
                DensityFunctions.cache2d(
                        DensityFunctions.noise(noises.getOrThrow(MiaNoiseData.LAYER2_RELIEF), 1, 0)
                                .clamp(-1, 1));
        context.register(
                SURFACE,
                DensityFunctions.cache2d(
                        MiaDensityFunctions.lerpDirect(
                                get(functions, RIVER),
                                add(scale(relief, 6), -104),
                                add(scale(relief, 2), -120))));
        context.register(UNDERSIDE, DensityFunctions.cache2d(add(scale(relief, 6), -152)));
        DensityFunction pillarNoise =
                DensityFunctions.cache2d(
                        DensityFunctions.noise(
                                noises.getOrThrow(MiaNoiseData.LAYER2_PILLAR), 1, 0));
        context.register(
                PILLAR,
                DensityFunctions.cache2d(
                        DensityFunctions.min(
                                scale(add(pillarNoise, -.35), 2),
                                scale(add(edge, -5 / 3.0), 1.5))));
        DensityFunction surface = get(functions, SURFACE);
        DensityFunction floor =
                DensityFunctions.min(
                        scale(DensityFunctions.add(surface, scale(y, -1)), 1 / 16.0),
                        scale(
                                DensityFunctions.add(y, scale(get(functions, UNDERSIDE), -1)),
                                1 / 16.0));
        DensityFunction neck = add(scale(add(y, 60).abs(), .004), -.10);
        DensityFunction pillar =
                DensityFunctions.min(
                        DensityFunctions.add(get(functions, PILLAR), neck),
                        DensityFunctions.min(
                                scale(add(y, 152), 1 / 16.0),
                                scale(add(scale(y, -1), 16), 1 / 16.0)));
        // The river exclusion applies after the flared ends, so no base can regrow in a channel.
        pillar = DensityFunctions.min(pillar, scale(add(edge, -5 / 3.0), 1.5));
        DensityFunction roofHeight =
                add(
                        DensityFunctions.add(
                                scale(relief, 4), scale(add(pillarNoise, -.1).clamp(0, 1), -40)),
                        -16);
        context.register(ROOF, DensityFunctions.cache2d(roofHeight));
        DensityFunction roof =
                scale(DensityFunctions.add(y, scale(get(functions, ROOF), -1)), 1 / 16.0);
        DensityFunction detail =
                scale(
                        add(get(functions, MiaDensityFunctions.BASE_3D_NOISE_THE_ABYSS), 0)
                                .clamp(-3, 3),
                        .04);
        context.register(
                TERRAIN,
                DensityFunctions.add(
                        DensityFunctions.max(DensityFunctions.max(floor, pillar), roof), detail));

        DensityFunction a = limitedNoise(y, noises, MiaNoiseData.LAYER2_CAVE_A, 1);
        DensityFunction b = limitedNoise(y, noises, MiaNoiseData.LAYER2_CAVE_B, 1.5);
        DensityFunction thickness =
                DensityFunctions.cache2d(
                        DensityFunctions.mappedNoise(
                                noises.getOrThrow(MiaNoiseData.LAYER2_CAVE_THICKNESS),
                                1,
                                0,
                                .065,
                                .095));
        DensityFunction cave =
                DensityFunctions.add(DensityFunctions.max(a.abs(), b.abs()), scale(thickness, -1));
        cave =
                DensityFunctions.max(
                        cave,
                        DensityFunctions.max(
                                DensityFunctions.yClampedGradient(-168, -144, 1, -1),
                                DensityFunctions.yClampedGradient(-152, -128, -1, 1)));
        cave =
                DensityFunctions.max(
                        cave, scale(add(DensityFunctions.add(y, scale(surface, -1)), 8), 1 / 16.0));
        cave =
                DensityFunctions.max(
                        cave,
                        DensityFunctions.max(
                                get(functions, PILLAR), scale(add(scale(edge, -1), 5 / 3.0), 1.5)));
        context.register(
                CAVES,
                DensityFunctions.rangeChoice(
                        y,
                        -168,
                        -128,
                        MiaDensityFunctions.lerpDirect(
                                get(functions, WEIGHT), DensityFunctions.constant(1), cave),
                        DensityFunctions.constant(1)));
        // Aquifer cell centres extend beyond the actual water band; the boundary adapter clips
        // blocks.
        context.register(
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

    private static DensityFunction limitedNoise(
            DensityFunction y,
            HolderGetter<NormalNoise.NoiseParameters> noises,
            ResourceKey<NormalNoise.NoiseParameters> key,
            double verticalScale) {
        return DensityFunctions.interpolated(
                DensityFunctions.rangeChoice(
                        y,
                        -168,
                        -128,
                        DensityFunctions.noise(noises.getOrThrow(key), 1, verticalScale),
                        DensityFunctions.zero()));
    }

    private Layer2DensityFunctions() {}
}
