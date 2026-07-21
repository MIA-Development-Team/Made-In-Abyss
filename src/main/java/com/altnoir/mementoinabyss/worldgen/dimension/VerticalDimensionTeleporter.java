package com.altnoir.mementoinabyss.worldgen.dimension;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Moves players across registered vertical world boundaries without a portal block. */
public final class VerticalDimensionTeleporter {
    private static final int TELEPORT_COOLDOWN_TICKS = 20;
    private static final double MAX_HORIZONTAL_COORDINATE = 29_999_872.0;
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

    public static void tick(MinecraftServer server) {
        int tick = server.getTickCount();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || player.isRemoved()) continue;
            Integer allowedAt = COOLDOWNS.get(player.getUUID());
            if (allowedAt != null) {
                if (tick < allowedAt) continue;
                COOLDOWNS.remove(player.getUUID());
            }

            VerticalDimensionLink below = VerticalDimensionLinks.below(player.level().dimension());
            if (below != null && player.getY() < below.upperHeight().minY()) {
                teleport(player, server.getLevel(below.lowerDimension()),
                        below.lowerHeight().maxY() - below.entryOffset(), tick);
                continue;
            }

            VerticalDimensionLink above = VerticalDimensionLinks.above(player.level().dimension());
            if (above != null && player.getY() >= above.lowerHeight().maxY()) {
                teleport(player, server.getLevel(above.upperDimension()),
                        above.upperHeight().minY() + above.entryOffset(), tick);
            }
        }
    }

    private static void teleport(ServerPlayer player, ServerLevel destination, double destinationY, int tick) {
        if (destination == null) return;
        double scale = DimensionType.getTeleportationScale(player.level().dimensionType(), destination.dimensionType());
        double x = Mth.clamp(player.getX() * scale, -MAX_HORIZONTAL_COORDINATE, MAX_HORIZONTAL_COORDINATE);
        double z = Mth.clamp(player.getZ() * scale, -MAX_HORIZONTAL_COORDINATE, MAX_HORIZONTAL_COORDINATE);
        Vec3 movement = player.getDeltaMovement();
        if (player.teleport(new TeleportTransition(
                destination, new Vec3(x, destinationY, z), movement,
                player.getYRot(), player.getXRot(), TeleportTransition.PLACE_PORTAL_TICKET)) != null) {
            COOLDOWNS.put(player.getUUID(), tick + TELEPORT_COOLDOWN_TICKS);
        }
    }

    public static void remove(ServerPlayer player) {
        COOLDOWNS.remove(player.getUUID());
    }

    public static void clear() {
        COOLDOWNS.clear();
    }

    private VerticalDimensionTeleporter() {
    }
}
