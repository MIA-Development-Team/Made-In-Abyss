package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client input for a server-authoritative node grab. No simulated node
 * positions are accepted from the client.
 */
public record RopeNodeGrabPayload(
        BlockPos connector,
        int point,
        Action action,
        Vec3 target
) implements CustomPacketPayload {
    public enum Action {
        BEGIN,
        UPDATE,
        RELEASE
    }

    public static final Type<RopeNodeGrabPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("rope_node_grab"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RopeNodeGrabPayload> STREAM_CODEC =
            StreamCodec.ofMember(RopeNodeGrabPayload::encode, RopeNodeGrabPayload::decode);

    public RopeNodeGrabPayload {
        connector = connector.immutable();
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.connector);
        buffer.writeVarInt(this.point);
        buffer.writeByte(this.action.ordinal());
        buffer.writeDouble(this.target.x());
        buffer.writeDouble(this.target.y());
        buffer.writeDouble(this.target.z());
    }

    private static RopeNodeGrabPayload decode(RegistryFriendlyByteBuf buffer) {
        BlockPos connector = buffer.readBlockPos();
        int point = buffer.readVarInt();
        int action = buffer.readUnsignedByte();
        if (action >= Action.values().length) {
            throw new IllegalArgumentException("Invalid rope grab action: " + action);
        }
        return new RopeNodeGrabPayload(
                connector,
                point,
                Action.values()[action],
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        );
    }

    public static void handle(RopeNodeGrabPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.level().getBlockEntity(payload.connector)
                instanceof RopeConnectorBlockEntity connector)) {
            return;
        }
        switch (payload.action) {
            case BEGIN -> connector.beginServerGrab(player, payload.point, payload.target);
            case UPDATE -> connector.updateServerGrab(player, payload.point, payload.target);
            case RELEASE -> connector.releaseServerGrab(player, payload.point);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
