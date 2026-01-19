package com.altnoir.mia.network.server;

import com.altnoir.mia.MIA;
import com.altnoir.mia.entity.projectile.HookEntity;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DiscardHookPayload(int id) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DiscardHookPayload> TYPE = new CustomPacketPayload.Type<>(MiaUtil.id(MIA.MOD_ID, "discard_hook"));

    public static final StreamCodec<ByteBuf, DiscardHookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            DiscardHookPayload::id,
            DiscardHookPayload::new
    );

    public static void handle(DiscardHookPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level().getEntity(packet.id) instanceof HookEntity entity) {
                entity.discard();
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
