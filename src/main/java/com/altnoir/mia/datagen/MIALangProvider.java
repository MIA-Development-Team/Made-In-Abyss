package com.altnoir.mia.datagen;

import com.altnoir.mia.content.block.BlocksRegister;
import com.altnoir.mia.content.items.ItemsRegister;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MIALangProvider extends LanguageProvider {
    private final DataGenerator gen;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public MIALangProvider(PackOutput output, String modid, String locale, DataGenerator gen) {
        super(output, modid, locale);
        this.gen = gen;
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.mia.mia", "Made In Abyss");

        BlocksRegister.getAll().stream()
                .map(RegistryObject::get)
                .forEach(block -> {
                    add(block, formatName(BuiltInRegistries.BLOCK.getKey(block).getPath()));
                });
        ItemsRegister.getAll().stream()
                .map(RegistryObject::get)
                .filter(item -> !(item instanceof BlockItem))
                .forEach(item -> {
                    add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath()));
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
