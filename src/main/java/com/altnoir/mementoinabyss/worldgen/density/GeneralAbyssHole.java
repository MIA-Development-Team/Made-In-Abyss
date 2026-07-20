package com.altnoir.mementoinabyss.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class GeneralAbyssHole extends AbstractAbyssHole {
    public static final KeyDispatchDataCodec<GeneralAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(value -> value.radius),
                    Codec.FLOAT.fieldOf("mul").forGetter(value -> value.multiplier)
            ).apply(instance, GeneralAbyssHole::new)));

    public GeneralAbyssHole(float radius, float multiplier) {
        super(radius, multiplier);
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        float x = context.blockX() / 8.0F;
        float z = context.blockZ() / 8.0F;
        float distance = Mth.sqrt(x * x + z * z);
        float value = distance * 8.0F - (HopperAbyssHole.abyssRadius() * multiplier + radius);
        return Mth.clamp(value, -80.0F, 100.0F) / 16.0F;
    }

    @Override
    public KeyDispatchDataCodec<GeneralAbyssHole> codec() {
        return CODEC;
    }
}
