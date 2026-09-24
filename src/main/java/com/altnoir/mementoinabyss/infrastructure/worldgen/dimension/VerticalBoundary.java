package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** The dimension on the other side of a vertical world boundary. */
public record VerticalBoundary(ResourceKey<Level> dimension, double entryOffset) {
    public static final Codec<VerticalBoundary> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            ResourceKey.codec(Registries.DIMENSION)
                                                    .fieldOf("dimension")
                                                    .forGetter(VerticalBoundary::dimension),
                                            Codec.DOUBLE
                                                    .fieldOf("entry_offset")
                                                    .forGetter(VerticalBoundary::entryOffset))
                                    .apply(instance, VerticalBoundary::new));

    public VerticalBoundary {
        if (!(entryOffset >= 0.0)) {
            throw new IllegalArgumentException("Invalid vertical entry offset: " + entryOffset);
        }
    }
}
