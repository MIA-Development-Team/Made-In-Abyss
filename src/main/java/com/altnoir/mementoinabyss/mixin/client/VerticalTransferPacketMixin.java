package com.altnoir.mementoinabyss.mixin.client;

import com.altnoir.mementoinabyss.impl.vertical.client.VerticalResidentWorlds;
import com.altnoir.mementoinabyss.impl.vertical.client.VerticalTransferClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPacketListener.class)
abstract class VerticalTransferPacketMixin {
    @Shadow private ClientLevel.ClientLevelData levelData;

    @WrapOperation(
            method = "handleRespawn",
            at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientLevel"))
    private ClientLevel mia$residentWorld(
            ClientPacketListener connection,
            ClientLevel.ClientLevelData data,
            ResourceKey<Level> dimension,
            Holder<DimensionType> type,
            int viewDistance,
            int simulationDistance,
            LevelRenderer renderer,
            boolean debug,
            long seed,
            int seaLevel,
            Operation<ClientLevel> original) {
        var resident = VerticalResidentWorlds.prepared(dimension);
        if (resident != null
                && resident.dimensionType().equals(type.value())
                && resident.getSeaLevel() == seaLevel) {
            levelData = resident.getLevelData();
            resident.getChunkSource().updateViewRadius(viewDistance);
            return resident;
        }
        VerticalResidentWorlds.clear();
        return original.call(
                connection,
                data,
                dimension,
                type,
                viewDistance,
                simulationDistance,
                Minecraft.getInstance().levelRenderer,
                debug,
                seed,
                seaLevel);
    }

    @WrapOperation(
            method = "handleRespawn",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V"))
    private void mia$activateResident(Minecraft mc, ClientLevel level, Operation<Void> original) {
        if (!VerticalResidentWorlds.activate(level)) original.call(mc, level);
    }

    @Shadow private @Nullable LevelLoadTracker levelLoadTracker;

    @Shadow
    private void notifyPlayerLoaded() {
        throw new AssertionError();
    }

    @ModifyVariable(
            method = "handleMovePlayer",
            argsOnly = true,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                            shift = At.Shift.AFTER))
    private ClientboundPlayerPositionPacket mia$continueMovement(
            ClientboundPlayerPositionPacket packet) {
        var replacement = VerticalTransferClient.position(packet);
        if (replacement != packet) {
            // Cached sections were installed by Clone before the authoritative position packet.
            notifyPlayerLoaded();
            levelLoadTracker = null;
            var mc = Minecraft.getInstance();
            if (mc.screen instanceof LevelLoadingScreen) mc.setScreen(null);
        }
        return replacement;
    }
}
