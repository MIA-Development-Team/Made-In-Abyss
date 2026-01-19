package com.altnoir.mia.network.client;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaCapabilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CurseCapabilityPayload(int curse, int maxCurse) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CurseCapabilityPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "curse_capability"));
    public static final StreamCodec<ByteBuf, CurseCapabilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CurseCapabilityPayload::curse,
            ByteBufCodecs.VAR_INT,
            CurseCapabilityPayload::maxCurse,
            CurseCapabilityPayload::new
    );

    public static void handle(CurseCapabilityPayload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null) return;

            var curse = mc.player.getCapability(MiaCapabilities.CURSE, null);
            if (curse != null) {
                curse.setCurse(packet.curse());
                curse.setMaxCurse(packet.maxCurse());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
