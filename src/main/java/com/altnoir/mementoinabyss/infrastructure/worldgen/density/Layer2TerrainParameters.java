package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

/** Region selectors supply scalar inputs; the serialized vanilla graph shapes the terrain. */
public final class Layer2TerrainParameters implements DensityFunction {
    public enum Parameter {
        TERRAIN_KIND,
        PRIMARY_BLEND
    }

    public static final KeyDispatchDataCodec<Layer2TerrainParameters> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("layout")
                                                            .forGetter(
                                                                    Layer2TerrainParameters
                                                                            ::layout),
                                                    Codec.STRING
                                                            .xmap(
                                                                    Parameter::valueOf,
                                                                    Parameter::name)
                                                            .fieldOf("parameter")
                                                            .forGetter(
                                                                    Layer2TerrainParameters
                                                                            ::parameter))
                                            .apply(instance, Layer2TerrainParameters::new)));

    private final DensityFunction layout;
    private volatile @Nullable Layer2Layout partition;
    private final Parameter parameter;

    public Layer2TerrainParameters(DensityFunction layout, Parameter parameter) {
        this(layout, parameter, null);
    }

    private Layer2TerrainParameters(
            DensityFunction layout, Parameter parameter, @Nullable Layer2Layout partition) {
        this.layout = layout;
        this.parameter = parameter;
        this.partition = partition;
    }

    private Layer2Layout partition() {
        // Registry bootstrap can construct the selector before the referenced layout is bound.
        Layer2Layout cached = partition;
        if (cached != null) return cached;
        Layer2Layout[] found = new Layer2Layout[1];
        layout.mapAll(
                function -> {
                    if (function instanceof Layer2Layout candidate) found[0] = candidate;
                    return function;
                });
        if (found[0] == null) throw new IllegalArgumentException("Missing layer 2 partition");
        this.partition = found[0];
        return found[0];
    }

    public DensityFunction layout() {
        return layout;
    }

    public Parameter parameter() {
        return parameter;
    }

    @Override
    public double compute(FunctionContext context) {
        var region = partition().sample(context.blockX(), context.blockZ());
        return switch (parameter) {
            case TERRAIN_KIND -> Layer2Layout.terrainKind(region).ordinal();
            case PRIMARY_BLEND -> region.primaryBlend();
        };
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        Layer2Layout cached = partition;
        // The bound partition is the only runtime input; remapping its codec field adds unused
        // chunk caches.
        if (cached != null && cached.bound()) return visitor.apply(this);
        Layer2Layout[] found = {cached};
        DensityFunction mapped =
                layout.mapAll(
                        new Visitor() {
                            @Override
                            public DensityFunction apply(DensityFunction function) {
                                DensityFunction result = visitor.apply(function);
                                if (result instanceof Layer2Layout candidate) found[0] = candidate;
                                else if (function instanceof Layer2Layout candidate)
                                    found[0] = candidate;
                                return result;
                            }

                            @Override
                            public NoiseHolder visitNoise(NoiseHolder noise) {
                                return visitor.visitNoise(noise);
                            }
                        });
        // Chunk cache wrappers can hide their children after mapping, so retain the bound
        // partition while it is still visible to the visitor.
        return visitor.apply(new Layer2TerrainParameters(mapped, parameter, found[0]));
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return switch (parameter) {
            case TERRAIN_KIND -> 3;
            case PRIMARY_BLEND -> 1;
        };
    }

    @Override
    public KeyDispatchDataCodec<Layer2TerrainParameters> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof Layer2TerrainParameters selector
                        && parameter == selector.parameter
                        && layout.equals(selector.layout);
    }

    @Override
    public int hashCode() {
        return 31 * layout.hashCode() + parameter.hashCode();
    }
}
