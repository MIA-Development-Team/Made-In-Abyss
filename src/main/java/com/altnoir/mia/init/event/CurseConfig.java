package com.altnoir.mia.init.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.dimension.MIADimensions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CurseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("mia/curse_setting.json");
    private static final Map<ResourceLocation, List<EffectConfig>> DIMENSION_CURSE = new HashMap<>();

    public static class EffectConfig {
        public String id;
        public int duration;
        public int amplifier;

        public EffectConfig(String id, int duration, int amplifier) {
            this.id = id;
            this.duration = duration;
            this.amplifier = amplifier;
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
    public static void loadConfig() {
        DIMENSION_CURSE.clear();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                // 创建默认JSON配置
                List<DimensionEntry> defaultEntries = new ArrayList<>();

                // 示例配置
                DimensionEntry AB = new DimensionEntry();
                AB.dimension = MIADimensions.ABYSS_BRINK.location().toString();
                AB.effects = new ArrayList<>();
                AB.effects.add(new EffectConfig(Objects.requireNonNull(MobEffects.HUNGER.getKey()).location().toString(), 600, 0));
                AB.effects.add(new EffectConfig(Objects.requireNonNull(MobEffects.DARKNESS.getKey()).location().toString(), 60, 0));
                AB.effects.add(new EffectConfig(Objects.requireNonNull(MobEffects.BLINDNESS.getKey()).location().toString(), 20, 0));
                defaultEntries.add(AB);

                String defaultJson = GSON.toJson(defaultEntries);
                Files.writeString(CONFIG_PATH, defaultJson, StandardCharsets.UTF_8);

                // 将默认配置加载到内存
                for (DimensionEntry entry : defaultEntries) {
                    ResourceLocation dimId = ResourceLocation.parse(entry.dimension);
                    DIMENSION_CURSE.put(dimId, entry.effects);
                }
                return;
            }

            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            Type type = new TypeToken<List<DimensionEntry>>() {}.getType();
            List<DimensionEntry> entries = GSON.fromJson(json, type);

            for (DimensionEntry entry : entries) {
                ResourceLocation dimId = ResourceLocation.parse(entry.dimension);
                DIMENSION_CURSE.put(dimId, entry.effects);
            }
        } catch (IOException e) {
            MIA.LOGGER.error("Failed to load dimension curse config", e);
        }
    }
}