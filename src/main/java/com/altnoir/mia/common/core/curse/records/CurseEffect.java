package com.altnoir.mia.common.core.curse.records;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public record CurseEffect(ResourceKey<MobEffect> effect, int amplifier, int duration) {
}
