package com.altnoir.mementoinabyss.impl.vertical.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record VerticalSectionPayload(
        ResourceKey<Level> viewer,
        ResourceKey<Level> source,
        int offset,
        int minY,
        int height,
        int chunkX,
        int sectionY,
        int chunkZ,
        long revision,
        int[] palette,
        short[] cells,
        byte[] light,
        int[] biomes)
        implements CustomPacketPayload {
    public static final int CELLS = 4096;
    public static final int BIOMES = 64;
    public static final int MAX_PALETTE = 4096;
    public static final int MAX_ENCODED_BYTES = 48 * 1024;
    public static final Type<VerticalSectionPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("vertical_section"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VerticalSectionPayload> STREAM_CODEC =
            StreamCodec.ofMember(VerticalSectionPayload::encode, VerticalSectionPayload::decode);

    public VerticalSectionPayload {
        if (viewer == null || source == null)
            throw new IllegalArgumentException("Missing vertical section dimension");
        if (palette == null || cells == null || light == null || biomes == null)
            throw new IllegalArgumentException("Missing vertical section data");
        if (!validHeight(minY, height))
            throw new IllegalArgumentException("Invalid vertical section height");
        if (!ChunkPos.isValid(chunkX, chunkZ))
            throw new IllegalArgumentException("Invalid vertical section chunk");
        if (offset % 16 != 0 || (long) minY + offset < Integer.MIN_VALUE)
            throw new IllegalArgumentException("Invalid vertical section offset");
        if ((long) minY + offset > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid vertical section offset");
        if (sectionY < SectionPos.blockToSectionCoord(minY)
                || sectionY > SectionPos.blockToSectionCoord(minY + height - 1))
            throw new IllegalArgumentException("Vertical section outside source height");
        if (revision <= 0) throw new IllegalArgumentException("Invalid vertical section revision");
        if (palette.length == 0 || palette.length > MAX_PALETTE)
            throw new IllegalArgumentException("Invalid vertical section palette");
        if (cells.length != CELLS || light.length != CELLS || biomes.length != BIOMES)
            throw new IllegalArgumentException("Invalid vertical section lengths");
        for (int state : palette)
            if (state < 0) throw new IllegalArgumentException("Negative block state");
        for (short cell : cells)
            if (Short.toUnsignedInt(cell) >= palette.length)
                throw new IllegalArgumentException("Invalid vertical section palette index");
        for (int biome : biomes)
            if (biome < 0) throw new IllegalArgumentException("Negative biome id");
        if (encodedSize(
                        viewer, source, offset, minY, height, chunkX, sectionY, chunkZ, palette,
                        biomes)
                >= MAX_ENCODED_BYTES)
            throw new IllegalArgumentException("Oversized vertical section");
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceKey(viewer);
        buffer.writeResourceKey(source);
        buffer.writeVarInt(offset);
        buffer.writeVarInt(minY);
        buffer.writeVarInt(height);
        buffer.writeVarInt(chunkX);
        buffer.writeVarInt(sectionY);
        buffer.writeVarInt(chunkZ);
        buffer.writeLong(revision);
        buffer.writeVarInt(palette.length);
        for (int state : palette) buffer.writeVarInt(state);
        for (short cell : cells) buffer.writeShort(cell);
        buffer.writeBytes(light);
        for (int biome : biomes) buffer.writeVarInt(biome);
    }

    private static VerticalSectionPayload decode(RegistryFriendlyByteBuf buffer) {
        if (buffer.readableBytes() >= MAX_ENCODED_BYTES)
            throw new IllegalArgumentException("Oversized vertical section");
        int start = buffer.readerIndex();
        ResourceKey<Level> viewer = buffer.readResourceKey(Registries.DIMENSION);
        ResourceKey<Level> source = buffer.readResourceKey(Registries.DIMENSION);
        int offset = buffer.readVarInt();
        int minY = buffer.readVarInt();
        int height = buffer.readVarInt();
        int chunkX = buffer.readVarInt();
        int sectionY = buffer.readVarInt();
        int chunkZ = buffer.readVarInt();
        long revision = buffer.readLong();
        if (!validHeight(minY, height)
                || !ChunkPos.isValid(chunkX, chunkZ)
                || offset % 16 != 0
                || (long) minY + offset < Integer.MIN_VALUE
                || (long) minY + offset > Integer.MAX_VALUE
                || sectionY < SectionPos.blockToSectionCoord(minY)
                || sectionY > SectionPos.blockToSectionCoord(minY + height - 1)
                || revision <= 0) {
            throw new IllegalArgumentException("Invalid vertical section header");
        }
        int paletteSize = buffer.readVarInt();
        if (paletteSize <= 0 || paletteSize > MAX_PALETTE)
            throw new IllegalArgumentException("Invalid vertical section palette");
        int[] palette = new int[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            palette[i] = buffer.readVarInt();
            if (palette[i] < 0) throw new IllegalArgumentException("Negative block state");
        }
        if (buffer.readableBytes() < CELLS * 2 + CELLS)
            throw new IllegalArgumentException("Truncated vertical section");
        short[] cells = new short[CELLS];
        for (int i = 0; i < CELLS; i++) {
            short cell = buffer.readShort();
            if (Short.toUnsignedInt(cell) >= paletteSize)
                throw new IllegalArgumentException("Invalid vertical section palette index");
            cells[i] = cell;
        }
        byte[] light = new byte[CELLS];
        buffer.readBytes(light);
        int[] biomes = new int[BIOMES];
        for (int i = 0; i < BIOMES; i++) {
            biomes[i] = buffer.readVarInt();
            if (biomes[i] < 0) throw new IllegalArgumentException("Negative biome id");
        }
        if (buffer.readerIndex() - start >= MAX_ENCODED_BYTES)
            throw new IllegalArgumentException("Oversized vertical section");
        var payload =
                new VerticalSectionPayload(
                        viewer, source, offset, minY, height, chunkX, sectionY, chunkZ, revision,
                        palette, cells, light, biomes);
        if (buffer.isReadable())
            throw new IllegalArgumentException("Trailing vertical section data");
        return payload;
    }

    private static int encodedSize(
            ResourceKey<Level> viewer,
            ResourceKey<Level> source,
            int offset,
            int minY,
            int height,
            int chunkX,
            int sectionY,
            int chunkZ,
            int[] palette,
            int[] biomes) {
        int bytes = identifierSize(viewer.identifier()) + identifierSize(source.identifier());
        bytes +=
                varIntSize(offset)
                        + varIntSize(minY)
                        + varIntSize(height)
                        + varIntSize(chunkX)
                        + varIntSize(sectionY)
                        + varIntSize(chunkZ)
                        + Long.BYTES
                        + varIntSize(palette.length);
        for (int state : palette) bytes += varIntSize(state);
        bytes += CELLS * Short.BYTES + CELLS;
        for (int biome : biomes) bytes += varIntSize(biome);
        return bytes;
    }

    private static int identifierSize(Identifier identifier) {
        int length = identifier.toString().getBytes(StandardCharsets.UTF_8).length;
        return varIntSize(length) + length;
    }

    private static int varIntSize(int value) {
        int bytes = 1;
        while ((value & ~127) != 0) {
            bytes++;
            value >>>= 7;
        }
        return bytes;
    }

    private static boolean validHeight(int minY, int height) {
        return minY >= DimensionType.MIN_Y
                && height >= DimensionType.MIN_HEIGHT
                && height <= DimensionType.Y_SIZE
                && minY % 16 == 0
                && height % 16 == 0
                && (long) minY + height <= DimensionType.MAX_Y + 1;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
