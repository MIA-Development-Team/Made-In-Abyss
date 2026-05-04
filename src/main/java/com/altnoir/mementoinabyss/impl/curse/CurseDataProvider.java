package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class CurseDataProvider implements DataProvider {
    private final String modId;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    private final Map<Identifier, CurseBuilder> entries = new LinkedHashMap<>();

    private final PackOutput.PathProvider pathProvider;

    protected CurseDataProvider(
            String modId,
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup
    ) {
        this.modId = modId;
        this.output = output;
        this.lookup = lookup;
        this.pathProvider =
                output.createPathProvider(PackOutput.Target.DATA_PACK, modId + "/curse");
    }

    protected CurseBuilder curse(String path) {
        var id = Identifier.fromNamespaceAndPath(modId, path);
        return entries.computeIfAbsent(id, CurseBuilder::new);
    }

    protected abstract void generate(HolderLookup.Provider lookup);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookup.thenCompose(reg -> {

            entries.clear();
            generate(reg);

            List<CompletableFuture<?>> futures = new ArrayList<>();

            for (CurseBuilder builder : entries.values()) {

                var def = builder.build();
                var encoded = CurseDimension.CODEC
                        .encodeStart(JsonOps.INSTANCE, def)
                        .getOrThrow();

                var path = pathProvider.json(def.id());

                futures.add(
                        DataProvider.saveStable(cache, encoded, path)
                );
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    @Override
    public String getName() {
        return "Curse data for " + this.modId;
    }
}
