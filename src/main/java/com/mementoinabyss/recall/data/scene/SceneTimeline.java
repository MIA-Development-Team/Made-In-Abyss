package com.mementoinabyss.recall.data.scene;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mementoinabyss.recall.data.scene.SceneStructure.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Absolute-time scene instructions: every frame can be evaluated independently, including backward seeks. */
public record SceneTimeline(float duration, List<Instruction> instructions) {
    public SceneTimeline {
        instructions = List.copyOf(instructions);
    }

    public static SceneTimeline parse(
            JsonObject root, Set<String> groups, int zeroLayer, Set<Position> blocks) {
        if (!root.keySet().equals(Set.of("timeline"))
                && !root.keySet().equals(Set.of("timeline", "zeroLayer")))
            throw new IllegalArgumentException("Scene JSON must contain only timeline data");
        List<Instruction> instructions = new ArrayList<>();
        float end = 0;
        if (root.has("timeline")) {
            JsonArray array = root.getAsJsonArray("timeline");
            if (array.size() > 128)
                throw new IllegalArgumentException("Scene exceeds 128 instructions");
            for (var element : array) {
                JsonObject json = element.getAsJsonObject();
                Type type =
                        Type.valueOf(
                                json.get("type").getAsString().toUpperCase(java.util.Locale.ROOT));
                String group = json.has("group") ? json.get("group").getAsString() : "main";
                if (type.grouped() && !groups.contains(group))
                    throw new IllegalArgumentException("Unknown scene group: " + group);
                float at = number(json, "at", 0, 0, 600);
                float duration = number(json, "duration", 0, 0, 600);
                Position block = null;
                String text = json.has("text") ? json.get("text").getAsString() : "";
                if (type == Type.TOOLTIP) {
                    if (!json.has("block"))
                        throw new IllegalArgumentException(
                                "Scene tooltip requires a block coordinate");
                    block = Position.fromJson(json.getAsJsonArray("block"), zeroLayer);
                    if (!blocks.contains(block))
                        throw new IllegalArgumentException(
                                "Scene tooltip targets a missing NBT block: " + block);
                    if (duration <= 0 || text.isBlank() || text.length() > 160)
                        throw new IllegalArgumentException(
                                "Scene tooltip requires text and a duration");
                } else if (json.has("block")) {
                    throw new IllegalArgumentException("Only tooltips may target a block");
                }
                instructions.add(
                        new Instruction(
                                type,
                                group,
                                at,
                                duration,
                                vector(json, "offset"),
                                vector(json, "rotation"),
                                number(json, "yaw", 35, -720, 720),
                                number(json, "pitch", 30, 10, 80),
                                block,
                                text));
                end = Math.max(end, at + duration);
            }
        }
        instructions.sort(Comparator.comparing(Instruction::at));
        if (end > 600) throw new IllegalArgumentException("Scene exceeds 600 seconds");
        return new SceneTimeline(end, instructions);
    }

    public Frame evaluate(float time, Set<String> groups) {
        Map<String, GroupPose> poses = new LinkedHashMap<>();
        Map<String, Vector> transitions = new LinkedHashMap<>();
        for (String group : groups) {
            boolean hidden =
                    instructions.stream()
                            .filter(
                                    i ->
                                            i.group.equals(group)
                                                    && (i.type == Type.REVEAL
                                                            || i.type == Type.HIDE))
                            .findFirst()
                            .map(i -> i.type == Type.REVEAL)
                            .orElse(false);
            poses.put(group, new GroupPose(!hidden, Vector.ZERO, Vector.ZERO));
        }
        float yaw = 35, pitch = 30;
        String caption = "", title = "Scene";
        Tooltip tooltip = null;
        for (Instruction instruction : instructions) {
            if (time < instruction.at) continue;
            float linear =
                    instruction.duration == 0
                            ? 1
                            : Math.clamp((time - instruction.at) / instruction.duration, 0, 1);
            float progress = linear * linear * (3 - 2 * linear);
            if (instruction.type == Type.CAMERA) {
                yaw += (instruction.yaw - yaw) * progress;
                pitch += (instruction.pitch - pitch) * progress;
            } else if (instruction.type == Type.TITLE) {
                title = instruction.text;
            } else if (instruction.type == Type.CAPTION) {
                if (time < instruction.at + instruction.duration) caption = instruction.text;
            } else if (instruction.type == Type.TOOLTIP) {
                if (time < instruction.at + instruction.duration)
                    tooltip = new Tooltip(instruction.block, instruction.text);
            } else if (instruction.type == Type.WAIT) {
                // A hold extends the timeline without changing any scene state.
                continue;
            } else {
                GroupPose previous = poses.get(instruction.group);
                boolean visible = previous.visible;
                Vector offset = previous.offset, rotation = previous.rotation;
                switch (instruction.type) {
                    case REVEAL -> {
                        visible = true;
                        transitions.put(instruction.group, instruction.offset.scale(1 - progress));
                    }
                    case HIDE -> {
                        visible = linear < 1;
                        transitions.put(instruction.group, instruction.offset.scale(progress));
                    }
                    case MOVE -> offset = offset.add(instruction.offset.scale(progress));
                    case ROTATE -> rotation = rotation.add(instruction.rotation.scale(progress));
                    default -> throw new IllegalStateException("Unexpected group instruction");
                }
                poses.put(instruction.group, new GroupPose(visible, offset, rotation));
            }
        }
        poses.replaceAll(
                (group, pose) ->
                        new GroupPose(
                                pose.visible,
                                pose.offset.add(transitions.getOrDefault(group, Vector.ZERO)),
                                pose.rotation));
        return new Frame(Map.copyOf(poses), yaw, pitch, caption, title, tooltip);
    }

    private static Vector vector(JsonObject json, String key) {
        if (!json.has(key)) return Vector.ZERO;
        JsonArray array = json.getAsJsonArray(key);
        if (array.size() != 3)
            throw new IllegalArgumentException("Expected three components: " + key);
        float[] values = new float[3];
        for (int i = 0; i < 3; i++) {
            values[i] = array.get(i).getAsFloat();
            if (!Float.isFinite(values[i])
                    || Math.abs(values[i]) > (key.equals("rotation") ? 720 : 32))
                throw new IllegalArgumentException("Invalid scene " + key);
        }
        return new Vector(values[0], values[1], values[2]);
    }

    private static float number(JsonObject json, String key, float fallback, float min, float max) {
        float value = json.has(key) ? json.get(key).getAsFloat() : fallback;
        if (!Float.isFinite(value) || value < min || value > max)
            throw new IllegalArgumentException("Invalid timeline " + key);
        return value;
    }

    public enum Type {
        REVEAL,
        HIDE,
        MOVE,
        ROTATE,
        CAMERA,
        CAPTION,
        TITLE,
        TOOLTIP,
        WAIT;

        boolean grouped() {
            return this == REVEAL || this == HIDE || this == MOVE || this == ROTATE;
        }
    }

    public record Instruction(
            Type type,
            String group,
            float at,
            float duration,
            Vector offset,
            Vector rotation,
            float yaw,
            float pitch,
            Position block,
            String text) {}

    public record Vector(float x, float y, float z) {
        public static final Vector ZERO = new Vector(0, 0, 0);

        public Vector add(Vector other) {
            return new Vector(x + other.x, y + other.y, z + other.z);
        }

        public Vector scale(float value) {
            return new Vector(x * value, y * value, z * value);
        }
    }

    public record GroupPose(boolean visible, Vector offset, Vector rotation) {}

    public record Tooltip(Position block, String text) {}

    public record Frame(
            Map<String, GroupPose> groups,
            float yaw,
            float pitch,
            String caption,
            String title,
            Tooltip tooltip) {}
}
