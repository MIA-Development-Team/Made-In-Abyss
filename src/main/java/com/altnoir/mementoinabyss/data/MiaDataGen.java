package com.altnoir.mementoinabyss.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.utility.FilesHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.tterrag.registrate.providers.ProviderType;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MiaDataGen {
    public static void gatherDataHighPriority(GatherDataEvent.Client event) {
        if (event.getModContainer().getModId().contains(MementoInAbyss.ID))
            addExtraRegistrateData();
    }

    public static void gatherData(GatherDataEvent.Client event) {
        if (!event.getModContainer().getModId().equals(MementoInAbyss.ID))
            return;

        event.createProvider(MiaCurseDataProvider::new);
    }

    private static void addExtraRegistrateData() {
        MementoInAbyss.registrate().addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

            provideDefaultLang("interface", langConsumer);
            provideDefaultLang("tooltips", langConsumer);
        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        var path = "assets/mementoinabyss/lang/default/" + fileName + ".json";
        var jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        var jsonObject = jsonElement.getAsJsonObject();
        for (var entry : jsonObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
