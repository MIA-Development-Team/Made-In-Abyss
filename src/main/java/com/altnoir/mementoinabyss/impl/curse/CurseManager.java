package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import com.altnoir.mementoinabyss.impl.curse.record.CurseEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Optional;

public class CurseManager {
    private static volatile HolderLookup.Provider lookup;

    public CurseManager() {}

    public static void init(HolderLookup.Provider provider) {
        lookup = provider;
    }

    private static HolderLookup.Provider lookup() {
        if (lookup == null) {
            throw new IllegalStateException("CurseManager not ready");
        }
        return lookup;
    }

    public Optional<CurseDimension> get(Identifier id) {
        var key = ResourceKey.create(CurseRegistries.CURSE, id);
        return lookup().lookupOrThrow(CurseRegistries.CURSE).get(key)
                .map(Holder::value);
    }

    public List<CurseDimension> getAll() {
        return lookup().lookupOrThrow(CurseRegistries.CURSE)
                .listElements()
                .map(Holder::value)
                .toList();
    }
}
