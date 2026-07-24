package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.CpuMesh;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.HeightField;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.QuadBuffer;
import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.altnoir.mementoinabyss.compat.MiaMods;
import com.altnoir.mementoinabyss.compat.sodium.SodiumLodCompat;
import com.altnoir.mementoinabyss.network.CrossDimensionLodControlPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodPayload;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodKey;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/** Renders server-provided cross-dimension voxel chunks with six-direction greedy meshing. */
public final class CrossDimensionLodRenderer {
    private static final int MESH_UPLOADS_PER_FRAME = 4;
    private static final int MESH_SCHEDULES_PER_FRAME = 8;
    private static final int MAX_IN_FLIGHT_MESHES = 16;
    private static final int MAX_DIRTY_POLLS_PER_FRAME = 64;
    private static final long MESH_UPLOAD_BUDGET_NANOS = 1_500_000L;
    private static final int GPU_RETIRE_DELAY_FRAMES = 8;
    private static final int PAGE_CHUNKS = 4;
    private static final int PAGE_BUILD_DEBOUNCE_FRAMES = 4;
    private static final int MAX_IN_FLIGHT_PAGES = 2;
    private static final int PAGE_UPLOADS_PER_FRAME = 1;
    private static final int EVICTION_INTERVAL_FRAMES = 20;
    private static final int EVICTION_MARGIN_CHUNKS = 10;
    private static final int FADE_DURATION_FRAMES = 16;
    private static final int FADE_STEPS = 32;
    private static final int TRANSITION_PREWARM_FRAMES = 1;
    private static final long SLOW_LOD_FRAME_NANOS = 4_000_000L;
    private static final Map<Long, CrossDimensionLodPayload> DATA = new ConcurrentHashMap<>();
    private static final Map<Long, HeightField> HEIGHT_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Long, PackedChunk> PACKED_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<Long, ChunkMesh> CHUNKS = new ConcurrentHashMap<>();
    private static final Map<Long, LodTransition> TRANSITIONS = new HashMap<>();
    private static final Map<Long, PageMesh> PAGES = new HashMap<>();
    private static final Map<Long, PageTransition> PAGE_TRANSITIONS = new HashMap<>();
    private static final Map<Long, Long> DIRTY_PAGES = new HashMap<>();
    private static final Map<Long, Long> PAGE_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_PAGES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<PageBuildResult> COMPLETED_PAGES = new ConcurrentLinkedQueue<>();
    private static final ArrayDeque<RetiredResource> RETIRED_MESHES = new ArrayDeque<>();
    private static final Set<Long> DIRTY_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Long> DIRTY_CHUNK_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Map<Long, Long> MESH_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_MESHES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<MeshBuildResult> COMPLETED_MESHES = new ConcurrentLinkedQueue<>();
    private static final AtomicLong NEXT_MESH_REVISION = new AtomicLong();
    private static final AtomicLong PENDING_RECEIVE_NANOS = new AtomicLong();
    private static final AtomicLong WORK_EPOCH = new AtomicLong();
    private static final Map<Long, TextureAtlasSprite> FACE_SPRITES = new ConcurrentHashMap<>();
    /** Reused between extraction and rendering in the same frame; custom geometry is rendered before next extraction. */
    private static final ArrayList<ChunkMesh> VISIBLE_MESHES = new ArrayList<>();
    private static final ArrayList<LodTransition> VISIBLE_TRANSITIONS = new ArrayList<>();
    private static final ArrayList<PageMesh> VISIBLE_PAGES = new ArrayList<>();
    private static final ArrayList<PageTransition> VISIBLE_PAGE_TRANSITIONS = new ArrayList<>();
    private static final HashSet<TextureAtlasSprite> VISIBLE_SPRITES = new HashSet<>();
    private static long renderFrame;
    private static volatile int serverViewRadius;
    private static volatile FrameTiming lastTiming = FrameTiming.EMPTY;
    private static volatile FrameTiming peakTiming = FrameTiming.EMPTY;
    private static volatile FrameTiming lastSpike = FrameTiming.EMPTY;
    private static FrameTiming windowPeak = FrameTiming.EMPTY;
    private static GpuBuffer lodFogBuffer;
    private static int lodFogRadius = -1;
    private static GpuBuffer lodLightBuffer;
    private static ResourceKey<Level> lodLightSource;
    private static ClientState clientState = ClientState.RUNNING;
    private static Boolean reportedServerEnabled;

    public static DebugStats debugStats() {
        int viewRadius = serverViewRadius > 0 ? serverViewRadius
                : MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
        return new DebugStats(DATA.size(), CHUNKS.size(), PAGES.size(),
                VISIBLE_MESHES.size() + VISIBLE_TRANSITIONS.size()
                        + VISIBLE_PAGES.size() + VISIBLE_PAGE_TRANSITIONS.size(),
                DIRTY_CHUNKS.size() + DIRTY_PAGES.size(),
                IN_FLIGHT_MESHES.size() + IN_FLIGHT_PAGES.size(),
                COMPLETED_MESHES.size() + COMPLETED_PAGES.size(), viewRadius,
                MiaExecutors.threadCount(), MiaExecutors.activeTaskCount(), MiaExecutors.queuedTaskCount(),
                lastTiming, peakTiming, lastSpike);
    }

    public static void accept(CrossDimensionLodPayload payload) {
        long started = System.nanoTime();
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (!lodEnabled()) {
                transitionTo(ClientState.DISABLED);
                return;
            }
            if (minecraft.level == null) return;
            var activeLink = CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElse(null);
            if (activeLink == null || !activeLink.id().toString().equals(payload.linkId())) return;
            transitionTo(ClientState.RUNNING);
            applyPayload(payload);
        } finally {
            PENDING_RECEIVE_NANOS.addAndGet(System.nanoTime() - started);
        }
    }

    public static void clientTick() {
        MiaExecutors.refreshThreadLimit();
        Minecraft minecraft = Minecraft.getInstance();
        ClientState desiredState = desiredState(minecraft);
        transitionTo(desiredState);
        if (minecraft.getConnection() == null) {
            reportedServerEnabled = null;
            return;
        }
        boolean serverEnabled = desiredState == ClientState.RUNNING;
        if (reportedServerEnabled == null || reportedServerEnabled != serverEnabled) {
            ClientPacketDistributor.sendToServer(new CrossDimensionLodControlPayload(serverEnabled));
            reportedServerEnabled = serverEnabled;
        }
    }

    private static void applyPayload(CrossDimensionLodPayload payload) {
        serverViewRadius = payload.radius();
        if (payload.reset()) resetStreamData();
        long key = CrossDimensionLodKey.pack(payload.chunkX(), payload.chunkZ());
        DATA.put(key, payload);
        HeightField heightField = CrossDimensionLodMesher.buildHeightField(payload);
        HEIGHT_FIELDS.put(key, heightField);
        markDirty(payload.chunkX(), payload.chunkZ());
        markDirtyIfPresent(payload.chunkX() - 1, payload.chunkZ());
        markDirtyIfPresent(payload.chunkX() + 1, payload.chunkZ());
        markDirtyIfPresent(payload.chunkX(), payload.chunkZ() - 1);
        markDirtyIfPresent(payload.chunkX(), payload.chunkZ() + 1);
    }

    private static void markDirty(int chunkX, int chunkZ) {
        long key = CrossDimensionLodKey.pack(chunkX, chunkZ);
        MESH_REVISIONS.put(key, NEXT_MESH_REVISION.incrementAndGet());
        if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
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
        int polled = 0;
        while (scheduled < MESH_SCHEDULES_PER_FRAME && polled++ < MAX_DIRTY_POLLS_PER_FRAME
                && IN_FLIGHT_MESHES.size() < MAX_IN_FLIGHT_MESHES) {
            Long queuedKey = DIRTY_CHUNK_QUEUE.poll();
            if (queuedKey == null) break;
            long key = queuedKey;
            if (!DIRTY_CHUNKS.remove(key)) continue;
            if (IN_FLIGHT_MESHES.containsKey(key)) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            CrossDimensionLodPayload payload = DATA.get(key);
            Long revision = MESH_REVISIONS.get(key);
            if (payload == null || revision == null) continue;
            if (IN_FLIGHT_MESHES.putIfAbsent(key, revision) != null) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            scheduled++;
            long workEpoch = WORK_EPOCH.get();
            MiaExecutors.execute(MiaExecutors.Priority.LOD_MESH, () -> {
                if (workEpoch != WORK_EPOCH.get() || !lodEnabled()) return;
                try {
                    CpuMesh mesh = CrossDimensionLodMesher.build(payload, DATA, HEIGHT_FIELDS);
                    if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                        COMPLETED_MESHES.add(new MeshBuildResult(key, revision, payload, mesh, null));
                    }
                } catch (Throwable throwable) {
                    if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                        COMPLETED_MESHES.add(new MeshBuildResult(key, revision, payload, null, throwable));
                    }
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
            // A neighbour update deliberately queues another seam rebuild, but it must not
            // invalidate useful work for an unchanged source chunk. Only replacement of the
            // source payload makes this completed mesh obsolete.
            if (DATA.get(result.key) != result.sourcePayload) continue;
            PackedChunk packed = packMesh(result.mesh, renderFrame, irisSkyExposureRegion());
            PACKED_CHUNKS.put(result.key, packed);
            long pageKey = pageKey(result.mesh.chunkX, result.mesh.chunkZ);
            markPageDirty(pageKey);

            // Once a page exists it remains visible until its complete replacement is ready.
            // Avoid creating short-lived per-chunk VBOs for subsequent updates.
            if (PAGES.containsKey(pageKey)) {
                ChunkMesh obsolete = CHUNKS.remove(result.key);
                if (obsolete != null) retire(obsolete);
                LodTransition obsoleteTransition = TRANSITIONS.remove(result.key);
                if (obsoleteTransition != null) retire(obsoleteTransition.oldMesh);
                uploaded++;
                continue;
            }
            ChunkMesh previous = CHUNKS.get(result.key);
            LodTransition active = TRANSITIONS.get(result.key);
            long fadeStartFrame;
            if (previous == null) {
                fadeStartFrame = renderFrame;
            } else if (previous.cellSize == result.mesh.cellSize) {
                fadeStartFrame = previous.fadeStartFrame;
            } else fadeStartFrame = renderFrame - FADE_DURATION_FRAMES;
            packed = packed.withFadeStartFrame(fadeStartFrame);
            PACKED_CHUNKS.put(result.key, packed);
            ChunkMesh replacement = uploadChunkMesh(packed);
            CHUNKS.put(result.key, replacement);
            if (previous != null && previous.cellSize != replacement.cellSize) {
                if (active != null) retire(active.oldMesh);
                TRANSITIONS.put(result.key, new LodTransition(
                        previous, renderFrame + TRANSITION_PREWARM_FRAMES));
            } else if (previous != null) retire(previous);
            uploaded++;
        }
    }

    private static void clearResources() {
        resetStreamData();
        VISIBLE_MESHES.clear();
        VISIBLE_TRANSITIONS.clear();
        VISIBLE_PAGES.clear();
        VISIBLE_PAGE_TRANSITIONS.clear();
        VISIBLE_SPRITES.clear();
        FACE_SPRITES.clear();
        serverViewRadius = 0;
        PENDING_RECEIVE_NANOS.set(0L);
        lastTiming = FrameTiming.EMPTY;
        peakTiming = FrameTiming.EMPTY;
        lastSpike = FrameTiming.EMPTY;
        windowPeak = FrameTiming.EMPTY;
        if (lodFogBuffer != null) lodFogBuffer.close();
        lodFogBuffer = null;
        lodFogRadius = -1;
        if (lodLightBuffer != null) lodLightBuffer.close();
        lodLightBuffer = null;
        lodLightSource = null;
    }

    public static void disconnect() {
        reportedServerEnabled = null;
        transitionTo(ClientState.DISCONNECTED);
    }

    private static void resetStreamData() {
        invalidateMeshWork();
        DATA.clear();
        HEIGHT_FIELDS.clear();
        PACKED_CHUNKS.clear();
        closeMeshes();
        DIRTY_CHUNKS.clear();
        DIRTY_CHUNK_QUEUE.clear();
        MESH_REVISIONS.clear();
        DIRTY_PAGES.clear();
        PAGE_REVISIONS.clear();
    }

    private static ClientState desiredState(Minecraft minecraft) {
        if (minecraft.getConnection() == null) return ClientState.DISCONNECTED;
        if (!lodEnabled()) return ClientState.DISABLED;
        return ClientState.RUNNING;
    }

    private static void transitionTo(ClientState nextState) {
        if (clientState == nextState) return;
        clientState = nextState;
        if (nextState != ClientState.RUNNING) clearResources();
    }

    private static void invalidateMeshWork() {
        WORK_EPOCH.incrementAndGet();
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_MESH);
        IN_FLIGHT_MESHES.clear();
        COMPLETED_MESHES.clear();
        IN_FLIGHT_PAGES.clear();
        COMPLETED_PAGES.clear();
    }

    private static void closeMeshes() {
        for (ChunkMesh mesh : CHUNKS.values()) mesh.close();
        CHUNKS.clear();
        for (LodTransition transition : TRANSITIONS.values()) transition.oldMesh.close();
        TRANSITIONS.clear();
        for (PageMesh page : PAGES.values()) page.close();
        PAGES.clear();
        for (PageTransition transition : PAGE_TRANSITIONS.values()) transition.oldMesh.close();
        PAGE_TRANSITIONS.clear();
        while (!RETIRED_MESHES.isEmpty()) RETIRED_MESHES.removeFirst().resource.close();
    }

    /** Draws persistent chunk fallbacks and CPU-batched pages. */
    public static void renderPersistent(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        long callbackStarted = System.nanoTime();
        Minecraft minecraft = Minecraft.getInstance();
        if (!lodEnabled()) {
            transitionTo(ClientState.DISABLED);
            return;
        }
        if (minecraft.level == null) return;
        transitionTo(ClientState.RUNNING);
        var activeLink = CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElse(null);
        if (activeLink == null) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        if (camera == null) return;

        renderFrame++;
        int viewRadius = serverViewRadius > 0 ? serverViewRadius
                : MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
        if (renderFrame % EVICTION_INTERVAL_FRAMES == 0) evictFarChunks(camera, viewRadius);
        long meshStarted = System.nanoTime();
        closeRetiredMeshes();
        updateMeshes();
        finishTransitions();
        long meshNanos = System.nanoTime() - meshStarted;
        long pageStarted = System.nanoTime();
        updatePages();
        finishPageTransitions();
        long pageNanos = System.nanoTime() - pageStarted;
        long visibilityStarted = System.nanoTime();
        var frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        VISIBLE_MESHES.clear();
        VISIBLE_TRANSITIONS.clear();
        VISIBLE_PAGES.clear();
        VISIBLE_PAGE_TRANSITIONS.clear();
        VISIBLE_SPRITES.clear();
        int maximumIndexCount = 0;
        boolean sodiumLoaded = MiaMods.SODIUM.isLoaded();
        for (PageMesh page : PAGES.values()) {
            if ((page.indexCount > 0 || page.seamIndexCount > 0)
                    && isWithinHorizontalDistance(page.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(page.bounds))) {
                VISIBLE_PAGES.add(page);
                maximumIndexCount = Math.max(maximumIndexCount, page.indexCount);
                maximumIndexCount = Math.max(maximumIndexCount, page.seamIndexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(page.sprites);
            }
        }
        for (PageTransition transition : PAGE_TRANSITIONS.values()) {
            PageMesh page = transition.oldMesh;
            if (page.indexCount > 0 && isWithinHorizontalDistance(page.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(page.bounds))) {
                VISIBLE_PAGE_TRANSITIONS.add(transition);
                maximumIndexCount = Math.max(maximumIndexCount, page.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(page.sprites);
            }
        }
        for (ChunkMesh mesh : CHUNKS.values()) {
            if ((mesh.indexCount > 0 || mesh.seamIndexCount > 0)
                    && isWithinHorizontalDistance(mesh.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(mesh.bounds))) {
                VISIBLE_MESHES.add(mesh);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.indexCount);
                maximumIndexCount = Math.max(maximumIndexCount, mesh.seamIndexCount);
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
        if (VISIBLE_MESHES.isEmpty() && VISIBLE_TRANSITIONS.isEmpty()
                && VISIBLE_PAGES.isEmpty() && VISIBLE_PAGE_TRANSITIONS.isEmpty()) {
            recordTiming(callbackStarted, receiveNanos, meshNanos, pageNanos, visibilityNanos, 0L);
            return;
        }
        long drawStarted = System.nanoTime();
        if (sodiumLoaded) {
            for (TextureAtlasSprite sprite : VISIBLE_SPRITES) {
                SodiumLodCompat.markSpriteActive(sprite);
            }
        }

        boolean irisShaders = IrisRenderCompat.isShaderPackInUse();
        var pipeline = irisShaders
                ? CrossDimensionLodRenderTypes.irisBlocksPipeline()
                : CrossDimensionLodRenderTypes.tiledBlocksPipeline();
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
            if (fadeStep > 0) {
                fadeTransforms.computeIfAbsent(-fadeStep, step ->
                        RenderSystem.getDynamicUniforms().writeTransform(modelView,
                                new Vector4f(1.0F, 1.0F, 1.0F, step / (float) FADE_STEPS),
                                new Vector3f(), new Matrix4f()));
            }
        }
        for (PageMesh page : VISIBLE_PAGES) {
            int fadeStep = fadeStep(page.fadeStartFrame);
            if (fadeStep < FADE_STEPS) {
                fadeTransforms.computeIfAbsent(fadeStep, step ->
                        RenderSystem.getDynamicUniforms().writeTransform(modelView,
                                new Vector4f(1.0F, 1.0F, 1.0F, step / (float) FADE_STEPS),
                                new Vector3f(), new Matrix4f()));
            }
        }
        for (PageTransition transition : VISIBLE_PAGE_TRANSITIONS) {
            int fadeStep = transitionFadeStep(transition.startFrame);
            if (fadeStep > 0) {
                fadeTransforms.computeIfAbsent(-fadeStep, step ->
                        RenderSystem.getDynamicUniforms().writeTransform(modelView,
                                new Vector4f(1.0F, 1.0F, 1.0F, step / (float) FADE_STEPS),
                                new Vector3f(), new Matrix4f()));
            }
        }
        var atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        GpuBufferSlice lodFog = lodFog(viewRadius);
        GpuBufferSlice lodLight = lodLight(activeLink.source());

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Cross-dimension persistent LOD", target.getColorTextureView(),
                java.util.OptionalInt.empty(), target.getDepthTextureView(), java.util.OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            if (!irisShaders) {
                pass.setUniform("LodFog", lodFog);
                pass.setUniform("LodLight", lodLight);
            }
            pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
            pass.setIndexBuffer(indices, indexStorage.type());
            // Draw the incoming level first. The outgoing level is then laid over it and
            // dithered away, so a lower or higher replacement is already present before
            // the previous geometry starts disappearing.
            for (ChunkMesh mesh : VISIBLE_MESHES) {
                int fadeStep = fadeStep(mesh);
                int indexCount = irisShaders ? mesh.irisIndexCount : mesh.indexCount;
                if (indexCount > 0) {
                    pass.setUniform("DynamicTransforms",
                            fadeStep < FADE_STEPS ? fadeTransforms.get(fadeStep) : transforms);
                    pass.setVertexBuffer(0, irisShaders ? mesh.irisVertexBuffer : mesh.vertexBuffer);
                    pass.drawIndexed(0, 0, indexCount, 1);
                }
            }
            for (PageMesh page : VISIBLE_PAGES) {
                int indexCount = irisShaders ? page.irisIndexCount : page.indexCount;
                if (indexCount == 0) continue;
                int fadeStep = fadeStep(page.fadeStartFrame);
                pass.setUniform("DynamicTransforms",
                        fadeStep < FADE_STEPS ? fadeTransforms.get(fadeStep) : transforms);
                pass.setVertexBuffer(0, irisShaders ? page.irisVertexBuffer : page.vertexBuffer);
                pass.drawIndexed(0, 0, indexCount, 1);
            }
            for (PageTransition transition : VISIBLE_PAGE_TRANSITIONS) {
                int fadeStep = transitionFadeStep(transition.startFrame);
                pass.setUniform("DynamicTransforms",
                        fadeStep == 0 ? transforms : fadeTransforms.get(-fadeStep));
                PageMesh page = transition.oldMesh;
                pass.setVertexBuffer(0, irisShaders ? page.irisVertexBuffer : page.vertexBuffer);
                pass.drawIndexed(0, 0, irisShaders ? page.irisIndexCount : page.indexCount, 1);
            }
            for (LodTransition transition : VISIBLE_TRANSITIONS) {
                int fadeStep = transitionFadeStep(transition);
                pass.setUniform("DynamicTransforms",
                        fadeStep == 0 ? transforms : fadeTransforms.get(-fadeStep));
                ChunkMesh mesh = transition.oldMesh;
                pass.setVertexBuffer(0, irisShaders ? mesh.irisVertexBuffer : mesh.vertexBuffer);
                pass.drawIndexed(0, 0, irisShaders ? mesh.irisIndexCount : mesh.indexCount, 1);
            }
            // Cross-resolution boundary walls switch atomically. Fading these walls with the
            // terrain would expose empty pixels because the outgoing same-LOD mesh has no wall.
            pass.setUniform("DynamicTransforms", transforms);
            for (ChunkMesh mesh : VISIBLE_MESHES) {
                int indexCount = irisShaders ? mesh.irisSeamIndexCount : mesh.seamIndexCount;
                if (indexCount == 0) continue;
                pass.setVertexBuffer(0, irisShaders ? mesh.irisSeamVertexBuffer : mesh.seamVertexBuffer);
                pass.drawIndexed(0, 0, indexCount, 1);
            }
            for (PageMesh page : VISIBLE_PAGES) {
                int indexCount = irisShaders ? page.irisSeamIndexCount : page.seamIndexCount;
                if (indexCount == 0) continue;
                pass.setVertexBuffer(0, irisShaders ? page.irisSeamVertexBuffer : page.seamVertexBuffer);
                pass.drawIndexed(0, 0, indexCount, 1);
            }
        }
        recordTiming(callbackStarted, receiveNanos, meshNanos, pageNanos, visibilityNanos,
                System.nanoTime() - drawStarted);
    }

    private static void recordTiming(long callbackStarted, long receiveNanos, long meshNanos,
                                     long pageNanos, long visibilityNanos, long drawNanos) {
        long callbackNanos = System.nanoTime() - callbackStarted;
        FrameTiming sample = new FrameTiming(renderFrame, callbackNanos + receiveNanos,
                receiveNanos, meshNanos, pageNanos, visibilityNanos, drawNanos);
        lastTiming = sample;
        windowPeak = windowPeak.max(sample);
        if (sample.totalNanos >= SLOW_LOD_FRAME_NANOS) lastSpike = sample;
        if (renderFrame % 60 == 0) {
            peakTiming = windowPeak;
            windowPeak = FrameTiming.EMPTY;
        }
    }

    private static int fadeStep(ChunkMesh mesh) {
        return fadeStep(mesh.fadeStartFrame);
    }

    private static int fadeStep(long fadeStartFrame) {
        float progress = Math.clamp((renderFrame - fadeStartFrame) / (float) FADE_DURATION_FRAMES,
                0.0F, 1.0F);
        progress = progress * progress * (3.0F - 2.0F * progress);
        return Math.clamp(Math.round(progress * FADE_STEPS), 1, FADE_STEPS);
    }

    private static int transitionFadeStep(LodTransition transition) {
        return transitionFadeStep(transition.startFrame);
    }

    private static int transitionFadeStep(long startFrame) {
        if (renderFrame < startFrame) return 0;
        float progress = Math.clamp((renderFrame - startFrame) / (float) FADE_DURATION_FRAMES,
                0.0F, 1.0F);
        progress = progress * progress * progress
                * (progress * (progress * 6.0F - 15.0F) + 10.0F);
        return Math.clamp(Math.round(progress * FADE_STEPS), 1, FADE_STEPS);
    }

    private static void finishTransitions() {
        var iterator = TRANSITIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LodTransition transition = entry.getValue();
            if (renderFrame - transition.startFrame <= FADE_DURATION_FRAMES) continue;
            retire(transition.oldMesh);
            iterator.remove();
        }
    }

    private static void retire(GpuResource resource) {
        RETIRED_MESHES.addLast(new RetiredResource(resource, renderFrame + GPU_RETIRE_DELAY_FRAMES));
    }

    private static void closeRetiredMeshes() {
        while (!RETIRED_MESHES.isEmpty() && RETIRED_MESHES.peekFirst().closeAfterFrame <= renderFrame) {
            RETIRED_MESHES.removeFirst().resource.close();
        }
    }

    private static long pageKey(int chunkX, int chunkZ) {
        return CrossDimensionLodKey.pack(Math.floorDiv(chunkX, PAGE_CHUNKS),
                Math.floorDiv(chunkZ, PAGE_CHUNKS));
    }

    private static void markPageDirty(long pageKey) {
        long revision = NEXT_MESH_REVISION.incrementAndGet();
        PAGE_REVISIONS.put(pageKey, revision);
        DIRTY_PAGES.put(pageKey, renderFrame);
    }

    private static void updatePages() {
        drainCompletedPages();
        if (IN_FLIGHT_PAGES.size() >= MAX_IN_FLIGHT_PAGES) return;
        var iterator = DIRTY_PAGES.entrySet().iterator();
        while (iterator.hasNext() && IN_FLIGHT_PAGES.size() < MAX_IN_FLIGHT_PAGES) {
            var entry = iterator.next();
            long key = entry.getKey();
            if (renderFrame - entry.getValue() < PAGE_BUILD_DEBOUNCE_FRAMES || pageHasChunkFade(key)) continue;
            Long revision = PAGE_REVISIONS.get(key);
            if (revision == null || IN_FLIGHT_PAGES.putIfAbsent(key, revision) != null) continue;
            iterator.remove();
            PackedChunk[] chunks = pageChunks(key);
            long workEpoch = WORK_EPOCH.get();
            MiaExecutors.execute(MiaExecutors.Priority.LOD_MESH, () -> {
                if (workEpoch != WORK_EPOCH.get() || !lodEnabled()) return;
                try {
                    PageData data = buildPageData(key, chunks);
                    if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                        COMPLETED_PAGES.add(new PageBuildResult(key, revision, data, null));
                    }
                } catch (Throwable throwable) {
                    if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                        COMPLETED_PAGES.add(new PageBuildResult(key, revision, null, throwable));
                    }
                }
            });
        }
    }

    private static void drainCompletedPages() {
        int uploaded = 0;
        while (uploaded < PAGE_UPLOADS_PER_FRAME) {
            PageBuildResult result = COMPLETED_PAGES.poll();
            if (result == null) break;
            IN_FLIGHT_PAGES.remove(result.key, result.revision);
            if (result.failure != null) {
                MementoInAbyss.LOGGER.error("Failed to build cross-dimension LOD page [{},{}]",
                        CrossDimensionLodKey.x(result.key), CrossDimensionLodKey.z(result.key), result.failure);
                DIRTY_PAGES.put(result.key, renderFrame);
                continue;
            }
            if (!Long.valueOf(result.revision).equals(PAGE_REVISIONS.get(result.key))) continue;
            PageMesh previous = PAGES.get(result.key);
            PageTransition active = PAGE_TRANSITIONS.remove(result.key);
            if (active != null) retire(active.oldMesh);
            if (result.data == null) {
                PAGES.remove(result.key);
                PAGE_REVISIONS.remove(result.key, result.revision);
                if (previous != null) retire(previous);
            } else {
                long fadeStart = renderFrame - FADE_DURATION_FRAMES;
                PageMesh replacement = uploadPage(result.data, fadeStart);
                PAGES.put(result.key, replacement);
                if (previous != null) {
                    PAGE_TRANSITIONS.put(result.key, new PageTransition(
                            previous, renderFrame + TRANSITION_PREWARM_FRAMES));
                }
                retireChunkMeshesInPage(result.key);
            }
            uploaded++;
        }
    }

    private static boolean pageHasChunkFade(long key) {
        for (LodTransition transition : TRANSITIONS.values()) {
            if (pageKey(transition.oldMesh.chunkX, transition.oldMesh.chunkZ) == key) return true;
        }
        for (ChunkMesh mesh : CHUNKS.values()) {
            if (pageKey(mesh.chunkX, mesh.chunkZ) == key && fadeStep(mesh) < FADE_STEPS) return true;
        }
        return false;
    }

    private static PackedChunk[] pageChunks(long key) {
        int originX = CrossDimensionLodKey.x(key) * PAGE_CHUNKS;
        int originZ = CrossDimensionLodKey.z(key) * PAGE_CHUNKS;
        ArrayList<PackedChunk> chunks = new ArrayList<>(PAGE_CHUNKS * PAGE_CHUNKS);
        for (int z = 0; z < PAGE_CHUNKS; z++) {
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                PackedChunk chunk = PACKED_CHUNKS.get(CrossDimensionLodKey.pack(originX + x, originZ + z));
                if (chunk != null) chunks.add(chunk);
            }
        }
        return chunks.toArray(PackedChunk[]::new);
    }

    private static PageData buildPageData(long key, PackedChunk[] chunks) {
        if (chunks.length == 0) return null;
        int terrainBytes = 0;
        int seamBytes = 0;
        int irisTerrainBytes = 0;
        int irisSeamBytes = 0;
        int terrainIndices = 0;
        int seamIndices = 0;
        int irisTerrainIndices = 0;
        int irisSeamIndices = 0;
        AABB bounds = null;
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        for (PackedChunk chunk : chunks) {
            terrainBytes = Math.addExact(terrainBytes, chunk.terrain.bytes.length);
            seamBytes = Math.addExact(seamBytes, chunk.seam.bytes.length);
            irisTerrainBytes = Math.addExact(irisTerrainBytes, chunk.irisTerrain.bytes.length);
            irisSeamBytes = Math.addExact(irisSeamBytes, chunk.irisSeam.bytes.length);
            terrainIndices = Math.addExact(terrainIndices, chunk.terrain.indexCount);
            seamIndices = Math.addExact(seamIndices, chunk.seam.indexCount);
            irisTerrainIndices = Math.addExact(irisTerrainIndices, chunk.irisTerrain.indexCount);
            irisSeamIndices = Math.addExact(irisSeamIndices, chunk.irisSeam.indexCount);
            bounds = bounds == null ? chunk.bounds : bounds.minmax(chunk.bounds);
            sprites.addAll(chunk.sprites);
        }
        byte[] terrain = new byte[terrainBytes];
        byte[] seam = new byte[seamBytes];
        byte[] irisTerrain = new byte[irisTerrainBytes];
        byte[] irisSeam = new byte[irisSeamBytes];
        int terrainOffset = 0;
        int seamOffset = 0;
        int irisTerrainOffset = 0;
        int irisSeamOffset = 0;
        for (PackedChunk chunk : chunks) {
            System.arraycopy(chunk.terrain.bytes, 0, terrain, terrainOffset, chunk.terrain.bytes.length);
            System.arraycopy(chunk.seam.bytes, 0, seam, seamOffset, chunk.seam.bytes.length);
            System.arraycopy(chunk.irisTerrain.bytes, 0, irisTerrain, irisTerrainOffset,
                    chunk.irisTerrain.bytes.length);
            System.arraycopy(chunk.irisSeam.bytes, 0, irisSeam, irisSeamOffset,
                    chunk.irisSeam.bytes.length);
            terrainOffset += chunk.terrain.bytes.length;
            seamOffset += chunk.seam.bytes.length;
            irisTerrainOffset += chunk.irisTerrain.bytes.length;
            irisSeamOffset += chunk.irisSeam.bytes.length;
        }
        return new PageData(key, new PackedBuffer(terrain, terrainIndices),
                new PackedBuffer(seam, seamIndices),
                new PackedBuffer(irisTerrain, irisTerrainIndices),
                new PackedBuffer(irisSeam, irisSeamIndices), Set.copyOf(sprites), bounds);
    }

    private static PageMesh uploadPage(PageData data, long fadeStartFrame) {
        int x = CrossDimensionLodKey.x(data.key);
        int z = CrossDimensionLodKey.z(data.key);
        GpuBuffer terrain = uploadPackedBuffer(data.terrain, "page terrain", x, z);
        try {
            GpuBuffer seam = uploadPackedBuffer(data.seam, "page seam", x, z);
            try {
                GpuBuffer irisTerrain = uploadPackedBuffer(data.irisTerrain, "page Iris terrain", x, z);
                try {
                    GpuBuffer irisSeam = uploadPackedBuffer(data.irisSeam, "page Iris seam", x, z);
                    return new PageMesh(terrain, data.terrain.indexCount, seam, data.seam.indexCount,
                            irisTerrain, data.irisTerrain.indexCount,
                            irisSeam, data.irisSeam.indexCount,
                            data.sprites, data.bounds, fadeStartFrame);
                } catch (Throwable throwable) {
                    closeBuffer(irisTerrain);
                    throw throwable;
                }
            } catch (Throwable throwable) {
                closeBuffer(seam);
                throw throwable;
            }
        } catch (Throwable throwable) {
            closeBuffer(terrain);
            throw throwable;
        }
    }

    private static void retireChunkMeshesInPage(long key) {
        var iterator = CHUNKS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            ChunkMesh mesh = entry.getValue();
            if (pageKey(mesh.chunkX, mesh.chunkZ) != key) continue;
            iterator.remove();
            retire(mesh);
            LodTransition transition = TRANSITIONS.remove(entry.getKey());
            if (transition != null) retire(transition.oldMesh);
        }
    }

    private static void finishPageTransitions() {
        var iterator = PAGE_TRANSITIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (renderFrame - entry.getValue().startFrame <= FADE_DURATION_FRAMES) continue;
            retire(entry.getValue().oldMesh);
            iterator.remove();
        }
    }

    private static GpuBufferSlice lodFog(int viewRadius) {
        if (lodFogBuffer == null || lodFogRadius != viewRadius) {
            if (lodFogBuffer != null) lodFogBuffer.close();
            float end = Math.max(16.0F, viewRadius);
            float start = Math.max(0.0F, end * 0.80F);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var data = stack.malloc(16);
                Std140Builder.intoBuffer(data).putVec4(start, end, 0.0F, 0.0F);
                lodFogBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Cross-dimension LOD fog", GpuBuffer.USAGE_UNIFORM, data.flip());
            }
            lodFogRadius = viewRadius;
        }
        return lodFogBuffer.slice();
    }

    private static GpuBufferSlice lodLight(ResourceKey<Level> sourceDimension) {
        if (lodLightBuffer == null || !sourceDimension.equals(lodLightSource)) {
            if (lodLightBuffer != null) lodLightBuffer.close();
            RegionalSkyLight.Region region = RegionalSkyLight.resolve(sourceDimension);
            float centerX = 0.0F;
            float centerZ = 0.0F;
            float radius = -1.0F;
            float fadeDistance = 0.0F;
            float ambient = 1.0F;
            if (region != null) {
                RegionalSkyLight.RenderMask mask = region.renderMask();
                centerX = mask.centerX();
                centerZ = mask.centerZ();
                radius = mask.radius();
                fadeDistance = mask.fadeDistance();
                ambient = RegionalSkyLight.ambientBrightness(sourceDimension);
            }
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var data = stack.malloc(32);
                Std140Builder.intoBuffer(data)
                        .putVec4(centerX, centerZ, radius, fadeDistance)
                        .putVec4(ambient, 0.0F, 0.0F, 0.0F);
                lodLightBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Cross-dimension LOD regional light", GpuBuffer.USAGE_UNIFORM, data.flip());
            }
            lodLightSource = sourceDimension;
        }
        return lodLightBuffer.slice();
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
            PACKED_CHUNKS.remove(key);
            DIRTY_CHUNKS.remove(key);
            MESH_REVISIONS.remove(key);
            IN_FLIGHT_MESHES.remove(key);
            ChunkMesh mesh = CHUNKS.remove(key);
            if (mesh != null) retire(mesh);
            LodTransition transition = TRANSITIONS.remove(key);
            if (transition != null) retire(transition.oldMesh);
            markPageDirty(pageKey(CrossDimensionLodKey.x(key), CrossDimensionLodKey.z(key)));
            int chunkX = CrossDimensionLodKey.x(key);
            int chunkZ = CrossDimensionLodKey.z(key);
            markDirtyIfPresent(chunkX - 1, chunkZ);
            markDirtyIfPresent(chunkX + 1, chunkZ);
            markDirtyIfPresent(chunkX, chunkZ - 1);
            markDirtyIfPresent(chunkX, chunkZ + 1);
        }
    }

    private static PackedChunk packMesh(CpuMesh cpuMesh, long fadeStartFrame,
                                        RegionalSkyLight.Region irisSkyExposureRegion) {
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        PackedBuffer terrain = packQuadBuffer(cpuMesh.quads, sprites);
        PackedBuffer seam = packQuadBuffer(cpuMesh.seamQuads, sprites);
        PackedBuffer irisTerrain = MiaMods.IRIS.isLoaded()
                ? packIrisQuadBuffer(cpuMesh.quads, irisSkyExposureRegion) : PackedBuffer.EMPTY;
        PackedBuffer irisSeam = MiaMods.IRIS.isLoaded()
                ? packIrisQuadBuffer(cpuMesh.seamQuads, irisSkyExposureRegion) : PackedBuffer.EMPTY;
        return new PackedChunk(cpuMesh.chunkX, cpuMesh.chunkZ, terrain, seam, irisTerrain, irisSeam,
                Set.copyOf(sprites), cpuMesh.bounds, cpuMesh.cellSize, fadeStartFrame);
    }

    /**
     * Supplies only sky visibility to Iris. The shader pack still owns the
     * actual sun color, time-of-day response, shadowing, and final brightness.
     */
    private static RegionalSkyLight.Region irisSkyExposureRegion() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return null;
        var activeLink = CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElse(null);
        return activeLink == null ? null : RegionalSkyLight.resolve(activeLink.source());
    }

    private static PackedBuffer packQuadBuffer(QuadBuffer quads, Set<TextureAtlasSprite> sprites) {
        int vertexCount = Math.multiplyExact(quads.size, 4);
        int vertexBytes = Math.multiplyExact(vertexCount, CrossDimensionLodRenderTypes.LOD_VERTEX_FORMAT.getVertexSize());
        if (vertexBytes == 0) return PackedBuffer.EMPTY;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder = new BufferBuilder(bytes,
                    CrossDimensionLodRenderTypes.tiledBlocksPipeline().getVertexFormatMode(),
                    CrossDimensionLodRenderTypes.LOD_VERTEX_FORMAT);
            for (int quad = 0; quad < quads.size; quad++) {
                int attribute = quad * 2;
                int face = quads.attributes[attribute];
                TextureAtlasSprite sprite = blockSprite(quads.attributes[attribute + 1], face);
                sprites.add(sprite);
                emitQuad(builder, quads, quad, sprite);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                var vertexData = mesh.vertexBuffer().duplicate();
                byte[] packed = new byte[vertexData.remaining()];
                vertexData.get(packed);
                return new PackedBuffer(packed, mesh.drawState().indexCount());
            }
        }
    }

    private static PackedBuffer packIrisQuadBuffer(QuadBuffer quads,
                                                   RegionalSkyLight.Region skyExposureRegion) {
        int vertexCount = Math.multiplyExact(quads.size, 4);
        int vertexBytes = Math.multiplyExact(vertexCount,
                CrossDimensionLodRenderTypes.IRIS_VERTEX_FORMAT.getVertexSize());
        if (vertexBytes == 0) return PackedBuffer.EMPTY;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder = new BufferBuilder(bytes,
                    CrossDimensionLodRenderTypes.irisBlocksPipeline().getVertexFormatMode(),
                    CrossDimensionLodRenderTypes.IRIS_VERTEX_FORMAT);
            for (int quad = 0; quad < quads.size; quad++) {
                int attribute = quad * 2;
                int face = quads.attributes[attribute];
                TextureAtlasSprite sprite = blockSprite(quads.attributes[attribute + 1], face);
                emitIrisQuad(builder, quads, quad, sprite, skyExposureRegion);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                var vertexData = mesh.vertexBuffer().duplicate();
                byte[] packed = new byte[vertexData.remaining()];
                vertexData.get(packed);
                return new PackedBuffer(packed, mesh.drawState().indexCount());
            }
        }
    }

    private static ChunkMesh uploadChunkMesh(PackedChunk packed) {
        GpuBuffer terrain = uploadPackedBuffer(packed.terrain, "chunk terrain",
                packed.chunkX, packed.chunkZ);
        try {
            GpuBuffer seam = uploadPackedBuffer(packed.seam, "chunk seam",
                    packed.chunkX, packed.chunkZ);
            try {
                GpuBuffer irisTerrain = uploadPackedBuffer(packed.irisTerrain, "chunk Iris terrain",
                        packed.chunkX, packed.chunkZ);
                try {
                    GpuBuffer irisSeam = uploadPackedBuffer(packed.irisSeam, "chunk Iris seam",
                            packed.chunkX, packed.chunkZ);
                    return new ChunkMesh(packed.chunkX, packed.chunkZ,
                            terrain, packed.terrain.indexCount, seam, packed.seam.indexCount,
                            irisTerrain, packed.irisTerrain.indexCount,
                            irisSeam, packed.irisSeam.indexCount,
                            packed.sprites, packed.bounds, packed.cellSize, packed.fadeStartFrame);
                } catch (Throwable throwable) {
                    closeBuffer(irisTerrain);
                    throw throwable;
                }
            } catch (Throwable throwable) {
                closeBuffer(seam);
                throw throwable;
            }
        } catch (Throwable throwable) {
            closeBuffer(terrain);
            throw throwable;
        }
    }

    private static GpuBuffer uploadPackedBuffer(PackedBuffer packed, String part, int x, int z) {
        if (packed.bytes.length == 0) return null;
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "Cross-dimension LOD " + part + " [" + x + "," + z + "]",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, packed.bytes.length);
        try {
            var nativeBytes = MemoryUtil.memAlloc(packed.bytes.length);
            try {
                nativeBytes.put(packed.bytes).flip();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), nativeBytes);
            } finally {
                MemoryUtil.memFree(nativeBytes);
            }
            return buffer;
        } catch (Throwable throwable) {
            closeBuffer(buffer);
            throw throwable;
        }
    }

    private static void closeBuffer(GpuBuffer buffer) {
        if (buffer != null && !buffer.isClosed()) buffer.close();
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
        int attribute = index * 2;
        int face = quads.attributes[attribute];
        switch (face) {
            case 0 -> emitTextured(consumer, sprite, -1, 0, 0,
                    x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0);
            case 1 -> emitTextured(consumer, sprite, 1, 0, 0,
                    x0,y0,z0, x0,y1,z0, x0,y1,z1, x0,y0,z1);
            case 2 -> emitTextured(consumer, sprite, 0, -1, 0,
                    x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1);
            case 3 -> emitTextured(consumer, sprite, 0, 1, 0,
                    x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0);
            case 4 -> emitTextured(consumer, sprite, 0, 0, -1,
                    x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0);
            case 5 -> emitTextured(consumer, sprite, 0, 0, 1,
                    x1,y0,z0, x1,y1,z0, x0,y1,z0, x0,y0,z0);
        }
    }

    private static void emitTextured(VertexConsumer consumer, TextureAtlasSprite sprite,
                                     float nx, float ny, float nz,
                                     float ax, float ay, float az, float bx, float by, float bz,
                                     float cx, float cy, float cz, float dx, float dy, float dz) {
        texturedVertex(consumer, sprite, ax, ay, az, nx, ny, nz);
        texturedVertex(consumer, sprite, bx, by, bz, nx, ny, nz);
        texturedVertex(consumer, sprite, cx, cy, cz, nx, ny, nz);
        texturedVertex(consumer, sprite, dx, dy, dz, nx, ny, nz);
    }

    private static void texturedVertex(VertexConsumer consumer, TextureAtlasSprite sprite,
                                       double x, double y, double z,
                                       float nx, float ny, float nz) {
        int minimumUv = packUv(sprite.getU0(), sprite.getV0());
        int maximumUv = packUv(sprite.getU1(), sprite.getV1());
        consumer.addVertex((float) x, (float) y, (float) z)
                .setOverlay(minimumUv).setLight(maximumUv).setNormal(nx, ny, nz);
    }

    private static void emitIrisQuad(VertexConsumer consumer, QuadBuffer quads, int index,
                                     TextureAtlasSprite sprite,
                                     RegionalSkyLight.Region skyExposureRegion) {
        int coordinate = index * 6;
        float x0 = quads.coordinates[coordinate];
        float y0 = quads.coordinates[coordinate + 1];
        float z0 = quads.coordinates[coordinate + 2];
        float x1 = quads.coordinates[coordinate + 3];
        float y1 = quads.coordinates[coordinate + 4];
        float z1 = quads.coordinates[coordinate + 5];
        int face = quads.attributes[index * 2];
        switch (face) {
            case 0 -> emitIrisTextured(consumer, sprite, 0.8F, -1, 0, 0, skyExposureRegion,
                    x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0);
            case 1 -> emitIrisTextured(consumer, sprite, 0.8F, 1, 0, 0, skyExposureRegion,
                    x0,y0,z0, x0,y1,z0, x0,y1,z1, x0,y0,z1);
            case 2 -> emitIrisTextured(consumer, sprite, 0.6F, 0, -1, 0, skyExposureRegion,
                    x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1);
            case 3 -> emitIrisTextured(consumer, sprite, 1.0F, 0, 1, 0, skyExposureRegion,
                    x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0);
            case 4 -> emitIrisTextured(consumer, sprite, 0.8F, 0, 0, -1, skyExposureRegion,
                    x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0);
            case 5 -> emitIrisTextured(consumer, sprite, 0.8F, 0, 0, 1, skyExposureRegion,
                    x1,y0,z0, x1,y1,z0, x0,y1,z0, x0,y0,z0);
        }
    }

    private static void emitIrisTextured(VertexConsumer consumer, TextureAtlasSprite sprite,
                                         float shade, float nx, float ny, float nz,
                                         RegionalSkyLight.Region skyExposureRegion,
                                         float ax, float ay, float az, float bx, float by, float bz,
                                         float cx, float cy, float cz, float dx, float dy, float dz) {
        irisVertex(consumer, ax, ay, az, sprite.getU0(), sprite.getV1(),
                shade, nx, ny, nz, skyExposureRegion);
        irisVertex(consumer, bx, by, bz, sprite.getU0(), sprite.getV0(),
                shade, nx, ny, nz, skyExposureRegion);
        irisVertex(consumer, cx, cy, cz, sprite.getU1(), sprite.getV0(),
                shade, nx, ny, nz, skyExposureRegion);
        irisVertex(consumer, dx, dy, dz, sprite.getU1(), sprite.getV1(),
                shade, nx, ny, nz, skyExposureRegion);
    }

    private static void irisVertex(VertexConsumer consumer, float x, float y, float z,
                                   float u, float v, float shade,
                                   float nx, float ny, float nz,
                                   RegionalSkyLight.Region skyExposureRegion) {
        int skyExposure = skyExposureRegion == null
                ? 15 : skyExposureRegion.maxSkyLight(Mth.floor(x), Mth.floor(z));
        consumer.addVertex(x, y, z)
                .setColor(shade, shade, shade, 1.0F)
                .setUv(u, v)
                .setLight(LightCoordsUtil.pack(0, skyExposure))
                .setNormal(nx, ny, nz);
    }

    private static int packUv(float u, float v) {
        return packAtlasCoordinate(u) | packAtlasCoordinate(v) << 16;
    }

    private static int packAtlasCoordinate(float coordinate) {
        return Math.clamp(Math.round(coordinate * 32767.0F), 0, 32767);
    }

    private static boolean lodEnabled() {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodEnabled.get();
    }

    private enum ClientState {
        RUNNING,
        DISABLED,
        DISCONNECTED
    }

    private interface GpuResource extends AutoCloseable {
        @Override
        void close();
    }

    private record ChunkMesh(int chunkX, int chunkZ, GpuBuffer vertexBuffer, int indexCount,
                             GpuBuffer seamVertexBuffer, int seamIndexCount,
                             GpuBuffer irisVertexBuffer, int irisIndexCount,
                             GpuBuffer irisSeamVertexBuffer, int irisSeamIndexCount,
                             Set<TextureAtlasSprite> sprites, AABB bounds, int cellSize,
                             long fadeStartFrame) implements GpuResource {
        @Override
        public void close() {
            closeBuffer(vertexBuffer);
            closeBuffer(seamVertexBuffer);
            closeBuffer(irisVertexBuffer);
            closeBuffer(irisSeamVertexBuffer);
        }
    }
    private record PageMesh(GpuBuffer vertexBuffer, int indexCount,
                            GpuBuffer seamVertexBuffer, int seamIndexCount,
                            GpuBuffer irisVertexBuffer, int irisIndexCount,
                            GpuBuffer irisSeamVertexBuffer, int irisSeamIndexCount,
                            Set<TextureAtlasSprite> sprites, AABB bounds,
                            long fadeStartFrame) implements GpuResource {
        @Override
        public void close() {
            closeBuffer(vertexBuffer);
            closeBuffer(seamVertexBuffer);
            closeBuffer(irisVertexBuffer);
            closeBuffer(irisSeamVertexBuffer);
        }
    }
    private record PackedBuffer(byte[] bytes, int indexCount) {
        private static final PackedBuffer EMPTY = new PackedBuffer(new byte[0], 0);
    }
    private record PackedChunk(int chunkX, int chunkZ, PackedBuffer terrain, PackedBuffer seam,
                               PackedBuffer irisTerrain, PackedBuffer irisSeam,
                               Set<TextureAtlasSprite> sprites, AABB bounds, int cellSize,
                               long fadeStartFrame) {
        private PackedChunk withFadeStartFrame(long frame) {
            return new PackedChunk(chunkX, chunkZ, terrain, seam, irisTerrain, irisSeam,
                    sprites, bounds, cellSize, frame);
        }
    }
    private record PageData(long key, PackedBuffer terrain, PackedBuffer seam,
                            PackedBuffer irisTerrain, PackedBuffer irisSeam,
                            Set<TextureAtlasSprite> sprites, AABB bounds) {}
    private record LodTransition(ChunkMesh oldMesh, long startFrame) {}
    private record PageTransition(PageMesh oldMesh, long startFrame) {}
    private record RetiredResource(GpuResource resource, long closeAfterFrame) {}
    private record MeshBuildResult(long key, long revision, CrossDimensionLodPayload sourcePayload,
                                   CpuMesh mesh, Throwable failure) {}
    private record PageBuildResult(long key, long revision, PageData data, Throwable failure) {}
    public record DebugStats(int data, int meshes, int pages, int visible, int dirty,
                             int building, int ready, int viewRadius,
                             int cpuThreads, int cpuActive, int cpuQueued, FrameTiming lastTiming,
                             FrameTiming peakTiming, FrameTiming lastSpike) {}
    public record FrameTiming(long frame, long totalNanos, long receiveNanos, long meshNanos,
                              long pageNanos, long visibilityNanos, long drawNanos) {
        private static final FrameTiming EMPTY = new FrameTiming(0L, 0L, 0L, 0L, 0L, 0L, 0L);

        private FrameTiming max(FrameTiming other) {
            return new FrameTiming(other.frame, Math.max(totalNanos, other.totalNanos),
                    Math.max(receiveNanos, other.receiveNanos), Math.max(meshNanos, other.meshNanos),
                    Math.max(pageNanos, other.pageNanos), Math.max(visibilityNanos, other.visibilityNanos),
                    Math.max(drawNanos, other.drawNanos));
        }
    }
    private CrossDimensionLodRenderer() {}
}
