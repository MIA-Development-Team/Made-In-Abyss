package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.impl.vertical.server.VerticalSeamlessTransfer;
import java.util.Set;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class VerticalTransferConnectionMixin {
    @Shadow public ServerPlayer player;
    @Shadow private int awaitingTeleport;
    @Shadow private @Nullable Vec3 awaitingPositionFromClient;

    @Inject(
            method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V",
            at = @At("RETURN"))
    private void mia$issued(
            PositionMoveRotation destination, Set<Relative> relatives, CallbackInfo ci) {
        VerticalSeamlessTransfer.issued(player, awaitingTeleport);
    }

    @Inject(
            method = "handleMovePlayer",
            cancellable = true,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
                            shift = At.Shift.AFTER))
    private void mia$reconcile(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (VerticalSeamlessTransfer.reconcile(
                player, packet, awaitingTeleport, awaitingPositionFromClient != null)) ci.cancel();
    }
}
