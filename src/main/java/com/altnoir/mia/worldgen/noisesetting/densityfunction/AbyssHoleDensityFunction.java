package com.altnoir.mia.worldgen.noisesetting.densityfunction;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class AbyssHoleDensityFunction implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<AbyssHoleDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new AbyssHoleDensityFunction(0L))
    );
    private final SimplexNoise abyssNoise;

    public AbyssHoleDensityFunction(long seed) {
        RandomSource randomsource = new LegacyRandomSource(seed);
        randomsource.consumeCount(17292);
        this.abyssNoise = new SimplexNoise(randomsource);
    }
    private static float getHeightValue(SimplexNoise noise, int x, int z) {
        float f = 168.0F - Mth.sqrt((float)(x * x + z * z)) * 8.0F; // 这个大抵就是深渊半径了
        f = Mth.clamp(f, -100.0F, 80.0F);

        /*int i = x / 2;
        int j = z / 2;
        int k = x % 2;
        int l = z % 2;
        for (int i1 = -12; i1 <= 12; i1++) {
            for (int j1 = -12; j1 <= 12; j1++) {
                long k1 = i + i1;
                long l1 = j + j1;
                if (k1 * k1 + l1 * l1 < 42L && noise.getValue((double)k1, (double)l1) < ABYSS_THRESHOLD) {
                    float f1 = (Mth.abs((float)k1) * 3439.0F + Mth.abs((float)l1) * 247.0F) % 13.0F + 9.0F; // 这个值好像是控制地形平滑度的
                    float f2 = (float)(k - i1 * 2);
                    float f3 = (float)(l - j1 * 2);
                    float f4 = 100.0F - Mth.sqrt(f2 * f2 + f3 * f3) * f1;
                    f4 = Mth.clamp(f4, -100.0F, 80.0F);
                    f = Math.max(f, f4);
                }
            }
        }*/
        return f;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        // 反转计算结果：原公式 (height -8)/128 改为 (8 - height) / 128
        return (8.0 - (double)getHeightValue(this.abyssNoise, context.blockX() / 8, context.blockZ() / 8)) / 16; // 最终除的数越小，洞越平滑
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
