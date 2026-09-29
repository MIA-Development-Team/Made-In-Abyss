package com.mementoinabyss.recall.data.scene;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mementoinabyss.recall.data.scene.SceneStructure.Position;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;

/** Datagen for a timeline paired with a manually authored vanilla structure NBT. */
public final class SceneBuilder {
    private final Position size;
    private int zeroLayer;
    private boolean hasZeroLayer;
    private final Map<Position, SceneStructure.Block> blocks = new LinkedHashMap<>();
    private final JsonArray instructions = new JsonArray();

    private SceneBuilder(int x, int y, int z) {
        size = new Position(x, y, z);
    }

    /** Reads a manually authored structure NBT from the main resource pack. */
    public static SceneBuilder fromNbtResource(String resourcePath) {
        try (InputStream input = SceneBuilder.class.getResourceAsStream(resourcePath)) {
            if (input == null)
                throw new IllegalArgumentException("Missing structure resource: " + resourcePath);
            var nbt = NbtIo.readCompressed(input, NbtAccounter.create(32 * 1024 * 1024));
            if (nbt.contains("DataVersion"))
                nbt =
                        DataFixTypes.STRUCTURE.updateToCurrentVersion(
                                DataFixers.getDataFixer(),
                                nbt,
                                nbt.getInt("DataVersion").orElseThrow());
            var structure = SceneStructure.fromNbt(nbt);
            var builder =
                    new SceneBuilder(
                            structure.size().x(), structure.size().y(), structure.size().z());
            for (var block : structure.blocks()) builder.blocks.put(block.position(), block);
            return builder;
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Could not read structure resource: " + resourcePath, exception);
        }
    }

    public SceneBuilder zeroLayer(int y) {
        if (y < 0 || y >= size.y())
            throw new IllegalArgumentException("Zero layer must be inside the scene NBT");
        zeroLayer = y;
        hasZeroLayer = true;
        return this;
    }

    public SceneBuilder title(String title) {
        var json = instruction("title", 0, 0);
        json.addProperty("text", title);
        return this;
    }

    public SceneBuilder camera(float at, float duration, float yaw, float pitch) {
        var json = instruction("camera", at, duration);
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
        return this;
    }

    public SceneBuilder caption(float at, float duration, String text) {
        var json = instruction("caption", at, duration);
        json.addProperty("text", text);
        return this;
    }

    public SceneBuilder tooltip(float at, float duration, Position block, String text) {
        var json = instruction("tooltip", at, duration);
        json.add("block", block.toJson());
        json.addProperty("text", text);
        return this;
    }

    public SceneBuilder hold(float at, float duration) {
        instruction("wait", at, duration);
        return this;
    }

    public SceneBuilder reveal(
            String group,
            float at,
            float duration,
            float x,
            float y,
            float z,
            SceneSelection... selection) {
        var json = groupInstruction("reveal", group, at, duration, "offset", x, y, z);
        if (selection.length > 0) {
            var array = new JsonArray();
            for (var box : selection) array.add(box.toJson());
            json.add("selection", array);
        }
        return this;
    }

    public SceneBuilder move(String group, float at, float duration, float x, float y, float z) {
        groupInstruction("move", group, at, duration, "offset", x, y, z);
        return this;
    }

    public SceneBuilder rotate(String group, float at, float duration, float x, float y, float z) {
        groupInstruction("rotate", group, at, duration, "rotation", x, y, z);
        return this;
    }

    public SceneBuilder hide(String group, float at, float duration, float x, float y, float z) {
        groupInstruction("hide", group, at, duration, "offset", x, y, z);
        return this;
    }

    private JsonObject groupInstruction(
            String type,
            String group,
            float at,
            float duration,
            String vectorKey,
            float x,
            float y,
            float z) {
        var json = instruction(type, at, duration);
        json.addProperty("group", group);
        var vector = new JsonArray();
        vector.add(x);
        vector.add(y);
        vector.add(z);
        json.add(vectorKey, vector);
        return json;
    }

    private JsonObject instruction(String type, float at, float duration) {
        if (instructions.size() >= 128)
            throw new IllegalArgumentException("Scene exceeds 128 instructions");
        var json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("at", at);
        if (duration != 0) json.addProperty("duration", duration);
        instructions.add(json);
        return json;
    }

    public SceneStructure structure() {
        return new SceneStructure(size, blocks.values().stream().toList());
    }

    public JsonObject timeline() {
        var json = new JsonObject();
        json.add("timeline", instructions.deepCopy());
        if (hasZeroLayer) json.addProperty("zeroLayer", zeroLayer);
        SceneDefinition.parse(structure(), json); // Datagen and runtime share all validation.
        return json;
    }

    /** Writes only the paired timeline JSON; the NBT remains a hand-authored resource. */
    public CompletableFuture<?> saveTimeline(
            CachedOutput output, PackOutput.PathProvider paths, Identifier id) {
        return DataProvider.saveStable(output, timeline(), paths.json(id));
    }
}
