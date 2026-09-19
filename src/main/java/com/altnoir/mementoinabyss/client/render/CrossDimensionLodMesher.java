package com.altnoir.mementoinabyss.client.render;

import net.minecraft.world.phys.AABB;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodAmbientOcclusion.View;

import java.util.Arrays;

/** Pure CPU greedy meshing. Worker threads only read immutable payloads and concurrent lookup maps. */
final class CrossDimensionLodMesher {
    private final CrossDimensionLodColumn source;
    private final HeightField sourceHeightField;
    private CrossDimensionLodMesher(CrossDimensionLodColumn source) {
        this.source = source;
        this.sourceHeightField = source.heightField();
    }

    static CpuMesh build(CrossDimensionLodColumn payload, boolean ambientOcclusion) {
        int originX = payload.chunkX() * 16, originZ = payload.chunkZ() * 16;
        AABB bounds = new AABB(originX, payload.minY() + payload.displayYOffset(), originZ,
                originX + 16, payload.minY() + payload.yCells() * payload.cellSize() + payload.displayYOffset(), originZ + 16);
        boolean ao = ambientOcclusion && payload.cellSize() <= 4;
        return new CpuMesh(payload.interiorCopy(ao), payload.sides(), bounds,
                payload.chunkX(), payload.chunkZ(), payload.cellSize(), ao ? payload : null);
    }

    static QuadBuffer buildInterior(CrossDimensionLodColumn payload, boolean ao) {
        var quads = new QuadBuffer(128);
        View view = ao && payload.cellSize() <= 4
                ? new View(payload.chunkX(), payload.chunkZ(), 1, new Side[1][]).withSource(payload) : null;
        new CrossDimensionLodMesher(payload).meshInteriors(quads, view, false);
        return quads;
    }

    static void appendAoRim(QuadBuffer quads, CrossDimensionLodColumn source, View view) {
        if (source != null) new CrossDimensionLodMesher(source).meshInteriors(quads, view.withSource(source), true);
    }

    static HeightField buildHeightField(CrossDimensionLodColumn payload) {
        int size = 16 / payload.cellSize();
        int[] topY = new int[size * size];
        int[] topState = new int[size * size];
        int[] bottomY = new int[size * size];
        int[] bottomState = new int[size * size];
        Arrays.fill(topY, Integer.MIN_VALUE);
        Arrays.fill(topState, -1);
        Arrays.fill(bottomY, Integer.MAX_VALUE);
        Arrays.fill(bottomState, -1);
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                int index = z * size + x;
                for (int y = payload.yCells() - 1; y >= 0; y--) {
                    short paletteIndex = payload.voxels()[(z * size + x) * payload.yCells() + y];
                    if (paletteIndex != 0) {
                        topY[index] = payload.minY() + y * payload.cellSize();
                        topState[index] = payload.palette()[paletteIndex];
                        break;
                    }
                }
                for (int y = 0; y < payload.yCells(); y++) {
                    short paletteIndex = payload.voxels()[(z * size + x) * payload.yCells() + y];
                    if (paletteIndex != 0) {
                        bottomY[index] = payload.minY() + y * payload.cellSize();
                        bottomState[index] = payload.palette()[paletteIndex];
                        break;
                    }
                }
            }
        }
        return new HeightField(size, topY, topState, bottomY, bottomState);
    }

    private void meshInteriors(QuadBuffer quads, View ao, boolean rimOnly) {
        int size = 16 / source.cellSize();
        int[] mask = new int[size * Math.max(size, source.yCells())];
        int[] light = ao == null ? null : new int[mask.length];
        for (int face = 0; face < 6; face++) {
            if (source.cellSize() >= 8 && face == (source.displayYOffset() > 0 ? 3 : 2)) continue;
            int planes = face < 2 || face >= 4 ? size : source.yCells();
            int height = face < 2 || face >= 4 ? source.yCells() : size;
            for (int plane = 0; plane < planes; plane++) {
                if ((face == 0 || face == 4) && plane == 0 || (face == 1 || face == 5) && plane == size - 1) continue;
                if (ao == null) {
                    meshRegion(face, plane, 0, 0, size, height, quads, mask, light, null);
                } else if (!rimOnly) {
                    meshRegion(face, plane, 1, face == 2 || face == 3 ? 1 : 0,
                            size - 2, face == 2 || face == 3 ? height - 2 : height, quads, mask, light, ao);
                } else {
                    meshRegion(face, plane, 0, 0, 1, height, quads, mask, light, ao);
                    meshRegion(face, plane, size - 1, 0, 1, height, quads, mask, light, ao);
                    if (face == 2 || face == 3) {
                        meshRegion(face, plane, 1, 0, size - 2, 1, quads, mask, light, ao);
                        meshRegion(face, plane, 1, height - 1, size - 2, 1, quads, mask, light, ao);
                    }
                }
            }
        }
    }

    private void meshRegion(int face, int plane, int startU, int startV, int width, int height,
                            QuadBuffer quads, int[] mask, int[] light, View ao) {
        int cell = source.cellSize();
        int ox = source.chunkX() * 16, oy = source.minY() + source.displayYOffset(), oz = source.chunkZ() * 16;
        int offset = (plane + (face & 1)) * cell;
        if (face < 2) { ox += offset; oz += startU * cell; oy += startV * cell; }
        else if (face < 4) { oy += offset; ox += startU * cell; oz += startV * cell; }
        else { oz += offset; ox += startU * cell; oy += startV * cell; }
        for (int v = 0; v < height; v++) for (int u = 0; u < width; u++) {
            int su = startU + u, sv = startV + v;
            int x = face < 2 ? plane : su, y = face >= 2 && face < 4 ? plane : sv, z = face < 2 ? su : face < 4 ? sv : plane;
            int nx = x + (face == 0 ? -1 : face == 1 ? 1 : 0);
            int ny = y + (face == 2 ? -1 : face == 3 ? 1 : 0);
            int nz = z + (face == 4 ? -1 : face == 5 ? 1 : 0);
            int state = renderedState(source, sourceHeightField, x, y, z);
            int index = v * width + u;
            mask[index] = state >= 0 && renderedState(source, sourceHeightField, nx, ny, nz) < 0 ? state + 1 : 0;
            if (light != null && mask[index] != 0) light[index] = CrossDimensionLodAmbientOcclusion.corners(ao, face,
                    ox + (face < 2 ? 0 : u * cell), oy + (face >= 2 && face < 4 ? 0 : v * cell),
                    oz + (face < 2 ? u * cell : face < 4 ? v * cell : 0), cell, cell);
        }
        mergeMask(face, width, height, mask, light, quads, ox, oy, oz, cell, cell);
    }

    /** Immutable rendered boundary samples; never retains a column's full voxel body or packed vertices. */
    record Side(int cellSize, int minY, int yCells, int[] palette, short[] cells) {
        int maxY() { return minY + yCells * cellSize; }

        int state(int along, int worldY) {
            int y = Math.floorDiv(worldY - minY, cellSize);
            if (y < 0 || y >= yCells) return -1;
            int index = cells.length == 1 ? 0 : cells.length == yCells ? y : along / cellSize * yCells + y;
            int value = Short.toUnsignedInt(cells[index]);
            return value == 0 ? -1 : palette[value];
        }

        static Side capture(CrossDimensionLodColumn column, int side) {
            int width = 16 / column.cellSize(), height = column.yCells();
            short[] cells = new short[width * height];
            int edge = (side & 1) == 0 ? 0 : width - 1;
            for (int u = 0; u < width; u++) {
                int x = side < 2 ? edge : u, z = side < 2 ? u : edge;
                System.arraycopy(column.voxels(), (z * width + x) * height, cells, u * height, height);
                if (column.cellSize() >= 8) {
                    int y = column.displayYOffset() > 0 ? 0 : height - 1;
                    int direction = column.displayYOffset() > 0 ? 1 : -1;
                    while (y >= 0 && y < height && cells[u * height + y] == 0) y += direction;
                    if (y >= 0 && y < height) Arrays.fill(cells,
                            u * height + (direction > 0 ? y : 0),
                            u * height + (direction > 0 ? height : y + 1), cells[u * height + y]);
                }
            }
            // Flat strata and uniform sides need only one vertical strip, or one palette index.
            boolean repeated = true, uniform = true;
            for (int i = 1; i < cells.length; i++) {
                uniform &= cells[i] == cells[0];
                repeated &= cells[i] == cells[i % height];
            }
            if (uniform) cells = new short[]{cells[0]};
            else if (repeated && width > 1) cells = Arrays.copyOf(cells, height);
            return new Side(column.cellSize(), column.minY() + column.displayYOffset(), height, column.palette(), cells);
        }
    }

    /** Reuses one mask for all boundaries of a frozen page and its four shared edges. */
    static final class BoundaryBuilder {
        private int[] mask = new int[0], light = new int[0];
        private final View ao;

        BoundaryBuilder(View ao) { this.ao = ao; }

        void append(QuadBuffer quads, int side, int chunkX, int chunkZ, Side source, Side neighbour) {
            if (source == null) return;
            int cell = neighbour == null ? source.cellSize : Math.min(source.cellSize, neighbour.cellSize);
            int yStep = cell;
            if (neighbour != null) while (Math.floorMod(source.minY - neighbour.minY, yStep) != 0) yStep /= 2;
            int width = 16 / cell, height = source.yCells * source.cellSize / yStep;
            if (mask.length < width * height) {
                mask = new int[width * height];
                light = new int[mask.length];
            }
            int face = side < 2 ? side : side + 2;
            int ox = chunkX * 16 + (side == 1 ? 16 : 0), oz = chunkZ * 16 + (side == 3 ? 16 : 0);
            boolean shade = ao != null && source.cellSize <= 4;
            for (int v = 0; v < height; v++) {
                int y = source.minY + v * yStep;
                for (int u = 0; u < width; u++) {
                    int state = source.state(u * cell, y);
                    boolean covered = neighbour != null && neighbour.state(u * cell, y) >= 0;
                    int index = v * width + u;
                    mask[index] = state >= 0 && !covered ? state + 1 : 0;
                    if (shade && mask[index] != 0) light[index] = CrossDimensionLodAmbientOcclusion.corners(ao, face,
                            ox + (side < 2 ? 0 : u * cell), y, oz + (side < 2 ? u * cell : 0), cell, yStep);
                }
            }
            mergeMask(face, width, height, mask, shade ? light : null, quads,
                    ox, source.minY, oz, cell, yStep);
        }
    }

    private static void mergeMask(int face, int width, int height, int[] mask, int[] light, QuadBuffer quads,
                                  float ox, float oy, float oz, float cellU, float cellV) {
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width;) {
                int value = mask[v * width + u];
                if (value == 0) { u++; continue; }
                int ao = light == null ? CrossDimensionLodAmbientOcclusion.UNOCCLUDED : light[v * width + u];
                // A gradient may extend along its constant axis, never along the axis that changes brightness.
                boolean stretchU = CrossDimensionLodAmbientOcclusion.constantU(ao);
                boolean stretchV = CrossDimensionLodAmbientOcclusion.constantV(ao);
                int rectangleWidth = 1;
                while (stretchU && u + rectangleWidth < width && mask[v * width + u + rectangleWidth] == value
                        && (light == null || light[v * width + u + rectangleWidth] == ao)) {
                    rectangleWidth++;
                }
                int rectangleHeight = 1;
                heightLoop:
                while (stretchV && v + rectangleHeight < height) {
                    for (int x = 0; x < rectangleWidth; x++) {
                        int index = (v + rectangleHeight) * width + u + x;
                        if (mask[index] != value || light != null && light[index] != ao) break heightLoop;
                    }
                    rectangleHeight++;
                }
                addGreedyQuad(face, ox, oy, oz, u * cellU, v * cellV,
                        rectangleWidth * cellU, rectangleHeight * cellV, value - 1, ao, quads);
                for (int y = 0; y < rectangleHeight; y++) {
                    for (int x = 0; x < rectangleWidth; x++) mask[(v + y) * width + u + x] = 0;
                }
                u += rectangleWidth;
            }
        }
    }

    private static void addGreedyQuad(int face, float ox, float oy, float oz,
                                      float u, float v, float width, float height, int stateId, int ao, QuadBuffer quads) {
        float x0, x1, y0, y1, z0, z1;
        if (face < 2) {
            x0 = x1 = ox;
            z0 = oz + u; z1 = z0 + width;
            y0 = oy + v; y1 = y0 + height;
        } else if (face < 4) {
            y0 = y1 = oy;
            x0 = ox + u; x1 = x0 + width;
            z0 = oz + v; z1 = z0 + height;
        } else {
            z0 = z1 = oz;
            x0 = ox + u; x1 = x0 + width;
            y0 = oy + v; y1 = y0 + height;
        }
        quads.add(x0, y0, z0, x1, y1, z1, face, stateId, ao);
    }

    /** Local occupancy, including the finite vertical bounds of coarse height envelopes. */
    private static int renderedState(CrossDimensionLodColumn column, HeightField heights, int x, int y, int z) {
        int size = 16 / column.cellSize();
        if (x < 0 || x >= size || z < 0 || z >= size || y < 0 || y >= column.yCells()) return -1;
        if (heights != null) {
            int index = z * size + x, worldY = column.minY() + y * column.cellSize();
            return column.displayYOffset() > 0
                    ? worldY >= heights.bottomY[index] ? heights.bottomState[index] : -1
                    : worldY <= heights.topY[index] ? heights.topState[index] : -1;
        }
        int index = Short.toUnsignedInt(column.voxels()[(z * size + x) * column.yCells() + y]);
        return index == 0 ? -1 : column.palette()[index];
    }

    static final class CpuMesh {
        final QuadBuffer quads;
        final Side[] sides;
        final AABB bounds;
        final int chunkX;
        final int chunkZ;
        final int cellSize;
        final CrossDimensionLodColumn rimSource;

        private CpuMesh(QuadBuffer quads, Side[] sides, AABB bounds,
                        int chunkX, int chunkZ, int cellSize, CrossDimensionLodColumn rimSource) {
            this.quads = quads;
            this.sides = sides;
            this.bounds = bounds;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.cellSize = cellSize;
            this.rimSource = rimSource;
        }
    }

    static final class HeightField {
        final int size;
        final int[] topY;
        final int[] topState;
        final int[] bottomY;
        final int[] bottomState;

        private HeightField(int size, int[] topY, int[] topState, int[] bottomY, int[] bottomState) {
            this.size = size;
            this.topY = topY;
            this.topState = topState;
            this.bottomY = bottomY;
            this.bottomState = bottomState;
        }
    }

    /** Structure-of-arrays storage avoids one Java object allocation per greedy quad. */
    static final class QuadBuffer {
        static final int ATTRIBUTE_STRIDE = 3;
        float[] coordinates;
        int[] attributes;
        int size;

        QuadBuffer(int initialCapacity) {
            coordinates = new float[initialCapacity * 6];
            attributes = new int[initialCapacity * ATTRIBUTE_STRIDE];
        }

        private void add(float x0, float y0, float z0, float x1, float y1, float z1,
                         int face, int stateId, int ao) {
            ensureCapacity(size + 1);
            int coordinate = size * 6;
            coordinates[coordinate] = x0;
            coordinates[coordinate + 1] = y0;
            coordinates[coordinate + 2] = z0;
            coordinates[coordinate + 3] = x1;
            coordinates[coordinate + 4] = y1;
            coordinates[coordinate + 5] = z1;
            int attribute = size * ATTRIBUTE_STRIDE;
            attributes[attribute] = face;
            attributes[attribute + 1] = stateId;
            attributes[attribute + 2] = ao;
            size++;
        }

        void compact() {
            coordinates = Arrays.copyOf(coordinates, size * 6);
            attributes = Arrays.copyOf(attributes, size * ATTRIBUTE_STRIDE);
        }

        QuadBuffer copy() {
            var copy = new QuadBuffer(Math.max(128, size));
            System.arraycopy(coordinates, 0, copy.coordinates, 0, size * 6);
            System.arraycopy(attributes, 0, copy.attributes, 0, size * ATTRIBUTE_STRIDE);
            copy.size = size;
            return copy;
        }

        private void ensureCapacity(int wanted) {
            int capacity = attributes.length / ATTRIBUTE_STRIDE;
            if (wanted <= capacity) return;
            int grown = Math.max(wanted, capacity + (capacity >> 1));
            coordinates = Arrays.copyOf(coordinates, grown * 6);
            attributes = Arrays.copyOf(attributes, grown * ATTRIBUTE_STRIDE);
        }
    }
}
