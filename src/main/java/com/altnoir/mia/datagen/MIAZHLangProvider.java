package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MIAZHLangProvider extends LanguageProvider {
    public MIAZHLangProvider(PackOutput output, String locale) {
        super(output, MIA.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemgroup.mia", "来自深渊");

        add("item.mia.purin", "布丁");
        add("item.mia.endless_cup", "无尽水杯");

        add("block.mia.abyss_grass_block", "深界草方块");
        add("block.mia.fortitude_flower", "不屈之花");
        add("block.mia.covergrass_cobblestone", "覆草圆石");
        add("block.mia.covergrass_stone", "覆草石头");
        add("block.mia.covergrass_deepslate", "覆草深板岩");
        add("block.mia.covergrass_granite", "覆草花岗岩");
        add("block.mia.covergrass_diorite", "覆草闪长岩");
        add("block.mia.covergrass_andesite", "覆草安山岩");
        add("block.mia.covergrass_calcite", "覆草方解石");
        add("block.mia.covergrass_tuff", "覆草凝灰岩");
        add("block.mia.abyss_andesite", "深界安山岩");
        add("block.mia.abyss_cobbled_deepslate", "深界安山岩圆石");
        add("block.mia.chlorophyte_ore", "叶绿矿");
        add("block.mia.suspicious_andesite", "可疑的安山岩");
        add("block.mia.volcano_crucible", "火山熔炉之釜");
    }
}
