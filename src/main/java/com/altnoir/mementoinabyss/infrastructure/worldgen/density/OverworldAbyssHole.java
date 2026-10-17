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
        boolean surfaceOutput,
        boolean composed,
        DensityFunction sampledTerrain,
        // Legacy codec field retained for old graphs; underside sculpting has been removed.
        boolean sculptUnderside,
        boolean roughWall)
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
                                                                            ::surfaceOutput),
                                                    Codec.BOOL
                                                            .optionalFieldOf("composed", false)
                                                            .forGetter(
                                                                    OverworldAbyssHole::composed),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .optionalFieldOf(
                                                                    "sampled_terrain",
                                                                    DensityFunctions.zero())
                                                            .forGetter(
                                                                    OverworldAbyssHole
                                                                            ::sampledTerrain),
                                                    Codec.BOOL
                                                            .optionalFieldOf(
                                                                    "sculpt_underside", false)
                                                            .forGetter(
                                                                    OverworldAbyssHole
                                                                            ::sculptUnderside),
                                                    Codec.BOOL
                                                            .optionalFieldOf("rough_wall", false)
                                                            .forGetter(
                                                                    OverworldAbyssHole::roughWall))
                                            .apply(instance, OverworldAbyssHole::new)));

    public OverworldAbyssHole(
            DensityFunction input,
            DensityFunction surface,
            DensityFunction originalSurface,
            DensityFunction detail,
            OverworldAbyssShape shape,
            boolean surfaceOutput,
            boolean composed,
            DensityFunction sampledTerrain,
            boolean sculptUnderside) {
        this(
                input,
                surface,
                originalSurface,
                detail,
                shape,
                surfaceOutput,
                composed,
                sampledTerrain,
                sculptUnderside,
                false);
    }

    /** Older serialized graphs retain their six-field constructor and optional detail. */
    public OverworldAbyssHole(
            DensityFunction input,
            DensityFunction surface,
            DensityFunction originalSurface,
            DensityFunction detail,
            OverworldAbyssShape shape,
            boolean surfaceOutput,
            boolean composed,
            DensityFunction sampledTerrain) {
        this(
                input,
                surface,
                originalSurface,
                detail,
                shape,
                surfaceOutput,
                composed,
                sampledTerrain,
                false);
    }

    public OverworldAbyssHole(
            DensityFunction input,
            DensityFunction surface,
            DensityFunction originalSurface,
            DensityFunction detail,
            OverworldAbyssShape shape,
            boolean surfaceOutput) {
        this(
                input,
                surface,
                originalSurface,
                detail,
                shape,
                surfaceOutput,
                false,
                DensityFunctions.zero());
    }

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
        if (composed) {
            double value =
                    context instanceof net.minecraft.world.level.levelgen.NoiseChunk
                            ? surface.compute(context)
                            : sampledTerrain.compute(context);
            if (!roughWall && distance < 2.0 * shape.radius())
                value = Math.min(value, (distance - shape.radius() - 2) / 16.0);
            return value;
        }
        double weight = shape.outerWeight(context.blockX(), context.blockZ());
        double height = shape.innerHeight(x, z);
        // Compatibility evaluation for old documents. New installations use the composed graph.
        double target =
                Math.min(
                        (distance - shape.radius()) / 16.0,
                        (height - context.blockY()) / 24.0 + detail.compute(context));
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
            if (terrainDensity(OverworldAbyssInterpolated.context(x, y, z, context.getBlender()))
                    > 0.0) return y;
        }
        return shape.minY() - 1;
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        if (composed && !surfaceOutput) {
            surface.fillArray(output, provider);
            boolean outside = false;
            for (int i = 0; i < output.length; i++) {
                var context = provider.forIndex(i);
                if (!shape.affects(context.blockX(), context.blockZ())) {
                    outside = true;
                    break;
                }
            }
            double[] original = outside ? new double[output.length] : null;
            if (outside) input.fillArray(original, provider);
            for (int i = 0; i < output.length; i++) {
                var context = provider.forIndex(i);
                double distance = shape.distance(context.blockX(), context.blockZ());
                if (!shape.affects(context.blockX(), context.blockZ())) output[i] = original[i];
                else if (distance <= shape.radius()) output[i] = -1;
                else if (!roughWall && distance < 2.0 * shape.radius())
                    output[i] = Math.min(output[i], (distance - shape.radius() - 2) / 16.0);
            }
            return;
        }
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
                        surfaceOutput,
                        composed,
                        sampledTerrain.mapAll(visitor),
                        sculptUnderside,
                        roughWall));
    }

    @Override
    public double minValue() {
        return surfaceOutput
                ? Math.min(shape.minY() - 1, originalSurface.minValue())
                : Math.min(
                        -1.0,
                        Math.min(
                                input.minValue(),
                                composed ? surface.minValue() : Double.NEGATIVE_INFINITY));
    }

    @Override
    public double maxValue() {
        return surfaceOutput
                ? Math.max(shape.maxY() - 1, originalSurface.maxValue())
                : Math.max(
                        input.maxValue(), composed ? surface.maxValue() : Double.POSITIVE_INFINITY);
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssHole> codec() {
        return CODEC;
    }
}
