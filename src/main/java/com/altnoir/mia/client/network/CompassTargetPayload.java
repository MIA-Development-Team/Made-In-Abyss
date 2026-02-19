package com.altnoir.mia.client.network;

import com.altnoir.mia.core.event.client.CompassOverlayEvent;
import com.altnoir.mia.util.MiaUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CompassTargetPayload(int x, int y, int z, boolean hasTarget) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CompassTargetPayload> TYPE =
            new CustomPacketPayload.Type<>(MiaUtil.miaId("compass_target"));

    public static final StreamCodec<ByteBuf, CompassTargetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CompassTargetPayload::x,
            ByteBufCodecs.VAR_INT,
            CompassTargetPayload::y,
            ByteBufCodecs.VAR_INT,
            CompassTargetPayload::z,
            ByteBufCodecs.BOOL,
            CompassTargetPayload::hasTarget,
            CompassTargetPayload::new
    );

    public static void handle(CompassTargetPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.hasTarget()) {
                CompassOverlayEvent.setTargetPosition(new BlockPos(packet.x(), packet.y(), packet.z()));
            } else {
                CompassOverlayEvent.clearTargetPosition();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
