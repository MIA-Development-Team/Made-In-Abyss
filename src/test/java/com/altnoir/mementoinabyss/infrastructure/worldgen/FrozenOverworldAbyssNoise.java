package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Small-scale 3D rock detail; seeded once, immutable and safe for parallel generation. */
public final class FrozenOverworldAbyssNoise implements DensityFunction.SimpleFunction {
    public static final KeyDispatchDataCodec<FrozenOverworldAbyssNoise> CODEC =
            KeyDispatchDataCodec.of(
                    Codec.LONG
                            .fieldOf("seed")
                            .xmap(FrozenOverworldAbyssNoise::new, FrozenOverworldAbyssNoise::seed));
    private final long seed;
    private final NormalNoise noise;

    public FrozenOverworldAbyssNoise(long seed) {
        this.seed = seed;
        noise =
                NormalNoise.create(
                        RandomSource.create(seed ^ 0x6A09E667F3BCC909L), -4, 1.0, 0.5, 0.25);
    }

    public long seed() {
        return seed;
    }

    @Override
    public double compute(FunctionContext context) {
        return Math.clamp(
                noise.getValue(context.blockX(), context.blockY() * 0.6, context.blockZ()) * 2.0,
                -1.0,
                1.0);
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<FrozenOverworldAbyssNoise> codec() {
        return CODEC;
    }
}
