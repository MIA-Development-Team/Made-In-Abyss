package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/** Moves players across vertical world boundaries declared on the dimension type. */
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

            DimensionType type = player.level().dimensionType();
            EnvironmentAttributeMap attributes = type.attributes();
            int maxY = type.minY() + type.height();
            if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)
                    && player.getY() < type.minY()) {
                VerticalBoundary below =
                        boundary(attributes, MiaEnvironmentAttributes.VERTICAL_BELOW);
                ServerLevel destination = server.getLevel(below.dimension());
                if (destination == null) continue;
                int destinationMaxY =
                        destination.dimensionType().minY() + destination.dimensionType().height();
                teleport(player, destination, destinationMaxY - below.entryOffset(), tick);
                continue;
            }

            if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)
                    && player.getY() >= maxY) {
                VerticalBoundary above =
                        boundary(attributes, MiaEnvironmentAttributes.VERTICAL_ABOVE);
                ServerLevel destination = server.getLevel(above.dimension());
                if (destination == null) continue;
                teleport(
                        player,
                        destination,
                        destination.dimensionType().minY() + above.entryOffset(),
                        tick);
            }
        }
    }

    private static VerticalBoundary boundary(
            EnvironmentAttributeMap attributes, EnvironmentAttribute<VerticalBoundary> attribute) {
        return attributes.applyModifier(attribute, attribute.defaultValue());
    }

    private static void teleport(
            ServerPlayer player, ServerLevel destination, double destinationY, int tick) {
        double scale =
                DimensionType.getTeleportationScale(
                        player.level().dimensionType(), destination.dimensionType());
        double x =
                Mth.clamp(
                        player.getX() * scale,
                        -MAX_HORIZONTAL_COORDINATE,
                        MAX_HORIZONTAL_COORDINATE);
        double z =
                Mth.clamp(
                        player.getZ() * scale,
                        -MAX_HORIZONTAL_COORDINATE,
                        MAX_HORIZONTAL_COORDINATE);
        Vec3 movement = player.getDeltaMovement();
        if (player.teleport(
                        new TeleportTransition(
                                destination,
                                new Vec3(x, destinationY, z),
                                movement,
                                player.getYRot(),
                                player.getXRot(),
                                TeleportTransition.PLACE_PORTAL_TICKET))
                != null) {
            COOLDOWNS.put(player.getUUID(), tick + TELEPORT_COOLDOWN_TICKS);
        }
    }

    public static void remove(ServerPlayer player) {
        COOLDOWNS.remove(player.getUUID());
    }

    public static void clear() {
        COOLDOWNS.clear();
    }

    private VerticalDimensionTeleporter() {}
}
