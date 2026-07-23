package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Low-frequency server state used only by the F3 cross-dimension LOD entry. */
public record CrossDimensionLodDebugPayload(
        String linkId, String phase, boolean generating,
        int centralCursor, int centralTotal, int requested, int generated, int failed,
        int activeX, int activeZ, int lastX, int lastZ, long elapsedMillis, String lastResult,
        int queued, int scheduled, int sent, int loading, int ready, int known, int missing,
        int cpuThreads, int cpuActive, int cpuQueued)
        implements CustomPacketPayload {
    public static final Type<CrossDimensionLodDebugPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("cross_dimension_lod_debug"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodDebugPayload> STREAM_CODEC =
            StreamCodec.ofMember(CrossDimensionLodDebugPayload::encode, CrossDimensionLodDebugPayload::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(linkId, 256);
        buffer.writeUtf(phase, 32);
        buffer.writeBoolean(generating);
        buffer.writeVarInt(centralCursor);
        buffer.writeVarInt(centralTotal);
        buffer.writeVarInt(requested);
        buffer.writeVarInt(generated);
        buffer.writeVarInt(failed);
        buffer.writeInt(activeX);
        buffer.writeInt(activeZ);
        buffer.writeInt(lastX);
        buffer.writeInt(lastZ);
        buffer.writeVarLong(elapsedMillis);
        buffer.writeUtf(lastResult, 32);
        buffer.writeVarInt(queued);
        buffer.writeVarInt(scheduled);
        buffer.writeVarInt(sent);
        buffer.writeVarInt(loading);
        buffer.writeVarInt(ready);
        buffer.writeVarInt(known);
        buffer.writeVarInt(missing);
        buffer.writeVarInt(cpuThreads);
        buffer.writeVarInt(cpuActive);
        buffer.writeVarInt(cpuQueued);
    }

    private static CrossDimensionLodDebugPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CrossDimensionLodDebugPayload(
                buffer.readUtf(256), buffer.readUtf(32), buffer.readBoolean(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readVarLong(), buffer.readUtf(32), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
