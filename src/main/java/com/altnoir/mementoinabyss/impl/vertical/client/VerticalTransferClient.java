package com.altnoir.mementoinabyss.impl.vertical.client;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalTransferPayload;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VerticalTransferClient {
    private static @Nullable VerticalTransferPayload pending;
    private static long started;
    private static boolean mapped;

    public static void prepare(VerticalTransferPayload payload) {
        var mc = Minecraft.getInstance();
        if (!payload.active()) {
            if (pending != null && pending.token() == payload.token()) clear();
            return;
        }
        if (mc.level == null
                || !payload.source().equals(mc.level.dimension())
                || !MiaDimensions.isMiaDimension(payload.target())) return;
        boolean connected =
                CrossDimensionLodLinks.forTargets(mc.level).stream()
                        .anyMatch(
                                link ->
                                        link.source().equals(payload.target())
                                                && link.displayYOffset() == -payload.offset());
        if (!connected) return;
        pending = payload;
        started = System.nanoTime();
        mapped = false;
    }

    public static void clonePlayer(LocalPlayer oldPlayer, LocalPlayer newPlayer) {
        if (!valid()
                || !oldPlayer.level().dimension().equals(pending.source())
                || !newPlayer.level().dimension().equals(pending.target())) return;
        newPlayer.setPos(oldPlayer.position().add(0, pending.offset(), 0));
        newPlayer.setOldPosAndRot(
                oldPlayer.oldPosition().add(0, pending.offset(), 0),
                oldPlayer.yRotO,
                oldPlayer.xRotO);
        newPlayer.setDeltaMovement(oldPlayer.getDeltaMovement());
        newPlayer.setOnGround(oldPlayer.onGround());
        newPlayer.fallDistance = oldPlayer.fallDistance;
        newPlayer.yBodyRot = oldPlayer.yBodyRot;
        newPlayer.yBodyRotO = oldPlayer.yBodyRotO;
        newPlayer.setYHeadRot(oldPlayer.getYHeadRot());
        newPlayer.yHeadRotO = oldPlayer.yHeadRotO;
        mapped = true;
    }

    public static boolean skipLoadingScreen() {
        var level = Minecraft.getInstance().level;
        return valid() && level != null && level.dimension().equals(pending.target());
    }

    public static boolean isPreparedTarget(ResourceKey<Level> target) {
        return valid() && target.equals(pending.target());
    }

    public static ClientboundPlayerPositionPacket position(ClientboundPlayerPositionPacket packet) {
        var mc = Minecraft.getInstance();
        if (!valid()
                || !mapped
                || mc.level == null
                || !mc.level.dimension().equals(pending.target())) return packet;
        var change = packet.change();
        if (!pending.relative() && packet.relatives().isEmpty()) {
            clear();
            return new ClientboundPlayerPositionPacket(packet.id(), change, packet.relatives());
        }
        if (!packet.relatives().equals(VerticalTransferPayload.RELATIVES)
                || !change.position().equals(new Vec3(0, pending.offset(), 0))
                || !change.deltaMovement().equals(Vec3.ZERO)
                || change.yRot() != 0
                || change.xRot() != 0) {
            clear();
            return packet;
        }
        clear();
        return new ClientboundPlayerPositionPacket(
                packet.id(),
                new PositionMoveRotation(Vec3.ZERO, Vec3.ZERO, 0, 0),
                packet.relatives());
    }

    private static boolean valid() {
        // Once Clone translated the player, its matching position packet must not apply the offset
        // again.
        if (pending != null && !mapped && System.nanoTime() - started > 2_000_000_000L) clear();
        return pending != null;
    }

    public static void clear() {
        pending = null;
        mapped = false;
    }

    private VerticalTransferClient() {}
}
