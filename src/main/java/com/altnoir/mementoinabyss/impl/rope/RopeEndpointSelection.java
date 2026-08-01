package com.altnoir.mementoinabyss.impl.rope;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Connector currently carrying the first end of a rope item.
 */
public record RopeEndpointSelection(Identifier dimension, BlockPos connector, int length) {
    public static final Codec<RopeEndpointSelection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(RopeEndpointSelection::dimension),
            BlockPos.CODEC.fieldOf("connector").forGetter(RopeEndpointSelection::connector),
            Codec.INT.optionalFieldOf("length", 1).forGetter(RopeEndpointSelection::length)
    ).apply(instance, RopeEndpointSelection::new));

    public RopeEndpointSelection {
        connector = connector.immutable();
        length = Math.max(1, length);
    }

    public RopeEndpointSelection withLength(int newLength) {
        return new RopeEndpointSelection(this.dimension, this.connector, newLength);
    }
}
