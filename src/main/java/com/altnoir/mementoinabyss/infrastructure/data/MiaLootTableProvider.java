package com.altnoir.mementoinabyss.infrastructure.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class MiaLootTableProvider {
    public static LootTableProvider create(
            PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(
                output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(
                                MiaArchaeologyLoot::new, LootContextParamSets.ARCHAEOLOGY)),
                registries);
    }

    private MiaLootTableProvider() {}
}
