package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;

/** Evaluate the actual interpolation lattice when querying a column outside NoiseChunk's loop. */
public final class OverworldAbyssInterpolated implements DensityFunction {
    public static final KeyDispatchDataCodec<OverworldAbyssInterpolated> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("input")
                                                            .forGetter(
                                                                    OverworldAbyssInterpolated
                                                                            ::input),
                                                    Codec.intRange(1, 64)
                                                            .fieldOf("cell_width")
                                                            .forGetter(
                                                                    OverworldAbyssInterpolated
                                                                            ::cellWidth),
                                                    Codec.intRange(1, 64)
                                                            .fieldOf("cell_height")
                                                            .forGetter(
                                                                    OverworldAbyssInterpolated
                                                                            ::cellHeight))
                                            .apply(instance, OverworldAbyssInterpolated::new)));
    private final DensityFunction input;
    private final int cellWidth, cellHeight;
    private final Blender boundBlender;
    private final int hash;
    private final ThreadLocal<Cell> cache;

    public OverworldAbyssInterpolated(DensityFunction input, int cellWidth, int cellHeight) {
        this(input, cellWidth, cellHeight, null);
    }

    private OverworldAbyssInterpolated(
            DensityFunction input, int cellWidth, int cellHeight, Blender blender) {
        if (cellWidth < 1 || cellWidth > 64 || cellHeight < 1 || cellHeight > 64)
            throw new IllegalArgumentException("Invalid interpolation lattice");
        this.input = input;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.boundBlender = blender;
        this.cache =
                ThreadLocal.withInitial(() -> new Cell(input.getClass() == BlendedNoise.class));
        this.hash =
                java.util.Objects.hash(
                        input, cellWidth, cellHeight, System.identityHashCode(blender));
    }

    public OverworldAbyssInterpolated withBlender(Blender blender) {
        return new OverworldAbyssInterpolated(input, cellWidth, cellHeight, blender);
    }

    public DensityFunction input() {
        return input;
    }

    public int cellWidth() {
        return cellWidth;
    }

    public int cellHeight() {
        return cellHeight;
    }

    public static FunctionContext context(int x, int y, int z, Blender blender) {
        return new Sample(x, y, z, blender);
    }

    @Override
    public double compute(FunctionContext context) {
        int x = Math.floorDiv(context.blockX(), cellWidth) * cellWidth;
        int y = Math.floorDiv(context.blockY(), cellHeight) * cellHeight;
        int z = Math.floorDiv(context.blockZ(), cellWidth) * cellWidth;
        Cell cell = cache.get();
        Blender blender = boundBlender == null ? context.getBlender() : boundBlender;
        if (!cell.valid || cell.x != x || cell.y != y || cell.z != z || cell.blender != blender) {
            boolean sameColumn =
                    cell.valid && cell.x == x && cell.z == z && cell.blender == blender;
            boolean down = sameColumn && cell.y == y + cellHeight;
            boolean up = sameColumn && cell.y + cellHeight == y;
            // A descending surface scan shares a complete face with the preceding lattice cell.
            // Reuse it instead of evaluating the world's blended noise eight times per Y step.
            if (down) {
                cell.values[2] = cell.values[0];
                cell.values[3] = cell.values[1];
                cell.values[6] = cell.values[4];
                cell.values[7] = cell.values[5];
            } else if (up) {
                cell.values[0] = cell.values[2];
                cell.values[1] = cell.values[3];
                cell.values[4] = cell.values[6];
                cell.values[5] = cell.values[7];
            }
            cell.valid = false;
            for (int dz = 0; dz < 2; dz++)
                for (int dy = up ? 1 : 0; dy < (down ? 1 : 2); dy++)
                    for (int dx = 0; dx < 2; dx++)
                        cell.values[dx + 2 * dy + 4 * dz] =
                                sample(
                                        cell,
                                        x + dx * cellWidth,
                                        y + dy * cellHeight,
                                        z + dz * cellWidth,
                                        blender);
            cell.x = x;
            cell.y = y;
            cell.z = z;
            cell.blender = blender;
            cell.valid = true;
        }
        double[] v = cell.values;
        return Mth.lerp3(
                (context.blockX() - x) / (double) cellWidth,
                (context.blockY() - y) / (double) cellHeight,
                (context.blockZ() - z) / (double) cellWidth,
                v[0],
                v[1],
                v[2],
                v[3],
                v[4],
                v[5],
                v[6],
                v[7]);
    }

    private double sample(Cell cell, int x, int y, int z, Blender blender) {
        if (cell.nodes == null) return input.compute(new Sample(x, y, z, blender));
        return cell.nodes.sample(input, x, y, z, blender);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(
                new OverworldAbyssInterpolated(
                        input.mapAll(visitor), cellWidth, cellHeight, boundBlender));
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
    public KeyDispatchDataCodec<OverworldAbyssInterpolated> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OverworldAbyssInterpolated that
                && input.equals(that.input)
                && cellWidth == that.cellWidth
                && cellHeight == that.cellHeight
                && boundBlender == that.boundBlender;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private static final class Cell {
        boolean valid;
        int x, y, z;
        Blender blender;
        final double[] values = new double[8];
        final Nodes nodes;

        Cell(boolean immutableNoise) {
            nodes = immutableNoise ? new Nodes() : null;
        }
    }

    // Only raw BlendedNoise is independent of Blender and NoiseChunk's mutable caches. Reuse
    // its exact lattice values as adjacent columns revisit cells; unknown graphs stay uncached.
    private static final class Nodes {
        private static final int SIZE = 1024;
        final int[] xs = new int[SIZE], ys = new int[SIZE], zs = new int[SIZE];
        final boolean[] valid = new boolean[SIZE];
        final double[] values = new double[SIZE];

        double sample(DensityFunction input, int x, int y, int z, Blender blender) {
            int hash = x * 73428767 ^ y * 912931 ^ z * 42317861;
            int index = (hash ^ (hash >>> 16)) & (SIZE - 1);
            if (valid[index] && xs[index] == x && ys[index] == y && zs[index] == z)
                return values[index];
            double value = input.compute(new Sample(x, y, z, blender));
            xs[index] = x;
            ys[index] = y;
            zs[index] = z;
            values[index] = value;
            valid[index] = true;
            return value;
        }
    }

    private record Sample(int blockX, int blockY, int blockZ, Blender blender)
            implements FunctionContext {
        @Override
        public Blender getBlender() {
            return blender;
        }
    }
}
