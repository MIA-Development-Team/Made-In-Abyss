package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class CurseRegistries {
    public static final ResourceKey<Registry<CurseDimension>> CURSE =
            ResourceKey.createRegistryKey(
                    MementoInAbyss.asResource("curse")
            );

    public static final Codec<CurseDimension> CURSE_CODEC =
            CurseDimension.CODEC;

    private CurseRegistries() {}
}
