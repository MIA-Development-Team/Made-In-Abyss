package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

/** Local terrain composition: vertical shaft, cubic shoulder/ridge and original outer terrain. */
public record OverworldAbyssHole(
        DensityFunction input,
        DensityFunction surface,
        DensityFunction originalSurface,
        DensityFunction detail,
        OverworldAbyssShape shape,
        boolean surfaceOutput)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssHole> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(OverworldAbyssHole::input),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("surface")
                                                            .forGetter(OverworldAbyssHole::surface),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("original_surface")
                                                            .forGetter(
                                                                    OverworldAbyssHole
                                                                            ::originalSurface),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .optionalFieldOf(
                                                                    "detail",
                                                                    DensityFunctions.zero())
                                                            .forGetter(OverworldAbyssHole::detail),
                                                    OverworldAbyssShape.CODEC
                                                            .fieldOf("shape")
                                                            .forGetter(OverworldAbyssHole::shape),
                                                    Codec.BOOL
                                                            .optionalFieldOf(
                                                                    "surface_output", false)
                                                            .forGetter(
                                                                    OverworldAbyssHole
                                                                            ::surfaceOutput))
                                            .apply(instance, OverworldAbyssHole::new)));

    /** Older serialized graphs can omit detail; tests can disable detail to inspect the profile. */
    public OverworldAbyssHole(
            DensityFunction input,
            DensityFunction surface,
            DensityFunction originalSurface,
            OverworldAbyssShape shape,
            boolean surfaceOutput) {
        this(input, surface, originalSurface, DensityFunctions.zero(), shape, surfaceOutput);
    }

    @Override
    public double compute(FunctionContext context) {
        if (!shape.affects(context.blockX(), context.blockZ())) {
            return surfaceOutput
                    ? Math.floor(originalSurface.compute(context))
                    : input.compute(context);
        }
        return surfaceOutput ? surfaceHeight(context) : terrainDensity(context);
    }

    /** Unbeardified density, also used by opening guards so rock detail is never cut away. */
    public double terrainDensity(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        if (!shape.affects(x, z)) return input.compute(context);
        double distance = shape.distance(x, z);
        if (distance <= shape.radius()) return -1.0;
        double weight = shape.outerWeight(distance);
        double height = shape.innerHeight(x, z);
        if (weight < 1.0) {
            double original = originalSurface.compute(new SinglePointContext(x, 0, z));
            height =
                    Math.max(
                            shape.minY() - 1.0, OverworldAbyssShape.lerp(weight, original, height));
        }
        // Intersect the radial cliff and the cubic shoulder height, then roughen both in 3D.
        double value =
                Math.min(distance - shape.radius(), height - context.blockY())
                        + Math.clamp(detail.compute(context), -1.0, 1.0) * 12.0;
        // The added rock stays below the ridge ceiling; original outer mountains are not capped.
        value = Math.min(value, Math.min(256.0, shape.maxY() - 2.0) - context.blockY());
        double target = Math.clamp(value, -80.0, 100.0) / 16.0;
        return weight == 1.0
                ? target
                : OverworldAbyssShape.lerp(weight, input.compute(context), target);
    }

    public boolean isOpening(FunctionContext context) {
        double distance = shape.distance(context.blockX(), context.blockZ());
        return distance <= shape.radius()
                || (distance < 2.0 * shape.radius() && terrainDensity(context) <= 0.0);
    }

    private double surfaceHeight(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        if (shape.distance(x, z) <= shape.radius()) return shape.minY() - 1;
        // Scan the actual composed field: noise and modded thin layers move its zero crossing.
        for (int y = shape.maxY() - 1; y >= shape.minY(); y--) {
            if (terrainDensity(new SinglePointContext(x, y, z)) > 0.0) return y;
        }
        return shape.minY() - 1;
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssHole(
                        input.mapAll(visitor),
                        surface.mapAll(visitor),
                        originalSurface.mapAll(visitor),
                        detail.mapAll(visitor),
                        shape,
                        surfaceOutput));
    }

    @Override
    public double minValue() {
        return surfaceOutput
                ? Math.min(shape.minY() - 1, originalSurface.minValue())
                : Math.min(-5.0, input.minValue());
    }

    @Override
    public double maxValue() {
        return surfaceOutput
                ? Math.max(shape.maxY() - 1, originalSurface.maxValue())
                : Math.max(6.25, input.maxValue());
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssHole> codec() {
        return CODEC;
    }
}
