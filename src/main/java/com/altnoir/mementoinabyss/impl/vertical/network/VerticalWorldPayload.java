package com.altnoir.mementoinabyss.impl.vertical.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record VerticalWorldPayload(
        ResourceKey<Level> viewer,
        CommonPlayerSpawnInfo spawn,
        int viewDistance,
        int simulationDistance)
        implements CustomPacketPayload {
    public static final Type<VerticalWorldPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("vertical_world"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VerticalWorldPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeResourceKey(p.viewer);
                        p.spawn.write(buf);
                        buf.writeVarInt(p.viewDistance);
                        buf.writeVarInt(p.simulationDistance);
                    },
                    buf ->
                            new VerticalWorldPayload(
                                    buf.readResourceKey(Registries.DIMENSION),
                                    new CommonPlayerSpawnInfo(buf),
                                    buf.readVarInt(),
                                    buf.readVarInt()));

    public VerticalWorldPayload {
        if (viewDistance < 2
                || viewDistance > 32
                || simulationDistance < 2
                || simulationDistance > 32)
            throw new IllegalArgumentException("Invalid resident world distance");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
