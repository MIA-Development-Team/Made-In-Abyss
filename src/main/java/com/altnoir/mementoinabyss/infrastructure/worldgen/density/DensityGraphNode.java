package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** A named graph boundary with immutable, precomputed structural metadata. */
public final class DensityGraphNode implements DensityFunction {
    private record Mapping(Visitor visitor, Map<DensityGraphNode, DensityFunction> values) {}

    private static final ThreadLocal<Mapping> MAPPING = new ThreadLocal<>();
    public static final KeyDispatchDataCodec<DensityGraphNode> CODEC =
            KeyDispatchDataCodec.of(
                    DensityFunction.HOLDER_HELPER_CODEC
                            .fieldOf("argument")
                            .xmap(DensityGraphNode::new, DensityGraphNode::argument));

    private final DensityFunction argument;
    private final int hash;
    private final double min, max;

    public DensityGraphNode(DensityFunction argument) {
        this.argument = argument;
        this.hash = 31 * DensityGraphNode.class.hashCode() + argument.hashCode();
        this.min = argument.minValue();
        this.max = argument.maxValue();
    }

    public DensityFunction argument() {
        return argument;
    }

    @Override
    public double compute(FunctionContext context) {
        return argument.compute(context);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        argument.fillArray(output, provider);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        Mapping previous = MAPPING.get();
        boolean owner = previous == null || previous.visitor() != visitor;
        Mapping mapping = owner ? new Mapping(visitor, new IdentityHashMap<>()) : previous;
        if (owner) MAPPING.set(mapping);
        try {
            DensityFunction known = mapping.values().get(this);
            if (known != null) return known;
            DensityFunction result = visitor.apply(new DensityGraphNode(argument.mapAll(visitor)));
            mapping.values().put(this, result);
            return result;
        } finally {
            // Keep only one traversal's DAG; never retain a chunk visitor or world between calls.
            if (owner) {
                if (previous == null) MAPPING.remove();
                else MAPPING.set(previous);
            }
        }
    }

    @Override
    public double minValue() {
        return min;
    }

    @Override
    public double maxValue() {
        return max;
    }

    @Override
    public KeyDispatchDataCodec<DensityGraphNode> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof DensityGraphNode node
                        && hash == node.hash
                        && argument.equals(node.argument);
    }
}
