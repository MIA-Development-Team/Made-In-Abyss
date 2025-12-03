package com.altnoir.mia.core.spawner.records;

import net.minecraft.util.random.WeightedRandomList;

public record AbyssTrialSpawnerPattern(
        WeightedRandomList<LootTableInstance> lootTables,
        WeightedRandomList<EntityTableInstance> entityTables,
        int baseMobs,
        int mobsPerPlayer,
        int spawnPerTick,
        int spawnRange
        ) { }
