package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.item.MIAItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MIAENLangProvider extends LanguageProvider {
    public MIAENLangProvider(PackOutput output, String locale) {
        super(output, MIA.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemgroup.mia", "Made In Abyss");

        add("item.mia.purin", "Purin");
        add("item.mia.endless_cup", "Endless Cup");

        add("block.mia.abyss_grass_block", "Abyss Grass_block");
        add("block.mia.covergrass_cobblestone", "Covergrass Cobblestone");
        add("block.mia.covergrass_stone", "Covergrass Stone");
        add("block.mia.covergrass_deepslate", "Covergrass Deepslate");
        add("block.mia.covergrass_granite", "Covergrass Granite");
        add("block.mia.covergrass_diorite", "Covergrass Diorite");
        add("block.mia.covergrass_andesite", "Covergrass Andesite");
        add("block.mia.covergrass_calcite", "Covergrass Calcite");
        add("block.mia.covergrass_tuff", "Covergrass Tuff");
        add("block.mia.abyss_andesite", "Abyss Andesite");
        add("block.mia.abyss_cobbled_deepslate", "Abyss Cobbled Deepslate");
        add("block.mia.chlorophyte_ore", "Chlorophyte Ore");
        add("block.mia.suspicious_andesite", "Suspicious Andesite");
        add("block.mia.volcano_crucible", "Volcano Crucible");
    }
}
