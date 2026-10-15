package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Bounded per-worker column cache shared by preliminary surface and biome depth queries. */
public final class OverworldAbyssSurface implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssSurface> CODEC =
            KeyDispatchDataCodec.of(
                    DensityFunction.HOLDER_HELPER_CODEC
                            .fieldOf("input")
                            .xmap(OverworldAbyssSurface::new, OverworldAbyssSurface::input));
    private final DensityFunction input;
    private final int hash;
    private final ThreadLocal<Columns> cache = ThreadLocal.withInitial(Columns::new);
    private final ThreadLocal<Mapping> mapped = new ThreadLocal<>();

    public OverworldAbyssSurface(DensityFunction input) {
        this.input = input;
        this.hash = input.hashCode();
    }

    public DensityFunction input() {
        return input;
    }

    @Override
    public double compute(FunctionContext context) {
        Columns columns = cache.get();
        if (columns.blender != context.getBlender()) {
            columns.heights.clear();
            columns.blender = context.getBlender();
        }
        long key = ((long) context.blockX() << 32) ^ (context.blockZ() & 0xffffffffL);
        Double height = columns.heights.get(key);
        if (height == null) {
            height = input.compute(context);
            if (columns.heights.size() >= 256)
                columns.heights.remove(columns.heights.keySet().iterator().next());
            columns.heights.put(key, height);
        }
        return height;
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // The same reference surface is shared by many cave branches. Wire it once per
        // visitor rather than recursively copying the complete original router at every use.
        Mapping previous = mapped.get();
        if (previous != null && previous.visitor.get() == visitor) {
            DensityFunction result = previous.result.get();
            if (result != null) return result;
        }
        DensityFunction result = visitor.apply(new OverworldAbyssSurface(input.mapAll(visitor)));
        mapped.set(new Mapping(new WeakReference<>(visitor), new WeakReference<>(result)));
        return result;
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<OverworldAbyssSurface> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OverworldAbyssSurface that && input.equals(that.input);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private static final class Columns {
        Blender blender;
        final LinkedHashMap<Long, Double> heights = new LinkedHashMap<>();
    }

    private record Mapping(WeakReference<Visitor> visitor, WeakReference<DensityFunction> result) {}
}
