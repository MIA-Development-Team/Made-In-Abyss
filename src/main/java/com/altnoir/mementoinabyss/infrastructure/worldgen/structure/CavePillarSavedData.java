package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

final class CavePillarSavedData extends SavedData {
    private static final Codec<CavePillarSavedData> CODEC =
            Codec.LONG
                    .listOf()
                    .optionalFieldOf("processed", List.of())
                    .xmap(CavePillarSavedData::new, CavePillarSavedData::processedList)
                    .codec();

    static final SavedDataType<CavePillarSavedData> TYPE =
            new SavedDataType<>(
                    MementoInAbyss.asResource("the_abyss/delayed_cave_pillars"),
                    CavePillarSavedData::new,
                    CODEC);

    private final Set<Long> processed = new HashSet<>();

    private CavePillarSavedData() {}

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
