package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodAmbientOcclusion.View;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.BoundaryBuilder;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.CpuMesh;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.QuadBuffer;
import com.altnoir.mementoinabyss.client.render.CrossDimensionLodMesher.Side;
import com.altnoir.mementoinabyss.compat.MiaMods;
import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.altnoir.mementoinabyss.compat.sodium.SodiumLodCompat;
import com.altnoir.mementoinabyss.network.CrossDimensionLodBatchPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodCacheOfferPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodControlPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodReceiptPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodStreamPayload;
import com.altnoir.mementoinabyss.network.CrossDimensionLodViewPayload;
import com.altnoir.mementoinabyss.util.concurrent.MiaExecutors;
import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodKey;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodStateNames;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodView;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
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

/** Renders server-provided cross-dimension voxel chunks with six-direction greedy meshing. */
public final class CrossDimensionLodRenderer {
    private static final int MESH_RESULTS_PER_FRAME = 16;
    private static final int MESH_SCHEDULES_PER_FRAME = 8;
    private static final int MAX_PENDING_PAYLOADS = 256;
    private static final int MAX_PENDING_PAYLOAD_BYTES = 2 * 1024 * 1024;
    private static final int RECEIVE_ITEMS_PER_FRAME = 32;
    private static final int MATERIALS_PER_TICK = 16;
    private static final long PAYLOAD_DRAIN_BUDGET_NANOS = 1_000_000L;
    private static final long SPIKE_LIFETIME_FRAMES = 120L;
    private static final int MAX_IN_FLIGHT_MESHES = 16;
    private static final int MAX_DIRTY_POLLS_PER_FRAME = 256;
    private static final long MESH_RESULT_DRAIN_BUDGET_NANOS = 1_500_000L;
    private static final int GPU_RETIRE_DELAY_FRAMES = 8;
    private static final int PAGE_CHUNKS = 4;
    private static final int PAGE_BUILD_DEBOUNCE_FRAMES = 4;
    private static final int MAX_IN_FLIGHT_PAGES = 2;
    private static final int PAGE_UPLOADS_PER_FRAME = 4;
    private static final long PAGE_UPLOAD_BUDGET_NANOS = 1_500_000L;
    private static final int PAGE_UPLOAD_BYTES_PER_FRAME = 2 * 1024 * 1024;
    private static final int EVICTION_INTERVAL_FRAMES = 20;
    private static final int EVICTION_MARGIN_CHUNKS = 10;
    private static final long FADE_DURATION_NANOS = 240_000_000L;
    private static final int FADE_STEPS = 64;
    private static final long SLOW_LOD_FRAME_NANOS = 4_000_000L;
    private static final Map<Long, CrossDimensionLodColumn> DATA = new ConcurrentHashMap<>();

    /** Server-validated detail selected from the client's camera interest. */
    private static final Map<Long, CrossDimensionLodColumn> SELECTED_DATA =
            new ConcurrentHashMap<>();

    private static final CrossDimensionLodIngress INGRESS =
            new CrossDimensionLodIngress(
                    MAX_PENDING_PAYLOADS, MAX_PENDING_PAYLOAD_BYTES, 16 * 1024 * 1024);
    private static final CrossDimensionLodSectionStore SECTION_STORE =
            new CrossDimensionLodSectionStore();
    private static final CrossDimensionLodReceivePump RECEIVE_PUMP =
            new CrossDimensionLodReceivePump(System::nanoTime);
    private static long lastReceiveNanos, lastExpiryTick = -1;
    private static long clientTick;
    private static MiaLodView cameraView, reportedView;
    private static long viewSequence, lastViewTick = Long.MIN_VALUE / 2;
    private static String reportedViewLink;
    private static int reportedViewRadius;

    static MiaLodView cameraView() {
        return cameraView;
    }

    private static CrossDimensionLodClientCache clientCache;

    static CrossDimensionLodClientCache.Stats cacheStats() {
        return clientCache == null ? CrossDimensionLodClientCache.Stats.EMPTY : clientCache.stats();
    }

    private static final Map<Long, PackedChunk> PACKED_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<Long, PageMesh> PAGES = new HashMap<>();
    private static final Map<Long, Side[]> PUBLISHED_SIDES = new HashMap<>();
    private static final Map<EdgeKey, EdgeMesh> PAGE_EDGES = new HashMap<>();
    private static final ArrayList<EdgeMesh> VISIBLE_EDGES = new ArrayList<>();
    private static final Map<Long, PageTransition> PAGE_TRANSITIONS = new HashMap<>();
    private static final Map<Long, Long> DIRTY_PAGES = new HashMap<>();
    private static final Map<Long, Long> PAGE_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_PAGES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<PageBuildResult> COMPLETED_PAGES =
            new ConcurrentLinkedQueue<>();
    private static final ArrayDeque<RetiredResource> RETIRED_MESHES = new ArrayDeque<>();
    private static final Set<Long> DIRTY_CHUNKS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Long> DIRTY_CHUNK_QUEUE =
            new ConcurrentLinkedQueue<>();
    private static final Map<Long, Long> MESH_REVISIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> IN_FLIGHT_MESHES = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<MeshBuildResult> COMPLETED_MESHES =
            new ConcurrentLinkedQueue<>();
    private static final AtomicLong NEXT_MESH_REVISION = new AtomicLong();
    private static final AtomicLong PENDING_RECEIVE_NANOS = new AtomicLong();
    private static final AtomicLong LAST_MESH_WORK_NANOS = new AtomicLong();
    private static final AtomicLong PEAK_MESH_WORK_NANOS = new AtomicLong();
    private static final AtomicLong LAST_PAGE_WORK_NANOS = new AtomicLong();
    private static final AtomicLong PEAK_PAGE_WORK_NANOS = new AtomicLong();
    private static final AtomicLong WORK_EPOCH = new AtomicLong();
    private static final Map<Long, TextureAtlasSprite> FACE_SPRITES = new ConcurrentHashMap<>();
    private static final Set<Integer> MATERIAL_PENDING = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Integer> MATERIAL_QUEUE =
            new ConcurrentLinkedQueue<>();
    private static final Map<Long, Integer> MESH_FAILURES = new ConcurrentHashMap<>();
    private static final Map<Long, Long> MESH_RETRY_AFTER = new ConcurrentHashMap<>();
    private static Boolean packedIrisFormat;

    /** Reused between extraction and rendering in the same frame. */
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
    private static Object materialModelSet;
    private static ClientState clientState = ClientState.RUNNING;
    private static Boolean reportedServerEnabled;

    public static DebugStats debugStats() {
        int viewRadius =
                serverViewRadius > 0
                        ? serverViewRadius
                        : MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get()
                                * 16;
        return new DebugStats(
                DATA.size(),
                PACKED_CHUNKS.size(),
                PAGES.size(),
                VISIBLE_PAGES.size() + VISIBLE_PAGE_TRANSITIONS.size() + VISIBLE_EDGES.size(),
                DIRTY_CHUNKS.size() + DIRTY_PAGES.size(),
                IN_FLIGHT_MESHES.size() + IN_FLIGHT_PAGES.size(),
                COMPLETED_MESHES.size() + COMPLETED_PAGES.size(),
                INGRESS.size(),
                INGRESS.bytes(),
                INGRESS.retainedBytes(),
                MATERIAL_PENDING.size(),
                MESH_FAILURES.size(),
                viewRadius,
                MiaExecutors.threadCount(),
                MiaExecutors.activeTaskCount(),
                MiaExecutors.queuedTaskCount(),
                LAST_MESH_WORK_NANOS.get(),
                PEAK_MESH_WORK_NANOS.get(),
                LAST_PAGE_WORK_NANOS.get(),
                PEAK_PAGE_WORK_NANOS.get(),
                lastTiming,
                peakTiming,
                lastSpike);
    }

    /** Network callback entry point. No Minecraft or renderer state is touched here. */
    public static void accept(CrossDimensionLodStreamPayload payload) {
        INGRESS.offer(payload);
    }

    private static void drainPayloads() {
        long started = System.nanoTime();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || clientCache == null) return;
        lastReceiveNanos = started;
        if (INGRESS.takeReset()) resetStreamData();
        var activeLink = CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).orElse(null);
        if (activeLink == null) return;
        for (int i = 0; i < RECEIVE_ITEMS_PER_FRAME; i++) {
            var rejected = INGRESS.pollRejected();
            if (rejected == null) break;
            if (rejected.linkId().equals(activeLink.id().toString()))
                ClientPacketDistributor.sendToServer(rejected);
        }
        String link = activeLink.id().toString();
        RECEIVE_PUMP.drain(
                RECEIVE_ITEMS_PER_FRAME,
                PAYLOAD_DRAIN_BUDGET_NANOS,
                () -> applyNextNetwork(link),
                () -> applyNextCache(link));
        if (lastExpiryTick != clientTick) {
            lastExpiryTick = clientTick;
            for (var receipt : SECTION_STORE.expire(clientTick))
                ClientPacketDistributor.sendToServer(receipt);
        }
        PENDING_RECEIVE_NANOS.addAndGet(System.nanoTime() - started);
    }

    private static boolean applyNextNetwork(String link) {
        var poll = INGRESS.poll();
        if (poll.reset()) resetStreamData();
        var payload = poll.payload();
        if (payload == null) return false;
        if (!link.equals(payload.transfer().linkId())) return true;
        serverViewRadius = payload.transfer().radius();
        if (payload instanceof CrossDimensionLodCacheOfferPayload offer) {
            if (!SECTION_STORE.offer(offer.transfer())) return true;
            if (SECTION_STORE.alreadyApplied(offer.transfer())) {
                ClientPacketDistributor.sendToServer(
                        CrossDimensionLodReceiptPayload.of(
                                offer.transfer(), CrossDimensionLodReceiptPayload.APPLIED));
            } else if (!clientCache.lookup(offer, clientTick)) {
                ClientPacketDistributor.sendToServer(
                        CrossDimensionLodReceiptPayload.of(
                                offer.transfer(), CrossDimensionLodReceiptPayload.CACHE_MISS));
            }
            return true;
        }
        var batch = (CrossDimensionLodBatchPayload) payload;
        var result = SECTION_STORE.accept(batch, clientTick);
        if (result.column() != null) applyColumn(result.column());
        // ACK is still after the complete CPU transaction, never after mere receipt or disk IO.
        if (result.receipt() != null) ClientPacketDistributor.sendToServer(result.receipt());
        if (result.column() != null)
            clientCache.save(batch.transfer(), SECTION_STORE.snapshot(batch.transfer()));
        return true;
    }

    private static boolean applyNextCache(String link) {
        if (INGRESS.takeReset()) resetStreamData();
        var read = clientCache.poll(clientTick);
        if (read == null) return false;
        var transfer = read.offer().transfer();
        if (!transfer.linkId().equals(link)) return true;
        var sections = clientCache.resolve(read);
        if (sections == null) {
            clientCache.miss();
            ClientPacketDistributor.sendToServer(
                    CrossDimensionLodReceiptPayload.of(
                            transfer, CrossDimensionLodReceiptPayload.CACHE_MISS));
            return true;
        }
        var result = SECTION_STORE.restore(transfer, sections, clientTick);
        if (result.column() != null) {
            applyColumn(result.column());
            clientCache.hit();
        }
        if (result.receipt() != null) ClientPacketDistributor.sendToServer(result.receipt());
        return true;
    }

    private static void clearPendingPayloads() {
        INGRESS.clear();
    }

    public static void clientTick() {
        clientTick++;
        MiaExecutors.refreshThreadLimit();
        Minecraft minecraft = Minecraft.getInstance();
        refreshMaterialGeneration(minecraft);
        ClientState desiredState = desiredState(minecraft);
        transitionTo(desiredState);
        if (desiredState == ClientState.RUNNING) {
            if (clientCache == null)
                clientCache =
                        new CrossDimensionLodClientCache(
                                minecraft
                                        .gameDirectory
                                        .toPath()
                                        .resolve("cache/mementoinabyss/lod-v1"),
                                MiaLodStateNames::name,
                                MiaLodStateNames::resolve);
            // Normally serviced once per rendered frame; keep ACK/timeouts alive when rendering is
            // suspended.
            if (System.nanoTime() - lastReceiveNanos >= 50_000_000L) drainPayloads();
            drainMaterialRequests();
        } else {
            clearPendingPayloads();
        }
        if (minecraft.getConnection() == null) {
            reportedServerEnabled = null;
            return;
        }
        boolean serverEnabled = desiredState == ClientState.RUNNING;
        if (reportedServerEnabled == null || reportedServerEnabled != serverEnabled) {
            ClientPacketDistributor.sendToServer(
                    new CrossDimensionLodControlPayload(serverEnabled));
            reportedServerEnabled = serverEnabled;
        }
    }

    private static void applyColumn(CrossDimensionLodColumn payload) {
        serverViewRadius = payload.radius();
        long key = CrossDimensionLodKey.pack(payload.chunkX(), payload.chunkZ());
        // Revisions belong to individual sections; the assembled mesh input has no column-wide
        // revision gate that could discard a second section from the same capture.
        DATA.put(key, payload);
        // The server owns the authoritative 1/4/16 selection. Never build a speculative
        // client mip or height field on packet or render paths.
        SELECTED_DATA.put(key, payload);
        markDirty(payload.chunkX(), payload.chunkZ());
    }

    private static void markDirty(int chunkX, int chunkZ) {
        long key = CrossDimensionLodKey.pack(chunkX, chunkZ);
        MESH_FAILURES.remove(key);
        MESH_RETRY_AFTER.remove(key);
        MESH_REVISIONS.put(key, NEXT_MESH_REVISION.incrementAndGet());
        if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
    }

    private static void updateMeshes() {
        drainMaterialRequests();
        drainCompletedMeshes();
        scheduleDirtyMeshes();
    }

    private static void scheduleDirtyMeshes() {
        List<RankedKey> candidates = new ArrayList<>();
        int polls = Math.min(MAX_DIRTY_POLLS_PER_FRAME, DIRTY_CHUNK_QUEUE.size());
        for (int i = 0; i < polls; i++) {
            Long key = DIRTY_CHUNK_QUEUE.poll();
            if (key != null && DIRTY_CHUNKS.contains(key))
                candidates.add(new RankedKey(key, chunkPriority(key)));
        }
        candidates.sort(Comparator.comparingDouble(RankedKey::priority));
        int scheduled = 0;
        for (var candidate : candidates) {
            long key = candidate.key;
            if (scheduled >= MESH_SCHEDULES_PER_FRAME
                    || IN_FLIGHT_MESHES.size() >= MAX_IN_FLIGHT_MESHES) {
                DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            if (!DIRTY_CHUNKS.remove(key)) continue;
            if (IN_FLIGHT_MESHES.containsKey(key)) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            Long retryAfter = MESH_RETRY_AFTER.get(key);
            if (retryAfter != null && renderFrame < retryAfter) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            CrossDimensionLodColumn payload = DATA.get(key);
            CrossDimensionLodColumn selectedPayload = SELECTED_DATA.getOrDefault(key, payload);
            Long revision = MESH_REVISIONS.get(key);
            if (payload == null || selectedPayload == null || revision == null) continue;
            if (!preparePayloadMaterials(selectedPayload)) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            if (IN_FLIGHT_MESHES.putIfAbsent(key, revision) != null) {
                if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
                continue;
            }
            scheduled++;
            RegionalSkyLight.Region skyExposureRegion = irisSkyExposureRegion();
            boolean irisFormat = Boolean.TRUE.equals(packedIrisFormat);
            long workEpoch = WORK_EPOCH.get();
            try {
                MiaExecutors.execute(
                        MiaExecutors.Priority.LOD_MESH,
                        () -> {
                            if (workEpoch != WORK_EPOCH.get() || !lodEnabled()) return;
                            long workStarted = System.nanoTime();
                            try {
                                CpuMesh mesh =
                                        CrossDimensionLodMesher.build(selectedPayload, !irisFormat);
                                PackedChunk packed = packMesh(mesh, skyExposureRegion, irisFormat);
                                recordMeshWork(System.nanoTime() - workStarted);
                                if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                                    COMPLETED_MESHES.add(
                                            new MeshBuildResult(
                                                    key,
                                                    revision,
                                                    workEpoch,
                                                    payload,
                                                    selectedPayload,
                                                    packed,
                                                    null));
                                }
                            } catch (Throwable throwable) {
                                recordMeshWork(System.nanoTime() - workStarted);
                                if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                                    COMPLETED_MESHES.add(
                                            new MeshBuildResult(
                                                    key,
                                                    revision,
                                                    workEpoch,
                                                    payload,
                                                    selectedPayload,
                                                    null,
                                                    throwable));
                                }
                            }
                        });
            } catch (RuntimeException exception) {
                IN_FLIGHT_MESHES.remove(key, revision);
                MementoInAbyss.LOGGER.error(
                        "Failed to schedule cross-dimension LOD mesh [{},{}]",
                        CrossDimensionLodKey.x(key),
                        CrossDimensionLodKey.z(key),
                        exception);
                retryMesh(key, revision);
            }
        }
    }

    private static void recordMeshWork(long nanos) {
        LAST_MESH_WORK_NANOS.set(nanos);
        PEAK_MESH_WORK_NANOS.accumulateAndGet(nanos, Math::max);
    }

    private static void recordPageWork(long nanos) {
        LAST_PAGE_WORK_NANOS.set(nanos);
        PEAK_PAGE_WORK_NANOS.accumulateAndGet(nanos, Math::max);
    }

    private static void retryMesh(long key, long revision) {
        if (!DATA.containsKey(key) || !Long.valueOf(revision).equals(MESH_REVISIONS.get(key)))
            return;
        int failures = MESH_FAILURES.merge(key, 1, Integer::sum);
        long backoff = Math.min(60L, 1L << Math.min(failures - 1, 5));
        MESH_RETRY_AFTER.put(key, renderFrame + backoff);
        if (DIRTY_CHUNKS.add(key)) DIRTY_CHUNK_QUEUE.add(key);
    }

    private static void drainCompletedMeshes() {
        long started = System.nanoTime();
        int processed = 0;
        while (processed < MESH_RESULTS_PER_FRAME
                && (processed == 0
                        || System.nanoTime() - started < MESH_RESULT_DRAIN_BUDGET_NANOS)) {
            MeshBuildResult result = COMPLETED_MESHES.poll();
            if (result == null) break;
            processed++;
            IN_FLIGHT_MESHES.remove(result.key, result.revision);
            if (result.epoch != WORK_EPOCH.get()) continue;
            if (result.failure != null) {
                MementoInAbyss.LOGGER.error(
                        "Failed to build cross-dimension LOD mesh [{},{}]",
                        CrossDimensionLodKey.x(result.key),
                        CrossDimensionLodKey.z(result.key),
                        result.failure);
                if (DATA.containsKey(result.key)
                        && Long.valueOf(result.revision).equals(MESH_REVISIONS.get(result.key))) {
                    retryMesh(result.key, result.revision);
                }
                continue;
            }
            // Interiors and boundary samples must describe the same immutable source column.
            if (DATA.get(result.key) != result.sourcePayload
                    || SELECTED_DATA.get(result.key) != result.selectedPayload) continue;
            PackedChunk packed = result.packed;
            MESH_FAILURES.remove(result.key);
            MESH_RETRY_AFTER.remove(result.key);
            PackedChunk previous = PACKED_CHUNKS.put(result.key, packed);
            markPageDirty(
                    pageKey(packed.chunkX, packed.chunkZ),
                    previous == null || previous.cellSize != packed.cellSize);
        }
    }

    private static void clearResources() {
        cameraView = null;
        reportedView = null;
        reportedViewLink = null;
        reportedViewRadius = 0;
        lastViewTick = Long.MIN_VALUE / 2;
        clearPendingPayloads();
        resetStreamData();
        VISIBLE_PAGES.clear();
        VISIBLE_PAGE_TRANSITIONS.clear();
        FACE_SPRITES.clear();
        MATERIAL_PENDING.clear();
        MATERIAL_QUEUE.clear();
        packedIrisFormat = null;
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
        materialModelSet = null;
    }

    public static void disconnect() {
        reportedServerEnabled = null;
        transitionTo(ClientState.DISCONNECTED);
    }

    private static void refreshMaterialGeneration(Minecraft minecraft) {
        Object currentModelSet = minecraft.getModelManager().getBlockStateModelSet();
        if (materialModelSet == null) {
            materialModelSet = currentModelSet;
        } else if (materialModelSet != currentModelSet) {
            materialModelSet = currentModelSet;
            onResourceReload();
        }
    }

    /** Called on the client thread after block models and the block atlas have reloaded. */
    public static void onResourceReload() {
        FACE_SPRITES.clear();
        MATERIAL_PENDING.clear();
        MATERIAL_QUEUE.clear();
        invalidateMeshWork();
        PACKED_CHUNKS.clear();
        closeMeshes();
        VISIBLE_PAGES.clear();
        VISIBLE_PAGE_TRANSITIONS.clear();
        VISIBLE_SPRITES.clear();
        DIRTY_CHUNKS.clear();
        DIRTY_CHUNK_QUEUE.clear();
        MESH_REVISIONS.clear();
        MESH_FAILURES.clear();
        MESH_RETRY_AFTER.clear();
        DIRTY_PAGES.clear();
        PAGE_REVISIONS.clear();
        for (CrossDimensionLodColumn payload : DATA.values()) {
            markDirty(payload.chunkX(), payload.chunkZ());
        }
    }

    private static void resetStreamData() {
        invalidateMeshWork();
        if (clientCache != null) clientCache.clearSession();
        SECTION_STORE.clear();
        DATA.clear();
        SELECTED_DATA.clear();
        PACKED_CHUNKS.clear();
        closeMeshes();
        DIRTY_CHUNKS.clear();
        DIRTY_CHUNK_QUEUE.clear();
        MESH_REVISIONS.clear();
        MESH_FAILURES.clear();
        MESH_RETRY_AFTER.clear();
        DIRTY_PAGES.clear();
        PAGE_REVISIONS.clear();
    }

    private static ClientState desiredState(Minecraft minecraft) {
        if (minecraft.getConnection() == null) return ClientState.DISCONNECTED;
        if (!lodEnabled()
                || minecraft.level == null
                || CrossDimensionLodLinks.forTarget(minecraft.level.dimension()).isEmpty()) {
            return ClientState.DISABLED;
        }
        return ClientState.RUNNING;
    }

    private static void transitionTo(ClientState nextState) {
        if (clientState == nextState) return;
        clientState = nextState;
        if (nextState != ClientState.RUNNING) clearResources();
    }

    private static void ensurePackedFormat(boolean irisFormat) {
        if (packedIrisFormat != null && packedIrisFormat == irisFormat) return;
        packedIrisFormat = irisFormat;
        invalidateMeshWork();
        PACKED_CHUNKS.clear();
        closeMeshes();
        DIRTY_CHUNKS.clear();
        DIRTY_CHUNK_QUEUE.clear();
        MESH_REVISIONS.clear();
        MESH_FAILURES.clear();
        MESH_RETRY_AFTER.clear();
        DIRTY_PAGES.clear();
        PAGE_REVISIONS.clear();
        for (CrossDimensionLodColumn payload : DATA.values()) {
            markDirty(payload.chunkX(), payload.chunkZ());
        }
    }

    private static void invalidateMeshWork() {
        WORK_EPOCH.incrementAndGet();
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_MESH);
        MiaExecutors.discardQueuedTasks(MiaExecutors.Priority.LOD_PAGE);
        IN_FLIGHT_MESHES.clear();
        COMPLETED_MESHES.clear();
        IN_FLIGHT_PAGES.clear();
        COMPLETED_PAGES.clear();
    }

    private static void closeMeshes() {
        for (PageMesh page : PAGES.values()) page.close();
        PAGES.clear();
        for (EdgeMesh edge : PAGE_EDGES.values()) edge.close();
        PAGE_EDGES.clear();
        PUBLISHED_SIDES.clear();
        VISIBLE_EDGES.clear();
        for (PageTransition transition : PAGE_TRANSITIONS.values()) transition.close();
        PAGE_TRANSITIONS.clear();
        while (!RETIRED_MESHES.isEmpty()) RETIRED_MESHES.removeFirst().resource.close();
    }

    /** Draws persistent CPU-batched pages. */
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
        updateCameraView(event, activeLink.id().toString());
        drainPayloads();

        renderFrame++;
        boolean irisShaders = IrisRenderCompat.isShaderPackInUse();
        ensurePackedFormat(irisShaders);
        int viewRadius =
                serverViewRadius > 0
                        ? serverViewRadius
                        : MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get()
                                * 16;
        if (renderFrame % EVICTION_INTERVAL_FRAMES == 0)
            evictFarChunks(
                    camera,
                    viewRadius,
                    activeLink.sourceHeight().minY() + activeLink.displayYOffset(),
                    activeLink.sourceHeight().maxY() + activeLink.displayYOffset());
        long meshStarted = System.nanoTime();
        closeRetiredMeshes();
        updateMeshes();
        long meshNanos = System.nanoTime() - meshStarted;
        long pageStarted = System.nanoTime();
        finishPageTransitions(pageStarted);
        updatePages();
        long transitionTime = System.nanoTime();
        long pageNanos = System.nanoTime() - pageStarted;
        long visibilityStarted = System.nanoTime();
        var frustum = event.getLevelRenderState().cameraRenderState.cullFrustum;
        VISIBLE_EDGES.clear();
        VISIBLE_PAGES.clear();
        VISIBLE_PAGE_TRANSITIONS.clear();
        VISIBLE_SPRITES.clear();
        int maximumIndexCount = 0;
        boolean sodiumLoaded = MiaMods.SODIUM.isLoaded();
        for (PageMesh page : PAGES.values()) {
            int terrainCount = irisShaders ? page.irisIndexCount : page.indexCount;
            int seamCount = irisShaders ? page.irisSeamIndexCount : page.seamIndexCount;
            maximumIndexCount = Math.max(maximumIndexCount, terrainCount);
            maximumIndexCount = Math.max(maximumIndexCount, seamCount);
            if ((terrainCount > 0 || seamCount > 0)
                    && isWithinViewDistance(page.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(page.bounds))) {
                VISIBLE_PAGES.add(page);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(page.sprites);
            }
        }
        for (PageTransition transition : PAGE_TRANSITIONS.values()) {
            PageMesh page = transition.oldMesh;
            boolean visible = false;
            if (page != null) {
                maximumIndexCount = Math.max(maximumIndexCount, page.indexCount);
                maximumIndexCount = Math.max(maximumIndexCount, page.seamIndexCount);
                visible =
                        hasNativeGeometry(page)
                                && isWithinViewDistance(page.bounds, camera, viewRadius)
                                && (frustum == null || frustum.isVisible(page.bounds));
            }
            for (EdgeMesh edge : transition.oldEdges) {
                maximumIndexCount = Math.max(maximumIndexCount, edge.indexCount);
                visible |=
                        isWithinViewDistance(edge.bounds, camera, viewRadius)
                                && (frustum == null || frustum.isVisible(edge.bounds));
            }
            // A shared edge may remain visible even when the outgoing page body is empty or off
            // screen.
            if (visible) {
                VISIBLE_PAGE_TRANSITIONS.add(transition);
                if (sodiumLoaded) {
                    if (page != null) VISIBLE_SPRITES.addAll(page.sprites);
                    for (EdgeMesh edge : transition.oldEdges) VISIBLE_SPRITES.addAll(edge.sprites);
                }
            }
        }
        for (EdgeMesh edge : PAGE_EDGES.values()) {
            if (isWithinViewDistance(edge.bounds, camera, viewRadius)
                    && (frustum == null || frustum.isVisible(edge.bounds))) {
                VISIBLE_EDGES.add(edge);
                maximumIndexCount = Math.max(maximumIndexCount, edge.indexCount);
                if (sodiumLoaded) VISIBLE_SPRITES.addAll(edge.sprites);
            }
        }
        long visibilityNanos = System.nanoTime() - visibilityStarted;
        long receiveNanos = PENDING_RECEIVE_NANOS.getAndSet(0L);
        if (VISIBLE_PAGES.isEmpty()
                && VISIBLE_PAGE_TRANSITIONS.isEmpty()
                && VISIBLE_EDGES.isEmpty()) {
            recordTiming(callbackStarted, receiveNanos, meshNanos, pageNanos, visibilityNanos, 0L);
            return;
        }
        long drawStarted = System.nanoTime();
        if (sodiumLoaded) {
            for (TextureAtlasSprite sprite : VISIBLE_SPRITES) {
                SodiumLodCompat.markSpriteActive(sprite);
            }
        }

        var pipeline =
                irisShaders
                        ? CrossDimensionLodRenderTypes.irisBlocksPipeline()
                        : CrossDimensionLodRenderTypes.tiledBlocksPipeline();
        RenderTarget target = minecraft.getMainRenderTarget();
        var indexStorage = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        GpuBuffer indices = indexStorage.getBuffer(maximumIndexCount);
        Matrix4f modelView =
                new Matrix4f(event.getModelViewMatrix())
                        .translate((float) -camera.x, (float) -camera.y, (float) -camera.z);
        GpuBufferSlice transforms =
                RenderSystem.getDynamicUniforms()
                        .writeTransform(
                                modelView, new Vector4f(1.0F), new Vector3f(), new Matrix4f());
        Map<Integer, GpuBufferSlice> fadeTransforms = new HashMap<>();
        fadeTransforms.put(FADE_STEPS, transforms);
        Map<GpuResource, Integer> incomingSteps = new IdentityHashMap<>();
        for (PageTransition transition : PAGE_TRANSITIONS.values()) {
            int incoming = transitionFadeStep(transition.startNanos, transitionTime, true);
            int outgoing = transitionFadeStep(transition.startNanos, transitionTime, false);
            if (transition.newMesh != null) incomingSteps.put(transition.newMesh, incoming);
            for (EdgeMesh edge : transition.newEdges) incomingSteps.put(edge, incoming);
            prepareFadeTransform(fadeTransforms, modelView, incoming);
            prepareFadeTransform(fadeTransforms, modelView, outgoing);
        }
        var atlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        GpuBufferSlice lodFog = lodFog(viewRadius);
        GpuBufferSlice lodLight = lodLight(activeLink.source());

        try (RenderPass pass =
                RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(
                                () -> "Cross-dimension persistent LOD",
                                target.getColorTextureView(),
                                java.util.OptionalInt.empty(),
                                target.getDepthTextureView(),
                                java.util.OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            if (!irisShaders) {
                pass.setUniform("LodFog", lodFog);
                pass.setUniform("LodLight", lodLight);
            }
            pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
            pass.setIndexBuffer(indices, indexStorage.type());
            // RenderPass.drawMultipleIndexed is the Blaze3D-supported batching boundary.
            // It still emits ordinary indexed draws on the current backend, but keeps the
            // draw description independent of OpenGL. This is also the natural replacement
            // point if a future Blaze3D backend exposes indirect or multi-draw commands.
            ArrayList<RenderPass.Draw<GpuBufferSlice>> draws =
                    new ArrayList<>(VISIBLE_PAGES.size() + VISIBLE_PAGE_TRANSITIONS.size());
            // Draw each complete old snapshot before the incoming geometry, including its detached
            // shared edges.
            // At least one snapshot stays opaque: different silhouettes cannot use complementary
            // masks.
            for (PageTransition transition : VISIBLE_PAGE_TRANSITIONS) {
                GpuBufferSlice oldTransforms =
                        fadeTransforms.get(
                                transitionFadeStep(transition.startNanos, transitionTime, false));
                PageMesh page = transition.oldMesh;
                if (page != null) {
                    addDraw(draws, page.vertexBuffer, page.indexCount, oldTransforms);
                    addDraw(draws, page.seamVertexBuffer, page.seamIndexCount, oldTransforms);
                }
                for (EdgeMesh edge : transition.oldEdges)
                    addDraw(draws, edge.buffer, edge.indexCount, oldTransforms);
            }
            for (PageMesh page : VISIBLE_PAGES) {
                GpuBufferSlice pageTransforms =
                        fadeTransforms.get(incomingSteps.getOrDefault(page, FADE_STEPS));
                addDraw(
                        draws,
                        irisShaders ? page.irisVertexBuffer : page.vertexBuffer,
                        irisShaders ? page.irisIndexCount : page.indexCount,
                        pageTransforms);
                addDraw(
                        draws,
                        irisShaders ? page.irisSeamVertexBuffer : page.seamVertexBuffer,
                        irisShaders ? page.irisSeamIndexCount : page.seamIndexCount,
                        pageTransforms);
            }
            for (EdgeMesh edge : VISIBLE_EDGES) {
                addDraw(
                        draws,
                        edge.buffer,
                        edge.indexCount,
                        fadeTransforms.get(incomingSteps.getOrDefault(edge, FADE_STEPS)));
            }
            pass.drawMultipleIndexed(
                    draws,
                    indices,
                    indexStorage.type(),
                    java.util.List.of("DynamicTransforms"),
                    null);
        }
        recordTiming(
                callbackStarted,
                receiveNanos,
                meshNanos,
                pageNanos,
                visibilityNanos,
                System.nanoTime() - drawStarted);
    }

    private static void updateCameraView(
            RenderLevelStageEvent.AfterOpaqueBlocks event, String linkId) {
        var camera = event.getLevelRenderState().cameraRenderState;
        if (camera.projectionMatrix == null || camera.viewRotationMatrix == null) return;
        Matrix4f clip = new Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix);
        var planes = new ArrayList<MiaLodView.Plane>(4);
        try {
            for (int plane :
                    new int[] {
                        Matrix4f.PLANE_NX, Matrix4f.PLANE_PX, Matrix4f.PLANE_NY, Matrix4f.PLANE_PY
                    }) {
                Vector4f value = clip.frustumPlane(plane, new Vector4f());
                planes.add(new MiaLodView.Plane(value.x, value.y, value.z, value.w));
            }
            double scale =
                    Math.clamp(
                            Math.abs(camera.projectionMatrix.m11())
                                    * Minecraft.getInstance().getWindow().getHeight()
                                    * .5,
                            1,
                            16384);
            cameraView = new MiaLodView(camera.pos.x, camera.pos.y, camera.pos.z, scale, planes);
        } catch (IllegalArgumentException invalidProjection) {
            return; // An invalid transient camera matrix must never enter the network selection
            // state.
        }
        if (clientTick - lastViewTick < 2) return;
        int radius = MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
        if (!linkId.equals(reportedViewLink)
                || radius != reportedViewRadius
                || cameraView.materiallyDifferent(reportedView)
                || clientTick - lastViewTick >= 20) {
            ClientPacketDistributor.sendToServer(
                    new CrossDimensionLodViewPayload(linkId, ++viewSequence, radius, cameraView));
            reportedViewRadius = radius;
            reportedView = cameraView;
            reportedViewLink = linkId;
            lastViewTick = clientTick;
        }
    }

    private static double chunkPriority(long key) {
        var column = SELECTED_DATA.get(key);
        if (cameraView == null || column == null) return Double.MAX_VALUE;
        double x = column.chunkX() * 16.0, z = column.chunkZ() * 16.0;
        double y = column.minY() + column.displayYOffset();
        return cameraView.priority(
                x, y, z, x + 16, y + column.yCells() * column.cellSize(), z + 16);
    }

    private static double pagePriority(long key) {
        if (cameraView == null) return Double.MAX_VALUE;
        int x = CrossDimensionLodKey.x(key) * PAGE_CHUNKS,
                z = CrossDimensionLodKey.z(key) * PAGE_CHUNKS;
        double priority = Double.MAX_VALUE;
        for (int dx = 0; dx < PAGE_CHUNKS; dx++)
            for (int dz = 0; dz < PAGE_CHUNKS; dz++) {
                priority =
                        Math.min(
                                priority, chunkPriority(CrossDimensionLodKey.pack(x + dx, z + dz)));
            }
        return priority;
    }

    private static void addDraw(
            List<RenderPass.Draw<GpuBufferSlice>> draws,
            GpuBuffer vertexBuffer,
            int indexCount,
            GpuBufferSlice transforms) {
        if (vertexBuffer == null || indexCount <= 0 || transforms == null) return;
        draws.add(
                new RenderPass.Draw<>(
                        0,
                        vertexBuffer,
                        null,
                        null,
                        0,
                        indexCount,
                        0,
                        (ignored, uploader) -> uploader.upload("DynamicTransforms", transforms)));
    }

    private static void recordTiming(
            long callbackStarted,
            long receiveNanos,
            long meshNanos,
            long pageNanos,
            long visibilityNanos,
            long drawNanos) {
        long callbackNanos = System.nanoTime() - callbackStarted;
        FrameTiming sample =
                new FrameTiming(
                        renderFrame,
                        callbackNanos,
                        receiveNanos,
                        meshNanos,
                        pageNanos,
                        visibilityNanos,
                        drawNanos);
        lastTiming = sample;
        windowPeak = windowPeak.max(sample);
        if (sample.totalNanos >= SLOW_LOD_FRAME_NANOS) lastSpike = sample;
        else if (renderFrame - lastSpike.frame() > SPIKE_LIFETIME_FRAMES)
            lastSpike = FrameTiming.EMPTY;
        if (renderFrame % 60 == 0) {
            peakTiming = windowPeak;
            windowPeak = FrameTiming.EMPTY;
        }
    }

    private static int transitionFadeStep(long startNanos, long now, boolean incoming) {
        float progress = Math.clamp((now - startNanos) / (float) FADE_DURATION_NANOS, 0.0F, 1.0F);
        // First reveal the new scene over an opaque old scene, then dissolve the old over the
        // opaque new scene.
        float opacity = Math.clamp(incoming ? progress * 2.0F : 2.0F - progress * 2.0F, 0.0F, 1.0F);
        opacity = opacity * opacity * opacity * (opacity * (opacity * 6.0F - 15.0F) + 10.0F);
        return Math.clamp(Math.round(opacity * FADE_STEPS), 0, FADE_STEPS);
    }

    private static void prepareFadeTransform(
            Map<Integer, GpuBufferSlice> transforms, Matrix4f modelView, int step) {
        // A zero-opacity draw is omitted; all uniform writes happen before opening the render pass.
        if (step == 0) return;
        transforms.computeIfAbsent(
                step,
                value ->
                        RenderSystem.getDynamicUniforms()
                                .writeTransform(
                                        modelView,
                                        new Vector4f(1.0F, 1.0F, 1.0F, value / (float) FADE_STEPS),
                                        new Vector3f(),
                                        new Matrix4f()));
    }

    private static void retire(GpuResource resource) {
        RETIRED_MESHES.addLast(
                new RetiredResource(resource, renderFrame + GPU_RETIRE_DELAY_FRAMES));
    }

    private static void closeRetiredMeshes() {
        while (!RETIRED_MESHES.isEmpty()
                && RETIRED_MESHES.peekFirst().closeAfterFrame <= renderFrame) {
            RETIRED_MESHES.removeFirst().resource.close();
        }
    }

    private static long pageKey(int chunkX, int chunkZ) {
        return CrossDimensionLodKey.pack(
                Math.floorDiv(chunkX, PAGE_CHUNKS), Math.floorDiv(chunkZ, PAGE_CHUNKS));
    }

    private static void markPageDirty(long pageKey, boolean changedLevel) {
        long revision = NEXT_MESH_REVISION.incrementAndGet();
        PAGE_REVISIONS.put(pageKey, revision);
        // Coalesce neighbour repairs, but first coverage/LOD switches need not wait four more
        // frames.
        long eligibleSince = changedLevel ? renderFrame - PAGE_BUILD_DEBOUNCE_FRAMES : renderFrame;
        DIRTY_PAGES.merge(pageKey, eligibleSince, Math::min);
    }

    private static void updatePages() {
        drainCompletedPages();
        if (IN_FLIGHT_PAGES.size() >= MAX_IN_FLIGHT_PAGES) return;
        var candidates = new ArrayList<RankedKey>();
        for (long key : DIRTY_PAGES.keySet()) candidates.add(new RankedKey(key, pagePriority(key)));
        candidates.sort(Comparator.comparingDouble(RankedKey::priority));
        for (var candidate : candidates) {
            long key = candidate.key;
            if (IN_FLIGHT_PAGES.size() >= MAX_IN_FLIGHT_PAGES) break;
            // Include diagonal pages for AO, and retain the reservation through the visual
            // transition.
            if (pageOrNeighbourBusy(key)) continue;
            if (PAGES.containsKey(key)
                    && renderFrame - DIRTY_PAGES.get(key) < PAGE_BUILD_DEBOUNCE_FRAMES) continue;
            Long revision = PAGE_REVISIONS.get(key);
            if (revision == null || IN_FLIGHT_PAGES.putIfAbsent(key, revision) != null) continue;
            DIRTY_PAGES.remove(key);
            PackedChunk[] chunks = pageChunks(key);
            PageMesh current = PAGES.get(key);
            Side[][] previousSides = current == null ? new Side[4][PAGE_CHUNKS] : current.sides;
            Side[][] neighbours = new Side[4][];
            for (int side = 0; side < 4; side++) {
                PageMesh neighbour = PAGES.get(neighbourPage(key, side));
                neighbours[side] =
                        neighbour == null ? new Side[PAGE_CHUNKS] : neighbour.sides[side ^ 1];
            }
            Side[][][] edgeContexts = new Side[4][][];
            for (int side = 0; side < 4; side++) {
                EdgeMesh edge = PAGE_EDGES.get(edgeKey(key, side));
                edgeContexts[side] = edge == null ? null : edge.aoContext;
            }
            boolean irisFormat = Boolean.TRUE.equals(packedIrisFormat);
            View ao = irisFormat ? null : snapshotAo(key);
            RegionalSkyLight.Region skyExposure = irisSkyExposureRegion();
            long workEpoch = WORK_EPOCH.get();
            try {
                MiaExecutors.execute(
                        MiaExecutors.Priority.LOD_PAGE,
                        () -> {
                            if (workEpoch != WORK_EPOCH.get() || !lodEnabled()) return;
                            long workStarted = System.nanoTime();
                            try {
                                PageData data =
                                        buildPageData(
                                                key,
                                                chunks,
                                                previousSides,
                                                neighbours,
                                                edgeContexts,
                                                ao,
                                                irisFormat,
                                                skyExposure);
                                recordPageWork(System.nanoTime() - workStarted);
                                if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                                    COMPLETED_PAGES.add(
                                            new PageBuildResult(
                                                    key, revision, workEpoch, data, null));
                                }
                            } catch (Throwable throwable) {
                                recordPageWork(System.nanoTime() - workStarted);
                                if (workEpoch == WORK_EPOCH.get() && lodEnabled()) {
                                    COMPLETED_PAGES.add(
                                            new PageBuildResult(
                                                    key, revision, workEpoch, null, throwable));
                                }
                            }
                        });
            } catch (RuntimeException exception) {
                IN_FLIGHT_PAGES.remove(key, revision);
                MementoInAbyss.LOGGER.error(
                        "Failed to schedule cross-dimension LOD page [{},{}]",
                        CrossDimensionLodKey.x(key),
                        CrossDimensionLodKey.z(key),
                        exception);
                retryPage(key, revision);
            }
        }
    }

    private static void drainCompletedPages() {
        long started = System.nanoTime(), uploadedBytes = 0;
        int uploaded = 0;
        List<PageBuildResult> ready = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            var result = COMPLETED_PAGES.poll();
            if (result == null) break;
            ready.add(result);
        }
        ready.sort(Comparator.comparingDouble(result -> pagePriority(result.key)));
        for (PageBuildResult result : ready) {
            long size = result.data == null ? 0 : result.data.byteSize();
            if (uploaded >= PAGE_UPLOADS_PER_FRAME
                    || uploaded > 0
                            && (System.nanoTime() - started >= PAGE_UPLOAD_BUDGET_NANOS
                                    || uploadedBytes + size > PAGE_UPLOAD_BYTES_PER_FRAME)) {
                COMPLETED_PAGES.add(result);
                continue;
            }
            IN_FLIGHT_PAGES.remove(result.key, result.revision);
            if (result.epoch != WORK_EPOCH.get() || !PAGE_REVISIONS.containsKey(result.key))
                continue;
            if (result.failure != null) {
                MementoInAbyss.LOGGER.error(
                        "Failed to build cross-dimension LOD page [{},{}]",
                        CrossDimensionLodKey.x(result.key),
                        CrossDimensionLodKey.z(result.key),
                        result.failure);
                retryPage(result.key, result.revision);
                continue;
            }
            // Publish a useful frozen page even if more chunks arrived while it was built.
            // Those arrivals keep DIRTY_PAGES queued. An obsolete empty page must not erase newer
            // data.
            if (result.data.bounds == null
                    && !Long.valueOf(result.revision).equals(PAGE_REVISIONS.get(result.key)))
                continue;
            PageMesh previous = PAGES.get(result.key);
            PageMesh replacement = null;
            EdgeMesh[] edges = new EdgeMesh[4];
            try {
                if (result.data.bounds != null) replacement = uploadPage(result.data);
                for (int side = 0; side < 4; side++) {
                    EdgeData edge = result.data.edges[side];
                    if (edge != null && edge.geometry.indexCount > 0) {
                        GpuBuffer buffer =
                                uploadPackedBuffer(
                                        edge.geometry,
                                        "shared page edge",
                                        CrossDimensionLodKey.x(result.key),
                                        CrossDimensionLodKey.z(result.key));
                        edges[side] =
                                new EdgeMesh(
                                        buffer,
                                        edge.geometry.indexCount,
                                        edge.sprites,
                                        edge.bounds,
                                        edge.aoContext);
                    }
                }
            } catch (RuntimeException failure) {
                if (replacement != null) replacement.close();
                for (EdgeMesh edge : edges) if (edge != null) edge.close();
                MementoInAbyss.LOGGER.error(
                        "Failed to upload cross-dimension LOD page and edges [{},{}]",
                        CrossDimensionLodKey.x(result.key),
                        CrossDimensionLodKey.z(result.key),
                        failure);
                retryPage(result.key, result.revision);
                uploaded++;
                uploadedBytes += size;
                continue;
            }
            // One client-thread publication: neither a page nor either half of its shared edges is
            // visible early.
            List<EdgeMesh> oldEdges = new ArrayList<>(4), newEdges = new ArrayList<>(4);
            for (int side = 0; side < 4; side++) {
                if (result.data.edges[side] == null)
                    continue; // Unchanged edge supports both snapshots and stays opaque.
                EdgeKey edgeKey = edgeKey(result.key, side);
                EdgeMesh old =
                        edges[side] == null
                                ? PAGE_EDGES.remove(edgeKey)
                                : PAGE_EDGES.put(edgeKey, edges[side]);
                if (old != null) oldEdges.add(old);
                if (edges[side] != null) newEdges.add(edges[side]);
            }
            if (replacement == null) {
                PAGES.remove(result.key);
                PAGE_REVISIONS.remove(result.key, result.revision);
            } else {
                PAGES.put(result.key, replacement);
            }
            // AO repairs have identical source profiles: do not restart a visual transition or
            // reserve neighbours for them.
            if (!Boolean.TRUE.equals(packedIrisFormat)
                    && pageContentChanged(result.key, result.data.columns)
                    && (hasNativeGeometry(previous)
                            || hasNativeGeometry(replacement)
                            || !oldEdges.isEmpty()
                            || !newEdges.isEmpty())) {
                PAGE_TRANSITIONS.put(
                        result.key,
                        new PageTransition(
                                previous,
                                List.copyOf(oldEdges),
                                replacement,
                                List.copyOf(newEdges),
                                System.nanoTime()));
            } else {
                if (previous != null) retire(previous);
                for (EdgeMesh edge : oldEdges) retire(edge);
            }
            publishAoProfiles(result.key, result.data.columns);
            uploaded++;
            uploadedBytes += size;
        }
    }

    private static View snapshotAo(long page) {
        int x = CrossDimensionLodKey.x(page) * PAGE_CHUNKS - 1,
                z = CrossDimensionLodKey.z(page) * PAGE_CHUNKS - 1;
        int width = PAGE_CHUNKS + 2;
        Side[][] columns = new Side[width * width][];
        for (int dz = 0; dz < width; dz++)
            for (int dx = 0; dx < width; dx++) {
                columns[dz * width + dx] =
                        PUBLISHED_SIDES.get(CrossDimensionLodKey.pack(x + dx, z + dz));
            }
        return new View(x, z, width, columns);
    }

    private static void publishAoProfiles(long page, Side[][] columns) {
        int ox = CrossDimensionLodKey.x(page) * PAGE_CHUNKS,
                oz = CrossDimensionLodKey.z(page) * PAGE_CHUNKS;
        Set<Long> repairs = new HashSet<>();
        for (int z = 0; z < PAGE_CHUNKS; z++)
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                long key = CrossDimensionLodKey.pack(ox + x, oz + z);
                Side[] next = columns[z * PAGE_CHUNKS + x];
                Side[] old =
                        next == null ? PUBLISHED_SIDES.remove(key) : PUBLISHED_SIDES.put(key, next);
                if (Boolean.TRUE.equals(packedIrisFormat)
                        || old == next
                        || !castsAo(old) && !castsAo(next)) continue;
                for (int dz = -1; dz <= 1; dz++)
                    for (int dx = -1; dx <= 1; dx++) {
                        long neighbourPage = pageKey(ox + x + dx, oz + z + dz);
                        if (neighbourPage != page
                                && PAGES.containsKey(neighbourPage)
                                && castsAo(
                                        PUBLISHED_SIDES.get(
                                                CrossDimensionLodKey.pack(
                                                        ox + x + dx, oz + z + dz))))
                            repairs.add(neighbourPage);
                    }
            }
        // AO-only publications reuse the identical profiles and therefore cannot trigger a repair
        // loop.
        for (long repair : repairs) markPageDirty(repair, false);
    }

    private static boolean castsAo(Side[] sides) {
        return sides != null && sides[0].cellSize() <= 4;
    }

    private static Side[][] edgeAoContext(View view, int ox, int oz, int side) {
        if (view == null) return null;
        Side[][] context = new Side[(PAGE_CHUNKS + 2) * 2][];
        int plane =
                side < 2 ? ox + (side == 0 ? 0 : PAGE_CHUNKS) : oz + (side == 2 ? 0 : PAGE_CHUNKS);
        for (int u = -1; u <= PAGE_CHUNKS; u++)
            for (int n = 0; n < 2; n++) {
                context[(u + 1) * 2 + n] =
                        view.column(
                                side < 2 ? plane - 1 + n : ox + u,
                                side < 2 ? oz + u : plane - 1 + n);
            }
        return context;
    }

    private static boolean sameSide(Side[] first, Side[] second) {
        for (int u = 0; u < PAGE_CHUNKS; u++) if (first[u] != second[u]) return false;
        return true;
    }

    private static boolean hasNativeGeometry(PageMesh page) {
        return page != null && (page.indexCount > 0 || page.seamIndexCount > 0);
    }

    private static boolean pageContentChanged(long page, Side[][] columns) {
        int ox = CrossDimensionLodKey.x(page) * PAGE_CHUNKS,
                oz = CrossDimensionLodKey.z(page) * PAGE_CHUNKS;
        for (int z = 0; z < PAGE_CHUNKS; z++)
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                if (columns[z * PAGE_CHUNKS + x]
                        != PUBLISHED_SIDES.get(CrossDimensionLodKey.pack(ox + x, oz + z)))
                    return true;
            }
        return false;
    }

    private static long neighbourPage(long key, int side) {
        return CrossDimensionLodKey.pack(
                CrossDimensionLodKey.x(key) + (side == 0 ? -1 : side == 1 ? 1 : 0),
                CrossDimensionLodKey.z(key) + (side == 2 ? -1 : side == 3 ? 1 : 0));
    }

    private static EdgeKey edgeKey(long page, int side) {
        return new EdgeKey((side & 1) == 0 ? neighbourPage(page, side) : page, side < 2);
    }

    private static boolean pageOrNeighbourBusy(long key) {
        int x = CrossDimensionLodKey.x(key), z = CrossDimensionLodKey.z(key);
        for (int dz = -1; dz <= 1; dz++)
            for (int dx = -1; dx <= 1; dx++) {
                long neighbour = CrossDimensionLodKey.pack(x + dx, z + dz);
                if (IN_FLIGHT_PAGES.containsKey(neighbour)
                        || PAGE_TRANSITIONS.containsKey(neighbour)) return true;
            }
        return false;
    }

    private static void retryPage(long key, long revision) {
        if (Long.valueOf(revision).equals(PAGE_REVISIONS.get(key))) {
            DIRTY_PAGES.putIfAbsent(key, renderFrame);
        }
    }

    private static PackedChunk[] pageChunks(long key) {
        int originX = CrossDimensionLodKey.x(key) * PAGE_CHUNKS;
        int originZ = CrossDimensionLodKey.z(key) * PAGE_CHUNKS;
        ArrayList<PackedChunk> chunks = new ArrayList<>(PAGE_CHUNKS * PAGE_CHUNKS);
        for (int z = 0; z < PAGE_CHUNKS; z++) {
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                PackedChunk chunk =
                        PACKED_CHUNKS.get(CrossDimensionLodKey.pack(originX + x, originZ + z));
                if (chunk != null) chunks.add(chunk);
            }
        }
        return chunks.toArray(PackedChunk[]::new);
    }

    private static PageData buildPageData(
            long key,
            PackedChunk[] chunks,
            Side[][] previousSides,
            Side[][] neighbours,
            Side[][][] previousContexts,
            View ao,
            boolean irisFormat,
            RegionalSkyLight.Region skyExposure) {
        int terrainBytes = 0, irisTerrainBytes = 0, terrainIndices = 0, irisTerrainIndices = 0;
        int originX = CrossDimensionLodKey.x(key) * PAGE_CHUNKS,
                originZ = CrossDimensionLodKey.z(key) * PAGE_CHUNKS;
        PackedChunk[] grid = new PackedChunk[PAGE_CHUNKS * PAGE_CHUNKS];
        Side[][] columns = new Side[grid.length][];
        AABB bounds = null;
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        for (PackedChunk chunk : chunks) {
            grid[(chunk.chunkZ - originZ) * PAGE_CHUNKS + chunk.chunkX - originX] = chunk;
            terrainBytes = Math.addExact(terrainBytes, chunk.terrain.bytes.length);
            irisTerrainBytes = Math.addExact(irisTerrainBytes, chunk.irisTerrain.bytes.length);
            terrainIndices = Math.addExact(terrainIndices, chunk.terrain.indexCount);
            irisTerrainIndices = Math.addExact(irisTerrainIndices, chunk.irisTerrain.indexCount);
            bounds = bounds == null ? chunk.bounds : bounds.minmax(chunk.bounds);
            sprites.addAll(chunk.sprites);
        }
        byte[] terrain = new byte[terrainBytes], irisTerrain = new byte[irisTerrainBytes];
        int terrainOffset = 0, irisTerrainOffset = 0;
        for (PackedChunk chunk : chunks) {
            System.arraycopy(
                    chunk.terrain.bytes, 0, terrain, terrainOffset, chunk.terrain.bytes.length);
            System.arraycopy(
                    chunk.irisTerrain.bytes,
                    0,
                    irisTerrain,
                    irisTerrainOffset,
                    chunk.irisTerrain.bytes.length);
            terrainOffset += chunk.terrain.bytes.length;
            irisTerrainOffset += chunk.irisTerrain.bytes.length;
        }
        // The centre is the page we will publish; the halo is the geometry already displayed by
        // neighbours.
        for (int z = 0; z < PAGE_CHUNKS; z++)
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                PackedChunk chunk = grid[z * PAGE_CHUNKS + x];
                columns[z * PAGE_CHUNKS + x] = chunk == null ? null : chunk.sides;
                if (ao != null)
                    ao.columns[(z + 1) * ao.width + x + 1] = columns[z * PAGE_CHUNKS + x];
            }
        var builder = new BoundaryBuilder(ao);
        var internal = new QuadBuffer(128);
        Side[][] sides = new Side[4][PAGE_CHUNKS];
        for (int z = 0; z < PAGE_CHUNKS; z++)
            for (int x = 0; x < PAGE_CHUNKS; x++) {
                PackedChunk chunk = grid[z * PAGE_CHUNKS + x];
                if (chunk == null) continue;
                if (ao != null) CrossDimensionLodMesher.appendAoRim(internal, chunk.rimSource, ao);
                for (int side = 0; side < 4; side++) {
                    int nx = x + (side == 0 ? -1 : side == 1 ? 1 : 0);
                    int nz = z + (side == 2 ? -1 : side == 3 ? 1 : 0);
                    if (nx < 0 || nx >= PAGE_CHUNKS || nz < 0 || nz >= PAGE_CHUNKS) {
                        sides[side][side < 2 ? z : x] = chunk.sides[side];
                    } else {
                        PackedChunk neighbour = grid[nz * PAGE_CHUNKS + nx];
                        builder.append(
                                internal,
                                side,
                                chunk.chunkX,
                                chunk.chunkZ,
                                chunk.sides[side],
                                neighbour == null ? null : neighbour.sides[side ^ 1]);
                    }
                }
            }
        PackedBuffer seam = irisFormat ? PackedBuffer.EMPTY : packQuadBuffer(internal, sprites);
        PackedBuffer irisSeam =
                irisFormat
                        ? packIrisQuadBuffer(internal, skyExposure, sprites)
                        : PackedBuffer.EMPTY;
        EdgeData[] edges = new EdgeData[4];
        for (int side = 0; side < 4; side++) {
            Side[][] context = edgeAoContext(ao, originX, originZ, side);
            if (sameSide(previousSides[side], sides[side])
                    && (ao == null
                            || previousContexts[side] == null
                            || java.util.Arrays.equals(previousContexts[side], context))) continue;
            var quads = new QuadBuffer(32);
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (int u = 0; u < PAGE_CHUNKS; u++) {
                Side own = sides[side][u], other = neighbours[side][u];
                int x = originX + (side < 2 ? (side == 0 ? 0 : PAGE_CHUNKS - 1) : u);
                int z = originZ + (side < 2 ? u : (side == 2 ? 0 : PAGE_CHUNKS - 1));
                builder.append(quads, side, x, z, own, other);
                builder.append(
                        quads,
                        side ^ 1,
                        x + (side == 0 ? -1 : side == 1 ? 1 : 0),
                        z + (side == 2 ? -1 : side == 3 ? 1 : 0),
                        other,
                        own);
                if (own != null) {
                    minY = Math.min(minY, own.minY());
                    maxY = Math.max(maxY, own.maxY());
                }
                if (other != null) {
                    minY = Math.min(minY, other.minY());
                    maxY = Math.max(maxY, other.maxY());
                }
            }
            Set<TextureAtlasSprite> edgeSprites = new HashSet<>();
            PackedBuffer geometry =
                    irisFormat
                            ? packIrisQuadBuffer(quads, skyExposure, edgeSprites)
                            : packQuadBuffer(quads, edgeSprites);
            double x = originX * 16.0 + (side == 1 ? PAGE_CHUNKS * 16 : 0);
            double z = originZ * 16.0 + (side == 3 ? PAGE_CHUNKS * 16 : 0);
            AABB edgeBounds =
                    geometry.indexCount == 0
                            ? null
                            : new AABB(
                                    x - .01,
                                    minY,
                                    z - .01,
                                    x + (side < 2 ? .01 : PAGE_CHUNKS * 16),
                                    maxY,
                                    z + (side < 2 ? PAGE_CHUNKS * 16 : .01));
            edges[side] = new EdgeData(geometry, Set.copyOf(edgeSprites), edgeBounds, context);
        }
        return new PageData(
                key,
                new PackedBuffer(terrain, terrainIndices),
                seam,
                new PackedBuffer(irisTerrain, irisTerrainIndices),
                irisSeam,
                Set.copyOf(sprites),
                bounds,
                sides,
                columns,
                edges);
    }

    private static PageMesh uploadPage(PageData data) {
        int x = CrossDimensionLodKey.x(data.key);
        int z = CrossDimensionLodKey.z(data.key);
        GpuBuffer terrain = uploadPackedBuffer(data.terrain, "page terrain", x, z);
        try {
            GpuBuffer seam = uploadPackedBuffer(data.seam, "page seam", x, z);
            try {
                GpuBuffer irisTerrain =
                        uploadPackedBuffer(data.irisTerrain, "page Iris terrain", x, z);
                try {
                    GpuBuffer irisSeam = uploadPackedBuffer(data.irisSeam, "page Iris seam", x, z);
                    return new PageMesh(
                            terrain,
                            data.terrain.indexCount,
                            seam,
                            data.seam.indexCount,
                            irisTerrain,
                            data.irisTerrain.indexCount,
                            irisSeam,
                            data.irisSeam.indexCount,
                            data.sprites,
                            data.bounds,
                            data.sides);
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

    private static void finishPageTransitions(long now) {
        var iterator = PAGE_TRANSITIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (now - entry.getValue().startNanos < FADE_DURATION_NANOS) continue;
            retire(entry.getValue());
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
                lodFogBuffer =
                        RenderSystem.getDevice()
                                .createBuffer(
                                        () -> "Cross-dimension LOD fog",
                                        GpuBuffer.USAGE_UNIFORM,
                                        data.flip());
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
                lodLightBuffer =
                        RenderSystem.getDevice()
                                .createBuffer(
                                        () -> "Cross-dimension LOD regional light",
                                        GpuBuffer.USAGE_UNIFORM,
                                        data.flip());
            }
            lodLightSource = sourceDimension;
        }
        return lodLightBuffer.slice();
    }

    private static boolean isWithinViewDistance(AABB bounds, Vec3 camera, double radius) {
        double dx = Math.max(Math.max(bounds.minX - camera.x, 0), camera.x - bounds.maxX);
        double dy = Math.max(Math.max(bounds.minY - camera.y, 0), camera.y - bounds.maxY);
        double dz = Math.max(Math.max(bounds.minZ - camera.z, 0), camera.z - bounds.maxZ);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    private static void evictFarChunks(Vec3 camera, int viewRadius, int minY, int maxY) {
        double retentionRadius = viewRadius + EVICTION_MARGIN_CHUNKS * 16.0;
        SECTION_STORE.retain(
                key ->
                        isWithinViewDistance(
                                new AABB(
                                        CrossDimensionLodKey.x(key) * 16.0,
                                        minY,
                                        CrossDimensionLodKey.z(key) * 16.0,
                                        CrossDimensionLodKey.x(key) * 16.0 + 16,
                                        maxY,
                                        CrossDimensionLodKey.z(key) * 16.0 + 16),
                                camera,
                                retentionRadius));
        for (Map.Entry<Long, CrossDimensionLodColumn> entry : DATA.entrySet()) {
            long key = entry.getKey();
            var column = entry.getValue();
            double x = column.chunkX() * 16.0,
                    z = column.chunkZ() * 16.0,
                    y = column.minY() + column.displayYOffset();
            if (isWithinViewDistance(
                            new AABB(
                                    x,
                                    y,
                                    z,
                                    x + 16,
                                    y + column.yCells() * column.cellSize(),
                                    z + 16),
                            camera,
                            retentionRadius)
                    || !DATA.remove(key, column)) continue;

            int chunkX = CrossDimensionLodKey.x(key);
            int chunkZ = CrossDimensionLodKey.z(key);
            SECTION_STORE.evict(key);
            SELECTED_DATA.remove(key);
            PACKED_CHUNKS.remove(key);
            DIRTY_CHUNKS.remove(key);
            MESH_REVISIONS.remove(key);
            IN_FLIGHT_MESHES.remove(key);
            MESH_FAILURES.remove(key);
            MESH_RETRY_AFTER.remove(key);
            markPageDirty(pageKey(chunkX, chunkZ), false);
        }
    }

    private static PackedChunk packMesh(
            CpuMesh cpuMesh, RegionalSkyLight.Region irisSkyExposureRegion, boolean irisFormat) {
        Set<TextureAtlasSprite> sprites = new HashSet<>();
        PackedBuffer terrain =
                irisFormat ? PackedBuffer.EMPTY : packQuadBuffer(cpuMesh.quads, sprites);
        PackedBuffer irisTerrain =
                irisFormat
                        ? packIrisQuadBuffer(cpuMesh.quads, irisSkyExposureRegion, sprites)
                        : PackedBuffer.EMPTY;
        return new PackedChunk(
                cpuMesh.chunkX,
                cpuMesh.chunkZ,
                cpuMesh.cellSize,
                terrain,
                irisTerrain,
                Set.copyOf(sprites),
                cpuMesh.bounds,
                cpuMesh.sides,
                cpuMesh.rimSource);
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
        int vertexBytes =
                Math.multiplyExact(
                        vertexCount,
                        CrossDimensionLodRenderTypes.LOD_VERTEX_FORMAT.getVertexSize());
        if (vertexBytes == 0) return PackedBuffer.EMPTY;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder =
                    new BufferBuilder(
                            bytes,
                            CrossDimensionLodRenderTypes.tiledBlocksPipeline()
                                    .getVertexFormatMode(),
                            CrossDimensionLodRenderTypes.LOD_VERTEX_FORMAT);
            for (int quad = 0; quad < quads.size; quad++) {
                int attribute = quad * QuadBuffer.ATTRIBUTE_STRIDE;
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

    private static PackedBuffer packIrisQuadBuffer(
            QuadBuffer quads,
            RegionalSkyLight.Region skyExposureRegion,
            Set<TextureAtlasSprite> sprites) {
        int vertexCount = Math.multiplyExact(quads.size, 4);
        int vertexBytes =
                Math.multiplyExact(
                        vertexCount,
                        CrossDimensionLodRenderTypes.IRIS_VERTEX_FORMAT.getVertexSize());
        if (vertexBytes == 0) return PackedBuffer.EMPTY;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(vertexBytes)) {
            BufferBuilder builder =
                    new BufferBuilder(
                            bytes,
                            CrossDimensionLodRenderTypes.irisBlocksPipeline().getVertexFormatMode(),
                            CrossDimensionLodRenderTypes.IRIS_VERTEX_FORMAT);
            for (int quad = 0; quad < quads.size; quad++) {
                int attribute = quad * QuadBuffer.ATTRIBUTE_STRIDE;
                int face = quads.attributes[attribute];
                TextureAtlasSprite sprite = blockSprite(quads.attributes[attribute + 1], face);
                sprites.add(sprite);
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

    private static GpuBuffer uploadPackedBuffer(PackedBuffer packed, String part, int x, int z) {
        if (packed.bytes.length == 0) return null;
        GpuBuffer buffer =
                RenderSystem.getDevice()
                        .createBuffer(
                                () -> "Cross-dimension LOD " + part + " [" + x + "," + z + "]",
                                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                                packed.bytes.length);
        try {
            var nativeBytes = MemoryUtil.memAlloc(packed.bytes.length);
            try {
                nativeBytes.put(packed.bytes).flip();
                RenderSystem.getDevice()
                        .createCommandEncoder()
                        .writeToBuffer(buffer.slice(), nativeBytes);
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

    private static boolean preparePayloadMaterials(CrossDimensionLodColumn payload) {
        boolean ready = true;
        int[] palette = payload.palette();
        for (int i = 1; i < palette.length; i++) {
            int stateId = palette[i];
            if (isMaterialReady(stateId)) continue;
            ready = false;
            if (MATERIAL_PENDING.add(stateId)) MATERIAL_QUEUE.add(stateId);
        }
        return ready;
    }

    private static boolean isMaterialReady(int stateId) {
        for (int face = 0; face < 6; face++) {
            if (!FACE_SPRITES.containsKey(((long) stateId << 3) | face)) return false;
        }
        return true;
    }

    /** Model lookup is client-thread-only; workers consume the immutable sprite cache. */
    private static void drainMaterialRequests() {
        for (int resolved = 0; resolved < MATERIALS_PER_TICK; resolved++) {
            Integer stateId = MATERIAL_QUEUE.poll();
            if (stateId == null) break;
            MATERIAL_PENDING.remove(stateId);
            if (isMaterialReady(stateId)) continue;
            resolveMaterialSprites(stateId);
        }
    }

    private static TextureAtlasSprite blockSprite(int stateId, int face) {
        TextureAtlasSprite sprite = FACE_SPRITES.get(((long) stateId << 3) | face);
        if (sprite != null) return sprite;
        throw new IllegalStateException(
                "LOD material was not resolved before mesh packing: " + stateId);
    }

    private static void resolveMaterialSprites(int stateId) {
        var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        var state = Block.stateById(stateId);
        var model = modelSet.get(state);
        List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts =
                new ArrayList<>();
        model.collectParts(
                net.minecraft.client.renderer.block.BlockAndTintGetter.EMPTY,
                BlockPos.ZERO,
                state,
                RandomSource.create(0L),
                parts);
        // The seeded model parts are the same for all six faces; collect them once per state.
        TextureAtlasSprite fallback = null;
        Direction[] directions = {
            Direction.WEST,
            Direction.EAST,
            Direction.DOWN,
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH
        };
        for (int face = 0; face < directions.length; face++) {
            TextureAtlasSprite sprite = null;
            for (var part : parts) {
                var quads = part.getQuads(directions[face]);
                if (!quads.isEmpty()) {
                    sprite = quads.getFirst().materialInfo().sprite();
                    break;
                }
            }
            if (sprite == null) {
                if (fallback == null) {
                    for (var part : parts) {
                        var quads = part.getQuads(null);
                        if (!quads.isEmpty()) {
                            fallback = quads.getFirst().materialInfo().sprite();
                            break;
                        }
                    }
                    if (fallback == null) fallback = modelSet.getParticleMaterial(state).sprite();
                }
                sprite = fallback;
            }
            FACE_SPRITES.put(((long) stateId << 3) | face, sprite);
        }
    }

    private static void emitQuad(
            VertexConsumer consumer, QuadBuffer quads, int index, TextureAtlasSprite sprite) {
        int coordinate = index * 6;
        float x0 = quads.coordinates[coordinate];
        float y0 = quads.coordinates[coordinate + 1];
        float z0 = quads.coordinates[coordinate + 2];
        float x1 = quads.coordinates[coordinate + 3];
        float y1 = quads.coordinates[coordinate + 4];
        float z1 = quads.coordinates[coordinate + 5];
        int attribute = index * QuadBuffer.ATTRIBUTE_STRIDE;
        int face = quads.attributes[attribute];
        int ao =
                CrossDimensionLodAmbientOcclusion.outwardOrder(
                        face, quads.attributes[attribute + 2]);
        switch (face) {
            case 0 ->
                    emitTextured(
                            consumer, sprite, ao, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0,
                            y0, z0);
            case 1 ->
                    emitTextured(
                            consumer, sprite, ao, 1, 0, 0, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0,
                            y0, z1);
            case 2 ->
                    emitTextured(
                            consumer, sprite, ao, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0,
                            y0, z1);
            case 3 ->
                    emitTextured(
                            consumer, sprite, ao, 0, 1, 0, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0,
                            y0, z0);
            case 4 ->
                    emitTextured(
                            consumer, sprite, ao, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1,
                            y0, z0);
            case 5 ->
                    emitTextured(
                            consumer, sprite, ao, 0, 0, 1, x1, y0, z0, x1, y1, z0, x0, y1, z0, x0,
                            y0, z0);
        }
    }

    private static void emitTextured(
            VertexConsumer consumer,
            TextureAtlasSprite sprite,
            int ao,
            float nx,
            float ny,
            float nz,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz) {
        float a = CrossDimensionLodAmbientOcclusion.shade(ao, 0),
                b = CrossDimensionLodAmbientOcclusion.shade(ao, 1);
        float c = CrossDimensionLodAmbientOcclusion.shade(ao, 2),
                d = CrossDimensionLodAmbientOcclusion.shade(ao, 3);
        if (CrossDimensionLodAmbientOcclusion.flipDiagonal(ao)) {
            texturedVertex(consumer, sprite, bx, by, bz, nx, ny, nz, b);
            texturedVertex(consumer, sprite, cx, cy, cz, nx, ny, nz, c);
            texturedVertex(consumer, sprite, dx, dy, dz, nx, ny, nz, d);
            texturedVertex(consumer, sprite, ax, ay, az, nx, ny, nz, a);
        } else {
            texturedVertex(consumer, sprite, ax, ay, az, nx, ny, nz, a);
            texturedVertex(consumer, sprite, bx, by, bz, nx, ny, nz, b);
            texturedVertex(consumer, sprite, cx, cy, cz, nx, ny, nz, c);
            texturedVertex(consumer, sprite, dx, dy, dz, nx, ny, nz, d);
        }
    }

    private static void texturedVertex(
            VertexConsumer consumer,
            TextureAtlasSprite sprite,
            double x,
            double y,
            double z,
            float nx,
            float ny,
            float nz,
            float ao) {
        int minimumUv = packUv(sprite.getU0(), sprite.getV0());
        int maximumUv = packUv(sprite.getU1(), sprite.getV1());
        consumer.addVertex((float) x, (float) y, (float) z)
                .setColor(ao, ao, ao, 1.0F)
                .setOverlay(minimumUv)
                .setLight(maximumUv)
                .setNormal(nx, ny, nz);
    }

    private static void emitIrisQuad(
            VertexConsumer consumer,
            QuadBuffer quads,
            int index,
            TextureAtlasSprite sprite,
            RegionalSkyLight.Region skyExposureRegion) {
        int coordinate = index * 6;
        float x0 = quads.coordinates[coordinate];
        float y0 = quads.coordinates[coordinate + 1];
        float z0 = quads.coordinates[coordinate + 2];
        float x1 = quads.coordinates[coordinate + 3];
        float y1 = quads.coordinates[coordinate + 4];
        float z1 = quads.coordinates[coordinate + 5];
        int face = quads.attributes[index * QuadBuffer.ATTRIBUTE_STRIDE];
        switch (face) {
            case 0 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            0.8F,
                            -1,
                            0,
                            0,
                            skyExposureRegion,
                            x0,
                            y0,
                            z1,
                            x0,
                            y1,
                            z1,
                            x0,
                            y1,
                            z0,
                            x0,
                            y0,
                            z0);
            case 1 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            0.8F,
                            1,
                            0,
                            0,
                            skyExposureRegion,
                            x0,
                            y0,
                            z0,
                            x0,
                            y1,
                            z0,
                            x0,
                            y1,
                            z1,
                            x0,
                            y0,
                            z1);
            case 2 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            0.6F,
                            0,
                            -1,
                            0,
                            skyExposureRegion,
                            x0,
                            y0,
                            z0,
                            x1,
                            y0,
                            z0,
                            x1,
                            y0,
                            z1,
                            x0,
                            y0,
                            z1);
            case 3 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            1.0F,
                            0,
                            1,
                            0,
                            skyExposureRegion,
                            x0,
                            y0,
                            z1,
                            x1,
                            y0,
                            z1,
                            x1,
                            y0,
                            z0,
                            x0,
                            y0,
                            z0);
            case 4 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            0.8F,
                            0,
                            0,
                            -1,
                            skyExposureRegion,
                            x0,
                            y0,
                            z0,
                            x0,
                            y1,
                            z0,
                            x1,
                            y1,
                            z0,
                            x1,
                            y0,
                            z0);
            case 5 ->
                    emitIrisTextured(
                            consumer,
                            sprite,
                            0.8F,
                            0,
                            0,
                            1,
                            skyExposureRegion,
                            x1,
                            y0,
                            z0,
                            x1,
                            y1,
                            z0,
                            x0,
                            y1,
                            z0,
                            x0,
                            y0,
                            z0);
        }
    }

    private static void emitIrisTextured(
            VertexConsumer consumer,
            TextureAtlasSprite sprite,
            float shade,
            float nx,
            float ny,
            float nz,
            RegionalSkyLight.Region skyExposureRegion,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz) {
        irisVertex(
                consumer,
                ax,
                ay,
                az,
                sprite.getU0(),
                sprite.getV1(),
                shade,
                nx,
                ny,
                nz,
                skyExposureRegion);
        irisVertex(
                consumer,
                bx,
                by,
                bz,
                sprite.getU0(),
                sprite.getV0(),
                shade,
                nx,
                ny,
                nz,
                skyExposureRegion);
        irisVertex(
                consumer,
                cx,
                cy,
                cz,
                sprite.getU1(),
                sprite.getV0(),
                shade,
                nx,
                ny,
                nz,
                skyExposureRegion);
        irisVertex(
                consumer,
                dx,
                dy,
                dz,
                sprite.getU1(),
                sprite.getV1(),
                shade,
                nx,
                ny,
                nz,
                skyExposureRegion);
    }

    private static void irisVertex(
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            float u,
            float v,
            float shade,
            float nx,
            float ny,
            float nz,
            RegionalSkyLight.Region skyExposureRegion) {
        int skyExposure =
                skyExposureRegion == null
                        ? 15
                        : skyExposureRegion.maxSkyLight(Mth.floor(x), Mth.floor(z));
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

    private record PageMesh(
            GpuBuffer vertexBuffer,
            int indexCount,
            GpuBuffer seamVertexBuffer,
            int seamIndexCount,
            GpuBuffer irisVertexBuffer,
            int irisIndexCount,
            GpuBuffer irisSeamVertexBuffer,
            int irisSeamIndexCount,
            Set<TextureAtlasSprite> sprites,
            AABB bounds,
            Side[][] sides)
            implements GpuResource {
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

    private record PackedChunk(
            int chunkX,
            int chunkZ,
            int cellSize,
            PackedBuffer terrain,
            PackedBuffer irisTerrain,
            Set<TextureAtlasSprite> sprites,
            AABB bounds,
            Side[] sides,
            CrossDimensionLodColumn rimSource) {}

    private record EdgeKey(long lowerPage, boolean xAxis) {}

    private record EdgeData(
            PackedBuffer geometry,
            Set<TextureAtlasSprite> sprites,
            AABB bounds,
            Side[][] aoContext) {}

    private record EdgeMesh(
            GpuBuffer buffer,
            int indexCount,
            Set<TextureAtlasSprite> sprites,
            AABB bounds,
            Side[][] aoContext)
            implements GpuResource {
        @Override
        public void close() {
            closeBuffer(buffer);
        }
    }

    private record PageData(
            long key,
            PackedBuffer terrain,
            PackedBuffer seam,
            PackedBuffer irisTerrain,
            PackedBuffer irisSeam,
            Set<TextureAtlasSprite> sprites,
            AABB bounds,
            Side[][] sides,
            Side[][] columns,
            EdgeData[] edges) {
        long byteSize() {
            long size =
                    (long) terrain.bytes.length
                            + seam.bytes.length
                            + irisTerrain.bytes.length
                            + irisSeam.bytes.length;
            for (EdgeData edge : edges) if (edge != null) size += edge.geometry.bytes.length;
            return size;
        }
    }

    private record RankedKey(long key, double priority) {}

    private record PageTransition(
            PageMesh oldMesh,
            List<EdgeMesh> oldEdges,
            PageMesh newMesh,
            List<EdgeMesh> newEdges,
            long startNanos)
            implements GpuResource {
        // Only the detached old resources belong to the transition. PAGES/PAGE_EDGES still own the
        // new ones.
        @Override
        public void close() {
            if (oldMesh != null) oldMesh.close();
            for (EdgeMesh edge : oldEdges) edge.close();
        }
    }

    private record RetiredResource(GpuResource resource, long closeAfterFrame) {}

    private record MeshBuildResult(
            long key,
            long revision,
            long epoch,
            CrossDimensionLodColumn sourcePayload,
            CrossDimensionLodColumn selectedPayload,
            PackedChunk packed,
            Throwable failure) {}

    private record PageBuildResult(
            long key, long revision, long epoch, PageData data, Throwable failure) {}

    public record DebugStats(
            int data,
            int meshes,
            int pages,
            int visible,
            int dirty,
            int building,
            int ready,
            int pendingPayloads,
            int pendingPayloadBytes,
            int pendingArrayBytes,
            int pendingMaterials,
            int meshRetries,
            int viewRadius,
            int cpuThreads,
            int cpuActive,
            int cpuQueued,
            long lastMeshWorkNanos,
            long peakMeshWorkNanos,
            long lastPageWorkNanos,
            long peakPageWorkNanos,
            FrameTiming lastTiming,
            FrameTiming peakTiming,
            FrameTiming lastSpike) {}

    public record FrameTiming(
            long frame,
            long totalNanos,
            long receiveNanos,
            long meshNanos,
            long pageNanos,
            long visibilityNanos,
            long drawNanos) {
        private static final FrameTiming EMPTY = new FrameTiming(0L, 0L, 0L, 0L, 0L, 0L, 0L);

        private FrameTiming max(FrameTiming other) {
            return other.totalNanos > totalNanos ? other : this;
        }
    }

    private CrossDimensionLodRenderer() {}
}
