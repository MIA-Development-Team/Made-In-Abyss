package com.altnoir.mia.core.spawner.records;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableInstance extends WeightedEntry.IntrusiveBase {
    private final ResourceKey<LootTable> lootTableKey;

    public LootTableInstance(ResourceKey<LootTable> lootTableKey, int weight) {
        super(weight);
        this.lootTableKey = lootTableKey;
    }

    public ResourceKey<LootTable> getLootTableKey() {
        return lootTableKey;
    }
}
