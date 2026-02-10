package com.altnoir.mia.core.curse;

import com.altnoir.mia.core.curse.records.CurseDimension;
import com.altnoir.mia.core.curse.records.CurseEffect;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CurseDataProvider implements DataProvider {
    private final String name;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final List<CurseDimension> definitions = new ArrayList<>();

    public CurseDataProvider(String name, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.name = name;
        this.output = output;
        this.registries = registries;
    }

    protected abstract void addCurse();

    protected void add(ResourceLocation dimension, CurseEffect[] effects, int level) {
        definitions.add(new CurseDimension(dimension, effects, level));
    }

    private void validate(HolderLookup.Provider registries, ResourceLocation dimension, CurseEffect[] effects) {
        //var dimLookup = registries.lookupOrThrow(Registries.LEVEL_STEM);
        var effLookup = registries.lookupOrThrow(Registries.MOB_EFFECT);

        /*
        var dimKey = ResourceKey.create(Registries.LEVEL_STEM, dimension);
        if (dimLookup.get(dimKey).isEmpty()) {
            throw new IllegalArgumentException("Unknown dimension: " + dimension);
        }
        */

        for (CurseEffect effect : effects) {
            if (effect.effect() != null && effLookup.get(effect.effect()).isEmpty()) {
                throw new IllegalArgumentException("Unknown effect: " + effect.effect());
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return registries.thenCompose(lookup -> {
            addCurse();

            var basePath = output.getOutputFolder().resolve("data/" + name + "/mia/curse");
            var futures = new ArrayList<>();

            for (var def : definitions) {
                validate(lookup, def.dimension(), def.curseEffects());

                var root = new JsonObject();
                var effectsArray = new JsonArray();
                for (var effect : def.curseEffects()) {
                    JsonObject e = new JsonObject();
                    e.addProperty("effect", effect.effect().location().toString());
                    e.addProperty("amplifier", effect.amplifier());
                    e.addProperty("duration", effect.duration());
                    effectsArray.add(e);
                }
                root.add("effects", effectsArray);
                root.addProperty("level", def.level());

                var dim = def.dimension();
                var namespace = dim.getNamespace();
                var fileName = dim.getPath() + ".json";
                var path = basePath.resolve(namespace).resolve(fileName);

                var future = DataProvider.saveStable(cachedOutput, root, path);
                futures.add(future);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public String getName() {
        return "Curse data for " + this.name;
    }
}
