package com.mementoinabyss.recall.data.scene;

import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;

/** Bounded vanilla structure NBT. Registry resolution is deferred until client model preparation. */
public record SceneStructure(Position size, List<Block> blocks) {
    public static final int MAX_BLOCKS = 32767;
    public static final int MAX_SIZE = 65;

    public SceneStructure {
        if (size.x < 1
                || size.y < 1
                || size.z < 1
                || size.x > MAX_SIZE
                || size.y > MAX_SIZE
                || size.z > MAX_SIZE)
            throw new IllegalArgumentException("Structure size must be in [1, 65]");
        blocks = List.copyOf(blocks);
        if (blocks.isEmpty() || blocks.size() > MAX_BLOCKS)
            throw new IllegalArgumentException(
                    "Structure must contain 1 to " + MAX_BLOCKS + " non-air blocks");
        var occupied = new HashSet<Position>();
        for (Block block : blocks) {
            Position pos = block.position;
            if (pos.x < 0
                    || pos.y < 0
                    || pos.z < 0
                    || pos.x >= size.x
                    || pos.y >= size.y
                    || pos.z >= size.z)
                throw new IllegalArgumentException("Block outside structure bounds: " + pos);
            if (!occupied.add(pos))
                throw new IllegalArgumentException("Duplicate structure position: " + pos);
        }
    }

    public static SceneStructure fromNbt(CompoundTag tag) {
        return fromNbt(tag, block -> true);
    }

    public static SceneStructure fromNbt(CompoundTag tag, Predicate<Block> include) {
        Position size = Position.fromNbt(tag.getList("size").orElseThrow());
        ListTag palette =
                tag.getList("palette")
                        .orElseGet(
                                () ->
                                        tag.getList("palettes")
                                                .orElseThrow()
                                                .getList(0)
                                                .orElseThrow());
        if (palette.isEmpty() || palette.size() > MAX_BLOCKS)
            throw new IllegalArgumentException("Invalid structure palette size");
        List<State> states = new ArrayList<>();
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag entry = palette.getCompound(i).orElseThrow();
            Map<String, String> properties = new TreeMap<>();
            CompoundTag values = entry.getCompoundOrEmpty("Properties");
            for (String key : values.keySet())
                properties.put(key, values.getString(key).orElseThrow());
            states.add(
                    new State(Identifier.parse(entry.getString("Name").orElseThrow()), properties));
        }
        ListTag entries = tag.getList("blocks").orElseThrow();
        if (entries.size() > MAX_SIZE * MAX_SIZE * MAX_SIZE)
            throw new IllegalArgumentException("Structure placement list is too large");
        List<Block> blocks = new ArrayList<>();
        var occupied = new HashSet<Position>();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i).orElseThrow();
            Position position = Position.fromNbt(entry.getList("pos").orElseThrow());
            if (!occupied.add(position))
                throw new IllegalArgumentException("Duplicate structure position");
            if (position.x < 0
                    || position.y < 0
                    || position.z < 0
                    || position.x >= size.x
                    || position.y >= size.y
                    || position.z >= size.z)
                throw new IllegalArgumentException("Block outside structure bounds");
            int index = entry.getInt("state").orElseThrow();
            if (index < 0 || index >= states.size())
                throw new IllegalArgumentException("Invalid palette index");
            State state = states.get(index);
            if (!state.isAir()) {
                var block = new Block(position, state);
                if (include.test(block)) blocks.add(block);
            }
            if (blocks.size() > MAX_BLOCKS)
                throw new IllegalArgumentException("Structure exceeds " + MAX_BLOCKS + " blocks");
        }
        // Entity and block-entity NBT is intentionally inert: this is a model-only diorama.
        return new SceneStructure(size, blocks);
    }

    public CompoundTag toNbt(int dataVersion) {
        var tag = new CompoundTag();
        tag.putInt("DataVersion", dataVersion);
        tag.put("size", size.toNbt());
        Map<State, Integer> indices = new LinkedHashMap<>();
        var palette = new ListTag();
        var entries = new ListTag();
        for (Block block : blocks) {
            int index =
                    indices.computeIfAbsent(
                            block.state,
                            state -> {
                                var entry = new CompoundTag();
                                entry.putString("Name", state.name.toString());
                                if (!state.properties.isEmpty()) {
                                    var properties = new CompoundTag();
                                    new TreeMap<>(state.properties).forEach(properties::putString);
                                    entry.put("Properties", properties);
                                }
                                palette.add(entry);
                                return palette.size() - 1;
                            });
            var entry = new CompoundTag();
            entry.put("pos", block.position.toNbt());
            entry.putInt("state", index);
            entries.add(entry);
        }
        tag.put("palette", palette);
        tag.put("blocks", entries);
        tag.put("entities", new ListTag());
        return tag;
    }

    public record Position(int x, int y, int z) {
        static Position fromNbt(ListTag values) {
            if (values.size() != 3 || values.stream().anyMatch(value -> !(value instanceof IntTag)))
                throw new IllegalArgumentException("Expected three integer structure coordinates");
            return new Position(
                    values.getInt(0).orElseThrow(),
                    values.getInt(1).orElseThrow(),
                    values.getInt(2).orElseThrow());
        }

        static Position fromJson(JsonArray values, int zeroLayer) {
            if (values.size() != 3)
                throw new IllegalArgumentException("Expected three selection coordinates");
            int[] xyz = new int[3];
            for (int i = 0; i < 3; i++) {
                double value = values.get(i).getAsDouble();
                if (!Double.isFinite(value)
                        || value != Math.rint(value)
                        || (i == 1
                                ? value + zeroLayer < 0 || value + zeroLayer >= MAX_SIZE
                                : value < 0 || value >= MAX_SIZE))
                    throw new IllegalArgumentException("Scene coordinate outside structure bounds");
                xyz[i] = (int) value + (i == 1 ? zeroLayer : 0);
            }
            return new Position(xyz[0], xyz[1], xyz[2]);
        }

        ListTag toNbt() {
            var list = new ListTag();
            list.add(IntTag.valueOf(x));
            list.add(IntTag.valueOf(y));
            list.add(IntTag.valueOf(z));
            return list;
        }

        JsonArray toJson() {
            var array = new JsonArray();
            array.add(x);
            array.add(y);
            array.add(z);
            return array;
        }
    }

    public record State(Identifier name, Map<String, String> properties) {
        public State {
            properties = Map.copyOf(properties);
            for (var entry : properties.entrySet())
                if (!entry.getKey().matches("[a-z0-9_]+")
                        || !entry.getValue().matches("[a-z0-9_-]+"))
                    throw new IllegalArgumentException("Invalid block-state property");
        }

        public String commandString() {
            if (properties.isEmpty()) return name.toString();
            return name
                    + "["
                    + new TreeMap<>(properties)
                            .entrySet().stream()
                                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                                    .collect(java.util.stream.Collectors.joining(","))
                    + "]";
        }

        boolean isAir() {
            return name.getNamespace().equals("minecraft")
                    && switch (name.getPath()) {
                        case "air", "cave_air", "void_air", "structure_void" -> true;
                        default -> false;
                    };
        }
    }

    public record Block(Position position, State state) {}
}
