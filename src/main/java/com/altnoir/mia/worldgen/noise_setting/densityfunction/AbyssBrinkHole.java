package com.altnoir.mia.worldgen.noise_setting.densityfunction;

import com.altnoir.mia.MiaConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class AbyssBrinkHole implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<AbyssBrinkHole> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new AbyssBrinkHole(0L))
    );

    public AbyssBrinkHole(long seed) {
        RandomSource randomsource = new LegacyRandomSource(seed);
        randomsource.consumeCount(17292);
    }

    // 256 = √256 = 16区块.
    public static float getAbyssRadius() {
        return MiaConfig.abyssRadius;
    } // 深渊半径

    private static float getHeightValue(int x, int y, int z) {
        float d = Mth.sqrt((float) (x * x + z * z));

        float maxY = 2.5F;
        float r = Mth.clamp(1.0F + (float) y / 32.0F, 1.0F, maxY); // y除的数越小，坡度就越平滑

        float f = (getAbyssRadius() / maxY) * r - d * 8.0F;
        f = Mth.clamp(f, -100.0F, 80.0F);

        return f;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        // 反转计算结果：原公式 (height -8)/128 改为 (8 - height) / 128
        return (8.0 - (double) getHeightValue(context.blockX() / 8, context.blockY() / 8, context.blockZ() / 8)) / 64; // 最终除的数越小，洞越平滑
    }

    @Override
    public double minValue() {
        // 原最小值-0.84375反转后变为-0.5625（原最大值0.5625取反）
        return -0.5625;
    }

    @Override
    public double maxValue() {
        // 原最大值0.5625反转后变为0.84375（原最小值-0.84375取反）
        return 0.84375;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
