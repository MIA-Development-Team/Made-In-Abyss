package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.impl.curse.data.CurseRegistries;
import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;

public class CurseManager {
    public static HolderLookup<CurseDimension> lookup;

    public static void init(ServerStartedEvent event) {
        lookup = event.getServer()
                .registryAccess()
                .lookupOrThrow(CurseRegistries.CURSE);
    }

    public static Optional<CurseDimension> get(Identifier id) {
        return lookup.get(ResourceKey.create(CurseRegistries.CURSE, id))
                .map(Holder::value);
    }
}