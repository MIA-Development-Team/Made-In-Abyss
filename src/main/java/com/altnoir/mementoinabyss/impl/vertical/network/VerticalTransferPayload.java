package com.altnoir.mementoinabyss.impl.vertical.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;

public record VerticalTransferPayload(
        ResourceKey<Level> source,
        ResourceKey<Level> target,
        int offset,
        long token,
        boolean active,
        boolean relative)
        implements CustomPacketPayload {
    public static final Set<Relative> RELATIVES =
            Set.of(
                    Relative.X,
                    Relative.Y,
                    Relative.Z,
                    Relative.Y_ROT,
                    Relative.X_ROT,
                    Relative.DELTA_X,
                    Relative.DELTA_Y,
                    Relative.DELTA_Z);
    public static final Type<VerticalTransferPayload> TYPE =
            new Type<>(MementoInAbyss.asResource("vertical_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VerticalTransferPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeResourceKey(p.source);
                        buf.writeResourceKey(p.target);
                        buf.writeInt(p.offset);
                        buf.writeLong(p.token);
                        buf.writeBoolean(p.active);
                        buf.writeBoolean(p.relative);
                    },
                    buf ->
                            new VerticalTransferPayload(
                                    buf.readResourceKey(Registries.DIMENSION),
                                    buf.readResourceKey(Registries.DIMENSION),
                                    buf.readInt(),
                                    buf.readLong(),
                                    buf.readBoolean(),
                                    buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
