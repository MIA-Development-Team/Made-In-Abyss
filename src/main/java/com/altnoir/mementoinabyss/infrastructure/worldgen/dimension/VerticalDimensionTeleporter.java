package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

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

            if (!attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)
                    && !attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)
                    && canEnterOverworldAbyss(
                            player.level().dimension(),
                            MementoInAbyss.CONFIGS.worldGenSection.overworldAbyssEnabled.get(),
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            type.minY(),
                            OverworldAbyss.configuredRadius())) {
                ServerLevel destination = server.getLevel(MiaDimensions.THE_ABYSS_LEVEL);
                if (destination == null) continue;
                int destinationMaxY =
                        destination.dimensionType().minY() + destination.dimensionType().height();
                teleport(
                        player,
                        destination,
                        new Vec3(
                                player.getX(),
                                abyssDestinationY(player.getY(), type.minY(), destinationMaxY),
                                player.getZ()),
                        tick);
                continue;
            }

            if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)
                    && player.getY() >= maxY) {
                VerticalBoundary above =
                        boundary(attributes, MiaEnvironmentAttributes.VERTICAL_ABOVE);
                ServerLevel destination = server.getLevel(above.dimension());
                if (destination == null) continue;
                boolean central = centralOverworldLink(player.level().dimension(), above);
                if (central
                        && !OverworldAbyss.withinConnectionRange(
                                player.getX(), player.getZ(), OverworldAbyss.configuredRadius())) {
                    continue;
                }
                double destinationY =
                        aboveDestinationY(
                                player.level().dimension(),
                                above,
                                player.getY(),
                                maxY,
                                destination.dimensionType().minY());
                if (central) {
                    teleport(
                            player,
                            destination,
                            new Vec3(player.getX(), destinationY, player.getZ()),
                            tick);
                } else {
                    teleport(player, destination, destinationY, tick);
                }
                continue;
            }
        }
    }

    static boolean canEnterOverworldAbyss(
            ResourceKey<Level> dimension,
            boolean enabled,
            double x,
            double y,
            double z,
            int minY,
            int radius) {
        return enabled
                && Level.OVERWORLD.equals(dimension)
                && y < minY
                && OverworldAbyss.withinConnectionRange(x, z, radius);
    }

    static double abyssDestinationY(double playerY, int overworldMinY, int abyssMaxY) {
        return VerticalDimensionCoordinates.sourceY(
                playerY, VerticalDimensionCoordinates.displayOffset(overworldMinY, abyssMaxY));
    }

    static double overworldDestinationY(double playerY, int abyssMaxY, int overworldMinY) {
        return VerticalDimensionCoordinates.sourceY(
                playerY, VerticalDimensionCoordinates.displayOffset(abyssMaxY, overworldMinY));
    }

    static double aboveDestinationY(
            ResourceKey<Level> dimension,
            VerticalBoundary above,
            double playerY,
            int maxY,
            int destinationMinY) {
        if (centralOverworldLink(dimension, above)) {
            return overworldDestinationY(playerY, maxY, destinationMinY);
        }
        return destinationMinY + above.entryOffset();
    }

    static boolean centralOverworldLink(ResourceKey<Level> dimension, VerticalBoundary above) {
        return MiaDimensions.THE_ABYSS_LEVEL.equals(dimension)
                && Level.OVERWORLD.equals(above.dimension());
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
        teleport(player, destination, new Vec3(x, destinationY, z), tick);
    }

    private static void teleport(
            ServerPlayer player, ServerLevel destination, Vec3 position, int tick) {
        Vec3 movement = player.getDeltaMovement();
        if (player.teleport(
                        new TeleportTransition(
                                destination,
                                position,
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
