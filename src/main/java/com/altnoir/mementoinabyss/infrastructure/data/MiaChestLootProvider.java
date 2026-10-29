package com.altnoir.mementoinabyss.infrastructure.data;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public record MiaChestLootProvider(
        PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
        implements DataProvider {
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(
                lookup -> {
                    var paths = output.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
                    var saves = new ArrayList<CompletableFuture<?>>();
                    // LootTableProvider adds a persistent random sequence, which these legacy
                    // tables lack.
                    new MiaChestLoot(lookup)
                            .generate(
                                    (id, builder) ->
                                            saves.add(
                                                    DataProvider.saveStable(
                                                            cache,
                                                            lookup,
                                                            LootTable.DIRECT_CODEC,
                                                            builder.setParamSet(
                                                                            LootContextParamSets
                                                                                    .CHEST)
                                                                    .build(),
                                                            paths.json(id.identifier()))));
                    return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
                });
    }

    @Override
    public String getName() {
        return "Memento in Abyss chest loot tables";
    }
}
