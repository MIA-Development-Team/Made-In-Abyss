package com.altnoir.mementoinabyss.compat.jade;

import com.altnoir.mementoinabyss.content.lamptube.AmethystTubeBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

public final class LampTubeHeatProvider
        implements StreamServerDataProvider<BlockAccessor, LampTubeHeatProvider.StoredHeat> {
    public static final LampTubeHeatProvider INSTANCE = new LampTubeHeatProvider();

    private static final StreamCodec<RegistryFriendlyByteBuf, StoredHeat> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    StoredHeat::amount,
                    ByteBufCodecs.VAR_INT,
                    StoredHeat::capacity,
                    StoredHeat::new);

    private LampTubeHeatProvider() {}

    @Override
    public StoredHeat streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AmethystTubeBlockEntity tube)) {
            return new StoredHeat(0, AmethystTubeBlockEntity.HEAT_CAPACITY);
        }
        return new StoredHeat(tube.storedHeat().amount(), AmethystTubeBlockEntity.HEAT_CAPACITY);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StoredHeat> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return MiaJadePlugin.LAMP_TUBE_HEAT;
    }

    public record StoredHeat(int amount, int capacity) {}
}
