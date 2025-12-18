package com.altnoir.mia.worldgen.noise_setting.densityfunction.abs;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public abstract class AbstractAbyssHole implements DensityFunction.SimpleFunction {
    protected final float radius;

    protected abstract KeyDispatchDataCodec<? extends DensityFunction> getCodec();

    public AbstractAbyssHole(float radius) {
        this.radius = radius;
        RandomSource randomsource = new LegacyRandomSource(0L);
        randomsource.consumeCount(17292);
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
        return getCodec();
    }
}
