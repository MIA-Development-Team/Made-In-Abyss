package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.init.worldgen.MiaDensityFunctionTypes;
import com.altnoir.mia.worldgen.MiaHeight;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MiaNoiseRouterData extends NoiseRouterData {
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

    private static DensityFunction theAbyssDensity(HolderGetter<DensityFunction> densityFunctions) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction abyssGreatCave = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_GREAT_CAVE);

        DensityFunction abyssHole = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_HOLE_ABOVE);
        DensityFunction abyssBigHole = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_BIG_HOLE);

        DensityFunction insideAbyssHoleA = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_INSIDE_HOLE_ABOVE);
        DensityFunction insideAbyssHoleB = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_INSIDE_HOLE_BELOW);

        DensityFunction middleAbyssNoise = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_MIDDLE_BASE_3D);
        DensityFunction outsideAbyssNoise = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_OUTSIDE_BASE_3D);
        DensityFunction abyssPillars = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_PILLARS);

        DensityFunction middle = DensityFunctions.min(middleAbyssNoise,
                DensityFunctions.add(getFunction(densityFunctions, SPAGHETTI_2D), getFunction(densityFunctions, SPAGHETTI_ROUGHNESS_FUNCTION))
        );
        DensityFunction outside = DensityFunctions.min(outsideAbyssNoise,
                DensityFunctions.add(getFunction(densityFunctions, SPAGHETTI_2D), getFunction(densityFunctions, SPAGHETTI_ROUGHNESS_FUNCTION))
        );

        DensityFunction rangeChoice2 = DensityFunctions.rangeChoice(
                abyssBigHole, -1000000.0, 0.025, middle, outside
        );

        DensityFunction layer2 = DensityFunctions.add(DensityFunctions.constant(0.2), abyssGreatCave);
        DensityFunction layer = DensityFunctions.rangeChoice(
                yFunction, -128.0, MiaHeight.THE_ABYSS.midY(), layer2,
                DensityFunctions.max(rangeChoice2, abyssPillars)
        );

        DensityFunction rangeChoice1 = DensityFunctions.rangeChoice(
                yFunction, MiaHeight.THE_ABYSS.minY(), MiaHeight.THE_ABYSS.midY(), insideAbyssHoleB, insideAbyssHoleA
        );
        DensityFunction rangeChoice = DensityFunctions.rangeChoice(
                abyssHole, -1000000.0, 0.025, rangeChoice1, layer
        );

        DensityFunction ycg1 = DensityFunctions.yClampedGradient(MiaHeight.THE_ABYSS.maxY() - 64, MiaHeight.THE_ABYSS.maxY(), 1, 1);
        DensityFunction add1 = DensityFunctions.add(DensityFunctions.constant(1.025), rangeChoice);

        DensityFunction mul1 = DensityFunctions.mul(ycg1, add1);
        DensityFunction add2 = DensityFunctions.add(DensityFunctions.constant(-0.8975), mul1);

        DensityFunction ycg2 = DensityFunctions.yClampedGradient(-136, -120, 0, 1);
        DensityFunction add3 = DensityFunctions.add(DensityFunctions.constant(0.4), add2);

        DensityFunction mul2 = DensityFunctions.mul(ycg2, add3);
        DensityFunction add4 = DensityFunctions.add(DensityFunctions.constant(-0.4), mul2);

        return postProcess(add4);
    }

    private static NoiseRouter theAbyssRouter(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        DensityFunction yFunction = getFunction(densityFunctions, MiaNoiseRouterData.Y);
        DensityFunction fluidLevelFloodedness = theAbyssFluidLevel(noiseParameters, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, 0.335, 0.5, yFunction);
        DensityFunction fluidLevelSpread = theAbyssFluidLevel(noiseParameters, Noises.AQUIFER_FLUID_LEVEL_SPREAD, 0.357142857, 0.5, yFunction);

        DensityFunction shiftX = getFunction(densityFunctions, SHIFT_X);
        DensityFunction shiftZ = getFunction(densityFunctions, SHIFT_Z);
        DensityFunction temperature = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noiseParameters.getOrThrow(Noises.TEMPERATURE));
        DensityFunction vegetation = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, noiseParameters.getOrThrow(Noises.VEGETATION));

        DensityFunction idwj0 = getFunction(densityFunctions, FACTOR);
        DensityFunction idwj1 = DensityFunctions.rangeChoice(yFunction, -128, 0, MiaDensityFunctionTypes.generalAbyssHole(0.0F, 0.25F), MiaDensityFunctionTypes.hopperAbyssHole());
        DensityFunction idwj2 = noiseGradientDensity(DensityFunctions.cache2d(idwj0), idwj1);
        DensityFunction idwj3 = DensityFunctions.add(idwj2, DensityFunctions.constant(-0.703125));
        DensityFunction idwj4 = slide(idwj3, -64, MiaHeight.THE_ABYSS.maxY() - 64, 70, 0, -0.078125, 0, 24, 0.1171875);

        DensityFunction greatCaveNoodle = getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_GREAT_CAVE_NOODLE);
        DensityFunction noodle = DensityFunctions.add(DensityFunctions.yClampedGradient(-16, 16, -1.5, 0.0),
                DensityFunctions.add(DensityFunctions.yClampedGradient(0, 32, 1.5, 0.0), getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_NOODLE)));
        DensityFunction finalDensity = DensityFunctions.max(DensityFunctions.min(theAbyssDensity(densityFunctions), noodle), greatCaveNoodle);

        return new NoiseRouter(
                DensityFunctions.constant(1.0), // barrier 影响含水层是否在流体与空气之间放置阻挡方块，值越大概率越大。
                fluidLevelFloodedness, // fluid_level_floodedness 影响含水层放置流体的概率，大于1的值被视为1，小于0的值被视为0。
                fluidLevelSpread, // fluid_level_spread 影响含水层放置流体的高度，此值越低越不可能放置含水层。
                DensityFunctions.zero(), // lava 当绝对值大于0.3时含水层在Y=-58与海平面之间放置熔岩而不是默认流体。
                temperature, // temperature 生物群系温度函数。此参数同下方共六个参数也负责了多噪声型生物群系计算时使用的噪声参数。
                vegetation, // vegetation 生物群系湿度函数。
                DensityFunctions.zero(), // continents 生物群系大陆性函数。
                DensityFunctions.zero(), // erosion 生物群系侵蚀度函数。
                getFunction(densityFunctions, MiaDensityFunctions.THE_ABYSS_DEPTH), // depth 生物群系深度函数。
                getFunction(densityFunctions, RIDGES), // ridges 生物群系奇异度函数。
                idwj4, // initial_density_without_jaggedness// 预处理地表高度，影响表面规则的含水层的放置。游戏会从世界顶部以4*整型noise.size_vertical的精度向下查找，将首个大于25/64的值的高度作为预处理地表高度。
                finalDensity, // final_density 最终密度。大于0的区域将放置默认方块并被表面规则替换，小于0的区域将放置空气并被含水层替换。
                DensityFunctions.zero(),
                DensityFunctions.zero(),
                DensityFunctions.zero()
                // 最后三个参数是矿脉生成相关的，因为是硬编码就不用它的
        );
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

    private static DensityFunction noiseGradientDensity(DensityFunction minFunction, DensityFunction maxFunction) {
        DensityFunction densityfunction = DensityFunctions.mul(maxFunction, minFunction);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), densityfunction.quarterNegative());
    }

    private static DensityFunction theAbyssFluidLevel(HolderGetter<NormalNoise.NoiseParameters> noiseParameters, ResourceKey<NormalNoise.NoiseParameters> noiseKey, double xzScale, double yScale, DensityFunction yFunction) {
        DensityFunction noise = DensityFunctions.noise(noiseParameters.getOrThrow(noiseKey), xzScale, yScale);
        return DensityFunctions.rangeChoice(
                yFunction, -120, -64, noise,
                DensityFunctions.rangeChoice(
                        yFunction, 0, MiaHeight.THE_ABYSS.maxY(), noise, DensityFunctions.constant(1)
                )
        );
    }

    private static DensityFunction slide(DensityFunction input, int minY, int maxY, int i1, int i2, double v1, int i3, int i4, double v2) {
        DensityFunction densityFunction2 = DensityFunctions.yClampedGradient(minY + maxY - i1, minY + maxY - i2, 1.0, 0.0);
        DensityFunction lerped = DensityFunctions.lerp(densityFunction2, v1, input);

        DensityFunction densityFunction1 = DensityFunctions.yClampedGradient(minY + i3, minY + i4, 0.0, 1.0);
        return DensityFunctions.lerp(densityFunction1, v2, lerped);
    }

    public static NoiseRouter theAbyss(HolderGetter<DensityFunction> densityFunctions, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        return theAbyssRouter(densityFunctions, noiseParameters);
    }
}
