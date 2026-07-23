package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.Nullable;

/** Synchronizes the most recently located Star Compass target to one client. */
public record CompassTargetPayload(@Nullable BlockPos target) implements CustomPacketPayload {
    public static final Type<CompassTargetPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("compass_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CompassTargetPayload> STREAM_CODEC =
            StreamCodec.ofMember(CompassTargetPayload::encode, CompassTargetPayload::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(target != null);
        if (target != null) {
            buffer.writeBlockPos(target);
        }
    }

    private static CompassTargetPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CompassTargetPayload(buffer.readBoolean() ? buffer.readBlockPos() : null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
