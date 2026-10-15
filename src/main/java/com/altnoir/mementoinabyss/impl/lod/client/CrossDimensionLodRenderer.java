package com.altnoir.mementoinabyss.impl.lod.client;

import com.altnoir.mementoinabyss.foundation.concurrent.MiaExecutors;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodControlPayload;
import com.altnoir.mementoinabyss.impl.lod.network.CrossDimensionLodStreamPayload;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.lod.server.MiaLodView;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Routes independent source streams to per-link rendering and cache contexts. */
public final class CrossDimensionLodRenderer {
    private static final Map<String, CrossDimensionLodRenderContext> CONTEXTS =
            new ConcurrentHashMap<>();
    private static final CrossDimensionLodRenderContext EMPTY =
            new CrossDimensionLodRenderContext("");
    private static Boolean reportedServerEnabled;

    private static CrossDimensionLodRenderContext context(String linkId) {
        return CONTEXTS.computeIfAbsent(linkId, CrossDimensionLodRenderContext::new);
    }

    private static void ensureContexts() {
        var level = Minecraft.getInstance().level;
        if (level != null)
            for (var link : CrossDimensionLodLinks.forTargets(level)) context(link.id().toString());
    }

    private static CrossDimensionLodRenderContext primary() {
        var level = Minecraft.getInstance().level;
        if (level == null) return EMPTY;
        return CrossDimensionLodLinks.forTarget(level)
                .map(link -> context(link.id().toString()))
                .orElse(EMPTY);
    }

    public static DebugStats debugStats() {
        return primary().debugStats();
    }

    static MiaLodView cameraView() {
        return primary().cameraView();
    }

    static CrossDimensionLodClientCache.Stats cacheStats() {
        return primary().cacheStats();
    }

    /** Network callback: route without touching Minecraft or GPU state. */
    public static void accept(CrossDimensionLodStreamPayload payload) {
        context(payload.transfer().linkId()).accept(payload);
    }

    public static void clientTick() {
        MiaExecutors.refreshThreadLimit();
        CurrentTerrainReadiness.tick();
        ensureContexts();
        boolean enabled = false;
        for (var context : CONTEXTS.values()) {
            context.clientTick();
            enabled |= context.running();
        }
        if (Minecraft.getInstance().getConnection() == null) {
            reportedServerEnabled = null;
            return;
        }
        if (reportedServerEnabled == null || reportedServerEnabled != enabled) {
            ClientPacketDistributor.sendToServer(new CrossDimensionLodControlPayload(enabled));
            reportedServerEnabled = enabled;
        }
    }

    public static void renderPersistent(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        ensureContexts();
        for (var context : CONTEXTS.values()) context.renderPersistent(event);
    }

    public static void disconnect() {
        CurrentTerrainReadiness.reset();
        reportedServerEnabled = null;
        for (var context : CONTEXTS.values()) context.disconnect();
    }

    public static void onResourceReload() {
        for (var context : CONTEXTS.values()) context.onResourceReload();
    }

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
        static final FrameTiming EMPTY = new FrameTiming(0L, 0L, 0L, 0L, 0L, 0L, 0L);

        FrameTiming max(FrameTiming other) {
            return other.totalNanos > totalNanos ? other : this;
        }
    }

    private CrossDimensionLodRenderer() {}
}
