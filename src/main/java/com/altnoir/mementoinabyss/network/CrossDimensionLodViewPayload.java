package com.altnoir.mementoinabyss.network;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodServer;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

/** Camera interest, not arbitrary chunk requests. Server bounds position, cadence, radius and work credits. */
public record CrossDimensionLodViewPayload(String linkId, long sequence, int radius, MiaLodView view) implements CustomPacketPayload {
    public static final Type<CrossDimensionLodViewPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MementoInAbyss.ID, "cross_dimension_lod_view"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrossDimensionLodViewPayload> STREAM_CODEC =
            StreamCodec.ofMember(CrossDimensionLodViewPayload::encode, CrossDimensionLodViewPayload::decode);

    public CrossDimensionLodViewPayload {
        if (linkId == null || linkId.isBlank() || linkId.length() > 256 || sequence <= 0 || radius < 16 || radius > 2048 || radius % 16 != 0 || view == null) {
            throw new IllegalArgumentException("Invalid LOD view update");
        }
    }
    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(linkId, 256);
        buffer.writeLong(sequence);
        buffer.writeVarInt(radius);
        buffer.writeDouble(view.x()); buffer.writeDouble(view.y()); buffer.writeDouble(view.z());
        buffer.writeDouble(view.pixelScale());
        for (var plane : view.planes()) {
            buffer.writeFloat(plane.x()); buffer.writeFloat(plane.y()); buffer.writeFloat(plane.z()); buffer.writeFloat(plane.w());
        }
    }
    private static CrossDimensionLodViewPayload decode(RegistryFriendlyByteBuf buffer) {
        String link = buffer.readUtf(256);
        long sequence = buffer.readLong();
        int radius = buffer.readVarInt();
        double x = buffer.readDouble(), y = buffer.readDouble(), z = buffer.readDouble(), scale = buffer.readDouble();
        var planes = new ArrayList<MiaLodView.Plane>(4);
        for (int i = 0; i < 4; i++) planes.add(new MiaLodView.Plane(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
        return new CrossDimensionLodViewPayload(link, sequence, radius, new MiaLodView(x, y, z, scale, planes));
    }
    public static void handle(CrossDimensionLodViewPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) MiaLodServer.receiveView(player, payload);
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
