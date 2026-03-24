package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.*;
import com.altnoir.mia.util.FilesHelper;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MiaLangProvider extends LanguageProvider {
    public MiaLangProvider(PackOutput output) {
        super(output, MIA.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addDefault("interface");
        addDefault("tooltip");
        addDefault("ponder");

        MiaBiomes.BIOMES.forEach(biome -> {
            add("biome.mia." + biome.location().getPath(), formatName(biome));
        });

        MiaPaintingVariants.PAINTING_VARIANTS.forEach(paintingVariant -> {
            add("painting.mia." + paintingVariant.location().getPath() + ".title", formatName(paintingVariant.location().getPath()));
            add("painting.mia." + paintingVariant.location().getPath() + ".author", "Memento In Abyss");
        });

        MiaItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(item -> !(item instanceof BlockItem))
                .forEach(item -> add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath())));

        MiaBlocks.BLOCKS.getEntries().stream()
                .map(DeferredHolder::get)
                .forEach(block -> add(block, formatName(BuiltInRegistries.BLOCK.getKey(block).getPath())));

        MiaPotions.POTIONS.getEntries().forEach(holder -> {
            String name = holder.getId().getPath();
            addPotions(name);
        });

        MiaStats.CUSTOM_STATS.getEntries().forEach(holder -> {
            String name = holder.getId().getPath();
            add("stat.mia.interact_with_" + name, "Interactions with " + formatName(name));
        });

        // Tags start
        add("tag.item.curios.whistle", "Whistle");
        add("tag.item.mia.fossilized_logs", "Fossilized Logs");
        add("tag.item.mia.stripped_fossilized_logs", "Stripped Fossilized Logs");
        add("tag.item.mia.skyfog_logs", "Skyfog Logs");
        // Tags end
    }

    private void addPotions(String key) {
        String key1 = "item.minecraft.potion.effect." + key;
        String key2 = "item.minecraft.splash_potion.effect." + key;
        String key3 = "item.minecraft.lingering_potion.effect." + key;

        add(key1, "Potion of " + formatName(key));
        add(key2, "Splash Potion of " + formatName(key));
        add(key3, "Lingering Potion of " + formatName(key));
    }

    private void addDefault(String fileName) {
        var path = "assets/mia/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        var jsonObject = jsonElement.getAsJsonObject();
        for (var entry : jsonObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue().getAsString();
            add(key, value);
        }
    }

    private String formatName(ResourceKey<Biome> key) {
        var path = key.location().getPath();

        int index = path.lastIndexOf('/');
        if (index != -1) {
            path = path.substring(index + 1);
        }
        return formatName(path);
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word ->
                        Character.toUpperCase(word.charAt(0)) +
                                word.substring(1).toLowerCase()
                )
                .collect(Collectors.joining(" "));
    }
}
