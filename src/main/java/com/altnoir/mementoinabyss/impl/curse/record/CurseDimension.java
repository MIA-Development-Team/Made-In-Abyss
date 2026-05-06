package com.altnoir.mementoinabyss.impl.curse.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record CurseDimension(
        Identifier id,
        List<CurseEffect> effects,
        int level
) {
    public static final Codec<CurseDimension> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Identifier.CODEC.fieldOf("id")
                                    .forGetter(CurseDimension::id),
                            CurseEffect.CODEC.listOf()
                                    .fieldOf("effects")
                                    .forGetter(CurseDimension::effects),
                            Codec.INT.fieldOf("level")
                                    .forGetter(CurseDimension::level)
                    ).apply(instance, CurseDimension::new)
            );
}
