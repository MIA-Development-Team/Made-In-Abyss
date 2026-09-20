package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Reuse is offered only for a full replacement, validated by canonical named-state SHA-256. */
public record CrossDimensionLodCacheOfferPayload(
        CrossDimensionLodTransfer transfer, String digest, int checksum)
        implements CrossDimensionLodStreamPayload {
    public static final Type<CrossDimensionLodCacheOfferPayload> TYPE =
            new Type<>(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            MementoInAbyss.ID, "cross_dimension_lod_cache_offer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodCacheOfferPayload>
            STREAM_CODEC =
                    StreamCodec.ofMember(
                            CrossDimensionLodCacheOfferPayload::encode,
                            CrossDimensionLodCacheOfferPayload::decode);

    public CrossDimensionLodCacheOfferPayload(CrossDimensionLodTransfer transfer, String digest) {
        this(transfer, digest, checksum(transfer, digest));
    }

    public CrossDimensionLodCacheOfferPayload {
        if (transfer == null
                || !transfer.replacement()
                || digest == null
                || !digest.matches("[0-9a-f]{64}")
                || checksum != checksum(transfer, digest))
            throw new IllegalArgumentException("Invalid LOD cache offer");
    }

    private static int checksum(CrossDimensionLodTransfer transfer, String digest) {
        if (transfer == null || digest == null)
            throw new IllegalArgumentException("Null LOD cache offer");
        CRC32 crc = new CRC32();
        transfer.checksum(crc);
        crc.update(digest.getBytes(StandardCharsets.US_ASCII));
        return (int) crc.getValue();
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        transfer.encode(buffer);
        buffer.writeUtf(digest, 64);
        buffer.writeInt(checksum);
    }

    private static CrossDimensionLodCacheOfferPayload decode(RegistryFriendlyByteBuf buffer) {
        if (buffer.readableBytes() > 2048)
            throw new IllegalArgumentException("Oversized LOD cache offer");
        var result =
                new CrossDimensionLodCacheOfferPayload(
                        CrossDimensionLodTransfer.decode(buffer),
                        buffer.readUtf(64),
                        buffer.readInt());
        if (buffer.isReadable())
            throw new IllegalArgumentException("Trailing LOD cache offer data");
        return result;
    }

    @Override
    public int encodedSize() {
        return transfer.encodedSize() + 65 + 4;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
