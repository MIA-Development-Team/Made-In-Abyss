package com.altnoir.mementoinabyss.client.render;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.network.CrossDimensionLodDebugPayload;
import com.altnoir.mementoinabyss.worldgen.lod.CrossDimensionLodLinks;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/** Compact F3 diagnostics that identify generation, streaming, and rendering stalls independently. */
public final class CrossDimensionLodDebugEntry implements DebugScreenEntry {
    public static final CrossDimensionLodDebugEntry INSTANCE = new CrossDimensionLodDebugEntry();
    private static volatile CrossDimensionLodDebugPayload serverState;
    private static volatile long receivedAtNanos;

    public static void accept(CrossDimensionLodDebugPayload payload) {
        serverState = payload;
        receivedAtNanos = System.nanoTime();
    }

    public static void clear() {
        serverState = null;
        receivedAtNanos = 0L;
    }

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level,
                        @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        if (level == null) return;
        var link = CrossDimensionLodLinks.forTarget(level.dimension()).orElse(null);
        if (link == null) return;

        var client = CrossDimensionLodRenderer.debugStats();
        displayer.addLine(String.format(Locale.ROOT, "MIA LOD: %s (%s, radius %d)",
                link.id(), MementoInAbyss.CONFIGS.guiSection.crossDimensionLodEnabled.get() ? "on" : "off",
                client.viewRadius()));
        displayer.addLine(String.format(Locale.ROOT,
                "Client: data %d, loose %d, page %d, visible %d, dirty %d, build %d, ready %d",
                client.data(), client.meshes(), client.pages(), client.visible(), client.dirty(),
                client.building(), client.ready()));
        appendTiming(displayer, "LOD ms", client.lastTiming());
        appendTiming(displayer, "LOD peak/60f", client.peakTiming());
        var spike = client.lastSpike();
        if (spike.frame() > 0L) {
            displayer.addLine(String.format(Locale.ROOT,
                    "LOD spike: %.2fms (recv %.2f, mesh %.2f, page %.2f, cull %.2f, draw %.2f), %df ago",
                    millis(spike.totalNanos()), millis(spike.receiveNanos()), millis(spike.meshNanos()),
                    millis(spike.pageNanos()), millis(spike.visibilityNanos()), millis(spike.drawNanos()),
                    Math.max(0L, client.lastTiming().frame() - spike.frame())));
        }

        CrossDimensionLodDebugPayload state = serverState;
        if (state == null || !state.linkId().equals(link.id().toString())) {
            displayer.addLine("Server: waiting for debug state");
            return;
        }
        double ageSeconds = (System.nanoTime() - receivedAtNanos) / 1_000_000_000.0;
        String active = state.generating()
                ? String.format(Locale.ROOT, " active [%d,%d] %dms", state.activeX(), state.activeZ(), state.elapsedMillis())
                : " idle";
        displayer.addLine(String.format(Locale.ROOT,
                "Lazy: %s%s, center %d/%d, stored %d, failed %d, requested %d",
                state.phase(), active, state.centralCursor(), state.centralTotal(), state.generated(),
                state.failed(), state.requested()));
        String lastDuration = state.generating() ? "" : " " + state.elapsedMillis() + "ms";
        displayer.addLine(String.format(Locale.ROOT,
                "Last: [%d,%d] %s%s; debug %.1fs old",
                state.lastX(), state.lastZ(), state.lastResult(), lastDuration, ageSeconds));
        displayer.addLine(String.format(Locale.ROOT,
                "Stream: queue %d, scheduled %d, sent %d, loading %d, ready %d, known %d, missing %d",
                state.queued(), state.scheduled(), state.sent(), state.loading(), state.ready(),
                state.known(), state.missing()));
        displayer.addLine(String.format(Locale.ROOT,
                "CPU: client %d/%d active, %d queued; server %d/%d active, %d queued",
                client.cpuActive(), client.cpuThreads(), client.cpuQueued(),
                state.cpuActive(), state.cpuThreads(), state.cpuQueued()));
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }

    private static void appendTiming(DebugScreenDisplayer displayer, String label,
                                     CrossDimensionLodRenderer.FrameTiming timing) {
        displayer.addLine(String.format(Locale.ROOT,
                "%s: %.2f total; %.2f recv, %.2f mesh, %.2f page, %.2f cull, %.2f draw",
                label, millis(timing.totalNanos()), millis(timing.receiveNanos()), millis(timing.meshNanos()),
                millis(timing.pageNanos()), millis(timing.visibilityNanos()), millis(timing.drawNanos())));
    }

    private static double millis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private CrossDimensionLodDebugEntry() {}
}
