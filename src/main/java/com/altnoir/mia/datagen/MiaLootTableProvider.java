package com.altnoir.mia.datagen;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MiaLootTableProvider {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(
                output,
                Collections.emptySet(),
                List.of(
                        new LootTableProvider.SubProviderEntry(MiaBlockLootTable::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(MiaCheatLootTable::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(MiaArchaeologyLoot::new, LootContextParamSets.ARCHAEOLOGY)
                ),
                registries
        );
    }

    public static ResourceKey<LootTable> register(String name) {
        return register(ResourceKey.create(Registries.LOOT_TABLE, MiaUtil.miaId(name)));
    }

    public static ResourceKey<LootTable> register(ResourceKey<LootTable> name) {
        if (LOCATIONS.add(name)) {
            return name;
        } else {
            throw new IllegalArgumentException(name.location() + " is already a registered built-in loot table");
        }
    }
}
