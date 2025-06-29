package com.altnoir.mia.worldgen.noise_setting.densityfunction;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class AbyssBrinkBigHole implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<AbyssBrinkBigHole> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new AbyssBrinkBigHole(0L))
    );

    public AbyssBrinkBigHole(long seed) {
        RandomSource randomsource = new LegacyRandomSource(seed);
        randomsource.consumeCount(17292);
    }
    private static float getHeightValue(int x, int z) {
        // 400 = √400 = 20区块.
        float f = 400.0F - Mth.sqrt((float)(x * x + z * z)) * 8.0F; // 深渊半径
        f = Mth.clamp(f, -100.0F, 80.0F);

        return f;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        return (8.0 - (double)getHeightValue(context.blockX() / 8, context.blockZ() / 8)) / 16;
    }

    @Override
    public double minValue() {
        return -0.5625;
    }

    @Override
    public double maxValue() {
        return 0.84375;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}