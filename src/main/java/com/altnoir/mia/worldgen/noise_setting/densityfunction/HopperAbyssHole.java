package com.altnoir.mia.worldgen.noise_setting.densityfunction;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.abs.AbstractAbyssHole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public class HopperAbyssHole extends AbstractAbyssHole {

    public static final KeyDispatchDataCodec<HopperAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(g -> g.radius),
                    Codec.FLOAT.fieldOf("mul").forGetter(g -> g.mul)

            ).apply(instance, HopperAbyssHole::new))
    );


    public HopperAbyssHole(float radius, float mul) {
        super(radius, mul);
    }

    // 256 = √256 = 16区块.
    public static float getAbyssRadius() {
        return MiaConfig.abyssRadius * 2.0F;
    } // 深渊半径

    private static float getHeightValue(int x, int y, int z, float r, float m) {
        float v = 64.0F;// 数越大，坡越陡

        float max = 2.0F;
        float yr = Mth.clamp(1.0F + (float) y / v, 1.0F, max);

        float d = Mth.sqrt((float) (x * x + z * z));
        float f = d * 8.0F - ((getAbyssRadius() * m + r) / max) * yr;
        f = Mth.clamp(f, -80.0F, 100.0F);

        return f;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        return (double) getHeightValue(context.blockX() / 8, context.blockY() / 8, context.blockZ() / 8, this.radius, this.mul) / 64; // 最终除的数越小，洞越平滑
    }

    @Override
    protected KeyDispatchDataCodec<? extends DensityFunction> getCodec() {
        return CODEC;
    }
}
