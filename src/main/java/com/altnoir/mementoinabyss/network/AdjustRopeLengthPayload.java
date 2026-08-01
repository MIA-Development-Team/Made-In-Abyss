package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.item.RopeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AdjustRopeLengthPayload(int direction) implements CustomPacketPayload {
    public static final Type<AdjustRopeLengthPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("adjust_rope_length"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustRopeLengthPayload> STREAM_CODEC =
            StreamCodec.ofMember(AdjustRopeLengthPayload::encode, AdjustRopeLengthPayload::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeByte(Integer.signum(this.direction));
    }

    private static AdjustRopeLengthPayload decode(RegistryFriendlyByteBuf buffer) {
        return new AdjustRopeLengthPayload(buffer.readByte());
    }

    public static void handle(AdjustRopeLengthPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || payload.direction == 0) {
            return;
        }
        ItemStack stack = player.getUseItem();
        RopeItem.adjustSelectedLength(player, stack, payload.direction);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
