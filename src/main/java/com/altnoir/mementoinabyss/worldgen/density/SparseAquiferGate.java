package com.altnoir.mementoinabyss.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Deterministically enables one out of every N aquifer sampling cells. */
public record SparseAquiferGate(int oneIn) implements DensityFunction.SimpleFunction {
    private static final int CELL_WIDTH = 16;
    private static final int CELL_HEIGHT = 12;

    public static final KeyDispatchDataCodec<SparseAquiferGate> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.intRange(1, 1024).fieldOf("one_in").forGetter(SparseAquiferGate::oneIn)
            ).apply(instance, SparseAquiferGate::new)));

    @Override
    public double compute(FunctionContext context) {
        long x = Math.floorDiv(context.blockX(), CELL_WIDTH);
        long y = Math.floorDiv(context.blockY(), CELL_HEIGHT);
        long z = Math.floorDiv(context.blockZ(), CELL_WIDTH);
        long hash = x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return Math.floorMod(hash, oneIn) == 0L ? 1.0 : -1.0;
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
    public KeyDispatchDataCodec<SparseAquiferGate> codec() {
        return CODEC;
    }
}
