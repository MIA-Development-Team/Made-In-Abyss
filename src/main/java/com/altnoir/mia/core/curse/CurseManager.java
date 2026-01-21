package com.altnoir.mia.core.curse;

import com.altnoir.mia.core.curse.records.CurseDimension;
import com.altnoir.mia.core.curse.records.CurseEffect;
import com.altnoir.mia.util.MiaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CurseManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON =
            (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, CurseDimension> curseCache = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    public CurseManager() {
        super(GSON, "mia/curse");
    }

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> result = new HashMap<>();

        for (var namespace : resourceManager.getNamespaces()) {
            var basePath = "mia/curse";
            var resources = resourceManager.listResources(basePath, loc -> loc.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                var fileLoc = entry.getKey();
                var parts = MiaUtil.parseResourcePath(fileLoc.getPath(), basePath);
                
                if (parts == null || parts.length > 2) continue;
                
                var fixedLoc = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                var res = entry.getValue();
                try (var stream = res.open()) {
                    var json = GsonHelper.fromJson(GSON, new InputStreamReader(stream), JsonElement.class);
                    result.put(fixedLoc, json);
                } catch (IOException | JsonParseException e) {
                    LOGGER.error("Failed to load curse JSON from {}", fileLoc, e);
                }
            }
        }

        return result;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        curseCache.clear();

        LOGGER.info("Found {} curse config files.", resourceLocationJsonElementMap.size());

        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            var id = entry.getKey();
            var jsonElement = entry.getValue();

            if (!jsonElement.isJsonObject()) continue;

            var json = jsonElement.getAsJsonObject();

            var effects = new ArrayList<CurseEffect>();
            var effectsArray = json.getAsJsonArray("effects");
            for (var effectElement : effectsArray) {
                var effectJson = effectElement.getAsJsonObject();

                var effectId = ResourceLocation.parse(effectJson.get("effect").getAsString());
                var amplifier = effectJson.get("amplifier").getAsInt();
                var duration = effectJson.get("duration").getAsInt();
                var effectKey = ResourceKey.create(Registries.MOB_EFFECT, effectId);

                effects.add(new CurseEffect(effectKey, amplifier, duration));
            }

            var level = json.get("level").getAsInt();

            curseCache.put(id, new CurseDimension(id, effects.toArray(new CurseEffect[0]), level));
        }
    }

    public List<CurseEffect> getEffects(ResourceLocation dimensionId) {
        return Optional.ofNullable(curseCache.get(dimensionId))
                .map(d -> List.of(d.curseEffects()))
                .orElse(List.of());
    }
    public Set<ResourceLocation> getDimensionIds() {
        return curseCache.keySet();
    }

    public Map<ResourceLocation, CurseDimension> getCurseCache() {
        return curseCache;
    }
}
