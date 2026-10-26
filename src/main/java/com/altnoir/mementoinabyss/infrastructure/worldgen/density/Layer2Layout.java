package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Objects;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** A shared XZ partition; terrain consumes edge distance, biomes consume the region and sector. */
public final class Layer2Layout implements DensityFunction {
    public static final KeyDispatchDataCodec<Layer2Layout> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            i ->
                                    i.group(
                                                    Codec.intRange(256, 4096)
                                                            .fieldOf("cell_size")
                                                            .forGetter(Layer2Layout::cellSize),
                                                    Codec.doubleRange(.1, .3)
                                                            .fieldOf("primary_radius")
                                                            .forGetter(Layer2Layout::primaryRadius),
                                                    Codec.doubleRange(0, .45)
                                                            .fieldOf("cell_jitter")
                                                            .forGetter(Layer2Layout::cellJitter),
                                                    Codec.intRange(4, 128)
                                                            .fieldOf("river_bank_distance")
                                                            .forGetter(
                                                                    Layer2Layout
                                                                            ::riverBankDistance),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("warp_x")
                                                            .forGetter(Layer2Layout::warpX),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("warp_z")
                                                            .forGetter(Layer2Layout::warpZ),
                                                    Codec.INT
                                                            .fieldOf("min_water_y")
                                                            .forGetter(Layer2Layout::minWaterY),
                                                    Codec.INT
                                                            .fieldOf("water_level")
                                                            .forGetter(Layer2Layout::waterLevel),
                                                    Codec.floatRange(0, 2)
                                                            .fieldOf("opening_scale")
                                                            .forGetter(Layer2Layout::openingScale),
                                                    Codec.floatRange(0, 256)
                                                            .fieldOf("opening_offset")
                                                            .forGetter(Layer2Layout::openingOffset),
                                                    Codec.LONG
                                                            .fieldOf("salt")
                                                            .forGetter(Layer2Layout::salt))
                                            .apply(i, Layer2Layout::new)));

    private final int cellSize, minWaterY, waterLevel, riverBankDistance;
    private final double primaryRadius, cellJitter;
    private final DensityFunction warpX, warpZ;
    private final float openingScale, openingOffset;
    private final long salt, seed;
    private final boolean bound;
    private final int hash;
    private final ThreadLocal<Columns> cache = ThreadLocal.withInitial(Columns::new);

    public Layer2Layout(
            int cellSize,
            double primaryRadius,
            double cellJitter,
            int riverBankDistance,
            DensityFunction warpX,
            DensityFunction warpZ,
            int minWaterY,
            int waterLevel,
            float openingScale,
            float openingOffset,
            long salt) {
        this(
                cellSize,
                primaryRadius,
                cellJitter,
                riverBankDistance,
                warpX,
                warpZ,
                minWaterY,
                waterLevel,
                openingScale,
                openingOffset,
                salt,
                0,
                false);
    }

    private Layer2Layout(
            int cellSize,
            double primaryRadius,
            double cellJitter,
            int riverBankDistance,
            DensityFunction warpX,
            DensityFunction warpZ,
            int minWaterY,
            int waterLevel,
            float openingScale,
            float openingOffset,
            long salt,
            long seed,
            boolean bound) {
        if (minWaterY >= waterLevel)
            throw new IllegalArgumentException("Layer 2 water band is empty");
        this.cellSize = cellSize;
        this.primaryRadius = primaryRadius;
        this.cellJitter = cellJitter;
        this.riverBankDistance = riverBankDistance;
        this.warpX = warpX;
        this.warpZ = warpZ;
        this.minWaterY = minWaterY;
        this.waterLevel = waterLevel;
        this.openingScale = openingScale;
        this.openingOffset = openingOffset;
        this.salt = salt;
        this.seed = seed;
        this.bound = bound;
        this.hash =
                Objects.hash(
                        cellSize,
                        primaryRadius,
                        cellJitter,
                        riverBankDistance,
                        warpX,
                        warpZ,
                        minWaterY,
                        waterLevel,
                        openingScale,
                        openingOffset,
                        salt,
                        seed,
                        bound);
    }

    public int cellSize() {
        return cellSize;
    }

    public double primaryRadius() {
        return primaryRadius;
    }

    public double cellJitter() {
        return cellJitter;
    }

    public int riverBankDistance() {
        return riverBankDistance;
    }

    public DensityFunction warpX() {
        return warpX;
    }

    public DensityFunction warpZ() {
        return warpZ;
    }

    public int minWaterY() {
        return minWaterY;
    }

    public int waterLevel() {
        return waterLevel;
    }

    public float openingScale() {
        return openingScale;
    }

    public float openingOffset() {
        return openingOffset;
    }

    public long salt() {
        return salt;
    }

    public boolean bound() {
        return bound;
    }

    public double openingRadius() {
        return HopperAbyssHole.abyssRadius() * openingScale + openingOffset;
    }

    public Layer2Layout bind(long seed) {
        return bound
                ? this
                : new Layer2Layout(
                        cellSize,
                        primaryRadius,
                        cellJitter,
                        riverBankDistance,
                        warpX,
                        warpZ,
                        minWaterY,
                        waterLevel,
                        openingScale,
                        openingOffset,
                        salt,
                        seed,
                        true);
    }

    public Region sample(int blockX, int blockZ) {
        var columns = cache.get();
        var values = columns.values;
        long key = ((long) blockX << 32) ^ (blockZ & 0xffffffffL);
        Region result = values.getAndMoveToLast(key);
        if (result != null) return result;
        var point = new SinglePointContext(blockX, 0, blockZ);
        double x = blockX + warpX.compute(point), z = blockZ + warpZ.compute(point);
        int cellX = (int) Math.floor(x / cellSize), cellZ = (int) Math.floor(z / cellSize);
        long cellKey = ((long) cellX << 32) ^ (cellZ & 0xffffffffL);
        Cell sites = columns.cells.getAndMoveToLast(cellKey);
        if (sites == null) {
            sites = new Cell(cellX, cellZ, seed, salt, cellSize, cellJitter);
            if (columns.cells.size() >= 32) columns.cells.removeFirst();
            columns.cells.putAndMoveToLast(cellKey, sites);
        }
        double first = Double.POSITIVE_INFINITY;
        int nearest = 0;
        for (int i = 0; i < 25; i++) {
            double dx = x - sites.x[i], dz = z - sites.z[i];
            double d = dx * dx + dz * dz;
            columns.distance[i] = d;
            if (d < first) {
                first = d;
                nearest = i;
            }
        }
        double edge = cellSize;
        // Distance to every competing bisector avoids a jump when the second-nearest site changes.
        for (int i = 0; i < 25; i++) {
            if (i == nearest) continue;
            edge =
                    Math.min(
                            edge,
                            (columns.distance[i] - first) / sites.denominators[nearest * 25 + i]);
        }
        double dx = x - sites.x[nearest], dz = z - sites.z[nearest];
        int sector = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? 1 : 3) : (dz > 0 ? 2 : 0);
        double primaryDistance =
                Math.min(
                        cellSize * primaryRadius - Math.hypot(dx, dz),
                        edge - Math.max(32, riverBankDistance * 2));
        double blend = Math.clamp(primaryDistance / 64, 0, 1);
        blend *= blend * (3 - 2 * blend);
        result =
                new Region(
                        sites.ids[nearest],
                        edge,
                        dx * dx + dz * dz < Math.pow(cellSize * primaryRadius, 2)
                                && edge > Math.max(32, riverBankDistance * 2),
                        sector,
                        primaryDistance,
                        blend,
                        sites.x[nearest],
                        sites.z[nearest]);
        if (values.size() >= 4096) values.removeFirst();
        values.putAndMoveToLast(key, result);
        return result;
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    @Override
    public double compute(FunctionContext context) {
        return sample(context.blockX(), context.blockZ()).edgeDistance() / riverBankDistance;
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // Bound warps are pure world-seeded 2D functions; do not install unused chunk caches for
        // them.
        return visitor.apply(
                bound
                        ? this
                        : new Layer2Layout(
                                cellSize,
                                primaryRadius,
                                cellJitter,
                                riverBankDistance,
                                warpX.mapAll(visitor),
                                warpZ.mapAll(visitor),
                                minWaterY,
                                waterLevel,
                                openingScale,
                                openingOffset,
                                salt,
                                seed,
                                false));
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return cellSize / (double) riverBankDistance;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof Layer2Layout l
                        && cellSize == l.cellSize
                        && primaryRadius == l.primaryRadius
                        && cellJitter == l.cellJitter
                        && riverBankDistance == l.riverBankDistance
                        && minWaterY == l.minWaterY
                        && waterLevel == l.waterLevel
                        && openingScale == l.openingScale
                        && openingOffset == l.openingOffset
                        && salt == l.salt
                        && seed == l.seed
                        && bound == l.bound
                        && warpX.equals(l.warpX)
                        && warpZ.equals(l.warpZ);
    }

    private static final class Columns {
        final Long2ObjectLinkedOpenHashMap<Region> values = new Long2ObjectLinkedOpenHashMap<>();
        final Long2ObjectLinkedOpenHashMap<Cell> cells = new Long2ObjectLinkedOpenHashMap<>();
        final double[] distance = new double[25];
    }

    private static final class Cell {
        final long[] ids = new long[25];
        final double[] x = new double[25], z = new double[25];
        final double[] denominators = new double[625];

        Cell(int cellX, int cellZ, long seed, long salt, int cellSize, double cellJitter) {
            int i = 0;
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++) {
                    int cx = cellX + dx, cz = cellZ + dz;
                    long h =
                            mix(
                                    seed
                                            ^ salt
                                            ^ (long) cx * 0x9e3779b97f4a7c15L
                                            ^ (long) cz * 0xc2b2ae3d27d4eb4fL);
                    ids[i] = h;
                    x[i] =
                            (cx + .5 - cellJitter + ((h >>> 16) & 65535) / 65535.0 * 2 * cellJitter)
                                    * cellSize;
                    z[i] =
                            (cz + .5 - cellJitter + ((h >>> 32) & 65535) / 65535.0 * 2 * cellJitter)
                                    * cellSize;
                    i++;
                }
            // Keep the original division: reciprocal multiplication can shift density zero
            // crossings.
            for (i = 0; i < 25; i++)
                for (int j = i + 1; j < 25; j++) {
                    double distance = 2 * Math.hypot(x[i] - x[j], z[i] - z[j]);
                    denominators[i * 25 + j] = distance;
                    denominators[j * 25 + i] = distance;
                }
        }
    }

    public enum TerrainKind {
        WOODLAND,
        WETLAND,
        HIGHLAND,
        PLATEAU
    }

    public static TerrainKind terrainKind(long regionId) {
        return switch ((int) (mix(regionId ^ 0x624de903b718a5cfL) & 3)) {
            case 0 -> TerrainKind.WOODLAND;
            case 1 -> TerrainKind.WETLAND;
            case 2 -> TerrainKind.HIGHLAND;
            default -> TerrainKind.PLATEAU;
        };
    }

    public static TerrainKind terrainKind(Region region) {
        return terrainKind(region.id());
    }

    public record Region(
            long id,
            double edgeDistance,
            boolean primary,
            int sector,
            double primaryDistance,
            double primaryBlend,
            double siteX,
            double siteZ) {}
}
