package com.altnoir.mementoinabyss.impl.utility;

import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class FilesHelper {
    public static @Nullable JsonElement loadJsonResource(String filepath) {
        return loadJson(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(filepath)));
    }

    private static @Nullable JsonElement loadJson(InputStream inputStream) {
        try {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            reader.setStrictness(Strictness.LENIENT);
            JsonElement element = Streams.parse(reader);
            reader.close();
            inputStream.close();
            return element;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
