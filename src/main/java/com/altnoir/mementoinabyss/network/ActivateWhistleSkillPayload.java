package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.whistle.WhistleApi;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateWhistleSkillPayload(int fragmentIndex) implements CustomPacketPayload {
    public static final Type<ActivateWhistleSkillPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("activate_whistle_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateWhistleSkillPayload> STREAM_CODEC =
            StreamCodec.ofMember(
                    ActivateWhistleSkillPayload::encode,
                    ActivateWhistleSkillPayload::decode
            );

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(fragmentIndex);
    }

    private static ActivateWhistleSkillPayload decode(RegistryFriendlyByteBuf buffer) {
        return new ActivateWhistleSkillPayload(buffer.readVarInt());
    }

    public static void handle(
            ActivateWhistleSkillPayload payload,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer player
                && payload.fragmentIndex() >= 0
                && payload.fragmentIndex() < 64) {
            WhistleApi.activate(player, payload.fragmentIndex());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
