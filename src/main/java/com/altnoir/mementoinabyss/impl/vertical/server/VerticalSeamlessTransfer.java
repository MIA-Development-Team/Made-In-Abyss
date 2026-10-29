package com.altnoir.mementoinabyss.impl.vertical.server;

import com.altnoir.mementoinabyss.impl.vertical.network.VerticalTransferPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VerticalSeamlessTransfer {
    private static final long TIMEOUT_NANOS = 2_000_000_000L;
    private static final Map<UUID, Transfer> TRANSFERS = new HashMap<>();
    private static long nextToken;

    public static boolean teleport(ServerPlayer player, ServerLevel target, int offset) {
        return teleport(player, target, offset, null);
    }

    public static boolean teleport(
            ServerPlayer player, ServerLevel target, int offset, Vec3 absolute) {
        var transfer =
                new Transfer(
                        player.level().dimension(),
                        target.dimension(),
                        ++nextToken,
                        System.nanoTime(),
                        player.getKnownMovement());
        TRANSFERS.put(player.getUUID(), transfer);
        PacketDistributor.sendToPlayer(
                player,
                new VerticalTransferPayload(
                        transfer.source,
                        transfer.target,
                        offset,
                        transfer.token,
                        true,
                        absolute == null));
        boolean success = false;
        try {
            success =
                    player.teleport(
                                    new TeleportTransition(
                                            target,
                                            absolute == null ? new Vec3(0, offset, 0) : absolute,
                                            absolute == null
                                                    ? Vec3.ZERO
                                                    : player.getDeltaMovement(),
                                            absolute == null ? 0 : player.getYRot(),
                                            absolute == null ? 0 : player.getXRot(),
                                            absolute == null
                                                    ? VerticalTransferPayload.RELATIVES
                                                    : Set.of(),
                                            TeleportTransition.PLACE_PORTAL_TICKET))
                            != null;
            return success;
        } finally {
            if (!success) {
                TRANSFERS.remove(player.getUUID());
                PacketDistributor.sendToPlayer(
                        player,
                        new VerticalTransferPayload(
                                transfer.source,
                                transfer.target,
                                offset,
                                transfer.token,
                                false,
                                absolute == null));
            }
        }
    }

    public static boolean isCrossing(
            ServerPlayer player, ResourceKey<Level> source, ResourceKey<Level> target) {
        var transfer = TRANSFERS.get(player.getUUID());
        return transfer != null && transfer.source.equals(source) && transfer.target.equals(target);
    }

    public static void issued(ServerPlayer player, int teleportId) {
        var transfer = TRANSFERS.get(player.getUUID());
        if (transfer != null
                && transfer.teleportId == -1
                && transfer.target.equals(player.level().dimension()))
            transfer.teleportId = teleportId;
    }

    public static boolean reconcile(
            ServerPlayer player,
            ServerboundMovePlayerPacket packet,
            int teleportId,
            boolean awaiting) {
        var transfer = TRANSFERS.get(player.getUUID());
        if (transfer == null) return false;
        if (System.nanoTime() - transfer.started > TIMEOUT_NANOS
                || transfer.teleportId != teleportId
                || !transfer.target.equals(player.level().dimension())) {
            TRANSFERS.remove(player.getUUID());
            return false;
        }
        if (awaiting || !packet.hasPosition() || !player.connection.hasClientLoaded()) return false;
        TRANSFERS.remove(player.getUUID());
        var delta =
                new Vec3(
                        packet.getX(player.getX()) - player.getX(),
                        packet.getY(player.getY()) - player.getY(),
                        packet.getZ(player.getZ()) - player.getZ());
        double ticks = Math.min(40, (System.nanoTime() - transfer.started) / 50_000_000.0 + 2);
        double horizontal =
                Math.min(32, 1 + ticks * Math.max(.6, transfer.movement.horizontalDistance()));
        double vertical =
                Math.min(
                        96,
                        1
                                + ticks * Math.max(.6, Math.abs(transfer.movement.y))
                                + .04 * ticks * ticks);
        if (!Double.isFinite(delta.lengthSqr())
                || !Float.isFinite(packet.getXRot(0))
                || !Float.isFinite(packet.getYRot(0))
                || player.isPassenger()) return false;
        if (delta.horizontalDistanceSqr() > horizontal * horizontal || Math.abs(delta.y) > vertical)
            return correct(player);
        var bounds = player.getBoundingBox();
        var swept = bounds.expandTowards(delta);
        var level = player.level();
        for (int x = (int) Math.floor(swept.minX / 16); x <= (int) Math.floor(swept.maxX / 16); x++)
            for (int z = (int) Math.floor(swept.minZ / 16);
                    z <= (int) Math.floor(swept.maxZ / 16);
                    z++)
                if (level.getChunkSource().getChunkNow(x, z) == null) return correct(player);
        var allowed =
                Entity.collideBoundingBox(
                        player, delta, bounds, level, level.getEntityCollisions(player, swept));
        if (allowed.distanceToSqr(delta) > 1e-8) return correct(player);
        // The first acknowledged move includes travel predicted during the network round trip.
        // Sweep it before resetting vanilla's baseline so this allowance cannot tunnel through
        // rock.
        player.move(MoverType.PLAYER, delta);
        player.doCheckFallDamage(delta.x, delta.y, delta.z, packet.isOnGround());
        player.checkMovementStatistics(delta.x, delta.y, delta.z);
        player.connection.resetPosition();
        return false;
    }

    private static boolean correct(ServerPlayer player) {
        player.connection.teleport(
                player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        return true;
    }

    public static void expire(ServerPlayer player) {
        var transfer = TRANSFERS.get(player.getUUID());
        if (transfer != null && System.nanoTime() - transfer.started > TIMEOUT_NANOS)
            TRANSFERS.remove(player.getUUID());
    }

    public static void remove(UUID player) {
        TRANSFERS.remove(player);
    }

    public static void clear() {
        TRANSFERS.clear();
    }

    private static final class Transfer {
        final ResourceKey<Level> source;
        final ResourceKey<Level> target;
        final long token;
        final long started;
        final Vec3 movement;
        int teleportId = -1;

        Transfer(
                ResourceKey<Level> source,
                ResourceKey<Level> target,
                long token,
                long started,
                Vec3 movement) {
            this.source = source;
            this.target = target;
            this.token = token;
            this.started = started;
            this.movement = movement;
        }
    }

    private VerticalSeamlessTransfer() {}
}
