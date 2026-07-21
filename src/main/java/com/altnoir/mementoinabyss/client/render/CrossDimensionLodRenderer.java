package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.CpuMesh;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.HeightField;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.QuadBuffer;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodKey;
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

/** Renders server-provided cross-dimension voxel chunks with six-direction greedy meshing. */
public final class CrossDimensionLodRenderer {
    private static final int MESH_UPLOADS_PER_FRAME = 4;
    private static final int MESH_SCHEDULES_PER_FRAME = 8;
    private static final int MAX_IN_FLIGHT_MESHES = 16;
    private static final long MESH_UPLOAD_BUDGET_NANOS = 1_500_000L;
    private static final int REGION_CHUNKS = 4;
    private static final int REGION_REBUILDS_PER_FRAME = 1;
    private static final long REGION_REBUILD_BUDGET_NANOS = 1_000_000L;
    private static final int REGION_REBUILD_DEBOUNCE_FRAMES = 5;
    private static final int EVICTION_INTERVAL_FRAMES = 20;
    private static final int EVICTION_MARGIN_CHUNKS = 10;
    private static final int FADE_DURATION_FRAMES = 24;
    private static final int FADE_STEPS = 16;
    private static final long SLOW_LOD_FRAME_NANOS = 4_000_000L;
    private static final Map<Long, CrossDimensionLodPayload> DATA = new ConcurrentHashMap<>();
    private static final Map<Long, HeightField> HEIGHT_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Long, ChunkMesh> CHUNKS = new ConcurrentHashMap<>();
    private static final Map<Long, LodTransition> TRANSITIONS = new HashMap<>();
    private static final Map<Long, RegionMesh> REGIONS = new HashMap<>();
    private static final Map<Long, Long> DIRTY_REGIONS = new HashMap<>();
    private static final Set<Long> DIRTY_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final Map<Long, Long> MESH_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_MESHES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<MeshBuildResult> COMPLETED_MESHES = new ConcurrentLinkedQueue<>();
    private static final AtomicLong NEXT_MESH_REVISION = new AtomicLong();
    private static final AtomicLong PENDING_RECEIVE_NANOS = new AtomicLong();
    private static final ExecutorService MESH_WORKERS = Executors.newFixedThreadPool(
            Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors() - 2)), runnable -> {
                Thread thread = new Thread(runnable, "MIA LOD Mesher");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });
    private static final Map<Long, TextureAtlasSprite> FACE_SPRITES = new ConcurrentHashMap<>();
    /** Reused between extraction and rendering in the same frame; custom geometry is rendered before next extraction. */
    private static final ArrayList<ChunkMesh> VISIBLE_MESHES = new ArrayList<>();
    private static final ArrayList<LodTransition> VISIBLE_TRANSITIONS = new ArrayList<>();
    private static final ArrayList<RegionMesh> VISIBLE_REGIONS = new ArrayList<>();
    private static final HashSet<TextureAtlasSprite> VISIBLE_SPRITES = new HashSet<>();
    private static long renderFrame;
    private static volatile int serverViewRadius;
    private static volatile FrameTiming lastTiming = FrameTiming.EMPTY;
    private static volatile FrameTiming peakTiming = FrameTiming.EMPTY;
    private static volatile FrameTiming lastSpike = FrameTiming.EMPTY;
    private static FrameTiming windowPeak = FrameTiming.EMPTY;

    public static DebugStats debugStats() {
        int viewRadius = serverViewRadius > 0 ? serverViewRadius
                : MementoInAbyss.CONFIGS.guiSection.crossDimensionLodViewDistance.get() * 16;
        return new DebugStats(DATA.size(), CHUNKS.size(), REGIONS.size(),
                VISIBLE_MESHES.size() + VISIBLE_REGIONS.size() + VISIBLE_TRANSITIONS.size(),
                DIRTY_CHUNKS.size(), IN_FLIGHT_MESHES.size(), COMPLETED_MESHES.size(), viewRadius,
                lastTiming, peakTiming, lastSpike);
    }

    public static void accept(CrossDimensionLodPayload payload) {
        long started = System.nanoTime();
        try {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        var activeLink = CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElse(null);
        if (activeLink == null || !activeLink.id().toString().equals(payload.linkId())) return;
        serverViewRadius = payload.radius();
        if (payload.reset()) {
            DATA.clear();
            HEIGHT_FIELDS.clear();
            closeMeshes();
            DIRTY_CHUNKS.clear();
            MESH_REVISIONS.clear();
            IN_FLIGHT_MESHES.clear();
            COMPLETED_MESHES.clear();
        }
        long key = CrossDimensionLodKey.pack(payload.chunkX(), payload.chunkZ());
        DATA.put(key, payload);
        HeightField heightField = CrossDimensionLodMesher.buildHeightField(payload);
        HEIGHT_FIELDS.put(key, heightField);
        markDirty(payload.chunkX(), payload.chunkZ());
        markDirtyIfPresent(payload.chunkX() - 1, payload.chunkZ());
        markDirtyIfPresent(payload.chunkX() + 1, payload.chunkZ());
        markDirtyIfPresent(payload.chunkX(), payload.chunkZ() - 1);
        markDirtyIfPresent(payload.chunkX(), payload.chunkZ() + 1);
        } finally {
            PENDING_RECEIVE_NANOS.addAndGet(System.nanoTime() - started);
        }
    }

    private static void markDirty(int chunkX, int chunkZ) {
        long key = CrossDimensionLodKey.pack(chunkX, chunkZ);
        MESH_REVISIONS.put(key, NEXT_MESH_REVISION.incrementAndGet());
        DIRTY_CHUNKS.add(key);
    }

    private static void markDirtyIfPresent(int chunkX, int chunkZ) {
        long key = CrossDimensionLodKey.pack(chunkX, chunkZ);
        if (DATA.containsKey(key)) markDirty(chunkX, chunkZ);
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
                    COMPLETED_MESHES.add(new MeshBuildResult(key, revision,
                            CrossDimensionLodMesher.build(payload, DATA, HEIGHT_FIELDS), null));
                } catch (Throwable throwable) {
                    COMPLETED_MESHES.add(new MeshBuildResult(key, revision, null, throwable));
                }
            });
        }
    }

    private static void drainCompletedMeshes() {
        long started = System.nanoTime();
        int uploaded = 0;
        int processed = 0;
        while (uploaded < MESH_UPLOADS_PER_FRAME && processed < MAX_IN_FLIGHT_MESHES
                && (processed == 0 || System.nanoTime() - started < MESH_UPLOAD_BUDGET_NANOS)) {
            MeshBuildResult result = COMPLETED_MESHES.poll();
            if (result == null) break;
            processed++;
            IN_FLIGHT_MESHES.remove(result.key, result.revision);
            if (result.failure != null) {
                MementoInAbyss.LOGGER.error("Failed to build cross-dimension LOD mesh [{},{}]",
                        CrossDimensionLodKey.x(result.key), CrossDimensionLodKey.z(result.key), result.failure);
                continue;
            }
            if (!Long.valueOf(result.revision).equals(MESH_REVISIONS.get(result.key))
                    || DATA.get(result.key) == null) continue;
            ChunkMesh previous = CHUNKS.get(result.key);
            LodTransition active = TRANSITIONS.get(result.key);
            long fadeStartFrame;
            if (previous == null) {
                fadeStartFrame = renderFrame;
            } else if (previous.cellSize == result.mesh.cellSize) {
                fadeStartFrame = previous.fadeStartFrame;
            } else {
                fadeStartFrame = result.mesh.cellSize > previous.cellSize
                        ? renderFrame : renderFrame - FADE_DURATION_FRAMES;
            }
            ChunkMesh replacement = uploadMesh(result.mesh, fadeStartFrame);
            CHUNKS.put(result.key, replacement);
            if (previous != null && previous.cellSize != replacement.cellSize) {
                if (active != null) active.oldMesh.close();
                boolean fadeOld = replacement.cellSize < previous.cellSize;
                TRANSITIONS.put(result.key, new LodTransition(result.key, previous, renderFrame, fadeOld));
            } else if (previous != null) {
                previous.close();
            }
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
        VISIBLE_TRANSITIONS.clear();
        VISIBLE_REGIONS.clear();
        VISIBLE_SPRITES.clear();
        FACE_SPRITES.clear();
        serverViewRadius = 0;
        PENDING_RECEIVE_NANOS.set(0L);
        lastTiming = FrameTiming.EMPTY;
        peakTiming = FrameTiming.EMPTY;
        lastSpike = FrameTiming.EMPTY;
        windowPeak = FrameTiming.EMPTY;
    }

    private static void closeMeshes() {
        for (ChunkMesh mesh : CHUNKS.values()) mesh.close();
        CHUNKS.clear();
        for (LodTransition transition : TRANSITIONS.values()) transition.oldMesh.close();
        TRANSITIONS.clear();
        for (RegionMesh mesh : REGIONS.values()) mesh.close();
        REGIONS.clear();
        DIRTY_REGIONS.clear();
    }

    /** Draws already-uploaded chunk meshes without rebuilding a transient BufferBuilder every frame. */
    public static void renderPersistent(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        long callbackStarted = System.nanoTime();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).isEmpty()
                || !MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get()) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        if (camera == null) return;

        renderFrame++;
        int viewRadius = serverViewRadius > 0 ? serverViewRadius
                : MementoInAbyss.CONFIGS.guiSection.crossDimensionLodViewDistance.get() * 16;
        if (renderFrame % EVICTION_INTERVAL_FRAMES == 0) evictFarChunks(camera, viewRadius);
        long meshStarted = System.nanoTime();
        updateMeshes();
        finishTransitions();
        long meshNanos = System.nanoTime() - meshStarted;
        long regionStarted = System.nanoTime();
        rebuildDirtyRegions();
        long regionNanos = System.nanoTime() - regionStarted;
        long visibilityStarted = System.nanoTime();
        var frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        VISIBLE_MESHES.clear();
        VISIBLE_TRANSITIONS.clear();
        VISIBLE_REGIONS.clear();
        VISIBLE_SPRITES.clear();
        int maximumIndexCount = 0;
        boolean sodiumLoaded = com.altnoir.mementoinabyss.compat.SodiumLodCompat.isLoaded();
        for (RegionMesh mesh : REGIONS.values()) {
            if (!DIRTY_REGIONS.containsKey(mesh.key)
                    && isWithinHorizontalDistance(mesh.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_REGIONS.add(mesh);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(mesh.sprites);
            }
        }
        for (ChunkMesh mesh : CHUNKS.values()) {
            long regionKey = regionKey(mesh.chunkX, mesh.chunkZ);
            if ((DIRTY_REGIONS.containsKey(regionKey) || !REGIONS.containsKey(regionKey))
                    && mesh.indexCount > 0 && isWithinHorizontalDistance(mesh.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_MESHES.add(mesh);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(mesh.sprites);
            }
        }
        for (LodTransition transition : TRANSITIONS.values()) {
            ChunkMesh mesh = transition.oldMesh;
            if (mesh.indexCount > 0 && isWithinHorizontalDistance(mesh.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_TRANSITIONS.add(transition);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(mesh.sprites);
            }
        }
        long visibilityNanos = System.nanoTime() - visibilityStarted;
        long receiveNanos = PENDING_RECEIVE_NANOS.getAndSet(0L);
        if (VISIBLE_MESHES.isEmpty() && VISIBLE_REGIONS.isEmpty() && VISIBLE_TRANSITIONS.isEmpty()) {
            recordTiming(callbackStarted, receiveNanos, meshNanos, regionNanos, visibilityNanos, 0L);
            return;
        }
        long drawStarted = System.nanoTime();
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
        Map<Integer, GpuBufferSlice> fadeTransforms = new HashMap<>();
        for (ChunkMesh mesh : VISIBLE_MESHES) {
            int fadeStep = fadeStep(mesh);
            if (fadeStep < FADE_STEPS) {
                fadeTransforms.computeIfAbsent(fadeStep, step ->
                        RenderSystem.getDynamicUniforms().writeTransform(modelView,
                                new Vector4f(1.0F, 1.0F, 1.0F, step / (float) FADE_STEPS),
                                new Vector3f(), new Matrix4f()));
            }
        }
        for (LodTransition transition : VISIBLE_TRANSITIONS) {
            int fadeStep = transitionFadeStep(transition);
            if (fadeStep < FADE_STEPS) {
                fadeTransforms.computeIfAbsent(fadeStep, step ->
                        RenderSystem.getDynamicUniforms().writeTransform(modelView,
                                new Vector4f(1.0F, 1.0F, 1.0F, step / (float) FADE_STEPS),
                                new Vector3f(), new Matrix4f()));
            }
        }
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
            for (LodTransition transition : VISIBLE_TRANSITIONS) {
                int fadeStep = transitionFadeStep(transition);
                pass.setUniform("DynamicTransforms",
                        fadeStep < FADE_STEPS ? fadeTransforms.get(fadeStep) : transforms);
                pass.setVertexBuffer(0, transition.oldMesh.vertexBuffer);
                pass.drawIndexed(0, 0, transition.oldMesh.indexCount, 1);
            }
            for (ChunkMesh mesh : VISIBLE_MESHES) {
                int fadeStep = fadeStep(mesh);
                pass.setUniform("DynamicTransforms",
                        fadeStep < FADE_STEPS ? fadeTransforms.get(fadeStep) : transforms);
                pass.setVertexBuffer(0, mesh.vertexBuffer);
                pass.drawIndexed(0, 0, mesh.indexCount, 1);
            }
        }
        recordTiming(callbackStarted, receiveNanos, meshNanos, regionNanos, visibilityNanos,
                System.nanoTime() - drawStarted);
    }

    private static void recordTiming(long callbackStarted, long receiveNanos, long meshNanos,
                                     long regionNanos, long visibilityNanos, long drawNanos) {
        long callbackNanos = System.nanoTime() - callbackStarted;
        FrameTiming sample = new FrameTiming(renderFrame, callbackNanos + receiveNanos,
                receiveNanos, meshNanos, regionNanos, visibilityNanos, drawNanos);
        lastTiming = sample;
        windowPeak = windowPeak.max(sample);
        if (sample.totalNanos >= SLOW_LOD_FRAME_NANOS) lastSpike = sample;
        if (renderFrame % 60 == 0) {
            peakTiming = windowPeak;
            windowPeak = FrameTiming.EMPTY;
        }
    }

    private static int fadeStep(ChunkMesh mesh) {
        float progress = Math.clamp((renderFrame - mesh.fadeStartFrame) / (float) FADE_DURATION_FRAMES,
                0.0F, 1.0F);
        progress = progress * progress * (3.0F - 2.0F * progress);
        return Math.clamp(Math.round(progress * FADE_STEPS), 1, FADE_STEPS);
    }

    private static int transitionFadeStep(LodTransition transition) {
        if (!transition.fadeOld) return FADE_STEPS;
        int elapsed = (int) Math.clamp(renderFrame - transition.startFrame, 0L, FADE_DURATION_FRAMES);
        float progress = elapsed / (float) FADE_DURATION_FRAMES;
        progress = progress * progress * (3.0F - 2.0F * progress);
        return Math.clamp(Math.round((1.0F - progress) * FADE_STEPS), 1, FADE_STEPS);
    }

    private static void finishTransitions() {
        var iterator = TRANSITIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LodTransition transition = entry.getValue();
            if (renderFrame - transition.startFrame < FADE_DURATION_FRAMES) continue;
            transition.oldMesh.close();
            iterator.remove();
            markRegionDirty(entry.getKey());
        }
    }

    private static boolean isWithinHorizontalDistance(AABB bounds, Vec3 camera, double radius) {
        double closestX = Math.clamp(camera.x, bounds.minX, bounds.maxX);
        double closestZ = Math.clamp(camera.z, bounds.minZ, bounds.maxZ);
        double dx = camera.x - closestX;
        double dz = camera.z - closestZ;
        return dx * dx + dz * dz <= radius * radius;
    }

    private static void evictFarChunks(Vec3 camera, int viewRadius) {
        double retentionRadius = viewRadius + EVICTION_MARGIN_CHUNKS * 16.0;
        double retentionSquared = retentionRadius * retentionRadius;
        for (Map.Entry<Long, CrossDimensionLodPayload> entry : DATA.entrySet()) {
            long key = entry.getKey();
            double dx = CrossDimensionLodKey.x(key) * 16.0 + 8.0 - camera.x;
            double dz = CrossDimensionLodKey.z(key) * 16.0 + 8.0 - camera.z;
            if (dx * dx + dz * dz <= retentionSquared || !DATA.remove(key, entry.getValue())) continue;

            HEIGHT_FIELDS.remove(key);
            DIRTY_CHUNKS.remove(key);
            MESH_REVISIONS.remove(key);
            IN_FLIGHT_MESHES.remove(key);
            ChunkMesh mesh = CHUNKS.remove(key);
            if (mesh != null) mesh.close();
            LodTransition transition = TRANSITIONS.remove(key);
            if (transition != null) transition.oldMesh.close();
            markRegionDirty(key);
            int chunkX = CrossDimensionLodKey.x(key);
            int chunkZ = CrossDimensionLodKey.z(key);
            markDirtyIfPresent(chunkX - 1, chunkZ);
            markDirtyIfPresent(chunkX + 1, chunkZ);
            markDirtyIfPresent(chunkX, chunkZ - 1);
            markDirtyIfPresent(chunkX, chunkZ + 1);
        }
    }

    private static long regionKey(int chunkX, int chunkZ) {
        return CrossDimensionLodKey.pack(Math.floorDiv(chunkX, REGION_CHUNKS),
                Math.floorDiv(chunkZ, REGION_CHUNKS));
    }

    private static void markRegionDirty(long chunkKey) {
        DIRTY_REGIONS.put(regionKey(CrossDimensionLodKey.x(chunkKey), CrossDimensionLodKey.z(chunkKey)), renderFrame);
    }

    private static void rebuildDirtyRegions() {
        long started = System.nanoTime();
        int rebuilt = 0;
        var iterator = DIRTY_REGIONS.entrySet().iterator();
        while (iterator.hasNext() && rebuilt < REGION_REBUILDS_PER_FRAME
                && (rebuilt == 0 || System.nanoTime() - started < REGION_REBUILD_BUDGET_NANOS)) {
            var entry = iterator.next();
            if (renderFrame - entry.getValue() < REGION_REBUILD_DEBOUNCE_FRAMES) continue;
            long key = entry.getKey();
            if (regionHasFadingChunk(key)) continue;
            RegionMesh replacement = buildRegionMesh(key);
            RegionMesh previous = replacement == null ? REGIONS.remove(key) : REGIONS.put(key, replacement);
            if (previous != null) previous.close();
            iterator.remove();
            rebuilt++;
        }
    }

    private static boolean regionHasFadingChunk(long key) {
        int regionX = CrossDimensionLodKey.x(key);
        int regionZ = CrossDimensionLodKey.z(key);
        for (LodTransition transition : TRANSITIONS.values()) {
            ChunkMesh mesh = transition.oldMesh;
            if (Math.floorDiv(mesh.chunkX, REGION_CHUNKS) == regionX
                    && Math.floorDiv(mesh.chunkZ, REGION_CHUNKS) == regionZ) return true;
        }
        for (ChunkMesh mesh : CHUNKS.values()) {
            if (mesh.indexCount > 0 && fadeStep(mesh) < FADE_STEPS
                    && Math.floorDiv(mesh.chunkX, REGION_CHUNKS) == regionX
                    && Math.floorDiv(mesh.chunkZ, REGION_CHUNKS) == regionZ) return true;
        }
        return false;
    }

    private static RegionMesh buildRegionMesh(long key) {
        int regionX = CrossDimensionLodKey.x(key);
        int regionZ = CrossDimensionLodKey.z(key);
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

    private static ChunkMesh uploadMesh(CpuMesh cpuMesh, long fadeStartFrame) {
        QuadBuffer quads = cpuMesh.quads;
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        int vertexCount = Math.multiplyExact(quads.size, 4);
        int vertexBytes = Math.multiplyExact(vertexCount, DefaultVertexFormat.ENTITY.getVertexSize());
        if (vertexBytes == 0) return new ChunkMesh(cpuMesh.chunkX, cpuMesh.chunkZ,
                null, 0, Set.copyOf(sprites), cpuMesh.bounds, cpuMesh.cellSize, fadeStartFrame);
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder = new BufferBuilder(bytes, CrossDimensionLodRenderTypes.TILED_BLOCKS.mode(),
                    DefaultVertexFormat.ENTITY);
            for (int quad = 0; quad < quads.size; quad++) {
                int attribute = quad * 3;
                int face = quads.attributes[attribute + 1];
                TextureAtlasSprite sprite = blockSprite(quads.attributes[attribute + 2], face);
                sprites.add(sprite);
                emitQuad(builder, quads, quad, sprite);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                        () -> "Cross-dimension LOD chunk [" + cpuMesh.chunkX + "," + cpuMesh.chunkZ + "]",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_SRC, mesh.vertexBuffer());
                return new ChunkMesh(cpuMesh.chunkX, cpuMesh.chunkZ, buffer,
                        mesh.drawState().indexCount(), Set.copyOf(sprites), cpuMesh.bounds,
                        cpuMesh.cellSize, fadeStartFrame);
            }
        }
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

    private static void emitQuad(VertexConsumer consumer, QuadBuffer quads, int index,
                                 TextureAtlasSprite sprite) {
        int coordinate = index * 6;
        float x0 = quads.coordinates[coordinate];
        float y0 = quads.coordinates[coordinate + 1];
        float z0 = quads.coordinates[coordinate + 2];
        float x1 = quads.coordinates[coordinate + 3];
        float y1 = quads.coordinates[coordinate + 4];
        float z1 = quads.coordinates[coordinate + 5];
        int attribute = index * 3;
        int color = quads.attributes[attribute];
        int face = quads.attributes[attribute + 1];
        switch (face) {
            case 0 -> emitTextured(consumer, face, color, sprite, -1, 0, 0,
                    x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0);
            case 1 -> emitTextured(consumer, face, color, sprite, 1, 0, 0,
                    x0,y0,z0, x0,y1,z0, x0,y1,z1, x0,y0,z1);
            case 2 -> emitTextured(consumer, face, color, sprite, 0, -1, 0,
                    x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1);
            case 3 -> emitTextured(consumer, face, color, sprite, 0, 1, 0,
                    x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0);
            case 4 -> emitTextured(consumer, face, color, sprite, 0, 0, -1,
                    x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0);
            case 5 -> emitTextured(consumer, face, color, sprite, 0, 0, 1,
                    x1,y0,z0, x1,y1,z0, x0,y1,z0, x0,y0,z0);
        }
    }

    private static void emitTextured(VertexConsumer consumer, int face, int color, TextureAtlasSprite sprite,
                                     float nx, float ny, float nz,
                                     float ax, float ay, float az, float bx, float by, float bz,
                                     float cx, float cy, float cz, float dx, float dy, float dz) {
        texturedVertex(consumer, face, color, sprite, ax, ay, az, nx, ny, nz);
        texturedVertex(consumer, face, color, sprite, bx, by, bz, nx, ny, nz);
        texturedVertex(consumer, face, color, sprite, cx, cy, cz, nx, ny, nz);
        texturedVertex(consumer, face, color, sprite, dx, dy, dz, nx, ny, nz);
    }

    private static void texturedVertex(VertexConsumer consumer, int face, int color, TextureAtlasSprite sprite,
                                       double x, double y, double z,
                                       float nx, float ny, float nz) {
        float u;
        float v;
        if (face < 2) {
            u = (float) z;
            v = (float) -y;
        } else if (face < 4) {
            u = (float) x;
            v = (float) z;
        } else {
            u = (float) x;
            v = (float) -y;
        }
        int minimumUv = packUv(sprite.getU0(), sprite.getV0());
        int maximumUv = packUv(sprite.getU1(), sprite.getV1());
        consumer.addVertex((float) x, (float) y, (float) z).setColor(color).setUv(u, v)
                .setOverlay(minimumUv).setLight(maximumUv).setNormal(nx, ny, nz);
    }

    private static int packUv(float u, float v) {
        return packAtlasCoordinate(u) | packAtlasCoordinate(v) << 16;
    }

    private static int packAtlasCoordinate(float coordinate) {
        return Math.clamp(Math.round(coordinate * 32767.0F), 0, 32767);
    }
    private record ChunkMesh(int chunkX, int chunkZ, GpuBuffer vertexBuffer, int indexCount,
                             Set<TextureAtlasSprite> sprites, AABB bounds, int cellSize,
                             long fadeStartFrame) implements AutoCloseable {
        @Override
        public void close() {
            if (vertexBuffer != null && !vertexBuffer.isClosed()) vertexBuffer.close();
        }
    }
    private record LodTransition(long key, ChunkMesh oldMesh, long startFrame, boolean fadeOld) {}
    private record RegionMesh(long key, GpuBuffer vertexBuffer, int indexCount,
                              Set<TextureAtlasSprite> sprites, AABB bounds) implements AutoCloseable {
        @Override
        public void close() {
            if (!vertexBuffer.isClosed()) vertexBuffer.close();
        }
    }
    private record MeshBuildResult(long key, long revision, CpuMesh mesh, Throwable failure) {}
    public record DebugStats(int data, int meshes, int regions, int visible, int dirty,
                             int building, int ready, int viewRadius, FrameTiming lastTiming,
                             FrameTiming peakTiming, FrameTiming lastSpike) {}
    public record FrameTiming(long frame, long totalNanos, long receiveNanos, long meshNanos,
                              long regionNanos, long visibilityNanos, long drawNanos) {
        private static final FrameTiming EMPTY = new FrameTiming(0L, 0L, 0L, 0L, 0L, 0L, 0L);

        private FrameTiming max(FrameTiming other) {
            return new FrameTiming(other.frame, Math.max(totalNanos, other.totalNanos),
                    Math.max(receiveNanos, other.receiveNanos), Math.max(meshNanos, other.meshNanos),
                    Math.max(regionNanos, other.regionNanos), Math.max(visibilityNanos, other.visibilityNanos),
                    Math.max(drawNanos, other.drawNanos));
        }
    }
    private CrossDimensionLodRenderer() {}
}
