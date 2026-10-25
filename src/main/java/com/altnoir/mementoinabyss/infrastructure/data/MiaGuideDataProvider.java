package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.AbyssPortalFeature;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import im.ringlo.recall.api.data.RecallContentProvider;
import im.ringlo.recall.api.data.RecallDataOutput;
import im.ringlo.recall.api.data.SceneTimelineBuilder;
import im.ringlo.recall.api.scene.SceneActions;
import im.ringlo.recall.content.ContentSnapshot;
import im.ringlo.recall.content.scene.SceneStructure;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MiaGuideDataProvider extends RecallContentProvider {
    private static final String NAMESPACE = MementoInAbyss.ID;
    private static final Identifier PORTAL_SCENE = id("abyss_portal");
    private static final Identifier PORTAL_STRUCTURE = id("recall/scenes/abyss_portal.nbt");
    private static final int PORTAL_ZERO_LAYER = 15;
    private static final int PORTAL_CORE_X = 14;
    private static final int PORTAL_CORE_Y = 18;
    private static final int PORTAL_CORE_Z = 14;

    private final ResourceManager resources;

    public MiaGuideDataProvider(PackOutput output, ResourceManager resources) {
        super(output, NAMESPACE, resources);
        this.resources = resources;
    }

    @Override
    protected void build(RecallDataOutput content) {
        content.guide(abyssGuide());
        content.map(firstLayerMap());
        content.map(secondLayerMap());
        validatePortalStructure();
        content.scene(PORTAL_SCENE, abyssPortal());
    }

    @Override
    public String getName() {
        return "Memento In Abyss guide maps and the abyss portal scene";
    }

    private static ContentSnapshot.Guide abyssGuide() {
        return new ContentSnapshot.Guide(
                id("abyss"),
                "Abyss Guide",
                "The Journey Begins",
                List.of(id("abyss"), id("second_layer")),
                id("star_compass"));
    }

    private static ContentSnapshot.MapDefinition firstLayerMap() {
        return new ContentSnapshot.MapDefinition(
                id("abyss"),
                "First Layer",
                List.of(
                        new ContentSnapshot.MapNode(
                                id("entrance"),
                                List.of(id("getting_started"), id("star_compass")),
                                150f,
                                220f,
                                "Abyss Entrance",
                                "Start here for equipment, routes, and survival notes from the"
                                        + " Abyss.",
                                id("star_compass")),
                        new ContentSnapshot.MapNode(
                                id("whistle"),
                                List.of(id("red_whistle")),
                                470f,
                                180f,
                                "Whistle Echoes",
                                "Review the core whistle mechanics and the branches planned for"
                                        + " later updates.",
                                id("red_whistle"))),
                List.of(new ContentSnapshot.MapEdge(id("entrance"), id("whistle"), "related")),
                id("entrance"));
    }

    private static ContentSnapshot.MapDefinition secondLayerMap() {
        return new ContentSnapshot.MapDefinition(
                id("second_layer"),
                "Second Layer",
                List.of(
                        new ContentSnapshot.MapNode(
                                id("descent"),
                                List.of(),
                                130f,
                                120f,
                                "Descent Point",
                                "Prepare rope and supplies before descending from the entrance.",
                                id("rope")),
                        new ContentSnapshot.MapNode(
                                id("echo_reed"),
                                List.of(),
                                350f,
                                100f,
                                "Echo Reed",
                                "Record the uses of echo reeds and mark their locations along the"
                                        + " route.",
                                id("echo_reed")),
                        new ContentSnapshot.MapNode(
                                id("caerulite"),
                                List.of(),
                                220f,
                                300f,
                                "Caerulite Vein",
                                "Gather caerulite shards and mark a safe route through the vein.",
                                id("caerulite_shard")),
                        new ContentSnapshot.MapNode(
                                id("relic"),
                                List.of(),
                                490f,
                                270f,
                                "Relic Cache",
                                "Record recovered relics, nearby hazards, and a safe return route.",
                                id("test_artifact_1")),
                        new ContentSnapshot.MapNode(
                                id("resonance"),
                                List.of(),
                                390f,
                                410f,
                                "Resonance Fragment",
                                "Keep notes on resonance pressure fragments for a later field"
                                        + " guide.",
                                id("resonance_pressure_fragment"))),
                List.of(
                        new ContentSnapshot.MapEdge(id("descent"), id("echo_reed"), "related"),
                        new ContentSnapshot.MapEdge(id("descent"), id("caerulite"), "related"),
                        new ContentSnapshot.MapEdge(id("echo_reed"), id("relic"), "related"),
                        new ContentSnapshot.MapEdge(id("caerulite"), id("relic"), "related"),
                        new ContentSnapshot.MapEdge(id("relic"), id("resonance"), "related")),
                id("descent"));
    }

    static SceneTimelineBuilder abyssPortal() {
        var timeline = new SceneTimelineBuilder(PORTAL_ZERO_LAYER);
        timeline.action(
                SceneActions.INSTANCE.getTITLE(),
                0f,
                null,
                parameters(p -> p.addProperty("text", "Activating the Abyss portal")));
        timeline.action(
                SceneActions.INSTANCE.getCAMERA(),
                0f,
                0f,
                parameters(
                        p -> {
                            p.addProperty("yaw", 38);
                            p.addProperty("pitch", 30);
                        }));
        timeline.action(
                SceneActions.INSTANCE.getTOOLTIP(),
                0f,
                1f,
                parameters(
                        p -> {
                            p.add("block", vector(PORTAL_CORE_X, 3, PORTAL_CORE_Z));
                            p.addProperty("text", "The Star Compass activates this core.");
                        }));
        timeline.action(
                SceneActions.INSTANCE.getCAPTION(),
                0f,
                1f,
                parameters(
                        p ->
                                p.addProperty(
                                        "text", "Use the Star Compass on the Abyss Portal Core.")));
        for (int stage = 1; stage <= 11; stage++) {
            int layer = -stage;
            String group = "portal_layer_" + stage;
            String caption = stageCaption(stage);
            timeline.action(
                    SceneActions.INSTANCE.getREVEAL(),
                    (float) stage,
                    0f,
                    parameters(
                            p -> {
                                p.addProperty("group", group);
                                p.add("offset", vector(0, 0, 0));
                                p.add("selection", selection(6, layer, 6, 22, layer, 22));
                            }));
            timeline.action(
                    SceneActions.INSTANCE.getCAPTION(),
                    (float) stage,
                    1f,
                    parameters(p -> p.addProperty("text", caption)));
        }
        timeline.action(
                SceneActions.INSTANCE.getTOOLTIP(),
                1f,
                2f,
                parameters(
                        p -> {
                            p.add("block", vector(22, -1, 14));
                            p.addProperty(
                                    "text",
                                    "Each stage builds a new ring around the cleared center.");
                        }));
        timeline.action(
                SceneActions.INSTANCE.getTOOLTIP(),
                6f,
                2f,
                parameters(
                        p -> {
                            p.add("block", vector(14, -6, 14));
                            p.addProperty("text", "Fossilized wood forms the center column.");
                        }));
        timeline.action(
                SceneActions.INSTANCE.getREVEAL(),
                12f,
                0f,
                parameters(
                        p -> {
                            p.addProperty("group", "portal_surface");
                            p.add("offset", vector(0, 0, 0));
                            p.add("selection", selection(5, -12, 5, 23, -12, 23));
                        }));
        timeline.action(
                SceneActions.INSTANCE.getCAPTION(),
                12f,
                1f,
                parameters(
                        p ->
                                p.addProperty(
                                        "text",
                                        "Stage 12/12: the Abyss portal is created below the"
                                                + " core.")));
        timeline.action(
                SceneActions.INSTANCE.getTOOLTIP(),
                12f,
                2f,
                parameters(
                        p -> {
                            p.add("block", vector(14, -12, 13));
                            p.addProperty(
                                    "text", "The portal surface appears on the twelfth stage.");
                        }));
        timeline.action(
                SceneActions.INSTANCE.getCAMERA(),
                12f,
                1.5f,
                parameters(
                        p -> {
                            p.addProperty("yaw", -35);
                            p.addProperty("pitch", 25);
                        }));
        timeline.action(
                SceneActions.INSTANCE.getCAPTION(),
                13f,
                2.5f,
                parameters(
                        p ->
                                p.addProperty(
                                        "text",
                                        "The portal is active. Step into its surface to travel.")));
        return timeline;
    }

    private static String stageCaption(int stage) {
        return switch (stage) {
            case 1 -> "Stage 1/12: the core clears the first layer and builds its rim.";
            case 2 -> "Stage 2/12: another rim and center block appear below it.";
            case 3 -> "Stage 3/12: the ring wall grows downward.";
            case 4 -> "Stage 4/12: the core clears the next layer and rebuilds its edge.";
            case 5 -> "Stage 5/12: a fossilized-wood column forms at the center.";
            case 6 -> "Stage 6/12: the rim continues around the cleared center.";
            case 7 -> "Stage 7/12: the column and surrounding wall extend downward.";
            case 8 -> "Stage 8/12: the core builds another layer of the ring.";
            case 9 -> "Stage 9/12: the edge takes shape around the opening.";
            case 10 -> "Stage 10/12: the ring approaches the portal level.";
            case 11 -> "Stage 11/12: the final wall layer is in place.";
            default -> throw new IllegalArgumentException("Unexpected portal stage " + stage);
        };
    }

    /**
     * Checks that the hand-authored scene NBT still matches the blocks {@link AbyssPortalFeature}
     * writes for every activation stage.
     */
    private void validatePortalStructure() {
        SceneStructure structure;
        try (InputStream input = resources.getResourceOrThrow(PORTAL_STRUCTURE).open()) {
            structure = SceneStructure.Companion.read(input, PORTAL_ZERO_LAYER);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + PORTAL_STRUCTURE, exception);
        }
        Map<List<Integer>, String> blocks = new LinkedHashMap<>();
        for (ContentSnapshot.SceneBlock block : structure.getBlocks())
            blocks.put(List.of(block.getX(), block.getY(), block.getZ()), block.getState());

        String core =
                blocks.get(
                        List.of(PORTAL_CORE_X, PORTAL_CORE_Y - PORTAL_ZERO_LAYER, PORTAL_CORE_Z));
        if (core == null) throw new IllegalArgumentException("Portal core missing from scene NBT");
        Map<String, String> coreProperties = properties(core);
        if (!stateName(core).equals("mementoinabyss:abyss_portal_core")
                || !coreProperties.getOrDefault("compass", "false").equals("true")
                || !coreProperties.getOrDefault("stage", "-1").equals("0"))
            throw new IllegalArgumentException("Portal core must start at compass=true, stage=0");

        Set<String> rimBlocks =
                Set.of(
                        "mementoinabyss:fossilized_wood",
                        "minecraft:polished_tuff",
                        "minecraft:tuff");
        int radius = AbyssPortalFeature.RADIUS;
        for (int stage = 1; stage <= 12; stage++) {
            int y = -stage;
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int z = -radius - 1; z <= radius + 1; z++) {
                    boolean interior = AbyssPortalFeature.isPortalInterior(x, z);
                    boolean rim = AbyssPortalFeature.isPortalRim(x, z);
                    if (!interior && !rim) continue;
                    List<Integer> position = List.of(PORTAL_CORE_X + x, y, PORTAL_CORE_Z + z);
                    String state = blocks.get(position);
                    String actual = state == null ? null : stateName(state);
                    String expected =
                            x == 0 && z == 0
                                    ? "mementoinabyss:fossilized_wood"
                                    : stage == 12
                                            ? interior
                                                    ? "mementoinabyss:abyss_portal"
                                                    : "mementoinabyss:abyss_portal_frame"
                                            : interior ? null : "rim";
                    boolean matches =
                            "rim".equals(expected)
                                    ? rimBlocks.contains(actual)
                                    : Objects.equals(expected, actual);
                    if (!matches)
                        throw new IllegalArgumentException(
                                "Scene NBT differs from portal core stage "
                                        + stage
                                        + " at "
                                        + position
                                        + ": expected "
                                        + expected
                                        + ", got "
                                        + actual);
                }
            }
        }
    }

    private static String stateName(String state) {
        int bracket = state.indexOf('[');
        return bracket < 0 ? state : state.substring(0, bracket);
    }

    private static Map<String, String> properties(String state) {
        int bracket = state.indexOf('[');
        if (bracket < 0 || !state.endsWith("]")) return Map.of();
        Map<String, String> properties = new LinkedHashMap<>();
        for (String pair : state.substring(bracket + 1, state.length() - 1).split(",")) {
            int separator = pair.indexOf('=');
            if (separator > 0)
                properties.put(pair.substring(0, separator), pair.substring(separator + 1));
        }
        return properties;
    }

    private static Function1<JsonObject, Unit> parameters(Consumer<JsonObject> builder) {
        return json -> {
            builder.accept(json);
            return Unit.INSTANCE;
        };
    }

    private static JsonArray vector(Number... coordinates) {
        var array = new JsonArray();
        for (Number coordinate : coordinates) array.add(coordinate);
        return array;
    }

    private static JsonArray selection(int x1, int y1, int z1, int x2, int y2, int z2) {
        var box = new JsonObject();
        box.add("from", vector(x1, y1, z1));
        box.add("to", vector(x2, y2, z2));
        var array = new JsonArray();
        array.add(box);
        return array;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
