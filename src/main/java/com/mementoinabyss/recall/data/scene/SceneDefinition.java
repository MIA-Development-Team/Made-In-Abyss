package com.mementoinabyss.recall.data.scene;

import com.google.gson.JsonObject;
import com.mementoinabyss.recall.data.scene.SceneStructure.Position;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** NBT supplies all block content; the paired JSON supplies temporal instructions and selections. */
public record SceneDefinition(
        List<Block> blocks, Set<String> groups, SceneTimeline timeline, int zeroLayer) {
    public SceneDefinition {
        blocks = List.copyOf(blocks);
        groups = Set.copyOf(groups);
    }

    public static SceneDefinition parse(SceneStructure structure, JsonObject json) {
        if (!json.keySet().equals(Set.of("timeline"))
                && !json.keySet().equals(Set.of("timeline", "zeroLayer")))
            throw new IllegalArgumentException(
                    "Scene JSON must contain only timeline and its zeroLayer coordinate origin");
        int zeroLayer = 0;
        if (json.has("zeroLayer")) {
            double value = json.get("zeroLayer").getAsDouble();
            if (!Double.isFinite(value)
                    || value != Math.rint(value)
                    || value < 0
                    || value >= structure.size().y())
                throw new IllegalArgumentException("Scene zeroLayer must be inside the NBT height");
            zeroLayer = (int) value;
        }
        var instructions = json.getAsJsonArray("timeline");
        if (instructions.size() > 128)
            throw new IllegalArgumentException("Scene exceeds 128 instructions");
        Map<Position, String> membership = new HashMap<>();
        Set<String> declared = new HashSet<>();
        for (var element : instructions) {
            var instruction = element.getAsJsonObject();
            if (!instruction.has("selection")) continue;
            String type = instruction.get("type").getAsString();
            if (!Set.of("reveal", "hide", "move", "rotate").contains(type))
                throw new IllegalArgumentException(
                        "Only group instructions may select structure blocks");
            String group = instruction.get("group").getAsString();
            if (!group.matches("[a-z0-9_-]+") || group.equals("main") || !declared.add(group))
                throw new IllegalArgumentException(
                        "A named group must declare its selection exactly once: " + group);
            var selections = instruction.getAsJsonArray("selection");
            if (selections.isEmpty() || selections.size() > 128)
                throw new IllegalArgumentException("Invalid number of group selections");
            boolean matched = false;
            for (var selection : selections) {
                var box = SceneSelection.fromJson(selection.getAsJsonObject(), zeroLayer);
                if (box.to().x() >= structure.size().x()
                        || box.to().y() >= structure.size().y()
                        || box.to().z() >= structure.size().z())
                    throw new IllegalArgumentException("Selection exceeds NBT structure bounds");
                boolean boxMatched = false;
                for (var block : structure.blocks()) {
                    if (!box.contains(block.position())) continue;
                    String previous = membership.putIfAbsent(block.position(), group);
                    if (previous != null && !previous.equals(group))
                        throw new IllegalArgumentException(
                                "Scene groups overlap: " + previous + " / " + group);
                    matched = boxMatched = true;
                }
                if (!boxMatched)
                    throw new IllegalArgumentException("Selection contains no structure blocks");
            }
            if (!matched) throw new IllegalArgumentException("Empty scene group: " + group);
        }
        List<Block> blocks =
                structure.blocks().stream()
                        .map(
                                block ->
                                        new Block(
                                                block.position(),
                                                block.state(),
                                                membership.getOrDefault(block.position(), "main")))
                        .toList();
        Set<String> groups =
                blocks.stream().map(Block::group).collect(java.util.stream.Collectors.toSet());
        return new SceneDefinition(
                blocks,
                groups,
                SceneTimeline.parse(
                        json,
                        groups,
                        zeroLayer,
                        blocks.stream()
                                .map(Block::position)
                                .collect(java.util.stream.Collectors.toSet())),
                zeroLayer);
    }

    public record Block(Position position, SceneStructure.State state, String group) {}
}
