package com.altnoir.mementoinabyss.impl.vertical.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.vertical.server.VerticalNearReadiness;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VerticalSectionReceiptPayload(
        ResourceKey<Level> source, int x, int y, int z, long revision)
        implements CustomPacketPayload {
    public static final Type<VerticalSectionReceiptPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("vertical_section_receipt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VerticalSectionReceiptPayload>
            STREAM_CODEC =
                    StreamCodec.of(
                            (buf, p) -> {
                                buf.writeResourceKey(p.source);
                                buf.writeInt(p.x);
                                buf.writeInt(p.y);
                                buf.writeInt(p.z);
                                buf.writeLong(p.revision);
                            },
                            buf ->
                                    new VerticalSectionReceiptPayload(
                                            buf.readResourceKey(Registries.DIMENSION),
                                            buf.readInt(),
                                            buf.readInt(),
                                            buf.readInt(),
                                            buf.readLong()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VerticalSectionReceiptPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player)
            VerticalNearReadiness.accept(player, payload);
    }
}
