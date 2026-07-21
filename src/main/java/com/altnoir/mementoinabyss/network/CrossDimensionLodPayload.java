package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** One independently renderable, coarse voxel chunk from the Great Fault. */
public record CrossDimensionLodPayload(
        String linkId, int displayYOffset, int outsidePlaneY, int centerX, int centerZ,
        int radius, boolean reset, int chunkX, int chunkZ, int cellSize,
        int minY, int yCells, int[] palette, short[] voxels) implements CustomPacketPayload {
    public static final Type<CrossDimensionLodPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("cross_dimension_lod_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodPayload> STREAM_CODEC =
            StreamCodec.ofMember(CrossDimensionLodPayload::encode, CrossDimensionLodPayload::decode);

    public CrossDimensionLodPayload {
        int horizontalCells = 16 / cellSize;
        if (radius <= 0 || cellSize <= 0 || 16 % cellSize != 0 || yCells <= 0
                || palette.length == 0 || palette.length > Short.MAX_VALUE
                || voxels.length != horizontalCells * horizontalCells * yCells) {
            throw new IllegalArgumentException("Invalid cross-dimension voxel chunk");
        }
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(radius);
        buffer.writeUtf(linkId, 256);
        buffer.writeInt(displayYOffset);
        buffer.writeInt(outsidePlaneY);
        buffer.writeInt(centerX);
        buffer.writeInt(centerZ);
        buffer.writeBoolean(reset);
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeVarInt(cellSize);
        buffer.writeVarInt(minY);
        buffer.writeVarInt(yCells);
        buffer.writeVarInt(palette.length);
        for (int stateId : palette) buffer.writeVarInt(stateId);
        for (short voxel : voxels) buffer.writeShort(voxel);
    }

    private static CrossDimensionLodPayload decode(RegistryFriendlyByteBuf buffer) {
        int radius = buffer.readVarInt();
        String linkId = buffer.readUtf(256);
        int displayYOffset = buffer.readInt();
        int outsidePlaneY = buffer.readInt();
        int centerX = buffer.readInt();
        int centerZ = buffer.readInt();
        boolean reset = buffer.readBoolean();
        int chunkX = buffer.readInt();
        int chunkZ = buffer.readInt();
        int cellSize = buffer.readVarInt();
        int minY = buffer.readVarInt();
        int yCells = buffer.readVarInt();
        int paletteSize = buffer.readVarInt();
        if (paletteSize < 1 || paletteSize > 4096 || yCells < 1 || yCells > 1024
                || cellSize < 1 || cellSize > 16 || 16 % cellSize != 0) {
            throw new IllegalArgumentException("Invalid cross-dimension voxel payload dimensions");
        }
        int[] palette = new int[paletteSize];
        for (int i = 0; i < palette.length; i++) palette[i] = buffer.readVarInt();
        int horizontalCells = 16 / cellSize;
        short[] voxels = new short[Math.multiplyExact(horizontalCells * horizontalCells, yCells)];
        for (int i = 0; i < voxels.length; i++) voxels[i] = buffer.readShort();
        return new CrossDimensionLodPayload(linkId, displayYOffset, outsidePlaneY, centerX, centerZ,
                radius, reset, chunkX, chunkZ, cellSize,
                minY, yCells, palette, voxels);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(TYPE, STREAM_CODEC);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
