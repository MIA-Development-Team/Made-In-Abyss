package com.altnoir.mia.core.spawner;

import com.altnoir.mia.core.spawner.records.AbyssTrialSpawnerPattern;
import com.altnoir.mia.core.spawner.records.EntityTableInstance;
import com.altnoir.mia.core.spawner.records.LootTableInstance;
import com.altnoir.mia.util.MiaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class AbyssTrialSpawnerManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON =
            (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, AbyssTrialSpawnerPattern> spawnerPatterns = new HashMap<>();

    public AbyssTrialSpawnerManager() {
        super(GSON, "mia/trial_spawner");
    }

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> result = new HashMap<>();

        for (var namespace : resourceManager.getNamespaces()) {
            var basePath = "mia/trial_spawner";
            var resources = resourceManager.listResources(basePath, loc -> loc.getPath().endsWith(".json"));
            
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                var fileLoc = entry.getKey();
                var parts = MiaUtil.parseResourcePath(fileLoc.getPath(), basePath);
                
                if (parts == null || parts.length != 1) continue;
                
                var fixedLoc = ResourceLocation.fromNamespaceAndPath(namespace, parts[0]);
                var res = entry.getValue();
                
                try (var stream = res.open(); var reader = new InputStreamReader(stream)) {
                    var json = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                    result.put(fixedLoc, json);
                } catch (IOException | JsonParseException e) {
                    LOGGER.error("Failed to load trial spawner JSON from {}", fileLoc, e);
                }
            }
        }

        return result;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        spawnerPatterns.clear();

        LOGGER.info("Found {} trial spawner config files.", resourceLocationJsonElementMap.size());

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            var id = entry.getKey();
            var jsonElement = entry.getValue();

            if (!jsonElement.isJsonObject()) {
                LOGGER.warn("Trial spawner config {} is not a JSON object", id);
                continue;
            }

            try {
                var json = jsonElement.getAsJsonObject();

                var lootTablesList = new ArrayList<LootTableInstance>();
                if (json.has("loot_tables")) {
                    var lootTablesArray = json.getAsJsonArray("loot_tables");
                    for (var lootElement : lootTablesArray) {
                        var lootJson = lootElement.getAsJsonObject();
                        var lootTableId = ResourceLocation.parse(lootJson.get("loot_table").getAsString());
                        var weight = lootJson.get("weight").getAsInt();
                        var lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableId);
                        lootTablesList.add(new LootTableInstance(lootTableKey, weight));
                    }
                }

                var entityTablesList = new ArrayList<EntityTableInstance>();
                if (json.has("entity_tables")) {
                    var entityTablesArray = json.getAsJsonArray("entity_tables");
                    for (var entityElement : entityTablesArray) {
                        var entityJson = entityElement.getAsJsonObject();
                        var entityId = ResourceLocation.parse(entityJson.get("entity").getAsString());
                        var weight = entityJson.get("weight").getAsInt();
                        
                        var entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                        entityTablesList.add(new EntityTableInstance(entityType, weight));
                    }
                }

                var baseMobs = json.has("base_mobs") ? json.get("base_mobs").getAsInt() : 2;
                var mobsPerPlayer = json.has("mobs_per_player") ? json.get("mobs_per_player").getAsInt() : 1;
                var spawnPerTick = json.has("spawn_per_tick") ? json.get("spawn_per_tick").getAsInt() : 1;
                var spawnRange = json.has("spawn_range") ? json.get("spawn_range").getAsInt() : 4;

                var lootTables = WeightedRandomList.create(lootTablesList);
                var entityTables = WeightedRandomList.create(entityTablesList);

                var pattern = new AbyssTrialSpawnerPattern(
                        lootTables,
                        entityTables,
                        baseMobs,
                        mobsPerPlayer,
                        spawnPerTick,
                        spawnRange
                );

                spawnerPatterns.put(id, pattern);
                LOGGER.info("Loaded trial spawner pattern: {}", id);
                
            } catch (Exception e) {
                LOGGER.error("Failed to parse trial spawner config {}", id, e);
            }
        }
    }

    public Optional<AbyssTrialSpawnerPattern> getPattern(ResourceLocation id) {
        return Optional.ofNullable(spawnerPatterns.get(id));
    }

    public Set<ResourceLocation> getPatternIds() {
        return spawnerPatterns.keySet();
    }

    public Map<ResourceLocation, AbyssTrialSpawnerPattern> getPatterns() {
        return Collections.unmodifiableMap(spawnerPatterns);
    }
}
