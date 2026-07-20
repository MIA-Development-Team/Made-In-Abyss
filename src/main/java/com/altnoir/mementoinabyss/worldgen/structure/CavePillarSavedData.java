package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class CavePillarSavedData extends SavedData {
    private static final Codec<CavePillarSavedData> CODEC = Codec.LONG.listOf()
            .optionalFieldOf("processed", List.of())
            .xmap(CavePillarSavedData::new, CavePillarSavedData::processedList)
            .codec();

    static final SavedDataType<CavePillarSavedData> TYPE = new SavedDataType<>(
            MementoInAbyss.asResource("the_abyss/delayed_cave_pillars"),
            CavePillarSavedData::new,
            CODEC);

    private final Set<Long> processed = new HashSet<>();

    private CavePillarSavedData() {
    }

    private CavePillarSavedData(List<Long> processed) {
        this.processed.addAll(processed);
    }

    boolean isProcessed(long id) {
        return processed.contains(id);
    }

    void markProcessed(long id) {
        if (processed.add(id)) setDirty();
    }

    private List<Long> processedList() {
        return processed.stream().toList();
    }
}
