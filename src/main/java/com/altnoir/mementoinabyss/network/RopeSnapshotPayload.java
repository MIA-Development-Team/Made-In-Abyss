package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Compact server-to-client rope state. Coordinates are encoded as floats
 * relative to the owning connector instead of sending a full block-entity NBT.
 */
public record RopeSnapshotPayload(BlockPos connector, double[] points)
        implements CustomPacketPayload {
    private static final int MAX_POINT_COUNT = 256;
    public static final Type<RopeSnapshotPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("rope_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RopeSnapshotPayload> STREAM_CODEC =
            StreamCodec.ofMember(RopeSnapshotPayload::encode, RopeSnapshotPayload::decode);

    public RopeSnapshotPayload {
        connector = connector.immutable();
        points = points.clone();
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.connector);
        buffer.writeVarInt(this.points.length / 3);
        for (int offset = 0; offset < this.points.length; offset += 3) {
            buffer.writeFloat((float) (this.points[offset] - this.connector.getX()));
            buffer.writeFloat((float) (this.points[offset + 1] - this.connector.getY()));
            buffer.writeFloat((float) (this.points[offset + 2] - this.connector.getZ()));
        }
    }

    private static RopeSnapshotPayload decode(RegistryFriendlyByteBuf buffer) {
        BlockPos connector = buffer.readBlockPos();
        int pointCount = buffer.readVarInt();
        if (pointCount < 2 || pointCount > MAX_POINT_COUNT) {
            throw new IllegalArgumentException("Invalid rope point count: " + pointCount);
        }
        double[] points = new double[pointCount * 3];
        for (int offset = 0; offset < points.length; offset += 3) {
            points[offset] = connector.getX() + buffer.readFloat();
            points[offset + 1] = connector.getY() + buffer.readFloat();
            points[offset + 2] = connector.getZ() + buffer.readFloat();
        }
        return new RopeSnapshotPayload(connector, points);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
