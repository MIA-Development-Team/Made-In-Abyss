package com.altnoir.mementoinabyss.impl.lod.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.lod.server.MiaLodServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Feedback is only valid for an exact server-issued transfer; it cannot request arbitrary chunks. */
public record CrossDimensionLodReceiptPayload(
        String linkId,
        long streamEpoch,
        long transferId,
        int chunkX,
        int chunkZ,
        int cellSize,
        long targetRevision,
        int status)
        implements CustomPacketPayload {
    public static final int APPLIED = 0;
    public static final int RESYNC = 1;
    public static final int CACHE_MISS = 2;
    public static final Type<CrossDimensionLodReceiptPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            MementoInAbyss.ID, "cross_dimension_lod_receipt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodReceiptPayload>
            STREAM_CODEC =
                    StreamCodec.ofMember(
                            CrossDimensionLodReceiptPayload::encode,
                            CrossDimensionLodReceiptPayload::decode);

    public CrossDimensionLodReceiptPayload {
        if (linkId == null
                || linkId.isBlank()
                || linkId.length() > 256
                || streamEpoch <= 0
                || transferId <= 0
                || targetRevision <= 0
                || cellSize < 1
                || cellSize > 16
                || Integer.bitCount(cellSize) != 1
                || status < APPLIED
                || status > CACHE_MISS) {
            throw new IllegalArgumentException("Invalid LOD receipt");
        }
    }

    public static CrossDimensionLodReceiptPayload of(CrossDimensionLodTransfer t, int status) {
        return new CrossDimensionLodReceiptPayload(
                t.linkId(),
                t.streamEpoch(),
                t.transferId(),
                t.chunkX(),
                t.chunkZ(),
                t.cellSize(),
                t.targetRevision(),
                status);
    }

    public boolean matches(CrossDimensionLodTransfer t) {
        return linkId.equals(t.linkId())
                && streamEpoch == t.streamEpoch()
                && transferId == t.transferId()
                && chunkX == t.chunkX()
                && chunkZ == t.chunkZ()
                && cellSize == t.cellSize()
                && targetRevision == t.targetRevision();
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(linkId, 256);
        buffer.writeLong(streamEpoch);
        buffer.writeLong(transferId);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeVarInt(cellSize);
        buffer.writeLong(targetRevision);
        buffer.writeVarInt(status);
    }

    private static CrossDimensionLodReceiptPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CrossDimensionLodReceiptPayload(
                buffer.readUtf(256),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readVarInt(),
                buffer.readLong(),
                buffer.readVarInt());
    }

    public static void handle(CrossDimensionLodReceiptPayload payload, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.player() instanceof ServerPlayer player)
                        MiaLodServer.receiveLodReceipt(player, payload);
                });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
