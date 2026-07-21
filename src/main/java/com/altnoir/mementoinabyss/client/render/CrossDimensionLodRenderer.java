package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.compat.SodiumLodCompat;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/** Renders server-provided Great Fault voxel chunks with six-direction greedy meshing. */
public final class CrossDimensionLodRenderer {
    private static final double OUTSIDE_PLANE_SCALE = 4.0;
    private static final int PLANE_SEGMENTS = 128;
    private static final int MAX_QUADS_PER_FRAME = 750_000;
    private static final int MESH_REBUILDS_PER_FRAME = 12;
    private static final Map<Long, CrossDimensionLodPayload> DATA = new ConcurrentHashMap<>();
    private static final Map<Long, HeightField> HEIGHT_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Long, ChunkMesh> CHUNKS = new ConcurrentHashMap<>();
    private static final Set<Long> DIRTY_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final Map<Long, TextureAtlasSprite> FACE_SPRITES = new ConcurrentHashMap<>();
    /** Reused between extraction and rendering in the same frame; custom geometry is rendered before next extraction. */
    private static final ArrayList<ChunkMesh> VISIBLE_MESHES = new ArrayList<>();
    private static volatile int serverRadius;
    private static volatile int serverCenterX;
    private static volatile int serverCenterZ;
    private static volatile int serverPlaneY = -280;

    public static void accept(CrossDimensionLodPayload payload) {
        if (payload.reset()) {
            DATA.clear();
            HEIGHT_FIELDS.clear();
            CHUNKS.clear();
            DIRTY_CHUNKS.clear();
        }
        serverRadius = payload.radius();
        serverCenterX = payload.centerX();
        serverCenterZ = payload.centerZ();
        serverPlaneY = payload.outsidePlaneY();
        long key = ChunkPos.pack(payload.chunkX(), payload.chunkZ());
        DATA.put(key, payload);
        HEIGHT_FIELDS.put(key, buildHeightField(payload));
        markDirty(payload.chunkX(), payload.chunkZ());
        markDirty(payload.chunkX() - 1, payload.chunkZ());
        markDirty(payload.chunkX() + 1, payload.chunkZ());
        markDirty(payload.chunkX(), payload.chunkZ() - 1);
        markDirty(payload.chunkX(), payload.chunkZ() + 1);
        ChunkMesh mesh = CHUNKS.get(key);
        if (payload.reset() || (mesh != null && mesh.quads.isEmpty())) {
            MementoInAbyss.LOGGER.info("Received Great Fault LOD chunk [{},{}]: {} greedy quads",
                    payload.chunkX(), payload.chunkZ(), mesh == null ? 0 : mesh.quads.size());
        }
    }

    private static void rebuild(int chunkX, int chunkZ) {
        long key = ChunkPos.pack(chunkX, chunkZ);
        CrossDimensionLodPayload data = DATA.get(key);
        if (data != null) CHUNKS.put(key, buildMesh(data));
    }

    private static void markDirty(int chunkX, int chunkZ) {
        DIRTY_CHUNKS.add(ChunkPos.pack(chunkX, chunkZ));
    }

    private static void rebuildDirtyChunks() {
        int rebuilt = 0;
        for (long key : DIRTY_CHUNKS) {
            if (rebuilt++ >= MESH_REBUILDS_PER_FRAME) break;
            if (!DIRTY_CHUNKS.remove(key)) continue;
            rebuild(ChunkPos.getX(key), ChunkPos.getZ(key));
        }
    }

    public static void clear() {
        DATA.clear();
        HEIGHT_FIELDS.clear();
        CHUNKS.clear();
        DIRTY_CHUNKS.clear();
        VISIBLE_MESHES.clear();
        FACE_SPRITES.clear();
        serverRadius = 0;
        serverCenterX = serverCenterZ = 0;
    }

    public static void submit(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).isEmpty()
                || !MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get()) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        if (camera == null) return;
        rebuildDirtyChunks();
        double radius = serverRadius > 0 ? serverRadius
                : CrossDimensionLodLinks.radius(CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElseThrow());
        double farRadius = Math.max(radius * OUTSIDE_PLANE_SCALE,
                event.getLevelRenderState().cameraRenderState.depthFar * 1.25);
        var frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        VISIBLE_MESHES.clear();
        for (ChunkMesh mesh : CHUNKS.values()) {
            if (frustum == null || frustum.isVisible(mesh.bounds)) VISIBLE_MESHES.add(mesh);
        }
        List<ChunkMesh> meshes = VISIBLE_MESHES;

        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        event.getSubmitNodeCollector().submitCustomGeometry(poses, CrossDimensionLodRenderTypes.TILED_BLOCKS,
                (pose, consumer) -> renderMeshes(pose, consumer, meshes));
        event.getSubmitNodeCollector().submitCustomGeometry(poses, RenderTypes.debugQuads(),
                (pose, consumer) -> renderPlanes(pose, consumer, meshes.isEmpty(), radius, farRadius));
        poses.popPose();
    }

    private static void renderMeshes(PoseStack.Pose pose, VertexConsumer consumer, List<ChunkMesh> meshes) {
        int emitted = 0;
        try (SodiumLodCompat.VertexBatch batch = SodiumLodCompat.createVertexBatch(consumer, pose)) {
            outer:
            for (ChunkMesh mesh : meshes) {
                if (SodiumLodCompat.isLoaded()) {
                    for (TextureAtlasSprite sprite : mesh.sprites) SodiumLodCompat.markSpriteActive(sprite);
                }
                for (Quad quad : mesh.quads) {
                    if (emitted++ >= MAX_QUADS_PER_FRAME) break outer;
                    emitQuad(pose, consumer, batch, quad);
                }
            }
        }
    }

    private static void renderPlanes(PoseStack.Pose pose, VertexConsumer consumer,
                                     boolean fallback, double radius, double farRadius) {
        if (fallback) renderFallbackDisc(pose, consumer, radius);
        renderOutsidePlane(pose, consumer, radius, farRadius);
    }

    private static ChunkMesh buildMesh(CrossDimensionLodPayload payload) {
        List<Quad> quads = new ArrayList<>();
        boolean surfaceOnly = payload.cellSize() >= 8;
        for (int face = 0; face < 6; face++) {
            if (!surfaceOnly || face != 2) greedyFace(payload, face, surfaceOnly, quads);
        }
        int yOffset = payload.displayYOffset();
        int originX = payload.chunkX() * 16;
        int originZ = payload.chunkZ() * 16;
        AABB bounds = new AABB(originX, payload.minY() + yOffset, originZ,
                originX + 16, payload.minY() + payload.yCells() * payload.cellSize() + yOffset, originZ + 16);
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        for (Quad quad : quads) sprites.add(quad.sprite);
        return new ChunkMesh(List.copyOf(quads), Set.copyOf(sprites), bounds);
    }

    private static void greedyFace(CrossDimensionLodPayload payload, int face,
                                   boolean surfaceOnly, List<Quad> quads) {
        int size = 16 / payload.cellSize();
        int planes = face < 2 ? size : face < 4 ? payload.yCells() : size;
        int width = face < 2 ? size : face < 4 ? size : size;
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
                while (u + rectangleWidth < width && mask[v * width + u + rectangleWidth] == value) rectangleWidth++;
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
        TextureAtlasSprite sprite = blockSprite(stateId, face);
        int color = face == 2 ? 0xFF999999 : face == 3 ? 0xFFFFFFFF : 0xFFCCCCCC;
        quads.add(new Quad(x0, y0, z0, x1, y1, z1, color, face, sprite));
    }

    /** Returns the block-state ID, or -1 for air. Supports horizontal neighbor chunks. */
    private static int stateAt(CrossDimensionLodPayload source, int x, int y, int z) {
        if (y < 0 || y >= source.yCells()) return -1;
        int sourceSize = 16 / source.cellSize();
        int worldX = source.chunkX() * 16 + (x < 0 ? -1 : x >= sourceSize ? 16 : x * source.cellSize());
        int worldZ = source.chunkZ() * 16 + (z < 0 ? -1 : z >= sourceSize ? 16 : z * source.cellSize());
        int worldY = source.minY() + y * source.cellSize();
        int chunkX = Math.floorDiv(worldX, 16);
        int chunkZ = Math.floorDiv(worldZ, 16);
        boolean crossedHorizontalBoundary = chunkX != source.chunkX() || chunkZ != source.chunkZ();
        CrossDimensionLodPayload data = chunkX == source.chunkX() && chunkZ == source.chunkZ()
                ? source : DATA.get(ChunkPos.pack(chunkX, chunkZ));
        if (data == null) return -1;
        int dataHeight = data.yCells() * data.cellSize();
        if (worldY < data.minY() || worldY >= data.minY() + dataHeight) return -1;
        if (crossedHorizontalBoundary && data.cellSize() != source.cellSize()) {
            // The coarse side owns the complete boundary wall. The fine side only emits portions which rise above
            // the coarse height field.
            if (source.cellSize() > data.cellSize()) return -1;
            HeightField heights = HEIGHT_FIELDS.get(ChunkPos.pack(chunkX, chunkZ));
            if (heights == null) return -1;
            int heightX = Math.floorMod(worldX, 16) / data.cellSize();
            int heightZ = Math.floorMod(worldZ, 16) / data.cellSize();
            int heightIndex = heightZ * heights.size + heightX;
            return worldY <= heights.topY[heightIndex] ? heights.state[heightIndex] : -1;
        }
        int dataSize = 16 / data.cellSize();
        int dataX = Math.floorMod(worldX, 16) / data.cellSize();
        int dataZ = Math.floorMod(worldZ, 16) / data.cellSize();
        int dataY = (worldY - data.minY()) / data.cellSize();
        short paletteIndex = data.voxels()[(dataZ * dataSize + dataX) * data.yCells() + dataY];
        return paletteIndex == 0 ? -1 : data.palette()[paletteIndex];
    }

    private static int stateForMesh(CrossDimensionLodPayload source, int x, int y, int z,
                                    boolean surfaceOnly) {
        return surfaceOnly ? surfaceStateAt(source, x, y, z) : stateAt(source, x, y, z);
    }

    /** Height-field lookup used by coarse levels to discard caves and other hidden interior surfaces. */
    private static int surfaceStateAt(CrossDimensionLodPayload source, int x, int y, int z) {
        if (y < 0 || y >= source.yCells()) return -1;
        int sourceSize = 16 / source.cellSize();
        int worldX = source.chunkX() * 16 + (x < 0 ? -1 : x >= sourceSize ? 16 : x * source.cellSize());
        int worldZ = source.chunkZ() * 16 + (z < 0 ? -1 : z >= sourceSize ? 16 : z * source.cellSize());
        int worldY = source.minY() + y * source.cellSize();
        int chunkX = Math.floorDiv(worldX, 16);
        int chunkZ = Math.floorDiv(worldZ, 16);
        long key = ChunkPos.pack(chunkX, chunkZ);
        CrossDimensionLodPayload data = chunkX == source.chunkX() && chunkZ == source.chunkZ()
                ? source : DATA.get(key);
        HeightField heights = HEIGHT_FIELDS.get(key);
        if (data == null || heights == null) return -1;
        if ((chunkX != source.chunkX() || chunkZ != source.chunkZ())
                && data.cellSize() != source.cellSize() && source.cellSize() > data.cellSize()) return -1;
        int dataX = Math.floorMod(worldX, 16) / data.cellSize();
        int dataZ = Math.floorMod(worldZ, 16) / data.cellSize();
        int index = dataZ * heights.size + dataX;
        return worldY <= heights.topY[index] ? heights.state[index] : -1;
    }

    private static HeightField buildHeightField(CrossDimensionLodPayload payload) {
        int size = 16 / payload.cellSize();
        int[] topY = new int[size * size];
        int[] state = new int[size * size];
        java.util.Arrays.fill(topY, Integer.MIN_VALUE);
        java.util.Arrays.fill(state, -1);
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                for (int y = payload.yCells() - 1; y >= 0; y--) {
                    short paletteIndex = payload.voxels()[(z * size + x) * payload.yCells() + y];
                    if (paletteIndex != 0) {
                        int index = z * size + x;
                        topY[index] = payload.minY() + y * payload.cellSize();
                        state[index] = payload.palette()[paletteIndex];
                        break;
                    }
                }
            }
        }
        return new HeightField(size, topY, state);
    }

    private static int blockColor(int stateId, int x, int y, int z) {
        var state = Block.stateById(stateId);
        int rgb = state.getMapColor(Minecraft.getInstance().level, new BlockPos(x, y, z)).col;
        return 0xFF000000 | rgb;
    }

    private static TextureAtlasSprite blockSprite(int stateId, int face) {
        long key = ((long) stateId << 3) | face;
        return FACE_SPRITES.computeIfAbsent(key, ignored -> {
            var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
            var state = Block.stateById(stateId);
            var model = modelSet.get(state);
            List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(RandomSource.create(0L), parts);
            Direction direction = switch (face) {
                case 0 -> Direction.WEST;
                case 1 -> Direction.EAST;
                case 2 -> Direction.DOWN;
                case 3 -> Direction.UP;
                case 4 -> Direction.NORTH;
                default -> Direction.SOUTH;
            };
            for (var part : parts) {
                var quads = part.getQuads(direction);
                if (!quads.isEmpty()) return quads.getFirst().materialInfo().sprite();
            }
            for (var part : parts) {
                var quads = part.getQuads(null);
                if (!quads.isEmpty()) return quads.getFirst().materialInfo().sprite();
            }
            return modelSet.getParticleMaterial(state).sprite();
        });
    }

    private static int shade(int argb, double factor) {
        int r = (int) (((argb >> 16) & 255) * factor);
        int g = (int) (((argb >> 8) & 255) * factor);
        int b = (int) ((argb & 255) * factor);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void emitQuad(PoseStack.Pose pose, VertexConsumer consumer,
                                 SodiumLodCompat.VertexBatch batch, Quad q) {
        switch (q.face) {
            case 0 -> emitTextured(pose, consumer, batch, q, -1, 0, 0,
                    q.x0,q.y0,q.z1, q.x0,q.y1,q.z1, q.x0,q.y1,q.z0, q.x0,q.y0,q.z0);
            case 1 -> emitTextured(pose, consumer, batch, q, 1, 0, 0,
                    q.x0,q.y0,q.z0, q.x0,q.y1,q.z0, q.x0,q.y1,q.z1, q.x0,q.y0,q.z1);
            case 2 -> emitTextured(pose, consumer, batch, q, 0, -1, 0,
                    q.x0,q.y0,q.z0, q.x1,q.y0,q.z0, q.x1,q.y0,q.z1, q.x0,q.y0,q.z1);
            case 3 -> emitTextured(pose, consumer, batch, q, 0, 1, 0,
                    q.x0,q.y0,q.z1, q.x1,q.y0,q.z1, q.x1,q.y0,q.z0, q.x0,q.y0,q.z0);
            case 4 -> emitTextured(pose, consumer, batch, q, 0, 0, -1,
                    q.x0,q.y0,q.z0, q.x0,q.y1,q.z0, q.x1,q.y1,q.z0, q.x1,q.y0,q.z0);
            case 5 -> emitTextured(pose, consumer, batch, q, 0, 0, 1,
                    q.x1,q.y0,q.z0, q.x1,q.y1,q.z0, q.x0,q.y1,q.z0, q.x0,q.y0,q.z0);
        }
    }

    private static void emitTextured(PoseStack.Pose pose, VertexConsumer consumer,
                                     SodiumLodCompat.VertexBatch batch, Quad q,
                                     float nx, float ny, float nz,
                                     float ax, float ay, float az, float bx, float by, float bz,
                                     float cx, float cy, float cz, float dx, float dy, float dz) {
        texturedVertex(pose, consumer, batch, q, ax, ay, az, nx, ny, nz);
        texturedVertex(pose, consumer, batch, q, bx, by, bz, nx, ny, nz);
        texturedVertex(pose, consumer, batch, q, cx, cy, cz, nx, ny, nz);
        texturedVertex(pose, consumer, batch, q, dx, dy, dz, nx, ny, nz);
    }

    private static void renderFallbackDisc(PoseStack.Pose pose, VertexConsumer consumer, double radius) {
        double y = serverPlaneY - 72.0;
        for (int i = 0; i < PLANE_SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / PLANE_SEGMENTS, a1 = Math.PI * 2.0 * (i + 1) / PLANE_SEGMENTS;
            vertex(pose, consumer, serverCenterX, y, serverCenterZ, 0xFF26343A); vertex(pose, consumer, serverCenterX + Math.cos(a0) * radius, y, serverCenterZ + Math.sin(a0) * radius, 0xFF26343A);
            vertex(pose, consumer, serverCenterX + Math.cos(a1) * radius, y, serverCenterZ + Math.sin(a1) * radius, 0xFF26343A); vertex(pose, consumer, serverCenterX, y, serverCenterZ, 0xFF26343A);
        }
    }

    private static void renderOutsidePlane(PoseStack.Pose pose, VertexConsumer consumer, double radius, double farRadius) {
        double y = serverPlaneY;
        for (int i = 0; i < PLANE_SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / PLANE_SEGMENTS, a1 = Math.PI * 2.0 * (i + 1) / PLANE_SEGMENTS;
            vertex(pose, consumer, serverCenterX + Math.cos(a0) * radius, y, serverCenterZ + Math.sin(a0) * radius, 0xFF172129); vertex(pose, consumer, serverCenterX + Math.cos(a0) * farRadius, y, serverCenterZ + Math.sin(a0) * farRadius, 0xFF172129);
            vertex(pose, consumer, serverCenterX + Math.cos(a1) * farRadius, y, serverCenterZ + Math.sin(a1) * farRadius, 0xFF172129); vertex(pose, consumer, serverCenterX + Math.cos(a1) * radius, y, serverCenterZ + Math.sin(a1) * radius, 0xFF172129);
        }
    }

    private static void texturedVertex(PoseStack.Pose pose, VertexConsumer consumer,
                                       SodiumLodCompat.VertexBatch batch, Quad q,
                                       double x, double y, double z,
                                       float nx, float ny, float nz) {
        float u;
        float v;
        if (q.face < 2) {
            u = (float) z;
            v = (float) -y;
        } else if (q.face < 4) {
            u = (float) x;
            v = (float) z;
        } else {
            u = (float) x;
            v = (float) -y;
        }
        int minimumUv = packUv(q.sprite.getU0(), q.sprite.getV0());
        int maximumUv = packUv(q.sprite.getU1(), q.sprite.getV1());
        if (batch != null) {
            batch.write((float) x, (float) y, (float) z, q.color, u, v,
                    minimumUv, maximumUv, nx, ny, nz);
        } else {
            consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(q.color).setUv(u, v)
                    .setOverlay(minimumUv).setLight(maximumUv).setNormal(pose, nx, ny, nz);
        }
    }

    private static int packUv(float u, float v) {
        return packAtlasCoordinate(u) | packAtlasCoordinate(v) << 16;
    }

    private static int packAtlasCoordinate(float coordinate) {
        return Math.clamp(Math.round(coordinate * 32767.0F), 0, 32767);
    }
    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, double x, double y, double z, int color) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(color).setNormal(pose, 0, 1, 0);
    }

    private record ChunkMesh(List<Quad> quads, Set<TextureAtlasSprite> sprites, AABB bounds) {}
    private record HeightField(int size, int[] topY, int[] state) {}
    private record Quad(float x0, float y0, float z0, float x1, float y1, float z1,
                        int color, int face, TextureAtlasSprite sprite) {}
    private CrossDimensionLodRenderer() {}
}
