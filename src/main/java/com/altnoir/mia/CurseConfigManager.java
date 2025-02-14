package com.altnoir.mia;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class CurseConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("mia/curse.json");
    private static final Map<ResourceLocation, List<EffectConfig>> DIMENSION_CURSE = new HashMap<>();

    public static class EffectConfig {
        public String id;
        public int duration;
        public int amplifier;
    }

    public static void loadConfig() {
        DIMENSION_CURSE.clear();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                // 创建默认配置的JSON内容
                List<DimensionEntry> defaultEntries = new ArrayList<>();

                // 示例配置
                DimensionEntry OW = new DimensionEntry();
                OW.dimension = "minecraft:overworld";
                OW.effects = new ArrayList<>();
                EffectConfig nausea = new EffectConfig();
                nausea.id = "minecraft:nausea";
                nausea.duration = 300;
                nausea.amplifier = 0;
                OW.effects.add(nausea);

                DimensionEntry AF = new DimensionEntry();
                AF.dimension = "mia:alluring_forest";
                AF.effects = new ArrayList<>();
                EffectConfig nausea2 = new EffectConfig();
                nausea2.id = "minecraft:nausea";
                nausea2.duration = 600;
                nausea2.amplifier = 0;
                EffectConfig perspective = new EffectConfig();
                perspective.id = "mia:perspective";
                perspective.duration = 600;
                perspective.amplifier = 0;
                AF.effects.add(nausea2);
                AF.effects.add(perspective);

                defaultEntries.add(OW);
                defaultEntries.add(AF);

                // 将默认配置写入文件
                String defaultJson = GSON.toJson(defaultEntries);
                Files.write(CONFIG_PATH, defaultJson.getBytes(StandardCharsets.UTF_8));
                return;
            }

            String json = new String(Files.readAllBytes(CONFIG_PATH), StandardCharsets.UTF_8);
            Type type = new TypeToken<List<DimensionEntry>>() {}.getType();
            List<DimensionEntry> entries = GSON.fromJson(json, type);

            for (DimensionEntry entry : entries) {
                ResourceLocation dimId = new ResourceLocation(entry.dimension);
                DIMENSION_CURSE.put(dimId, entry.effects);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load dimension curse config", e);
        }
    }

    public static List<EffectConfig> getEffects(ResourceLocation dimensionId) {
        return DIMENSION_CURSE.getOrDefault(dimensionId, Collections.emptyList());
    }

    public static class DimensionEntry {
        public String dimension;
        public List<EffectConfig> effects;

        // 必须添加无参构造器供Gson使用
        public DimensionEntry() {}
    }
}