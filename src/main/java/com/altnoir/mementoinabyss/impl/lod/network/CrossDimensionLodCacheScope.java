package com.altnoir.mementoinabyss.impl.lod.network;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.CRC32;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Persistent world identity, independent of player, address, link offset and stream epoch. */
public record CrossDimensionLodCacheScope(UUID worldId, String dimension) {
    public CrossDimensionLodCacheScope {
        if (worldId == null
                || dimension == null
                || dimension.isBlank()
                || dimension.length() > 256) {
            throw new IllegalArgumentException("Invalid LOD cache scope");
        }
    }

    void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(worldId);
        buffer.writeUtf(dimension, 256);
    }

    static CrossDimensionLodCacheScope decode(RegistryFriendlyByteBuf buffer) {
        return new CrossDimensionLodCacheScope(buffer.readUUID(), buffer.readUtf(256));
    }

    int encodedSize() {
        int bytes = dimension.getBytes(StandardCharsets.UTF_8).length;
        return 16 + CrossDimensionLodTransfer.varIntSize(bytes) + bytes;
    }

    void checksum(CRC32 crc) {
        CrossDimensionLodTransfer.update(crc, worldId.getMostSignificantBits());
        CrossDimensionLodTransfer.update(crc, worldId.getLeastSignificantBits());
        byte[] bytes = dimension.getBytes(StandardCharsets.UTF_8);
        CrossDimensionLodTransfer.update(crc, bytes.length);
        crc.update(bytes);
    }
}
