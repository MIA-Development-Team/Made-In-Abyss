package com.mementoinabyss.recall.data.scene;

import com.google.gson.JsonObject;
import com.mementoinabyss.recall.data.scene.SceneStructure.Position;

/** Inclusive selection of an authored NBT structure; it never supplies replacement blocks. */
public record SceneSelection(Position from, Position to) {
    public SceneSelection {
        if (from.x() < 0
                || from.y() < -SceneStructure.MAX_SIZE + 1
                || from.z() < 0
                || to.x() >= SceneStructure.MAX_SIZE
                || to.y() >= SceneStructure.MAX_SIZE
                || to.z() >= SceneStructure.MAX_SIZE
                || from.x() > to.x()
                || from.y() > to.y()
                || from.z() > to.z())
            throw new IllegalArgumentException("Invalid scene selection bounds");
    }

    public static SceneSelection box(int x0, int y0, int z0, int x1, int y1, int z1) {
        return new SceneSelection(new Position(x0, y0, z0), new Position(x1, y1, z1));
    }

    public static SceneSelection block(int x, int y, int z) {
        return box(x, y, z, x, y, z);
    }

    public static SceneSelection fromJson(JsonObject json, int zeroLayer) {
        var from = Position.fromJson(json.getAsJsonArray("from"), zeroLayer);
        return new SceneSelection(
                from,
                json.has("to") ? Position.fromJson(json.getAsJsonArray("to"), zeroLayer) : from);
    }

    public JsonObject toJson() {
        var json = new JsonObject();
        json.add("from", from.toJson());
        json.add("to", to.toJson());
        return json;
    }

    public boolean contains(Position pos) {
        return pos.x() >= from.x()
                && pos.x() <= to.x()
                && pos.y() >= from.y()
                && pos.y() <= to.y()
                && pos.z() >= from.z()
                && pos.z() <= to.z();
    }
}
