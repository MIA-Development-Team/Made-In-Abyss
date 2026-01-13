package com.altnoir.mia.worldgen.noise_setting.densityfunction;

import com.altnoir.mia.worldgen.noise_setting.densityfunction.abs.AbstractAbyssHole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public class NoodleAbyssHole extends AbstractAbyssHole {
    public static final KeyDispatchDataCodec<NoodleAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(g -> g.radius),
                    Codec.FLOAT.fieldOf("mul").forGetter(g -> g.mul)
            ).apply(instance, NoodleAbyssHole::new))
    );

    public NoodleAbyssHole(float radius, float mul) {
        super(radius, mul);
    }

    private static float getHeightValue(int x, int z, float r, float m) {
        float d = Mth.sqrt((float) (x * x + z * z));
        float f = d - (HopperAbyssHole.getAbyssRadius() * m + r);
        f = Mth.clamp(f, -80.0F, 100.0F);

        return f;
    }

    @Override
    public double compute(FunctionContext context) {
        return (double) getHeightValue(context.blockX(), context.blockZ(), this.radius, this.mul) / 8;
    }

    @Override
    protected KeyDispatchDataCodec<? extends DensityFunction> getCodec() {
        return CODEC;
    }
}

