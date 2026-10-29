package com.altnoir.mementoinabyss.impl.vertical.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.vertical.server.VerticalBlockInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VerticalBlockActionPayload(ResourceKey<Level> source, BlockPos pos, Action action)
        implements CustomPacketPayload {
    public enum Action {
        START_BREAK,
        ABORT_BREAK,
        USE_BLOCK
    }

    public static final Type<VerticalBlockActionPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("vertical_block_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VerticalBlockActionPayload>
            STREAM_CODEC =
                    StreamCodec.of(
                            (buf, p) -> {
                                buf.writeResourceKey(p.source);
                                buf.writeBlockPos(p.pos);
                                buf.writeEnum(p.action);
                            },
                            buf ->
                                    new VerticalBlockActionPayload(
                                            buf.readResourceKey(Registries.DIMENSION),
                                            buf.readBlockPos(),
                                            buf.readEnum(Action.class)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VerticalBlockActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player)
            VerticalBlockInteractions.handle(player, payload);
    }
}
