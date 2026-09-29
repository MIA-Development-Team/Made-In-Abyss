package com.mementoinabyss.recall.client.scene;

import com.google.gson.JsonParser;
import com.mementoinabyss.recall.data.scene.SceneDefinition;
import com.mementoinabyss.recall.data.scene.SceneStructure;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SceneReloadListener
        extends SimplePreparableReloadListener<Map<Identifier, SceneDefinition>> {
    private final String root;

    public SceneReloadListener(String resourceRoot) {
        root = resourceRoot + "/scenes";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SceneReloadListener.class);
    private static Map<Identifier, SceneDefinition> scenes = Map.of();

    public static @Nullable SceneDefinition get(Identifier id) {
        return scenes.get(id);
    }

    @Override
    protected Map<Identifier, SceneDefinition> prepare(
            ResourceManager resources, ProfilerFiller profiler) {
        Map<Identifier, SceneDefinition> loaded = new HashMap<>();
        resources
                .listResources(root, id -> id.getPath().endsWith(".json"))
                .forEach(
                        (path, resource) -> {
                            try (var reader = resource.openAsReader()) {
                                Identifier id =
                                        Identifier.fromNamespaceAndPath(
                                                path.getNamespace(),
                                                path.getPath()
                                                        .substring(
                                                                root.length() + 1,
                                                                path.getPath().length() - 5));
                                Identifier structurePath =
                                        path.withPath(
                                                path.getPath()
                                                                .substring(
                                                                        0,
                                                                        path.getPath().length() - 5)
                                                        + ".nbt");
                                try (var input =
                                        resources.getResourceOrThrow(structurePath).open()) {
                                    var nbt =
                                            NbtIo.readCompressed(
                                                    input, NbtAccounter.create(8 * 1024 * 1024));
                                    if (nbt.contains("DataVersion"))
                                        nbt =
                                                DataFixTypes.STRUCTURE.updateToCurrentVersion(
                                                        DataFixers.getDataFixer(),
                                                        nbt,
                                                        nbt.getInt("DataVersion").orElseThrow());
                                    loaded.put(
                                            id,
                                            SceneDefinition.parse(
                                                    SceneStructure.fromNbt(nbt),
                                                    JsonParser.parseReader(reader)
                                                            .getAsJsonObject()));
                                }
                            } catch (Exception exception) {
                                LOGGER.error("Could not load guide scene {}", path, exception);
                            }
                        });
        return loaded;
    }

    @Override
    protected void apply(
            Map<Identifier, SceneDefinition> loaded,
            ResourceManager resources,
            ProfilerFiller profiler) {
        scenes = Map.copyOf(loaded);
        // Replacing the definitions also invalidates each view's cached block models.
        LOGGER.info("Loaded {} Recall scene(s)", scenes.size());
    }
}
