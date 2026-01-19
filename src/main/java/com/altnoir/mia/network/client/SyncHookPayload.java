package com.altnoir.mia.network.client;

import com.altnoir.mia.MIA;
import com.altnoir.mia.client.handler.HookHandler;
import com.altnoir.mia.entity.projectile.HookEntity;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncHookPayload(int id) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHookPayload> TYPE = new CustomPacketPayload.Type<>(MiaUtil.id(MIA.MOD_ID, "sync_hook"));

    public static final StreamCodec<ByteBuf, SyncHookPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncHookPayload::id,
            SyncHookPayload::new
    );

    public static void handle(SyncHookPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level().getEntity(packet.id) instanceof HookEntity entity) {
                HookHandler.hookEntity = entity;
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
