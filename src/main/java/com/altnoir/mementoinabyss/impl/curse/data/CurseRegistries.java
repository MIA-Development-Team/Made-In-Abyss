package com.altnoir.mementoinabyss.impl.curse.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class CurseRegistries {
    public static final ResourceKey<Registry<CurseDimension>> CURSE =
            ResourceKey.createRegistryKey(MementoInAbyss.asResource("curse"));

    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CURSE,
                CurseDimension.CODEC,
                CurseDimension.CODEC
        );
    }
}
