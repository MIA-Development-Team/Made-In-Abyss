package com.altnoir.mementoinabyss.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class NoodleAbyssHole extends AbstractAbyssHole {
    public static final KeyDispatchDataCodec<NoodleAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(value -> value.radius),
                    Codec.FLOAT.fieldOf("mul").forGetter(value -> value.multiplier)
            ).apply(instance, NoodleAbyssHole::new)));

    public NoodleAbyssHole(float radius, float multiplier) {
        super(radius, multiplier);
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        float x = context.blockX();
        float z = context.blockZ();
        float distance = Mth.sqrt(x * x + z * z);
        float value = distance - (HopperAbyssHole.abyssRadius() * multiplier + radius);
        return Mth.clamp(value, -80.0F, 100.0F) / 8.0F;
    }

    @Override
    public KeyDispatchDataCodec<NoodleAbyssHole> codec() {
        return CODEC;
    }
}
