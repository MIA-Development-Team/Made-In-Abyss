package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record GreatFaultProfile(DensityFunction noise, int minY, int maxY)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<GreatFaultProfile> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("noise")
                                                            .forGetter(GreatFaultProfile::noise),
                                                    Codec.INT
                                                            .fieldOf("min_y")
                                                            .forGetter(GreatFaultProfile::minY),
                                                    Codec.INT
                                                            .fieldOf("max_y")
                                                            .forGetter(GreatFaultProfile::maxY))
                                            .apply(instance, GreatFaultProfile::new)));

    private static final double TOP_OFFSET = 0.8975;
    private static final double WALL_OFFSET = 1.025;
    private static final double MIN_GEOMETRY = -TOP_OFFSET + WALL_OFFSET - 5;
    private static final double MAX_GEOMETRY = -TOP_OFFSET + WALL_OFFSET + 6.25;

    private double density(double sample, FunctionContext context) {
        int y = context.blockY();
        if (y <= minY) return -0.4;
        if (y >= maxY) return -TOP_OFFSET;

        float x = context.blockX() / 8.0F;
        float z = context.blockZ() / 8.0F;
        float distance = Mth.sqrt(x * x + z * z) * 8.0F;
        float radial = distance - HopperAbyssHole.abyssRadius() * 0.5F;
        int junctionY = maxY - 64;
        double raw;
        if (y <= junctionY) {
            // Preserve the lower cliff, including its original float arithmetic and density bias.
            double wall = Mth.clamp(radial, -80.0F, 100.0F) / 16.0F;
            raw = -TOP_OFFSET + (WALL_OFFSET + (wall + sample));
        } else {
            double top = (y - junctionY) / 64.0;
            double transition = Math.clamp((y - junctionY) / 16.0, 0, 1);
            transition *= transition * (3 - 2 * transition);
            double scale = 16 + 48 * transition;

            double atJunction = radial + 16 * (WALL_OFFSET - TOP_OFFSET);
            double geometry = ((1 - top) * atJunction - 64 * TOP_OFFSET * top) / scale;
            double detail = (1 - top) * (16 / scale) * sample;

            raw = Math.clamp(geometry, MIN_GEOMETRY, MAX_GEOMETRY) + detail;
        }

        double bottom = Math.clamp((y - minY) / 64.0, 0, 1);
        return -0.4 + bottom * (0.4 + raw);
    }

    @Override
    public double compute(FunctionContext context) {
        return density(noise.compute(context), context);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        noise.fillArray(output, provider);
        for (int i = 0; i < output.length; i++) {
            output[i] = density(output[i], provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new GreatFaultProfile(noise.mapAll(visitor), minY, maxY));
    }

    @Override
    public double minValue() {
        return Math.nextDown(MIN_GEOMETRY + Math.min(0, noise.minValue()));
    }

    @Override
    public double maxValue() {
        return Math.nextUp(MAX_GEOMETRY + Math.max(0, noise.maxValue()));
    }

    @Override
    public KeyDispatchDataCodec<GreatFaultProfile> codec() {
        return CODEC;
    }
}
