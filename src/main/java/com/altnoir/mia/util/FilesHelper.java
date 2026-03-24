package com.altnoir.mia.util;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FilesHelper {
    private static JsonElement loadJson(InputStream inputStream) {
        try {
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            reader.setLenient(true);
            JsonElement element = Streams.parse(reader);
            reader.close();
            inputStream.close();
            return element;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonElement loadJsonResource(String filepath) {
        return loadJson(ClassLoader.getSystemResourceAsStream(filepath));
    }
}
