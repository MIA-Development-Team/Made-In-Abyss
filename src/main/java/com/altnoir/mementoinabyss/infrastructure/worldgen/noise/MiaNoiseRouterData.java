package com.altnoir.mementoinabyss.infrastructure.worldgen.noise;

import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssTongueDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssTongueShape;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import com.altnoir.mementoinabyss.init.MiaDensityFunctionTypes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MiaNoiseRouterData extends NoiseRouterData {
    public static final ResourceKey<DensityFunction> Y = createKey("y");
    public static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
    public static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD =
            createKey("overworld/base_3d_noise");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER =
            createKey("nether/base_3d_noise");
    public static final ResourceKey<DensityFunction> BASE_3D_NOISE_END =
            createKey("end/base_3d_noise");
    public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
    public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
    public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
    public static final ResourceKey<DensityFunction> RIDGES_FOLDED =
            createKey("overworld/ridges_folded");
    public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
    public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
    public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
    public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
    public static final ResourceKey<DensityFunction> SLOPED_CHEESE =
            createKey("overworld/sloped_cheese");

    public static final ResourceKey<DensityFunction> ENTRANCES =
            createKey("overworld/caves/entrances");
    public static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
    public static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
    public static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR =
            createKey("overworld/caves/spaghetti_2d_thickness_modulator");

    private static ResourceKey<DensityFunction> createKey(String location) {
        return ResourceKey.create(
                Registries.DENSITY_FUNCTION, Identifier.withDefaultNamespace(location));
    }

    private static DensityFunction getFunction(
            HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }

    private static DensityFunction postProcess(DensityFunction densityFunction) {
        DensityFunction densityfunction = DensityFunctions.blendDensity(densityFunction);
        return DensityFunctions.mul(
                        DensityFunctions.interpolated(densityfunction),
                        DensityFunctions.constant(0.64))
                .squeeze();
    }

    static DensityFunction theAbyssRawDensity(HolderGetter<DensityFunction> functions) {
        DensityFunction y = getFunction(functions, Y);
        DensityFunction innerBelow =
                MiaDensityFunctions.lowerLayerDensity(
                        functions,
                        MiaDensityFunctions.THE_ABYSS_INSIDE_HOLE_BELOW,
                        MiaDensityFunctions.THE_ABYSS_HOLE_BELOW);
        DensityFunction innerAbove =
                DensityFunctions.min(
                        getFunction(functions, MiaDensityFunctions.THE_ABYSS_INSIDE_HOLE_ABOVE),
                        DensityFunctions.rangeChoice(
                                y,
                                MiaHeight.THE_ABYSS.middleY(),
                                MiaHeight.THE_ABYSS.maxY() - 64,
                                getFunction(functions, MiaDensityFunctions.BASE_3D_NOISE_THE_ABYSS),
                                DensityFunctions.constant(-1)));
        DensityFunction middle =
                MiaDensityFunctions.lowerLayerDensity(
                        functions,
                        MiaDensityFunctions.THE_ABYSS_MIDDLE_BASE_3D,
                        MiaDensityFunctions.BASE_3D_NOISE_THE_ABYSS);
        DensityFunction outer =
                getFunction(functions, MiaDensityFunctions.THE_ABYSS_OUTSIDE_BASE_3D);
        DensityFunction edge =
                DensityFunctions.cacheOnce(
                        DensityFunctions.add(
                                        DensityFunctions.constant(.5),
                                        getFunction(
                                                functions, MiaDensityFunctions.THE_ABYSS_BIG_HOLE))
                                .clamp(0, 1));
        DensityFunction smooth =
                DensityFunctions.mul(
                        DensityFunctions.mul(edge, edge),
                        DensityFunctions.add(
                                DensityFunctions.constant(3),
                                DensityFunctions.mul(DensityFunctions.constant(-2), edge)));
        DensityFunction oldOuter =
                DensityFunctions.max(
                        DensityFunctions.rangeChoice(
                                y,
                                MiaDensityFunctions.LOWER_TRANSITION_MAX,
                                MiaHeight.THE_ABYSS.maxY() - 64,
                                outer,
                                MiaDensityFunctions.lerpWithEndpoints(smooth, middle, outer)),
                        getFunction(functions, MiaDensityFunctions.THE_ABYSS_PILLARS));
        DensityFunction inner =
                DensityFunctions.rangeChoice(
                        y,
                        MiaHeight.THE_ABYSS.minY(),
                        MiaHeight.THE_ABYSS.middleY(),
                        innerBelow,
                        innerAbove);
        DensityFunction original =
                DensityFunctions.rangeChoice(
                        getFunction(functions, MiaDensityFunctions.THE_ABYSS_HOLE_ABOVE),
                        -1000000,
                        .025,
                        inner,
                        oldOuter);
        original =
                DensityFunctions.add(
                        DensityFunctions.constant(-.4),
                        DensityFunctions.mul(
                                DensityFunctions.yClampedGradient(-160, -120, 0, 1),
                                DensityFunctions.add(
                                        DensityFunctions.constant(.4),
                                        DensityFunctions.add(
                                                DensityFunctions.constant(-.8975),
                                                DensityFunctions.add(
                                                        DensityFunctions.constant(1.025),
                                                        original)))));
        return DensityFunctions.rangeChoice(
                y,
                -176,
                16,
                MiaDensityFunctions.lerpWithEndpoints(
                        Layer2DensityFunctions.get(functions, Layer2DensityFunctions.WEIGHT),
                        original,
                        Layer2DensityFunctions.get(functions, Layer2DensityFunctions.TERRAIN)),
                original);
    }

    private static DensityFunction greatFaultDensity(
            HolderGetter<DensityFunction> densityFunctions) {
        return postProcess(getFunction(densityFunctions, MiaDensityFunctions.GREAT_FAULT_TERRAIN));
    }

    static DensityFunction theAbyssTongueSupport(HolderGetter<DensityFunction> functions) {
        return theAbyssGround(
                functions, getFunction(functions, MiaDensityFunctions.THE_ABYSS_RAW_TERRAIN));
    }

    static DensityFunction theAbyssGround(
            HolderGetter<DensityFunction> functions, DensityFunction terrain) {
        DensityFunction rock = postProcess(terrain);
        DensityFunction weight =
                Layer2DensityFunctions.get(functions, Layer2DensityFunctions.WEIGHT);
        DensityFunction ordinary =
                DensityFunctions.min(
                        rock, getFunction(functions, MiaDensityFunctions.THE_ABYSS_LOWER_NOODLE));
        DensityFunction main =
                DensityFunctions.rangeChoice(
                        weight,
                        0,
                        Math.nextUp(0.0),
                        ordinary,
                        DensityFunctions.rangeChoice(
                                weight,
                                1,
                                2,
                                rock,
                                MiaDensityFunctions.lerpDirect(weight, ordinary, rock)));
        DensityFunction inverse =
                getFunction(functions, MiaDensityFunctions.THE_ABYSS_GREAT_CAVE_NOODLE);
        DensityFunction caves =
                DensityFunctions.rangeChoice(
                        getFunction(functions, Y),
                        -176,
                        16,
                        DensityFunctions.min(
                                main,
                                Layer2DensityFunctions.get(
                                        functions, Layer2DensityFunctions.CAVES)),
                        ordinary);
        return DensityFunctions.max(caves, inverse);
    }

    private static NoiseRouter theAbyssRouter(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction normalFloodedness =
                theAbyssFluidLevel(
                        noiseParameters,
                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS,
                        0.335,
                        0.5,
                        yFunction);
        DensityFunction sparseFloodedness =
                DensityFunctions.rangeChoice(
                        MiaDensityFunctionTypes.sparseAquifer(10),
                        0.0,
                        2.0,
                        normalFloodedness,
                        DensityFunctions.constant(-1.0));
        DensityFunction fluidLevelFloodedness =
                DensityFunctions.rangeChoice(
                        yFunction,
                        -160,
                        MiaHeight.THE_ABYSS.maxY(),
                        sparseFloodedness,
                        DensityFunctions.constant(-1.0));
        DensityFunction fluidLevelSpread =
                theAbyssFluidLevel(
                        noiseParameters,
                        Noises.AQUIFER_FLUID_LEVEL_SPREAD,
                        0.357142857,
                        0.5,
                        yFunction);

        fluidLevelFloodedness =
                DensityFunctions.rangeChoice(
                        yFunction,
                        -176,
                        0,
                        Layer2DensityFunctions.get(
                                densityFunctions, Layer2DensityFunctions.FLOODEDNESS),
                        fluidLevelFloodedness);
        fluidLevelSpread =
                DensityFunctions.rangeChoice(
                        yFunction, -176, 0, DensityFunctions.zero(), fluidLevelSpread);

        DensityFunction shiftX = getFunction(densityFunctions, SHIFT_X);
        DensityFunction shiftZ = getFunction(densityFunctions, SHIFT_Z);
        DensityFunction temperature =
                DensityFunctions.flatCache(
                        DensityFunctions.cache2d(
                                DensityFunctions.shiftedNoise2d(
                                        shiftX,
                                        shiftZ,
                                        0.25,
                                        noiseParameters.getOrThrow(Noises.TEMPERATURE))));
        DensityFunction vegetation =
                DensityFunctions.flatCache(
                        DensityFunctions.cache2d(
                                DensityFunctions.shiftedNoise2d(
                                        shiftX,
                                        shiftZ,
                                        0.25,
                                        noiseParameters.getOrThrow(Noises.VEGETATION))));

        DensityFunction idwj0 = getFunction(densityFunctions, FACTOR);
        DensityFunction idwj1 =
                DensityFunctions.rangeChoice(
                        yFunction,
                        -128,
                        5,
                        MiaDensityFunctionTypes.general(0.0F, 0.25F),
                        MiaDensityFunctionTypes.hopper());
        DensityFunction idwj2 = noiseGradientDensity(DensityFunctions.cache2d(idwj0), idwj1);
        DensityFunction idwj3 = DensityFunctions.add(idwj2, DensityFunctions.constant(-0.703125));
        DensityFunction idwj4 =
                slide(idwj3, -64, MiaHeight.THE_ABYSS.maxY(), 72, 0, -0.078125, 0, 24, 0.1171875);

        DensityFunction floorEstimate =
                DensityFunctions.add(
                        DensityFunctions.constant(.390625),
                        DensityFunctions.mul(
                                DensityFunctions.constant(1 / 16.0),
                                DensityFunctions.add(
                                        Layer2DensityFunctions.get(
                                                densityFunctions, Layer2DensityFunctions.SURFACE),
                                        DensityFunctions.mul(
                                                DensityFunctions.constant(-1), yFunction))));
        idwj4 =
                DensityFunctions.rangeChoice(
                        yFunction,
                        -176,
                        0,
                        localSurfaceEstimate(
                                Layer2DensityFunctions.get(
                                        densityFunctions, Layer2DensityFunctions.WEIGHT),
                                idwj4,
                                floorEstimate),
                        idwj4);

        DensityFunction terrain =
                getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_RAW_TERRAIN);
        DensityFunction support =
                getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_TONGUE_SUPPORT);
        double tongueRadius = HopperAbyssHole.abyssRadius();
        DensityFunction carved = new AbyssTongueDensity(terrain, support, tongueRadius, false);
        DensityFunction tongues =
                new AbyssTongueDensity(DensityFunctions.zero(), support, tongueRadius, true);
        DensityFunction combinedDensity = theAbyssGround(densityFunctions, carved);
        // Thin tips must be sampled per block; the 8-block terrain lattice can sever their necks.
        DensityFunction tongueBody =
                DensityFunctions.mul(
                                DensityFunctions.blendDensity(tongues),
                                DensityFunctions.constant(.64))
                        .squeeze();
        DensityFunction withTongues = DensityFunctions.max(combinedDensity, tongueBody);
        withTongues =
                DensityFunctions.rangeChoice(
                        MiaDensityFunctionTypes.noodle(0, .95F),
                        -1000000,
                        0,
                        withTongues,
                        combinedDensity);
        combinedDensity =
                DensityFunctions.rangeChoice(
                        yFunction,
                        AbyssTongueShape.MIN_Y,
                        AbyssTongueShape.MAX_Y,
                        withTongues,
                        combinedDensity);
        DensityFunction finalDensity =
                DensityFunctions.rangeChoice(
                        yFunction,
                        -190,
                        MiaHeight.THE_ABYSS.maxY(),
                        combinedDensity,
                        DensityFunctions.constant(-64.0));

        finalDensity =
                AbyssSeamDensity.abyss(
                        SharedAbyssWall.unbound(HopperAbyssHole.abyssRadius()),
                        finalDensity,
                        MiaHeight.THE_ABYSS.maxY());

        return new NoiseRouter(
                DensityFunctions.constant(1.0), // barrier 影响含水层是否在流体与空气之间放置阻挡方块，值越大概率越大。
                fluidLevelFloodedness, // fluid_level_floodedness 影响含水层放置流体的概率，大于1的值被视为1，小于0的值被视为0。
                fluidLevelSpread, // fluid_level_spread 影响含水层放置流体的高度，此值越低越不可能放置含水层。
                DensityFunctions.zero(), // lava 当绝对值大于0.3时含水层在Y=-58与海平面之间放置熔岩而不是默认流体。
                temperature, // temperature 生物群系温度函数。此参数同下方共六个参数也负责了多噪声型生物群系计算时使用的噪声参数。
                vegetation, // vegetation 生物群系湿度函数。
                DensityFunctions.zero(), // continents 生物群系大陆性函数。
                DensityFunctions.zero(), // erosion 生物群系侵蚀度函数。
                getFunction(
                        densityFunctions, MiaDensityFunctions.THE_ABYSS_DEPTH), // depth 生物群系深度函数。
                getFunction(densityFunctions, RIDGES), // ridges 生物群系奇异度函数。
                idwj4, // initial_density_without_jaggedness//
                // 预处理地表高度，影响表面规则的含水层的放置。游戏会从世界顶部以4*整型noise.size_vertical的精度向下查找，将首个大于25/64的值的高度作为预处理地表高度。
                finalDensity, // final_density 最终密度。大于0的区域将放置默认方块并被表面规则替换，小于0的区域将放置空气并被含水层替换。
                DensityFunctions.zero(), // vein_toggle
                DensityFunctions.zero(), // vein_ridged
                DensityFunctions.zero() // vein_gap
                // 最后三个参数是矿脉生成相关的，因为是硬编码就不用它的
                );
    }

    private static DensityFunction localSurfaceEstimate(
            DensityFunction weight, DensityFunction original, DensityFunction local) {
        // Preliminary surface scans reuse one context; CacheOnce must not freeze their Y weight.
        return DensityFunctions.rangeChoice(
                weight,
                0,
                Math.nextUp(0.0),
                original,
                DensityFunctions.rangeChoice(
                        weight,
                        1,
                        2,
                        local,
                        MiaDensityFunctions.lerpDirect(weight, original, local)));
    }

    private static NoiseRouter greatFaultRouter(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction fluidLevelFloodedness =
                theAbyssFluidLevel(
                        noiseParameters,
                        Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS,
                        0.335,
                        0.5,
                        yFunction);
        DensityFunction fluidLevelSpread =
                theAbyssFluidLevel(
                        noiseParameters,
                        Noises.AQUIFER_FLUID_LEVEL_SPREAD,
                        0.357142857,
                        0.5,
                        yFunction);

        DensityFunction shiftX = getFunction(densityFunctions, SHIFT_X);
        DensityFunction shiftZ = getFunction(densityFunctions, SHIFT_Z);

        DensityFunction noodle =
                DensityFunctions.min(
                        getFunction(densityFunctions, MiaDensityFunctions.GREAT_FAULT_NOODLE),
                        DensityFunctions.add(
                                DensityFunctions.interpolated(
                                        getFunction(
                                                densityFunctions,
                                                MiaDensityFunctions.GREAT_FAULT_BIG_CAVE)),
                                DensityFunctions.constant(-0.15)));
        DensityFunction finalDensity =
                DensityFunctions.min(greatFaultDensity(densityFunctions), noodle);

        return new NoiseRouter(
                DensityFunctions.zero(),
                fluidLevelFloodedness,
                fluidLevelSpread,
                DensityFunctions.zero(),
                DensityFunctions.shiftedNoise2d(
                        shiftX, shiftZ, 0.25, noiseParameters.getOrThrow(Noises.TEMPERATURE)),
                DensityFunctions.shiftedNoise2d(
                        shiftX, shiftZ, 0.25, noiseParameters.getOrThrow(Noises.VEGETATION)),
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_DEPTH),
                getFunction(densityFunctions, RIDGES),
                DensityFunctions.zero(),
                finalDensity,
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero());
    }

    private static DensityFunction noiseGradientDensity(
            DensityFunction minFunction, DensityFunction maxFunction) {
        DensityFunction densityfunction = DensityFunctions.mul(maxFunction, minFunction);
        return DensityFunctions.mul(
                DensityFunctions.constant(4.0), densityfunction.quarterNegative());
    }

    private static DensityFunction theAbyssFluidLevel(
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters,
            ResourceKey<NormalNoise.NoiseParameters> noiseKey,
            double xzScale,
            double yScale,
            DensityFunction yFunction) {
        DensityFunction noise =
                DensityFunctions.noise(noiseParameters.getOrThrow(noiseKey), xzScale, yScale);
        return DensityFunctions.rangeChoice(
                yFunction,
                -120,
                -64,
                noise,
                DensityFunctions.rangeChoice(
                        yFunction,
                        5,
                        MiaHeight.THE_ABYSS.maxY(),
                        noise,
                        DensityFunctions.constant(1)));
    }

    private static DensityFunction slide(
            DensityFunction input,
            int minY,
            int maxY,
            int i1,
            int i2,
            double v1,
            int i3,
            int i4,
            double v2) {
        DensityFunction densityFunction2 =
                DensityFunctions.yClampedGradient(minY + maxY - i1, minY + maxY - i2, 1.0, 0.0);
        DensityFunction lerped = DensityFunctions.lerp(densityFunction2, v1, input);

        DensityFunction densityFunction1 =
                DensityFunctions.yClampedGradient(minY + i3, minY + i4, 0.0, 1.0);
        return DensityFunctions.lerp(densityFunction1, v2, lerped);
    }

    public static NoiseRouter theAbyss(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return theAbyssRouter(densityFunctions, noiseParameters);
    }

    public static NoiseRouter greatFault(
            HolderGetter<DensityFunction> densityFunctions,
            HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return greatFaultRouter(densityFunctions, noiseParameters);
    }
}
