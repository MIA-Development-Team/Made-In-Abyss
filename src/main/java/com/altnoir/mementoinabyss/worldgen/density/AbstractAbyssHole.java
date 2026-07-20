package com.altnoir.mementoinabyss.worldgen.density;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public abstract class AbstractAbyssHole implements DensityFunction.SimpleFunction {
    protected final float radius;
    protected final float multiplier;

    protected AbstractAbyssHole(float radius, float multiplier) {
        this.radius = radius;
        this.multiplier = multiplier;
    }

    @Override
    public double minValue() {
        return -0.5625D;
    }

    @Override
    public double maxValue() {
        return 0.84375D;
    }

    @Override
    public abstract KeyDispatchDataCodec<? extends DensityFunction> codec();
}
