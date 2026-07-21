package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** Renders server-provided Great Fault voxel chunks with six-direction greedy meshing. */
public final class CrossDimensionLodRenderer {
    private static final double OUTSIDE_PLANE_SCALE = 4.0;
    private static final int PLANE_SEGMENTS = 128;
    private static final int MESH_UPLOADS_PER_FRAME = 12;
    private static final int MESH_SCHEDULES_PER_FRAME = 32;
    private static final int MAX_IN_FLIGHT_MESHES = 64;
    private static final int REGION_CHUNKS = 8;
    private static final int REGION_REBUILDS_PER_FRAME = 2;
    private static final int REGION_REBUILD_DEBOUNCE_FRAMES = 5;
    private static final Map<Long, CrossDimensionLodPayload> DATA = new ConcurrentHashMap<>();
    private static final Map<Long, HeightField> HEIGHT_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Long, ChunkMesh> CHUNKS = new ConcurrentHashMap<>();
    private static final Map<Long, RegionMesh> REGIONS = new HashMap<>();
    private static final Map<Long, Long> DIRTY_REGIONS = new HashMap<>();
    private static final Set<Long> DIRTY_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final Map<Long, Long> MESH_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_MESHES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<MeshBuildResult> COMPLETED_MESHES = new ConcurrentLinkedQueue<>();
    private static final AtomicLong NEXT_MESH_REVISION = new AtomicLong();
    private static final ExecutorService MESH_WORKERS = Executors.newFixedThreadPool(
            Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() - 1)), runnable -> {
                Thread thread = new Thread(runnable, "MIA LOD Mesher");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            });
    private static final Map<Long, TextureAtlasSprite> FACE_SPRITES = new ConcurrentHashMap<>();
    private static final Map<Long, Map<Integer, Integer>> CHUNK_SURFACE_COUNTS = new HashMap<>();
    private static final Map<Integer, Integer> SURFACE_COUNTS = new HashMap<>();
    /** Reused between extraction and rendering in the same frame; custom geometry is rendered before next extraction. */
    private static final ArrayList<ChunkMesh> VISIBLE_MESHES = new ArrayList<>();
    private static final ArrayList<RegionMesh> VISIBLE_REGIONS = new ArrayList<>();
    private static final HashSet<TextureAtlasSprite> VISIBLE_SPRITES = new HashSet<>();
    private static volatile int serverRadius;
    private static volatile int serverCenterX;
    private static volatile int serverCenterZ;
    private static volatile int serverPlaneY = -280;
    private static volatile boolean serverViewFromBelow;
    private static volatile int dominantSurfaceState = -1;
    private static long renderFrame;

    public static void accept(CrossDimensionLodPayload payload) {
        if (payload.reset()) {
            DATA.clear();
            HEIGHT_FIELDS.clear();
            closeMeshes();
            DIRTY_CHUNKS.clear();
            MESH_REVISIONS.clear();
            IN_FLIGHT_MESHES.clear();
            COMPLETED_MESHES.clear();
            clearSurfaceCounts();
        }
        serverRadius = payload.radius();
        serverCenterX = payload.centerX();
        serverCenterZ = payload.centerZ();
        serverPlaneY = payload.outsidePlaneY();
        serverViewFromBelow = payload.displayYOffset() > 0;
        long key = ChunkPos.pack(payload.chunkX(), payload.chunkZ());
        DATA.put(key, payload);
        HeightField heightField = buildHeightField(payload);
        HEIGHT_FIELDS.put(key, heightField);
        updateSurfaceCounts(key, payload, heightField);
        markDirty(payload.chunkX(), payload.chunkZ());
        markDirty(payload.chunkX() - 1, payload.chunkZ());
        markDirty(payload.chunkX() + 1, payload.chunkZ());
        markDirty(payload.chunkX(), payload.chunkZ() - 1);
        markDirty(payload.chunkX(), payload.chunkZ() + 1);
    }

    private static void markDirty(int chunkX, int chunkZ) {
        long key = ChunkPos.pack(chunkX, chunkZ);
        MESH_REVISIONS.put(key, NEXT_MESH_REVISION.incrementAndGet());
        DIRTY_CHUNKS.add(key);
    }

    private static void updateMeshes() {
        drainCompletedMeshes();
        scheduleDirtyMeshes();
    }

    private static void scheduleDirtyMeshes() {
        int scheduled = 0;
        for (long key : DIRTY_CHUNKS) {
            if (scheduled >= MESH_SCHEDULES_PER_FRAME || IN_FLIGHT_MESHES.size() >= MAX_IN_FLIGHT_MESHES) break;
            if (IN_FLIGHT_MESHES.containsKey(key)) continue;
            if (!DIRTY_CHUNKS.remove(key)) continue;
            CrossDimensionLodPayload payload = DATA.get(key);
            Long revision = MESH_REVISIONS.get(key);
            if (payload == null || revision == null) continue;
            if (IN_FLIGHT_MESHES.putIfAbsent(key, revision) != null) {
                DIRTY_CHUNKS.add(key);
                continue;
            }
            scheduled++;
            MESH_WORKERS.execute(() -> {
                try {
                    COMPLETED_MESHES.add(new MeshBuildResult(key, revision, buildCpuMesh(payload), null));
                } catch (Throwable throwable) {
                    COMPLETED_MESHES.add(new MeshBuildResult(key, revision, null, throwable));
                }
            });
        }
    }

    private static void drainCompletedMeshes() {
        int uploaded = 0;
        while (uploaded < MESH_UPLOADS_PER_FRAME) {
            MeshBuildResult result = COMPLETED_MESHES.poll();
            if (result == null) break;
            IN_FLIGHT_MESHES.remove(result.key, result.revision);
            if (result.failure != null) {
                MementoInAbyss.LOGGER.error("Failed to build cross-dimension LOD mesh [{},{}]",
                        ChunkPos.getX(result.key), ChunkPos.getZ(result.key), result.failure);
                continue;
            }
            if (!Long.valueOf(result.revision).equals(MESH_REVISIONS.get(result.key))
                    || DATA.get(result.key) == null) continue;
            ChunkMesh replacement = uploadMesh(result.mesh);
            ChunkMesh previous = CHUNKS.put(result.key, replacement);
            if (previous != null) previous.close();
            markRegionDirty(result.key);
            uploaded++;
        }
    }

    public static void clear() {
        DATA.clear();
        HEIGHT_FIELDS.clear();
        closeMeshes();
        DIRTY_CHUNKS.clear();
        MESH_REVISIONS.clear();
        IN_FLIGHT_MESHES.clear();
        COMPLETED_MESHES.clear();
        VISIBLE_MESHES.clear();
        VISIBLE_REGIONS.clear();
        VISIBLE_SPRITES.clear();
        FACE_SPRITES.clear();
        clearSurfaceCounts();
        serverRadius = 0;
        serverCenterX = serverCenterZ = 0;
        serverViewFromBelow = false;
    }

    private static void closeMeshes() {
        for (ChunkMesh mesh : CHUNKS.values()) mesh.close();
        CHUNKS.clear();
        for (RegionMesh mesh : REGIONS.values()) mesh.close();
        REGIONS.clear();
        DIRTY_REGIONS.clear();
    }

    public static void submit(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).isEmpty()
                || !MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get()) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        if (camera == null) return;
        double radius = serverRadius > 0 ? serverRadius
                : CrossDimensionLodLinks.radius(CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElseThrow());
        double farRadius = Math.max(radius * OUTSIDE_PLANE_SCALE,
                event.getLevelRenderState().cameraRenderState.depthFar * 1.25);
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        if (CHUNKS.isEmpty()) {
            event.getSubmitNodeCollector().submitCustomGeometry(poses, RenderTypes.debugQuads(),
                    (pose, consumer) -> renderFallbackDisc(pose, consumer, radius));
        }
        int surfaceState = dominantSurfaceState >= 0
                ? dominantSurfaceState : Block.getId(Blocks.STONE.defaultBlockState());
        TextureAtlasSprite surfaceSprite = blockSprite(surfaceState, 3);
        if (com.altnoir.mementoinabyss.compat.SodiumLodCompat.isLoaded()) {
            com.altnoir.mementoinabyss.compat.SodiumLodCompat.markSpriteActive(surfaceSprite);
        }
        event.getSubmitNodeCollector().submitCustomGeometry(poses, CrossDimensionLodRenderTypes.TILED_BLOCKS,
                (pose, consumer) -> renderOutsidePlane(pose, consumer, surfaceSprite, radius, farRadius));
        poses.popPose();
    }

    /** Draws already-uploaded chunk meshes without rebuilding a transient BufferBuilder every frame. */
    public static void renderPersistent(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).isEmpty()
                || !MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get()) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        if (camera == null) return;

        renderFrame++;
        updateMeshes();
        rebuildDirtyRegions();
        var frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        VISIBLE_MESHES.clear();
        VISIBLE_REGIONS.clear();
        VISIBLE_SPRITES.clear();
        int maximumIndexCount = 0;
        boolean sodiumLoaded = com.altnoir.mementoinabyss.compat.SodiumLodCompat.isLoaded();
        for (RegionMesh mesh : REGIONS.values()) {
            if (!DIRTY_REGIONS.containsKey(mesh.key)
                    && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_REGIONS.add(mesh);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(mesh.sprites);
            }
        }
        for (ChunkMesh mesh : CHUNKS.values()) {
            long regionKey = regionKey(mesh.chunkX, mesh.chunkZ);
            if ((DIRTY_REGIONS.containsKey(regionKey) || !REGIONS.containsKey(regionKey))
                    && mesh.indexCount > 0 && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_MESHES.add(mesh);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(mesh.sprites);
            }
        }
        if (VISIBLE_MESHES.isEmpty() && VISIBLE_REGIONS.isEmpty()) return;
        if (sodiumLoaded) {
            for (TextureAtlasSprite sprite : VISIBLE_SPRITES) {
                com.altnoir.mementoinabyss.compat.SodiumLodCompat.markSpriteActive(sprite);
            }
        }

        var renderType = CrossDimensionLodRenderTypes.TILED_BLOCKS;
        var pipeline = CrossDimensionLodRenderTypes.tiledBlocksPipeline();
        RenderTarget target = minecraft.getMainRenderTarget();
        var indexStorage = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        GpuBuffer indices = indexStorage.getBuffer(maximumIndexCount);
        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix())
                .translate((float) -camera.x, (float) -camera.y, (float) -camera.z);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView,
                new Vector4f(1.0F), new Vector3f(), new Matrix4f());
        var atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Cross-dimension persistent LOD", target.getColorTextureView(),
                java.util.OptionalInt.empty(), target.getDepthTextureView(), java.util.OptionalDouble.empty())) {
            pass.setPipeline(renderType.pipeline());
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
            pass.setIndexBuffer(indices, indexStorage.type());
            for (RegionMesh mesh : VISIBLE_REGIONS) {
                pass.setVertexBuffer(0, mesh.vertexBuffer);
                pass.drawIndexed(0, 0, mesh.indexCount, 1);
            }
            for (ChunkMesh mesh : VISIBLE_MESHES) {
                pass.setVertexBuffer(0, mesh.vertexBuffer);
                pass.drawIndexed(0, 0, mesh.indexCount, 1);
            }
        }
    }

    private static CpuMesh buildCpuMesh(CrossDimensionLodPayload payload) {
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
        return new CpuMesh(List.copyOf(quads), bounds, payload.chunkX(), payload.chunkZ());
    }

    private static long regionKey(int chunkX, int chunkZ) {
        return ChunkPos.pack(Math.floorDiv(chunkX, REGION_CHUNKS), Math.floorDiv(chunkZ, REGION_CHUNKS));
    }

    private static void markRegionDirty(long chunkKey) {
        DIRTY_REGIONS.put(regionKey(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey)), renderFrame);
    }

    private static void rebuildDirtyRegions() {
        int rebuilt = 0;
        var iterator = DIRTY_REGIONS.entrySet().iterator();
        while (iterator.hasNext() && rebuilt < REGION_REBUILDS_PER_FRAME) {
            var entry = iterator.next();
            if (renderFrame - entry.getValue() < REGION_REBUILD_DEBOUNCE_FRAMES) continue;
            long key = entry.getKey();
            RegionMesh replacement = buildRegionMesh(key);
            RegionMesh previous = replacement == null ? REGIONS.remove(key) : REGIONS.put(key, replacement);
            if (previous != null) previous.close();
            iterator.remove();
            rebuilt++;
        }
    }

    private static RegionMesh buildRegionMesh(long key) {
        int regionX = ChunkPos.getX(key);
        int regionZ = ChunkPos.getZ(key);
        long vertexBytes = 0L;
        int indexCount = 0;
        AABB bounds = null;
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        List<ChunkMesh> members = new ArrayList<>(REGION_CHUNKS * REGION_CHUNKS);
        for (ChunkMesh mesh : CHUNKS.values()) {
            if (mesh.vertexBuffer == null || mesh.indexCount == 0
                    || Math.floorDiv(mesh.chunkX, REGION_CHUNKS) != regionX
                    || Math.floorDiv(mesh.chunkZ, REGION_CHUNKS) != regionZ) continue;
            members.add(mesh);
            vertexBytes = Math.addExact(vertexBytes, mesh.vertexBuffer.size());
            indexCount = Math.addExact(indexCount, mesh.indexCount);
            bounds = bounds == null ? mesh.bounds : bounds.minmax(mesh.bounds);
            sprites.addAll(mesh.sprites);
        }
        if (members.isEmpty()) return null;

        GpuBuffer combined = RenderSystem.getDevice().createBuffer(
                () -> "Cross-dimension LOD region [" + regionX + "," + regionZ + "]",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, vertexBytes);
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        long offset = 0L;
        try {
            for (ChunkMesh member : members) {
                long length = member.vertexBuffer.size();
                encoder.copyToBuffer(member.vertexBuffer.slice(), combined.slice(offset, length));
                offset += length;
            }
            return new RegionMesh(key, combined, indexCount, Set.copyOf(sprites), bounds);
        } catch (Throwable throwable) {
            combined.close();
            throw throwable;
        }
    }

    private static ChunkMesh uploadMesh(CpuMesh cpuMesh) {
        List<Quad> quads = cpuMesh.quads;
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        int vertexCount = Math.multiplyExact(quads.size(), 4);
        int vertexBytes = Math.multiplyExact(vertexCount, DefaultVertexFormat.ENTITY.getVertexSize());
        if (vertexBytes == 0) return new ChunkMesh(cpuMesh.chunkX, cpuMesh.chunkZ,
                null, 0, Set.copyOf(sprites), cpuMesh.bounds);
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder = new BufferBuilder(bytes, CrossDimensionLodRenderTypes.TILED_BLOCKS.mode(),
                    DefaultVertexFormat.ENTITY);
            for (Quad quad : quads) {
                TextureAtlasSprite sprite = blockSprite(quad.stateId, quad.face);
                sprites.add(sprite);
                emitQuad(builder, quad, sprite);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                        () -> "Cross-dimension LOD chunk [" + cpuMesh.chunkX + "," + cpuMesh.chunkZ + "]",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_SRC, mesh.vertexBuffer());
                return new ChunkMesh(cpuMesh.chunkX, cpuMesh.chunkZ, buffer,
                        mesh.drawState().indexCount(), Set.copyOf(sprites), cpuMesh.bounds);
            }
        }
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
        int color = face == 2 ? 0xFF999999 : face == 3 ? 0xFFFFFFFF : 0xFFCCCCCC;
        quads.add(new Quad(x0, y0, z0, x1, y1, z1, color, face, stateId));
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
            if (source.displayYOffset() > 0) {
                return worldY >= heights.bottomY[heightIndex] ? heights.bottomState[heightIndex] : -1;
            }
            return worldY <= heights.topY[heightIndex] ? heights.topState[heightIndex] : -1;
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
        if (source.displayYOffset() > 0) {
            return worldY >= heights.bottomY[index] ? heights.bottomState[index] : -1;
        }
        return worldY <= heights.topY[index] ? heights.topState[index] : -1;
    }

    private static HeightField buildHeightField(CrossDimensionLodPayload payload) {
        int size = 16 / payload.cellSize();
        int[] topY = new int[size * size];
        int[] topState = new int[size * size];
        int[] bottomY = new int[size * size];
        int[] bottomState = new int[size * size];
        java.util.Arrays.fill(topY, Integer.MIN_VALUE);
        java.util.Arrays.fill(topState, -1);
        java.util.Arrays.fill(bottomY, Integer.MAX_VALUE);
        java.util.Arrays.fill(bottomState, -1);
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                for (int y = payload.yCells() - 1; y >= 0; y--) {
                    short paletteIndex = payload.voxels()[(z * size + x) * payload.yCells() + y];
                    if (paletteIndex != 0) {
                        int index = z * size + x;
                        topY[index] = payload.minY() + y * payload.cellSize();
                        topState[index] = payload.palette()[paletteIndex];
                        break;
                    }
                }
                for (int y = 0; y < payload.yCells(); y++) {
                    short paletteIndex = payload.voxels()[(z * size + x) * payload.yCells() + y];
                    if (paletteIndex != 0) {
                        int index = z * size + x;
                        bottomY[index] = payload.minY() + y * payload.cellSize();
                        bottomState[index] = payload.palette()[paletteIndex];
                        break;
                    }
                }
            }
        }
        return new HeightField(size, topY, topState, bottomY, bottomState);
    }

    private static synchronized void updateSurfaceCounts(long key, CrossDimensionLodPayload payload,
                                                         HeightField heightField) {
        Map<Integer, Integer> previous = CHUNK_SURFACE_COUNTS.remove(key);
        if (previous != null) {
            previous.forEach((state, count) -> SURFACE_COUNTS.compute(state,
                    (ignored, total) -> total == null || total <= count ? null : total - count));
        }
        if (payload.cellSize() == 4) {
            Map<Integer, Integer> chunkCounts = new HashMap<>();
            for (int state : heightField.topState) {
                if (state >= 0) chunkCounts.merge(state, 1, Integer::sum);
            }
            CHUNK_SURFACE_COUNTS.put(key, chunkCounts);
            chunkCounts.forEach((state, count) -> SURFACE_COUNTS.merge(state, count, Integer::sum));
        }
        dominantSurfaceState = SURFACE_COUNTS.entrySet().stream()
                .max(Map.Entry.<Integer, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey).orElse(-1);
    }

    private static synchronized void clearSurfaceCounts() {
        CHUNK_SURFACE_COUNTS.clear();
        SURFACE_COUNTS.clear();
        dominantSurfaceState = -1;
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

    private static void emitQuad(VertexConsumer consumer, Quad q, TextureAtlasSprite sprite) {
        switch (q.face) {
            case 0 -> emitTextured(consumer, q, sprite, -1, 0, 0,
                    q.x0,q.y0,q.z1, q.x0,q.y1,q.z1, q.x0,q.y1,q.z0, q.x0,q.y0,q.z0);
            case 1 -> emitTextured(consumer, q, sprite, 1, 0, 0,
                    q.x0,q.y0,q.z0, q.x0,q.y1,q.z0, q.x0,q.y1,q.z1, q.x0,q.y0,q.z1);
            case 2 -> emitTextured(consumer, q, sprite, 0, -1, 0,
                    q.x0,q.y0,q.z0, q.x1,q.y0,q.z0, q.x1,q.y0,q.z1, q.x0,q.y0,q.z1);
            case 3 -> emitTextured(consumer, q, sprite, 0, 1, 0,
                    q.x0,q.y0,q.z1, q.x1,q.y0,q.z1, q.x1,q.y0,q.z0, q.x0,q.y0,q.z0);
            case 4 -> emitTextured(consumer, q, sprite, 0, 0, -1,
                    q.x0,q.y0,q.z0, q.x0,q.y1,q.z0, q.x1,q.y1,q.z0, q.x1,q.y0,q.z0);
            case 5 -> emitTextured(consumer, q, sprite, 0, 0, 1,
                    q.x1,q.y0,q.z0, q.x1,q.y1,q.z0, q.x0,q.y1,q.z0, q.x0,q.y0,q.z0);
        }
    }

    private static void emitTextured(VertexConsumer consumer, Quad q, TextureAtlasSprite sprite,
                                     float nx, float ny, float nz,
                                     float ax, float ay, float az, float bx, float by, float bz,
                                     float cx, float cy, float cz, float dx, float dy, float dz) {
        texturedVertex(consumer, q, sprite, ax, ay, az, nx, ny, nz);
        texturedVertex(consumer, q, sprite, bx, by, bz, nx, ny, nz);
        texturedVertex(consumer, q, sprite, cx, cy, cz, nx, ny, nz);
        texturedVertex(consumer, q, sprite, dx, dy, dz, nx, ny, nz);
    }

    private static void renderFallbackDisc(PoseStack.Pose pose, VertexConsumer consumer, double radius) {
        double y = serverPlaneY + (serverViewFromBelow ? 72.0 : -72.0);
        for (int i = 0; i < PLANE_SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / PLANE_SEGMENTS, a1 = Math.PI * 2.0 * (i + 1) / PLANE_SEGMENTS;
            vertex(pose, consumer, serverCenterX, y, serverCenterZ, 0xFF26343A); vertex(pose, consumer, serverCenterX + Math.cos(a0) * radius, y, serverCenterZ + Math.sin(a0) * radius, 0xFF26343A);
            vertex(pose, consumer, serverCenterX + Math.cos(a1) * radius, y, serverCenterZ + Math.sin(a1) * radius, 0xFF26343A); vertex(pose, consumer, serverCenterX, y, serverCenterZ, 0xFF26343A);
        }
    }

    private static void renderOutsidePlane(PoseStack.Pose pose, VertexConsumer consumer,
                                           TextureAtlasSprite sprite, double radius, double farRadius) {
        double y = serverPlaneY;
        for (int i = 0; i < PLANE_SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / PLANE_SEGMENTS, a1 = Math.PI * 2.0 * (i + 1) / PLANE_SEGMENTS;
            double ix0 = serverCenterX + Math.cos(a0) * radius, iz0 = serverCenterZ + Math.sin(a0) * radius;
            double ix1 = serverCenterX + Math.cos(a1) * radius, iz1 = serverCenterZ + Math.sin(a1) * radius;
            double ox0 = serverCenterX + Math.cos(a0) * farRadius, oz0 = serverCenterZ + Math.sin(a0) * farRadius;
            double ox1 = serverCenterX + Math.cos(a1) * farRadius, oz1 = serverCenterZ + Math.sin(a1) * farRadius;
            if (serverViewFromBelow) {
                planeVertex(pose, consumer, sprite, ix0, y, iz0, -1.0F);
                planeVertex(pose, consumer, sprite, ox0, y, oz0, -1.0F);
                planeVertex(pose, consumer, sprite, ox1, y, oz1, -1.0F);
                planeVertex(pose, consumer, sprite, ix1, y, iz1, -1.0F);
            } else {
                planeVertex(pose, consumer, sprite, ix0, y, iz0, 1.0F);
                planeVertex(pose, consumer, sprite, ix1, y, iz1, 1.0F);
                planeVertex(pose, consumer, sprite, ox1, y, oz1, 1.0F);
                planeVertex(pose, consumer, sprite, ox0, y, oz0, 1.0F);
            }
        }
    }

    private static void planeVertex(PoseStack.Pose pose, VertexConsumer consumer, TextureAtlasSprite sprite,
                                    double x, double y, double z, float normalY) {
        int minimumUv = packUv(sprite.getU0(), sprite.getV0());
        int maximumUv = packUv(sprite.getU1(), sprite.getV1());
        consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(0xFFFFFFFF)
                .setUv((float) x, (float) z).setOverlay(minimumUv).setLight(maximumUv)
                .setNormal(pose, 0.0F, normalY, 0.0F);
    }

    private static void texturedVertex(VertexConsumer consumer, Quad q, TextureAtlasSprite sprite,
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
        int minimumUv = packUv(sprite.getU0(), sprite.getV0());
        int maximumUv = packUv(sprite.getU1(), sprite.getV1());
        consumer.addVertex((float) x, (float) y, (float) z).setColor(q.color).setUv(u, v)
                .setOverlay(minimumUv).setLight(maximumUv).setNormal(nx, ny, nz);
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

    private record ChunkMesh(int chunkX, int chunkZ, GpuBuffer vertexBuffer, int indexCount,
                             Set<TextureAtlasSprite> sprites, AABB bounds) implements AutoCloseable {
        @Override
        public void close() {
            if (vertexBuffer != null && !vertexBuffer.isClosed()) vertexBuffer.close();
        }
    }
    private record RegionMesh(long key, GpuBuffer vertexBuffer, int indexCount,
                              Set<TextureAtlasSprite> sprites, AABB bounds) implements AutoCloseable {
        @Override
        public void close() {
            if (!vertexBuffer.isClosed()) vertexBuffer.close();
        }
    }
    private record CpuMesh(List<Quad> quads, AABB bounds, int chunkX, int chunkZ) {}
    private record MeshBuildResult(long key, long revision, CpuMesh mesh, Throwable failure) {}
    private record HeightField(int size, int[] topY, int[] topState,
                               int[] bottomY, int[] bottomState) {}
    private record Quad(float x0, float y0, float z0, float x1, float y1, float z1,
                        int color, int face, int stateId) {}
    private CrossDimensionLodRenderer() {}
}
