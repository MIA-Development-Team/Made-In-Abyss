package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaAttributes;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.init.MiaPotions;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
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
        add("itemgroup.mia", "Made In Abyss");
        add("itemgroup.mia_artifact", "Made In Abyss: Artifact");

        //Tooltip start
        add("tooltip.mia.whistle.level", "Whistle Level: %s");
        add("tooltip.mia.whistle.capacity", "Capacity: %s");
        add("tooltip.mia.hold_shift", "Hold [%s] to show details");
        add("tooltip.mia.item.red_whistle", "Red Whistles are novice cave raiders allowed only in the Abyss’s first layer.");

        add("tooltip.mia.skill.cooldown_value", "Cooldown: %s");
        //Tooltip end

        // Painting Variant
        add("painting.mia.abyss_map.title", "Abyss Map");
        add("painting.mia.abyss.title", "Abyss");
        add("painting.mia.fossil_tree.title", "Fossil Tree");
        add("painting.mia.fortitude_flower.title", "Fortitude Flower");

        add("painting.mia.abyss_map.author", "Made In Abyss");
        add("painting.mia.abyss.author", "Made In Abyss");
        add("painting.mia.fossil_tree.author", "Made In Abyss");
        add("painting.mia.fortitude_flower.author", "Made In Abyss");

        //GUI start
        add("gui.mia.disconnect", "Quit?");
        //GUI end

        //Config start
        add("mia.configuration.title", "Made In Abyss Configuration");
        add("mia.configuration.curse", "Enable Curse");
        add("mia.configuration.curse.tooltip", "Whether to enable the Curse");
        add("mia.configuration.curse_icon", "Switch Curse Icon Position");
        add("mia.configuration.curse_icon.tooltip", "Whether to switch the Curse icon position");
        add("mia.configuration.curse_god", "Curse the Creative and Spectator");
        add("mia.configuration.curse_god.tooltip", "Whether to Curse the Creative and Spectator");
        add("mia.configuration.disconnect_visible", "Disconnect Button Visible");
        add("mia.configuration.disconnect_visible.tooltip", "Whether to show the Disconnect Button");
        add("mia.configuration.ban_disconnect", "Disable Disconnect Button");
        add("mia.configuration.ban_disconnect.tooltip", "Whether to disable the Disconnect Button");
        //Config end

        //Biome start
        add("biome.mia." + MiaBiomes.ABYSS_BRINK.location().getPath(), "Abyss Brink");
        add("biome.mia." + MiaBiomes.ABYSS_PLAINS.location().getPath(), "Abyss Plains");
        add("biome.mia." + MiaBiomes.SKYFOG_FOREST.location().getPath(), "Skyfog Forest");
        //Biome end

        MiaItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(item -> !(item instanceof BlockItem))
                .forEach(item -> {
                    add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath()));
                });

        MiaBlocks.BLOCKS.getEntries().stream()
                .map(DeferredHolder::get)
                .forEach(block -> {
                    add(block, formatName(BuiltInRegistries.BLOCK.getKey(block).getPath()));
                });

        MiaPotions.POTIONS.getEntries().stream()
                .map(DeferredHolder::get)
                .forEach(potion -> {
                    String name = BuiltInRegistries.POTION.getKey(potion).getPath();
                    String key1 = "item.minecraft.potion.effect." + name;
                    String key2 = "item.minecraft.splash_potion.effect." + name;
                    String key3 = "item.minecraft.lingering_potion.effect." + name;

                    add(key1, "Potion of " + formatName(name));
                    add(key2, "Splash Potion of " + formatName(name));
                    add(key3, "Lingering Potion of " + formatName(name));
                });

        //Compat start
        add("curios.identifier.whistle", "Whistle");
        add("curios.modifiers.whistle", "Red Whistle Attributes");
        add("tag.item.curios.whistle", "Whistle");
        add("emi.category.mia.lamp_tube", "Laser Catalysis");
        //Compat end
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
