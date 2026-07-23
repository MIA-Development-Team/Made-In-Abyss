package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client preference used to stop server-side streaming work for that player. */
public record CrossDimensionLodControlPayload(boolean active) implements CustomPacketPayload {
    public static final Type<CrossDimensionLodControlPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("cross_dimension_lod_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodControlPayload> STREAM_CODEC =
            StreamCodec.ofMember(CrossDimensionLodControlPayload::encode, CrossDimensionLodControlPayload::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(active);
    }

    private static CrossDimensionLodControlPayload decode(RegistryFriendlyByteBuf buffer) {
        return new CrossDimensionLodControlPayload(buffer.readBoolean());
    }

    public static void handle(CrossDimensionLodControlPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            MiaLodServer.setClientActive(player, payload.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
