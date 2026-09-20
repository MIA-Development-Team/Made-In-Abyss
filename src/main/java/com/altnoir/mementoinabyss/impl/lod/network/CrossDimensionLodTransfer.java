package com.altnoir.mementoinabyss.impl.lod.network;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Shared envelope for all bounded parts of an atomic column update. */
public record CrossDimensionLodTransfer(
        String linkId,
        int displayYOffset,
        int radius,
        long streamEpoch,
        long transferId,
        long baseRevision,
        long targetRevision,
        int chunkX,
        int chunkZ,
        int cellSize,
        int minY,
        int sectionCount,
        int updateCount,
        CrossDimensionLodCacheScope cacheScope) {
    public CrossDimensionLodTransfer {
        if (cacheScope == null
                || linkId == null
                || linkId.isBlank()
                || linkId.length() > 256
                || displayYOffset < -65_536
                || displayYOffset > 65_536
                || radius <= 0
                || radius > 16_384
                || streamEpoch <= 0
                || transferId <= 0
                || baseRevision < 0
                || targetRevision <= 0
                || targetRevision < baseRevision
                || cellSize < 1
                || cellSize > 16
                || Integer.bitCount(cellSize) != 1
                || minY < -65_536
                || minY > 65_536
                || minY % 16 != 0
                || sectionCount <= 0
                || sectionCount > 1024
                || updateCount < 0
                || updateCount > sectionCount
                || baseRevision == 0 && updateCount != sectionCount) {
            throw new IllegalArgumentException("Invalid LOD transfer envelope");
        }
        int size = 16 / cellSize;
        if (sectionCount * size > 1024 || sectionCount * size * size * size > 262_144) {
            throw new IllegalArgumentException("LOD transfer column exceeds size limit");
        }
    }

    void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(linkId, 256);
        buffer.writeInt(displayYOffset);
        buffer.writeVarInt(radius);
        buffer.writeLong(streamEpoch);
        buffer.writeLong(transferId);
        buffer.writeLong(baseRevision);
        buffer.writeLong(targetRevision);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeVarInt(cellSize);
        buffer.writeInt(minY);
        buffer.writeVarInt(sectionCount);
        buffer.writeVarInt(updateCount);
        cacheScope.encode(buffer);
    }

    static CrossDimensionLodTransfer decode(RegistryFriendlyByteBuf buffer) {
        return new CrossDimensionLodTransfer(
                buffer.readUtf(256),
                buffer.readInt(),
                buffer.readVarInt(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readVarInt(),
                buffer.readInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                CrossDimensionLodCacheScope.decode(buffer));
    }

    int encodedSize() {
        int utfBytes = linkId.getBytes(StandardCharsets.UTF_8).length;
        return varIntSize(utfBytes)
                + utfBytes
                + 4
                + varIntSize(radius)
                + 32
                + 8
                + varIntSize(cellSize)
                + 4
                + varIntSize(sectionCount)
                + varIntSize(updateCount)
                + cacheScope.encodedSize();
    }

    void checksum(CRC32 crc) {
        byte[] bytes = linkId.getBytes(StandardCharsets.UTF_8);
        update(crc, bytes.length);
        crc.update(bytes);
        for (long field :
                new long[] {
                    displayYOffset,
                    radius,
                    streamEpoch,
                    transferId,
                    baseRevision,
                    targetRevision,
                    chunkX,
                    chunkZ,
                    cellSize,
                    minY,
                    sectionCount,
                    updateCount
                }) update(crc, field);
        cacheScope.checksum(crc);
    }

    static void update(CRC32 crc, long value) {
        for (int shift = 56; shift >= 0; shift -= 8) crc.update((int) (value >>> shift));
    }

    static int varIntSize(int value) {
        int bytes = 1;
        while ((value & ~127) != 0) {
            bytes++;
            value >>>= 7;
        }
        return bytes;
    }

    public boolean replacement() {
        return baseRevision == 0;
    }

    public long chunkKey() {
        return com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodKey.pack(chunkX, chunkZ);
    }
}
