package com.altnoir.mementoinabyss.impl.vertical.client;

import com.altnoir.mementoinabyss.compat.iris.IrisRenderCompat;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionPayload;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalWorldPayload;
import com.altnoir.mementoinabyss.mixin.accessor.VerticalMinecraftAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;

public final class VerticalResidentWorlds {
    private static final Map<ResourceKey<Level>, Resident> WORLDS = new HashMap<>();
    private static @Nullable LevelRenderer originalRenderer;
    private static @Nullable Object models;

    public static void accept(VerticalWorldPayload payload) {
        var mc = Minecraft.getInstance();
        if (mc.level == null
                || mc.player == null
                || !mc.level.dimension().equals(payload.viewer())
                || mc.getConnection() == null
                || IrisRenderCompat.isShaderPackInUse()) return;
        var spawn = payload.spawn();
        if (CrossDimensionLodLinks.forTargets(mc.level).stream()
                .noneMatch(link -> link.source().equals(spawn.dimension()))) return;
        rememberCurrent();
        var existing = WORLDS.get(spawn.dimension());
        if (existing != null) return;
        if (WORLDS.size() >= 2) return;
        var renderer =
                new LevelRenderer(
                        mc,
                        mc.getEntityRenderDispatcher(),
                        mc.getBlockEntityRenderDispatcher(),
                        mc.renderBuffers(),
                        mc.gameRenderer.getGameRenderState(),
                        mc.gameRenderer.getFeatureRenderDispatcher());
        var data =
                new ClientLevel.ClientLevelData(
                        mc.level.getDifficulty(),
                        mc.level.getLevelData().isHardcore(),
                        spawn.isFlat());
        ClientLevel world = null;
        try {
            renderer.onResourceManagerReload(mc.getResourceManager());
            reloadClouds(renderer);
            world =
                    new ClientLevel(
                            mc.getConnection(),
                            data,
                            spawn.dimension(),
                            spawn.dimensionType(),
                            payload.viewDistance(),
                            payload.simulationDistance(),
                            renderer,
                            spawn.isDebug(),
                            spawn.seed(),
                            spawn.seaLevel());
            world.setTimeFromServer(mc.level.getGameTime());
            world.getChunkSource()
                    .updateViewCenter(mc.player.chunkPosition().x(), mc.player.chunkPosition().z());
            renderer.setLevel(world);
            WORLDS.put(spawn.dimension(), new Resident(world, renderer));
        } catch (RuntimeException | Error failure) {
            renderer.setLevel(null);
            renderer.close();
            if (world != null) NeoForge.EVENT_BUS.post(new LevelEvent.Unload(world));
            throw failure;
        }
    }

    private static void rememberCurrent() {
        var mc = Minecraft.getInstance();
        if (originalRenderer == null) {
            originalRenderer = mc.levelRenderer;
            models = mc.getModelManager().getBlockStateModelSet();
        }
        WORLDS.putIfAbsent(mc.level.dimension(), new Resident(mc.level, mc.levelRenderer));
    }

    private static void reloadClouds(LevelRenderer renderer) {
        var mc = Minecraft.getInstance();
        renderer.getCloudRenderer()
                .reload(
                        new PreparableReloadListener.SharedState(mc.getResourceManager()),
                        Util.backgroundExecutor(),
                        new PreparableReloadListener.PreparationBarrier() {
                            @Override
                            public <T> CompletableFuture<T> wait(T value) {
                                return CompletableFuture.completedFuture(value);
                            }
                        },
                        mc);
    }

    public static void accept(VerticalSectionPayload payload) {
        var resident = WORLDS.get(payload.source());
        if (resident != null && resident.level != Minecraft.getInstance().level)
            VerticalNearTerrain.install(resident.level, payload);
    }

    public static @Nullable ClientLevel prepared(ResourceKey<Level> dimension) {
        var resident = WORLDS.get(dimension);
        return resident != null && VerticalTransferClient.isPreparedTarget(dimension)
                ? resident.level
                : null;
    }

    public static boolean activate(ClientLevel level) {
        var resident = WORLDS.get(level.dimension());
        var mc = Minecraft.getInstance();
        if (resident == null
                || resident.level != level
                || !VerticalTransferClient.isPreparedTarget(level.dimension())) return false;
        rememberCurrent();
        var previous = mc.level;
        var entities = new ArrayList<Entity>();
        previous.entitiesForRendering().forEach(entities::add);
        entities.forEach(
                entity ->
                        previous.removeEntity(
                                entity.getId(), Entity.RemovalReason.CHANGED_DIMENSION));
        previous.getChunkSource().updateViewRadius(2);
        var center = mc.player.chunkPosition();
        previous.getChunkSource().updateViewCenter(center.x(), center.z());
        for (int x = center.x() - 5; x <= center.x() + 5; x++)
            for (int z = center.z() - 5; z <= center.z() + 5; z++)
                if (Math.abs(x - center.x()) > 1 || Math.abs(z - center.z()) > 1)
                    previous.getChunkSource().drop(new ChunkPos(x, z));
                else WORLDS.get(previous.dimension()).chunks.put(new ChunkPos(x, z), true);
        ((VerticalMinecraftAccessor) mc).mia$setLevelRenderer(resident.renderer);
        mc.level = level;
        mc.particleEngine.setLevel(level);
        mc.gameRenderer.setLevel(level);
        mc.updateTitle();
        return true;
    }

    public static boolean owns(ClientLevel level) {
        var resident = WORLDS.get(level.dimension());
        return resident != null && resident.level == level;
    }

    public static void tick() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (models != null && models != mc.getModelManager().getBlockStateModelSet()) {
            models = mc.getModelManager().getBlockStateModelSet();
            for (var resident : WORLDS.values()) {
                if (resident.renderer != originalRenderer) {
                    resident.renderer.onResourceManagerReload(mc.getResourceManager());
                    reloadClouds(resident.renderer);
                }
                resident.renderer.allChanged();
            }
        }
        for (var resident : WORLDS.values()) {
            if (resident.level == mc.level) continue;
            resident.level.setTimeFromServer(mc.level.getGameTime());
            resident.level.pollLightUpdates();
            var center = mc.player.chunkPosition();
            var iterator = resident.chunks.entrySet().iterator();
            while (iterator.hasNext()) {
                var pos = iterator.next().getKey();
                if (Math.abs((long) pos.x() - center.x()) > 1
                        || Math.abs((long) pos.z() - center.z()) > 1) {
                    resident.level.getChunkSource().drop(pos);
                    iterator.remove();
                }
            }
            resident.level.getChunkSource().updateViewCenter(center.x(), center.z());
        }
    }

    public static void tracked(VerticalSectionPayload payload) {
        var resident = WORLDS.get(payload.source());
        if (resident != null)
            resident.chunks.put(new ChunkPos(payload.chunkX(), payload.chunkZ()), true);
    }

    public static void clear() {
        var mc = Minecraft.getInstance();
        if (originalRenderer != null)
            ((VerticalMinecraftAccessor) mc).mia$setLevelRenderer(originalRenderer);
        for (var resident : WORLDS.values()) {
            if (resident.renderer != originalRenderer) {
                resident.renderer.setLevel(null);
                resident.renderer.close();
            }
            if (resident.level != mc.level)
                NeoForge.EVENT_BUS.post(new LevelEvent.Unload(resident.level));
        }
        WORLDS.clear();
        originalRenderer = null;
        models = null;
    }

    private record Resident(
            ClientLevel level, LevelRenderer renderer, Map<ChunkPos, Boolean> chunks) {
        Resident(ClientLevel level, LevelRenderer renderer) {
            this(level, renderer, new HashMap<>());
        }
    }

    private VerticalResidentWorlds() {}
}
