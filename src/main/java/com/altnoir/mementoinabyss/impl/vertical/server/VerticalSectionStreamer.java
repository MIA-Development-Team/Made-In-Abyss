package com.altnoir.mementoinabyss.impl.vertical.server;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLink;
import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionPayload;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalWorldPayload;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalSectionRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VerticalSectionStreamer {
    private static final int CHUNK_RADIUS = 1;
    private static final int MAX_PAYLOADS = 2;
    private static final int RESCAN_TICKS = 10;
    private static final long HASH_SEED = 0x9E3779B97F4A7C15L;
    private static final long HASH_PRIME = 0x100000001B3L;
    private static final Comparator<SectionKey> KEY_ORDER =
            Comparator.comparing((SectionKey key) -> key.dimension().identifier().toString())
                    .thenComparingInt(SectionKey::chunkX)
                    .thenComparingInt(SectionKey::sectionY)
                    .thenComparingInt(SectionKey::chunkZ);
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();
    private static long nextRevision = 1L;

    public static void tick(MinecraftServer server) {
        Set<UUID> present = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            present.add(playerId);
            if (!eligible(player)) {
                STATES.remove(playerId);
                continue;
            }
            tickPlayer(server, player);
        }
        STATES.keySet().removeIf(playerId -> !present.contains(playerId));
    }

    public static void remove(UUID playerId) {
        STATES.remove(playerId);
    }

    public static void clear() {
        STATES.clear();
    }

    private static boolean eligible(ServerPlayer player) {
        return player.isAlive()
                && !player.hasDisconnected()
                && MiaDimensions.isMiaDimension(player.level().dimension());
    }

    private static void tickPlayer(MinecraftServer server, ServerPlayer player) {
        List<LinkState> links = new ArrayList<>();
        for (CrossDimensionLodLink link : CrossDimensionLodLinks.forTargets(player.level())) {
            if (!MiaDimensions.isMiaDimension(link.source())) continue;
            if (server.getLevel(link.source()) == null) continue;
            links.add(LinkState.of(link));
        }
        PlayerState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new PlayerState());
        ResourceKey<Level> viewer = player.level().dimension();
        if (!viewer.equals(state.viewer) || !links.equals(state.links)) {
            state.reset(viewer, links);
            for (var link : links) {
                var target = server.getLevel(link.source());
                if (target != null)
                    PacketDistributor.sendToPlayer(
                            player,
                            new VerticalWorldPayload(
                                    viewer,
                                    player.createCommonSpawnInfo(target),
                                    Math.clamp(server.getPlayerList().getViewDistance(), 2, 32),
                                    Math.clamp(
                                            server.getPlayerList().getSimulationDistance(),
                                            2,
                                            32)));
            }
        }

        Map<SectionKey, LinkState> desired = desired(player, server, links);
        state.tracked.keySet().retainAll(desired.keySet());
        List<SectionKey> keys = new ArrayList<>(desired.keySet());
        ChunkPos center = player.chunkPosition();
        keys.sort(
                Comparator.comparingLong(
                                (SectionKey key) -> {
                                    long dx = (long) key.chunkX() - center.x();
                                    long dz = (long) key.chunkZ() - center.z();
                                    return dx * dx + dz * dz;
                                })
                        .thenComparingLong(
                                key ->
                                        Math.abs(
                                                (long) key.sectionY() * 16
                                                        + desired.get(key).offset()
                                                        - player.blockPosition().getY()))
                        .thenComparing(KEY_ORDER));
        if (keys.isEmpty()) {
            state.cursor = 0;
            return;
        }

        int sent = 0;
        for (SectionKey key : keys) {
            Tracked tracked = state.tracked.get(key);
            if (tracked == null || !tracked.pending) continue;
            if (sent >= MAX_PAYLOADS) break;
            if (sendPending(server, player, key, desired.get(key), tracked)) sent++;
        }

        int budget = Math.max(1, (keys.size() + RESCAN_TICKS - 1) / RESCAN_TICKS);
        int start = (int) Math.floorMod(state.cursor, keys.size());
        for (int index = 0; index < budget; index++) {
            SectionKey key = keys.get((start + index) % keys.size());
            Tracked tracked = state.tracked.computeIfAbsent(key, ignored -> new Tracked());
            ServerLevel source = server.getLevel(key.dimension());
            if (source == null) continue;
            LevelChunk chunk = source.getChunkSource().getChunkNow(key.chunkX(), key.chunkZ());
            LevelChunkSection section = section(chunk, key.sectionY());
            if (section == null) continue;
            Registry<Biome> biomeRegistry = source.registryAccess().lookupOrThrow(Registries.BIOME);
            long hash = hashSection(source, chunk, section, key.sectionY(), biomeRegistry);
            if (hash == tracked.hash) {
                tracked.pending = false;
                continue;
            }
            if (sent >= MAX_PAYLOADS) {
                tracked.pending = true;
                continue;
            }
            sendSnapshot(
                    player,
                    source,
                    chunk,
                    section,
                    key,
                    desired.get(key),
                    tracked,
                    hash,
                    biomeRegistry);
            sent++;
        }
        state.cursor = start + budget;
    }

    private static Map<SectionKey, LinkState> desired(
            ServerPlayer player, MinecraftServer server, List<LinkState> links) {
        Map<SectionKey, LinkState> desired = new HashMap<>();
        VerticalSectionRange window = VerticalSectionRange.around(player.getY());
        ChunkPos center = player.chunkPosition();
        for (LinkState link : links) {
            ServerLevel source = server.getLevel(link.source());
            if (source == null) continue;
            int displayedMinY = link.minY() + link.offset();
            int displayedMaxY = link.minY() + link.height() + link.offset();
            int minSection =
                    Math.max(window.minSection(), SectionPos.blockToSectionCoord(displayedMinY));
            int maxSection =
                    Math.min(
                            window.maxSection(), SectionPos.blockToSectionCoord(displayedMaxY - 1));
            if (minSection > maxSection) continue;
            int offsetSections = link.offset() / 16;
            int sourceMinSection =
                    Math.max(source.getMinSectionY(), SectionPos.blockToSectionCoord(link.minY()));
            int sourceMaxSection =
                    Math.min(
                            source.getMaxSectionY(),
                            SectionPos.blockToSectionCoord(link.minY() + link.height() - 1));
            for (int x = center.x() - CHUNK_RADIUS; x <= center.x() + CHUNK_RADIUS; x++) {
                for (int z = center.z() - CHUNK_RADIUS; z <= center.z() + CHUNK_RADIUS; z++) {
                    if (!ChunkPos.isValid(x, z)) continue;
                    for (int displayed = minSection; displayed <= maxSection; displayed++) {
                        int sectionY = displayed - offsetSections;
                        if (sectionY < sourceMinSection || sectionY > sourceMaxSection) continue;
                        desired.put(new SectionKey(link.source(), x, sectionY, z), link);
                    }
                }
            }
        }
        return desired;
    }

    private static boolean sendPending(
            MinecraftServer server,
            ServerPlayer player,
            SectionKey key,
            LinkState link,
            Tracked tracked) {
        ServerLevel source = server.getLevel(key.dimension());
        if (source == null) {
            tracked.pending = false;
            return false;
        }
        LevelChunk chunk = source.getChunkSource().getChunkNow(key.chunkX(), key.chunkZ());
        LevelChunkSection section = section(chunk, key.sectionY());
        if (section == null) {
            tracked.pending = false;
            return false;
        }
        Registry<Biome> biomeRegistry = source.registryAccess().lookupOrThrow(Registries.BIOME);
        long hash = hashSection(source, chunk, section, key.sectionY(), biomeRegistry);
        if (hash == tracked.hash) {
            tracked.pending = false;
            return false;
        }
        sendSnapshot(player, source, chunk, section, key, link, tracked, hash, biomeRegistry);
        return true;
    }

    private static void sendSnapshot(
            ServerPlayer player,
            ServerLevel source,
            LevelChunk chunk,
            LevelChunkSection section,
            SectionKey key,
            LinkState link,
            Tracked tracked,
            long hash,
            Registry<Biome> biomeRegistry) {
        VerticalSectionPayload payload =
                build(source, chunk, section, key.sectionY(), link, biomeRegistry);
        PacketDistributor.sendToPlayer(player, payload);
        tracked.hash = hash;
        tracked.pending = false;
    }

    private static VerticalSectionPayload build(
            ServerLevel source,
            LevelChunk chunk,
            LevelChunkSection section,
            int sectionY,
            LinkState link,
            Registry<Biome> biomeRegistry) {
        PalettedContainer<BlockState> states = section.getStates();
        Map<Integer, Short> lookup = new HashMap<>();
        int[] palette = new int[16];
        int paletteSize = 0;
        short[] cells = new short[VerticalSectionPayload.CELLS];
        for (int index = 0; index < VerticalSectionPayload.CELLS; index++) {
            int state = Block.getId(states.get(localX(index), localY(index), localZ(index)));
            Short slot = lookup.get(state);
            if (slot == null) {
                if (paletteSize == palette.length)
                    palette = Arrays.copyOf(palette, paletteSize << 1);
                slot = (short) paletteSize;
                palette[paletteSize++] = state;
                lookup.put(state, slot);
            }
            cells[index] = slot;
        }
        palette = Arrays.copyOf(palette, paletteSize);

        byte[] light = new byte[VerticalSectionPayload.CELLS];
        SectionPos pos = SectionPos.of(chunk.getPos(), sectionY);
        DataLayer sky =
                source.getLightEngine().getLayerListener(LightLayer.SKY).getDataLayerData(pos);
        DataLayer block =
                source.getLightEngine().getLayerListener(LightLayer.BLOCK).getDataLayerData(pos);
        var lightPos = new BlockPos.MutableBlockPos();
        for (int index = 0; index < VerticalSectionPayload.CELLS; index++) {
            int x = localX(index);
            int y = localY(index);
            int z = localZ(index);
            int skyValue =
                    sky == null
                            ? source.getBrightness(
                                    LightLayer.SKY,
                                    lightPos.set(
                                            chunk.getPos().getMinBlockX() + x,
                                            sectionY * 16 + y,
                                            chunk.getPos().getMinBlockZ() + z))
                            : sky.get(x, y, z);
            int blockValue = block == null ? 0 : block.get(x, y, z);
            light[index] = (byte) (skyValue << 4 | blockValue);
        }

        int[] biomeIds = new int[VerticalSectionPayload.BIOMES];
        for (int index = 0; index < VerticalSectionPayload.BIOMES; index++) {
            int x = index & 3;
            int z = (index >>> 2) & 3;
            int y = (index >>> 4) & 3;
            biomeIds[index] = biomeRegistry.getId(section.getNoiseBiome(x, y, z).value());
        }

        return new VerticalSectionPayload(
                link.target(),
                link.source(),
                link.offset(),
                link.minY(),
                link.height(),
                chunk.getPos().x(),
                sectionY,
                chunk.getPos().z(),
                nextRevision++,
                palette,
                cells,
                light,
                biomeIds);
    }

    private static long hashSection(
            ServerLevel source,
            LevelChunk chunk,
            LevelChunkSection section,
            int sectionY,
            Registry<Biome> biomeRegistry) {
        long hash = HASH_SEED;
        PalettedContainer<BlockState> states = section.getStates();
        for (int index = 0; index < VerticalSectionPayload.CELLS; index++) {
            hash = mix(hash, Block.getId(states.get(localX(index), localY(index), localZ(index))));
        }
        SectionPos pos = SectionPos.of(chunk.getPos(), sectionY);
        DataLayer sky =
                source.getLightEngine().getLayerListener(LightLayer.SKY).getDataLayerData(pos);
        DataLayer block =
                source.getLightEngine().getLayerListener(LightLayer.BLOCK).getDataLayerData(pos);
        var lightPos = new BlockPos.MutableBlockPos();
        for (int index = 0; index < VerticalSectionPayload.CELLS; index++) {
            int x = localX(index);
            int y = localY(index);
            int z = localZ(index);
            int skyValue =
                    sky == null
                            ? source.getBrightness(
                                    LightLayer.SKY,
                                    lightPos.set(
                                            chunk.getPos().getMinBlockX() + x,
                                            sectionY * 16 + y,
                                            chunk.getPos().getMinBlockZ() + z))
                            : sky.get(x, y, z);
            int blockValue = block == null ? 0 : block.get(x, y, z);
            hash = mix(hash, skyValue << 4 | blockValue);
        }
        for (int index = 0; index < VerticalSectionPayload.BIOMES; index++) {
            int x = index & 3;
            int z = (index >>> 2) & 3;
            int y = (index >>> 4) & 3;
            hash = mix(hash, biomeRegistry.getId(section.getNoiseBiome(x, y, z).value()));
        }
        return hash;
    }

    private static LevelChunkSection section(LevelChunk chunk, int sectionY) {
        if (chunk == null || sectionY < chunk.getMinSectionY() || sectionY > chunk.getMaxSectionY())
            return null;
        return chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
    }

    private static int localX(int index) {
        return index & 15;
    }

    private static int localY(int index) {
        return (index >>> 8) & 15;
    }

    private static int localZ(int index) {
        return (index >>> 4) & 15;
    }

    private static long mix(long hash, int value) {
        return (hash ^ (value & 0xFFFFFFFFL)) * HASH_PRIME;
    }

    private record SectionKey(ResourceKey<Level> dimension, int chunkX, int sectionY, int chunkZ) {}

    private record LinkState(
            Identifier id,
            ResourceKey<Level> source,
            ResourceKey<Level> target,
            int minY,
            int height,
            int offset) {
        static LinkState of(CrossDimensionLodLink link) {
            return new LinkState(
                    link.id(),
                    link.source(),
                    link.target(),
                    link.sourceHeight().minY(),
                    link.sourceHeight().height(),
                    link.displayYOffset());
        }
    }

    private static final class Tracked {
        long hash;
        boolean pending;
    }

    private static final class PlayerState {
        ResourceKey<Level> viewer;
        List<LinkState> links = List.of();
        final Map<SectionKey, Tracked> tracked = new HashMap<>();
        long cursor;

        void reset(ResourceKey<Level> viewer, List<LinkState> links) {
            this.viewer = viewer;
            this.links = List.copyOf(links);
            tracked.clear();
            cursor = 0;
        }
    }

    private VerticalSectionStreamer() {}
}
