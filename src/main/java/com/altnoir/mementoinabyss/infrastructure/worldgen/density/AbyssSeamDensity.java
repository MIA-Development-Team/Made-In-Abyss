package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Final per-block seam constraint. Apply after cave selection and interpolation. */
public record AbyssSeamDensity(
        SharedAbyssWall wall, DensityFunction input, int boundaryY, boolean overworld)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<AbyssSeamDensity> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    SharedAbyssWall.CODEC
                                                            .codec()
                                                            .fieldOf("wall")
                                                            .forGetter(AbyssSeamDensity::wall),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(AbyssSeamDensity::input),
                                                    Codec.INT
                                                            .fieldOf("boundary_y")
                                                            .forGetter(AbyssSeamDensity::boundaryY),
                                                    Codec.BOOL
                                                            .fieldOf("overworld")
                                                            .forGetter(AbyssSeamDensity::overworld))
                                            .apply(i, AbyssSeamDensity::new)));

    public static AbyssSeamDensity overworld(
            SharedAbyssWall wall, DensityFunction input, int minY) {
        return new AbyssSeamDensity(wall, input, minY, true);
    }

    public static AbyssSeamDensity abyss(SharedAbyssWall wall, DensityFunction input, int maxY) {
        return new AbyssSeamDensity(wall, input, maxY, false);
    }

    public AbyssSeamDensity bind(long seed) {
        return new AbyssSeamDensity(wall.bind(seed), input, boundaryY, overworld);
    }

    public AbyssSeamDensity withBoundaryY(int y) {
        return new AbyssSeamDensity(wall, input, y, overworld);
    }

    public static double smooth(double t) {
        t = Math.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public double weight(FunctionContext context) {
        int depth = overworld ? context.blockY() - boundaryY : boundaryY - 1 - context.blockY();
        if (depth < 0 || depth >= 48) return 0;
        double weight = 1 - smooth((depth - 15) / 32.0);
        if (overworld) {
            double distance = Math.hypot(context.blockX(), context.blockZ());
            weight *= 1 - smooth((distance - wall.layoutRadius() - 48) / 16);
        }
        return weight;
    }

    private double constrain(double original, FunctionContext context, double weight) {
        if (weight == 0) return original;
        double raw = wall.compute(context);
        // Same processed density on both ends, with a strictly shared zero crossing.
        double scaled = .64 * raw;
        // Minecraft's squeeze clamps its input to [-1,1].
        scaled = Math.clamp(scaled, -1, 1);
        double seam = scaled / 2 - scaled * scaled * scaled / 24;
        return weight == 1 ? seam : original + weight * (seam - original);
    }

    @Override
    public double compute(FunctionContext context) {
        double weight = weight(context);
        // The protected band has its own complete density. Avoid sampling the entire underlying
        // cave graph again for every surface/aquifer guard query.
        return constrain(weight == 1 ? 0 : input.compute(context), context, weight);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        input.fillArray(output, provider);
        for (int i = 0; i < output.length; i++) {
            var context = provider.forIndex(i);
            output[i] = constrain(output[i], context, weight(context));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new AbyssSeamDensity(
                        (SharedAbyssWall) wall.mapAll(visitor),
                        input.mapAll(visitor),
                        boundaryY,
                        overworld));
    }

    @Override
    public double minValue() {
        return Math.min(input.minValue(), -.5);
    }

    @Override
    public double maxValue() {
        return Math.max(input.maxValue(), .5);
    }

    @Override
    public KeyDispatchDataCodec<AbyssSeamDensity> codec() {
        return CODEC;
    }
}
