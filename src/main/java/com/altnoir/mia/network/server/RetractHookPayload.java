package com.altnoir.mia.network.server;

import com.altnoir.mia.MIA;
import com.altnoir.mia.common.entity.projectile.HookEntity;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RetractHookPayload(int id) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RetractHookPayload> TYPE = new CustomPacketPayload.Type<>(MiaUtil.id(MIA.MOD_ID, "discard_hook"));

    public static final StreamCodec<ByteBuf, RetractHookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            RetractHookPayload::id,
            RetractHookPayload::new
    );

    public static void handle(RetractHookPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level().getEntity(packet.id) instanceof HookEntity entity) {
                entity.setHookState(HookEntity.HookState.BACK);
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
