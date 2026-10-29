package com.altnoir.mementoinabyss.impl.vertical.server;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalSectionReceiptPayload;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalSectionRange;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class VerticalNearReadiness {
    private static final Map<UUID, Map<Key, Long>> RECEIPTS = new HashMap<>();

    public static void accept(ServerPlayer player, VerticalSectionReceiptPayload p) {
        if (!MiaDimensions.isMiaDimension(player.level().dimension()) || p.revision() <= 0) return;
        var link =
                CrossDimensionLodLinks.forTargets(player.level()).stream()
                        .filter(
                                value ->
                                        value.source().equals(p.source())
                                                && MiaDimensions.isMiaDimension(p.source()))
                        .findFirst();
        if (link.isEmpty()
                || Math.abs((long) p.x() - player.chunkPosition().x()) > 1
                || Math.abs((long) p.z() - player.chunkPosition().z()) > 1) return;
        long displayY = (long) p.y() * 16 + link.get().displayYOffset();
        var window = VerticalSectionRange.around(player.getY());
        if (displayY >= window.maxYExclusive() || displayY + 16 <= window.minY()) return;
        var map = RECEIPTS.computeIfAbsent(player.getUUID(), unused -> new HashMap<>());
        long now = player.level().getGameTime();
        int sectionOffset = Math.floorDiv(link.get().displayYOffset(), 16);
        map.entrySet()
                .removeIf(
                        entry ->
                                now - entry.getValue() > 100
                                        || Math.abs(
                                                        (long) entry.getKey().x()
                                                                - player.chunkPosition().x())
                                                > 1
                                        || Math.abs(
                                                        (long) entry.getKey().z()
                                                                - player.chunkPosition().z())
                                                > 1
                                        || (entry.getKey().source().equals(p.source())
                                                && !window.contains(
                                                        entry.getKey().y() + sectionOffset)));
        Key key = new Key(p.source(), p.x(), p.y(), p.z());
        if (map.containsKey(key) || map.size() < (VerticalSectionRange.RADIUS * 2 + 1) * 18)
            map.put(key, now);
    }

    public static boolean ready(ServerPlayer player, ServerLevel target, double y) {
        var map = RECEIPTS.get(player.getUUID());
        if (map == null) return false;
        var bounds = player.getBoundingBox().move(0, y - player.getY(), 0);
        int minX = (int) Math.floor(bounds.minX / 16), maxX = (int) Math.floor(bounds.maxX / 16);
        int minZ = (int) Math.floor(bounds.minZ / 16), maxZ = (int) Math.floor(bounds.maxZ / 16);
        int minY = Math.max(target.getMinSectionY(), (int) Math.floor(bounds.minY / 16));
        int maxY = Math.min(target.getMaxSectionY(), (int) Math.floor(bounds.maxY / 16));
        long now = player.level().getGameTime();
        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++)
                for (int s = minY; s <= maxY; s++) {
                    Long tick = map.get(new Key(target.dimension(), x, s, z));
                    if (tick == null || now - tick > 100) return false;
                }
        return minY <= maxY;
    }

    public static void remove(UUID player) {
        RECEIPTS.remove(player);
    }

    public static void clear() {
        RECEIPTS.clear();
    }

    private record Key(ResourceKey<Level> source, int x, int y, int z) {}

    private VerticalNearReadiness() {}
}
