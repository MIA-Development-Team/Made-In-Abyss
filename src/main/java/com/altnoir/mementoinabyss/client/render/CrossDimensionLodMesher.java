package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodKey;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Pure CPU greedy meshing. Worker threads only read immutable payloads and concurrent lookup maps. */
final class CrossDimensionLodMesher {
    private final CrossDimensionLodPayload source;
    private final HeightField sourceHeightField;
    private final CrossDimensionLodPayload west, east, north, south;
    private final HeightField westHeight, eastHeight, northHeight, southHeight;

    private CrossDimensionLodMesher(CrossDimensionLodPayload source,
                                    Map<Long, CrossDimensionLodPayload> chunks,
                                    Map<Long, HeightField> heightFields) {
        this.source = source;
        int x = source.chunkX();
        int z = source.chunkZ();
        this.sourceHeightField = heightFields.get(CrossDimensionLodKey.pack(x, z));
        this.west = chunks.get(CrossDimensionLodKey.pack(x - 1, z));
        this.east = chunks.get(CrossDimensionLodKey.pack(x + 1, z));
        this.north = chunks.get(CrossDimensionLodKey.pack(x, z - 1));
        this.south = chunks.get(CrossDimensionLodKey.pack(x, z + 1));
        this.westHeight = heightFields.get(CrossDimensionLodKey.pack(x - 1, z));
        this.eastHeight = heightFields.get(CrossDimensionLodKey.pack(x + 1, z));
        this.northHeight = heightFields.get(CrossDimensionLodKey.pack(x, z - 1));
        this.southHeight = heightFields.get(CrossDimensionLodKey.pack(x, z + 1));
    }

    static CpuMesh build(CrossDimensionLodPayload payload,
                         Map<Long, CrossDimensionLodPayload> chunks,
                         Map<Long, HeightField> heightFields) {
        return new CrossDimensionLodMesher(payload, chunks, heightFields).build();
    }

    private CpuMesh build() {
        CrossDimensionLodPayload payload = source;
        List<Quad> quads = new ArrayList<>();
        boolean surfaceOnly = payload.cellSize() >= 8;
        for (int face = 0; face < 6; face++) {
            int hiddenFarFace = payload.displayYOffset() > 0 ? 3 : 2;
            if (!surfaceOnly || face != hiddenFarFace) greedyFace(payload, face, surfaceOnly, quads);
        }
        int yOffset = payload.displayYOffset();
        int originX = payload.chunkX() * 16;
        int originZ = payload.chunkZ() * 16;
        AABB bounds = new AABB(originX, payload.minY() + yOffset, originZ,
                originX + 16, payload.minY() + payload.yCells() * payload.cellSize() + yOffset, originZ + 16);
        // The completed mesh owns this list; copying it here allocates another backing
        // array for every rebuilt chunk without providing any additional safety.
        return new CpuMesh(quads, bounds, payload.chunkX(), payload.chunkZ(), payload.cellSize());
    }

    static HeightField buildHeightField(CrossDimensionLodPayload payload) {
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

    private void greedyFace(CrossDimensionLodPayload payload, int face,
                            boolean surfaceOnly, List<Quad> quads) {
        int size = 16 / payload.cellSize();
        int planes = face < 2 ? size : face < 4 ? payload.yCells() : size;
        int width = size;
        int height = face < 2 ? payload.yCells() : face < 4 ? size : payload.yCells();
        int[] mask = new int[width * height];

        for (int plane = 0; plane < planes; plane++) {
            for (int v = 0; v < height; v++) {
                for (int u = 0; u < width; u++) {
                    int x, y, z, nx, ny, nz;
                    if (face < 2) {
                        x = plane; y = v; z = u;
                        nx = x + (face == 0 ? -1 : 1); ny = y; nz = z;
                    } else if (face < 4) {
                        x = u; y = plane; z = v;
                        nx = x; ny = y + (face == 2 ? -1 : 1); nz = z;
                    } else {
                        x = u; y = v; z = plane;
                        nx = x; ny = y; nz = z + (face == 4 ? -1 : 1);
                    }
                    int state = stateForMesh(payload, x, y, z, surfaceOnly);
                    int neighbor = stateForMesh(payload, nx, ny, nz, surfaceOnly);
                    mask[v * width + u] = state >= 0 && neighbor < 0 ? state + 1 : 0;
                }
            }
            mergeMask(payload, face, plane, width, height, mask, quads);
        }
    }

    private static void mergeMask(CrossDimensionLodPayload payload, int face, int plane,
                                  int width, int height, int[] mask, List<Quad> quads) {
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width;) {
                int value = mask[v * width + u];
                if (value == 0) { u++; continue; }
                int rectangleWidth = 1;
                while (u + rectangleWidth < width && mask[v * width + u + rectangleWidth] == value) {
                    rectangleWidth++;
                }
                int rectangleHeight = 1;
                heightLoop:
                while (v + rectangleHeight < height) {
                    for (int x = 0; x < rectangleWidth; x++) {
                        if (mask[(v + rectangleHeight) * width + u + x] != value) break heightLoop;
                    }
                    rectangleHeight++;
                }
                addGreedyQuad(payload, face, plane, u, v, rectangleWidth, rectangleHeight, value - 1, quads);
                for (int y = 0; y < rectangleHeight; y++) {
                    for (int x = 0; x < rectangleWidth; x++) mask[(v + y) * width + u + x] = 0;
                }
                u += rectangleWidth;
            }
        }
    }

    private static void addGreedyQuad(CrossDimensionLodPayload p, int face, int plane,
                                      int u, int v, int width, int height, int stateId, List<Quad> quads) {
        float cell = p.cellSize();
        float ox = p.chunkX() * 16.0F;
        float oy = p.minY() + p.displayYOffset();
        float oz = p.chunkZ() * 16.0F;
        float x0, x1, y0, y1, z0, z1;
        if (face < 2) {
            x0 = x1 = ox + (plane + (face == 1 ? 1 : 0)) * cell;
            z0 = oz + u * cell; z1 = z0 + width * cell;
            y0 = oy + v * cell; y1 = y0 + height * cell;
        } else if (face < 4) {
            y0 = y1 = oy + (plane + (face == 3 ? 1 : 0)) * cell;
            x0 = ox + u * cell; x1 = x0 + width * cell;
            z0 = oz + v * cell; z1 = z0 + height * cell;
        } else {
            z0 = z1 = oz + (plane + (face == 5 ? 1 : 0)) * cell;
            x0 = ox + u * cell; x1 = x0 + width * cell;
            y0 = oy + v * cell; y1 = y0 + height * cell;
        }
        int color = face == 2 ? 0xFF999999 : face == 3 ? 0xFFFFFFFF : 0xFFCCCCCC;
        quads.add(new Quad(x0, y0, z0, x1, y1, z1, color, face, stateId));
    }

    private int stateForMesh(CrossDimensionLodPayload source, int x, int y, int z, boolean surfaceOnly) {
        return surfaceOnly ? surfaceStateAt(source, x, y, z) : stateAt(source, x, y, z);
    }

    /** Returns the block-state ID, or -1 for air. Supports horizontal neighbor chunks. */
    private int stateAt(CrossDimensionLodPayload source, int x, int y, int z) {
        if (y < 0 || y >= source.yCells()) return -1;
        int sourceSize = 16 / source.cellSize();
        int worldX = source.chunkX() * 16 + (x < 0 ? -1 : x >= sourceSize ? 16 : x * source.cellSize());
        int worldZ = source.chunkZ() * 16 + (z < 0 ? -1 : z >= sourceSize ? 16 : z * source.cellSize());
        int worldY = source.minY() + y * source.cellSize();
        int chunkX = Math.floorDiv(worldX, 16);
        int chunkZ = Math.floorDiv(worldZ, 16);
        boolean crossedBoundary = chunkX != source.chunkX() || chunkZ != source.chunkZ();
        CrossDimensionLodPayload data = crossedBoundary ? neighborChunk(chunkX, chunkZ) : source;
        if (data == null) return -1;
        int dataHeight = data.yCells() * data.cellSize();
        if (worldY < data.minY() || worldY >= data.minY() + dataHeight) return -1;
        if (crossedBoundary && data.cellSize() != source.cellSize()) {
            // The coarse side owns the boundary wall; fine geometry only fills beyond its height field.
            if (source.cellSize() > data.cellSize()) return -1;
            HeightField heights = neighborHeightField(chunkX, chunkZ);
            if (heights == null) return -1;
            int index = Math.floorMod(worldZ, 16) / data.cellSize() * heights.size
                    + Math.floorMod(worldX, 16) / data.cellSize();
            if (source.displayYOffset() > 0) {
                return worldY >= heights.bottomY[index] ? heights.bottomState[index] : -1;
            }
            return worldY <= heights.topY[index] ? heights.topState[index] : -1;
        }
        int dataSize = 16 / data.cellSize();
        int dataX = Math.floorMod(worldX, 16) / data.cellSize();
        int dataZ = Math.floorMod(worldZ, 16) / data.cellSize();
        int dataY = (worldY - data.minY()) / data.cellSize();
        short paletteIndex = data.voxels()[(dataZ * dataSize + dataX) * data.yCells() + dataY];
        return paletteIndex == 0 ? -1 : data.palette()[paletteIndex];
    }

    /** Height-field lookup used by coarse levels to discard caves and hidden interior surfaces. */
    private int surfaceStateAt(CrossDimensionLodPayload source, int x, int y, int z) {
        if (y < 0 || y >= source.yCells()) return -1;
        int sourceSize = 16 / source.cellSize();
        int worldX = source.chunkX() * 16 + (x < 0 ? -1 : x >= sourceSize ? 16 : x * source.cellSize());
        int worldZ = source.chunkZ() * 16 + (z < 0 ? -1 : z >= sourceSize ? 16 : z * source.cellSize());
        int worldY = source.minY() + y * source.cellSize();
        int chunkX = Math.floorDiv(worldX, 16);
        int chunkZ = Math.floorDiv(worldZ, 16);
        boolean crossedBoundary = chunkX != source.chunkX() || chunkZ != source.chunkZ();
        CrossDimensionLodPayload data = crossedBoundary ? neighborChunk(chunkX, chunkZ) : source;
        HeightField heights = crossedBoundary ? neighborHeightField(chunkX, chunkZ) : sourceHeightField;
        if (data == null || heights == null) return -1;
        if (crossedBoundary && data.cellSize() != source.cellSize() && source.cellSize() > data.cellSize()) return -1;
        int dataX = Math.floorMod(worldX, 16) / data.cellSize();
        int dataZ = Math.floorMod(worldZ, 16) / data.cellSize();
        int index = dataZ * heights.size + dataX;
        if (source.displayYOffset() > 0) {
            return worldY >= heights.bottomY[index] ? heights.bottomState[index] : -1;
        }
        return worldY <= heights.topY[index] ? heights.topState[index] : -1;
    }

    private CrossDimensionLodPayload neighborChunk(int chunkX, int chunkZ) {
        if (chunkX < source.chunkX()) return west;
        if (chunkX > source.chunkX()) return east;
        if (chunkZ < source.chunkZ()) return north;
        if (chunkZ > source.chunkZ()) return south;
        return source;
    }

    private HeightField neighborHeightField(int chunkX, int chunkZ) {
        if (chunkX < source.chunkX()) return westHeight;
        if (chunkX > source.chunkX()) return eastHeight;
        if (chunkZ < source.chunkZ()) return northHeight;
        if (chunkZ > source.chunkZ()) return southHeight;
        return sourceHeightField;
    }

    static final class CpuMesh {
        final List<Quad> quads;
        final AABB bounds;
        final int chunkX;
        final int chunkZ;
        final int cellSize;

        private CpuMesh(List<Quad> quads, AABB bounds, int chunkX, int chunkZ, int cellSize) {
            this.quads = quads;
            this.bounds = bounds;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.cellSize = cellSize;
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

    static final class Quad {
        final float x0, y0, z0, x1, y1, z1;
        final int color;
        final int face;
        final int stateId;

        private Quad(float x0, float y0, float z0, float x1, float y1, float z1,
                     int color, int face, int stateId) {
            this.x0 = x0; this.y0 = y0; this.z0 = z0;
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.color = color;
            this.face = face;
            this.stateId = stateId;
        }
    }
}
