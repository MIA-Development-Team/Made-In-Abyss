package com.altnoir.mementoinabyss.worldgen.density;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class HopperAbyssHole extends AbstractAbyssHole {
    public static final KeyDispatchDataCodec<HopperAbyssHole> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(value -> value.radius),
                    Codec.FLOAT.fieldOf("mul").forGetter(value -> value.multiplier),
                    Codec.FLOAT.fieldOf("slope").forGetter(HopperAbyssHole::slope)
            ).apply(instance, HopperAbyssHole::new)));

    private final float slope;

    public HopperAbyssHole(float radius, float multiplier, float slope) {
        super(radius, multiplier);
        this.slope = slope;
    }

    public static float abyssRadius() {
        return MementoInAbyss.CONFIGS.worldGenSection.abyssRadius.get() * 2.0F;
    }

    public float slope() {
        return slope;
    }

    @Override
    public double compute(DensityFunction.FunctionContext context) {
        float x = context.blockX() / 8.0F;
        float y = context.blockY() / 8.0F;
        float z = context.blockZ() / 8.0F;
        float verticalScale = Mth.clamp(1.0F + y / slope, 1.0F, 2.0F);
        float distance = Mth.sqrt(x * x + z * z);
        float value = distance * 8.0F - ((abyssRadius() * multiplier + radius) / 2.0F) * verticalScale;
        return Mth.clamp(value, -80.0F, 100.0F) / 64.0F;
    }

    @Override
    public KeyDispatchDataCodec<HopperAbyssHole> codec() {
        return CODEC;
    }
}
