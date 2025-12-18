package com.altnoir.mia.worldgen.noise_setting.densityfunction;

import com.altnoir.mia.worldgen.noise_setting.densityfunction.abs.AbstractAbyssHole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public class GeneralAbyssHole extends AbstractAbyssHole {
    public static final KeyDispatchDataCodec<GeneralAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(g -> g.radius)
            ).apply(instance, GeneralAbyssHole::new))
    );

    public GeneralAbyssHole(float radius) {
        super(radius);
    }

    private static float getHeightValue(int x, int z, float r) {
        float d = Mth.sqrt((float) (x * x + z * z));
        // 400 = √400 = 20区块.
        float f = (HopperAbyssHole.getAbyssRadius() + r) - d * 8.0F; // 深渊半径
        f = Mth.clamp(f, -100.0F, 80.0F);

        return f;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        return (8.0 - (double) getHeightValue(context.blockX() / 8, context.blockZ() / 8, this.radius)) / 16;
    }

    @Override
    protected KeyDispatchDataCodec<? extends DensityFunction> getCodec() {
        return CODEC;
    }

}