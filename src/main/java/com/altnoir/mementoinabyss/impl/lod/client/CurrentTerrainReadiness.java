package com.altnoir.mementoinabyss.impl.lod.client;

import com.altnoir.mementoinabyss.compat.MiaMods;
import com.altnoir.mementoinabyss.impl.lod.LodTerrainReadiness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Wait for actual client chunks and local terrain rendering before spending LOD work. */
final class CurrentTerrainReadiness {
    private static ClientLevel level;
    private static final LodTerrainReadiness.Gate GATE = new LodTerrainReadiness.Gate();
    private static long tick;
    private static boolean covered, ready;

    static void tick() {
        var mc = Minecraft.getInstance();
        tick++;
        if (level != mc.level) {
            level = mc.level;
            GATE.reset();
            covered = false;
            ready = false;
            tick = 0;
        }
        if (level == null || mc.player == null) {
            ready = false;
            return;
        }
        if (tick % 5 == 0) {
            var center = mc.player.chunkPosition();
            covered =
                    LodTerrainReadiness.sample(
                                    center.x(),
                                    center.z(),
                                    mc.options.getEffectiveRenderDistance(),
                                    (x, z) ->
                                            level.getChunkSource()
                                                            .getChunk(x, z, ChunkStatus.FULL, false)
                                                    != null)
                            .mostlyReady();
        }
        // Sodium overrides hasRenderedAllSections with its own terrain queue. Its vanilla
        // dispatcher is not the readiness source and may be absent.
        boolean rendered =
                (MiaMods.SODIUM.isLoaded() || mc.levelRenderer.getSectionRenderDispatcher() != null)
                        && mc.levelRenderer.hasRenderedAllSections();
        ready = GATE.update(tick, covered && rendered);
    }

    static boolean ready() {
        return ready;
    }

    static void reset() {
        level = null;
        GATE.reset();
        ready = false;
        covered = false;
    }

    private CurrentTerrainReadiness() {}
}
