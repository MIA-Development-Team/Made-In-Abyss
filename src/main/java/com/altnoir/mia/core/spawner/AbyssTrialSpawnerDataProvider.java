package com.altnoir.mia.core.spawner;

import com.altnoir.mia.core.spawner.records.AbyssTrialSpawnerPattern;
import com.altnoir.mia.core.spawner.records.EntityTableInstance;
import com.altnoir.mia.core.spawner.records.LootTableInstance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbyssTrialSpawnerDataProvider implements DataProvider {
    private final String modId;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final List<SpawnerDefinition> definitions = new ArrayList<>();

    public AbyssTrialSpawnerDataProvider(String modId, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.modId = modId;
        this.output = output;
        this.registries = registries;
    }

    protected abstract void addSpawners();

    protected void add(ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
        definitions.add(new SpawnerDefinition(id, pattern));
    }

    protected AbyssTrialSpawnerPattern createPattern(
            List<EntityTableInstance> entities,
            List<LootTableInstance> loots,
            int baseMobs,
            int mobsPerPlayer,
            int spawnPerTick,
            int spawnRange) {
        var entityTables = WeightedRandomList.create(entities);
        var lootTables = WeightedRandomList.create(loots);
        return new AbyssTrialSpawnerPattern(lootTables, entityTables, baseMobs, mobsPerPlayer, spawnPerTick, spawnRange);
    }

    protected EntityTableInstance entity(EntityType<?> entityType, int weight) {
        return new EntityTableInstance(entityType, weight);
    }

    protected LootTableInstance loot(ResourceKey<LootTable> lootTableKey, int weight) {
        return new LootTableInstance(lootTableKey, weight);
    }

    protected LootTableInstance loot(ResourceLocation lootTableId, int weight) {
        return new LootTableInstance(ResourceKey.create(Registries.LOOT_TABLE, lootTableId), weight);
    }

    protected LootTableInstance loot(String namespace, String path, int weight) {
        return new LootTableInstance(
            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(namespace, path)), 
            weight
        );
    }

    private void validate(HolderLookup.Provider registries, ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
        for (var entity : pattern.entityTables().unwrap()) {
            var entityType = entity.getEntityType();
            var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        }

        if (pattern.baseMobs() < 0) {
            throw new IllegalArgumentException("baseMobs must be >= 0 in spawner " + id);
        }
        if (pattern.mobsPerPlayer() < 0) {
            throw new IllegalArgumentException("mobsPerPlayer must be >= 0 in spawner " + id);
        }
        if (pattern.spawnPerTick() <= 0) {
            throw new IllegalArgumentException("spawnPerTick must be > 0 in spawner " + id);
        }
        if (pattern.spawnRange() <= 0) {
            throw new IllegalArgumentException("spawnRange must be > 0 in spawner " + id);
        }
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return registries.thenCompose(lookup -> {
            addSpawners();

            var basePath = output.getOutputFolder().resolve("data/" + modId + "/mia/trial_spawner");
            var futures = new ArrayList<CompletableFuture<?>>();

            for (var def : definitions) {
                validate(lookup, def.id, def.pattern);

                var root = new JsonObject();

                if (!def.pattern.entityTables().unwrap().isEmpty()) {
                    var entityTablesArray = new JsonArray();
                    for (var entityEntry : def.pattern.entityTables().unwrap()) {
                        var entityJson = new JsonObject();
                        var entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityEntry.getEntityType());
                        entityJson.addProperty("entity", entityId.toString());
                        entityJson.addProperty("weight", entityEntry.getWeight().asInt());
                        entityTablesArray.add(entityJson);
                    }
                    root.add("entity_tables", entityTablesArray);
                }

                if (!def.pattern.lootTables().unwrap().isEmpty()) {
                    var lootTablesArray = new JsonArray();
                    for (var lootEntry : def.pattern.lootTables().unwrap()) {
                        var lootJson = new JsonObject();
                        lootJson.addProperty("loot_table", lootEntry.getLootTableKey().location().toString());
                        lootJson.addProperty("weight", lootEntry.getWeight().asInt());
                        lootTablesArray.add(lootJson);
                    }
                    root.add("loot_tables", lootTablesArray);
                }

                root.addProperty("base_mobs", def.pattern.baseMobs());
                root.addProperty("mobs_per_player", def.pattern.mobsPerPlayer());
                root.addProperty("spawn_per_tick", def.pattern.spawnPerTick());
                root.addProperty("spawn_range", def.pattern.spawnRange());

                var fileName = def.id.getPath() + ".json";
                var path = basePath.resolve(fileName);

                var future = DataProvider.saveStable(cachedOutput, root, path);
                futures.add(future);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public @NotNull String getName() {
        return "Abyss Trial Spawner data for " + this.modId;
    }

    private record SpawnerDefinition(ResourceLocation id, AbyssTrialSpawnerPattern pattern) {
    }
}
