package com.altnoir.mementoinabyss.impl.curse.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public record CurseEffect(
        ResourceKey<MobEffect> effect,
        int amplifier,
        int duration
) {
    public static final Codec<CurseEffect> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceKey.codec(Registries.MOB_EFFECT)
                                    .fieldOf("effect")
                                    .forGetter(CurseEffect::effect),
                            Codec.INT.fieldOf("amplifier").forGetter(CurseEffect::amplifier),
                            Codec.INT.fieldOf("duration").forGetter(CurseEffect::duration)
                    ).apply(instance, CurseEffect::new)
            );
}
