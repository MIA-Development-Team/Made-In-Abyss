package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MiaLangProvider extends LanguageProvider {
    public MiaLangProvider(PackOutput output, String locale) {
        super(output, MIA.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemgroup.mia", "Made In Abyss");

        //Tooltip start
        add("tooltip.mia.hold_shift", "Hold [%s] to show details");
        add("tooltip.mia.whistle.level", "Whistle Level: %s");
        add("tooltip.mia.item.red_whistle", "Red Whistles are novice cave raiders allowed only in the Abyss’s first layer.");
        //Tooltip end

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
        add("biome.mia.abyss_brink/abyss_brink", "Abyss Brink");
        //Biome end

        //Compat start
        add("curios.identifier.whistle", "Whistle");
        add("curios.modifiers.whistle", "Red Whistle Attributes");
        add("tag.item.curios.whistle", "Whistle");
        add("emi.category.mia.lamp_tube", "Laser Catalysis");
        //Compat end


        MiaItems.ITEMS.getEntries().stream()
                        .map(DeferredHolder::get)
                        .filter(item -> !(item instanceof BlockItem))
                        .forEach(item -> {
                            add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath()));
                        });

        MiaBlocks.BLOCKS.getEntries().stream()
                        .map(DeferredHolder::get)
                        .forEach(item -> {
                            add(item, formatName(BuiltInRegistries.BLOCK.getKey(item).getPath()));
                        });
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
