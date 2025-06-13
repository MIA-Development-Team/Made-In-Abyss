package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.item.MIAItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MIALangProvider extends LanguageProvider {
    public MIALangProvider(PackOutput output, String locale) {
        super(output, MIA.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemgroup.mia", "Made In Abyss");
        add("curios.identifier.whistle", "Whistle");

        MIAItems.ITEMS.getEntries().stream()
                        .map(DeferredHolder::get)
                        .filter(item -> !(item instanceof BlockItem))
                        .forEach(item -> {
                            add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath()));
                        });

        MIABlocks.BLOCKS.getEntries().stream()
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
