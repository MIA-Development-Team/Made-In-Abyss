package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.AbyssPortalFeature;
import com.mementoinabyss.recall.data.GuideBuilder;
import com.mementoinabyss.recall.data.scene.SceneBuilder;
import com.mementoinabyss.recall.data.scene.SceneSelection;
import com.mementoinabyss.recall.data.scene.SceneStructure;
import com.mementoinabyss.recall.data.scene.SceneStructure.Position;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/** Generates guide nodes, article metadata and explicitly authored connections in the asset pack. */
public final class MiaGuideDataProvider implements DataProvider {
    private static final Identifier ABYSS = Identifier.parse("mementoinabyss:abyss");
    private static final Identifier SECOND_LAYER = Identifier.parse("mementoinabyss:second_layer");
    private final PackOutput.PathProvider paths;
    private final PackOutput.PathProvider scenePaths;

    public MiaGuideDataProvider(PackOutput output) {
        paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "abyss_guide/maps");
        scenePaths =
                output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "abyss_guide/scenes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
                DataProvider.saveStable(output, abyss().toJson(), paths.json(ABYSS)),
                DataProvider.saveStable(output, secondLayer().toJson(), paths.json(SECOND_LAYER)),
                abyssPortal()
                        .saveTimeline(
                                output,
                                scenePaths,
                                Identifier.parse("mementoinabyss:abyss_portal")));
    }

    static SceneBuilder abyssPortal() {
        var scene =
                SceneBuilder.fromNbtResource(
                                "/assets/mementoinabyss/abyss_guide/scenes/abyss_portal.nbt")
                        .zeroLayer(15);
        validatePortalStructure(scene.structure());
        for (int stage = 1; stage <= 11; stage++) {
            String group = "portal_layer_" + stage;
            int y = -stage;
            scene.reveal(group, stage, 0, 0, 0, 0, SceneSelection.box(6, y, 6, 22, y, 22));
        }
        return scene.title("Activating the Abyss portal")
                .camera(0, 0, 38, 30)
                .tooltip(0, 1, new Position(14, 3, 14), "The Star Compass activates this core.")
                .tooltip(
                        1,
                        2,
                        new Position(22, -1, 14),
                        "Each stage builds a new ring around the cleared center.")
                .tooltip(6, 2, new Position(14, -6, 14), "Fossilized wood forms the center column.")
                .tooltip(
                        12,
                        2,
                        new Position(14, -12, 13),
                        "The portal surface appears on the twelfth stage.")
                .caption(0, 1.0F, "Use the Star Compass on the Abyss Portal Core.")
                .caption(1, 1.0F, "Stage 1/12: the core clears the first layer and builds its rim.")
                .caption(2, 1.0F, "Stage 2/12: another rim and center block appear below it.")
                .caption(3, 1.0F, "Stage 3/12: the ring wall grows downward.")
                .caption(
                        4,
                        1.0F,
                        "Stage 4/12: the core clears the next layer and rebuilds its edge.")
                .caption(5, 1.0F, "Stage 5/12: a fossilized-wood column forms at the center.")
                .caption(6, 1.0F, "Stage 6/12: the rim continues around the cleared center.")
                .caption(7, 1.0F, "Stage 7/12: the column and surrounding wall extend downward.")
                .caption(8, 1.0F, "Stage 8/12: the core builds another layer of the ring.")
                .caption(9, 1.0F, "Stage 9/12: the edge takes shape around the opening.")
                .caption(10, 1.0F, "Stage 10/12: the ring approaches the portal level.")
                .caption(11, 1.0F, "Stage 11/12: the final wall layer is in place.")
                .reveal(
                        "portal_surface",
                        12.0F,
                        0,
                        0,
                        0,
                        0,
                        SceneSelection.box(5, -12, 5, 23, -12, 23))
                .caption(12.0F, 1.0F, "Stage 12/12: the Abyss portal is created below the core.")
                .camera(12.0F, 1.5F, -35, 25)
                .caption(13.0F, 2.5F, "The portal is active. Step into its surface to travel.");
    }

    private static void validatePortalStructure(SceneStructure structure) {
        Map<Position, String> blocks =
                structure.blocks().stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        SceneStructure.Block::position,
                                        block -> block.state().name().toString()));
        var core =
                structure.blocks().stream()
                        .filter(block -> block.position().equals(new Position(14, 18, 14)))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Portal core missing from scene NBT"));
        if (!core.state().name().toString().equals("mementoinabyss:abyss_portal_core")
                || !core.state().properties().getOrDefault("compass", "false").equals("true")
                || !core.state().properties().getOrDefault("stage", "-1").equals("0"))
            throw new IllegalArgumentException("Portal core must start at compass=true, stage=0");

        Set<String> rimBlocks =
                Set.of(
                        "mementoinabyss:fossilized_wood",
                        "minecraft:polished_tuff",
                        "minecraft:tuff");
        int radius = AbyssPortalFeature.RADIUS;
        for (int stage = 1; stage <= 12; stage++) {
            int y = 15 - stage;
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int z = -radius - 1; z <= radius + 1; z++) {
                    boolean interior = AbyssPortalFeature.isPortalInterior(x, z);
                    boolean rim = AbyssPortalFeature.isPortalRim(x, z);
                    if (!interior && !rim) continue;
                    Position position = new Position(14 + x, y, 14 + z);
                    String actual = blocks.get(position);
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
                                    : java.util.Objects.equals(expected, actual);
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

    static GuideBuilder abyss() {
        return new GuideBuilder(ABYSS, "guide.mementoinabyss.first_layer.title")
                .subtitle("guide.mementoinabyss.first_layer.subtitle")
                .icon(Identifier.parse("mementoinabyss:star_compass"))
                .background(Identifier.parse("mementoinabyss:skybox/the_abyss"))
                .layer(1)
                .start("entrance")
                .article(
                        "getting_started",
                        "Getting Started",
                        "Learn how the map, nodes, cards, and inline articles fit together.",
                        "articles/getting_started.md")
                .article(
                        "star_compass",
                        "Star Compass",
                        "Use the Star Compass to keep your route through the Abyss readable.",
                        "articles/star_compass.md")
                .article(
                        "red_whistle",
                        "Red Whistle",
                        "A reference entry for the Red Whistle and its future guide branches.",
                        "articles/red_whistle.md")
                .node(
                        "entrance",
                        Identifier.parse("mementoinabyss:star_compass"),
                        150,
                        220,
                        "Abyss Entrance",
                        "Start here for equipment, routes, and survival notes from the Abyss.",
                        "getting_started",
                        "star_compass")
                .node(
                        "whistle",
                        Identifier.parse("mementoinabyss:red_whistle"),
                        470,
                        180,
                        "Whistle Echoes",
                        "Review the core whistle mechanics and the branches planned for later"
                                + " updates.",
                        "red_whistle")
                .connect("entrance", "whistle");
    }

    static GuideBuilder secondLayer() {
        return new GuideBuilder(SECOND_LAYER, "guide.mementoinabyss.second_layer.title")
                .icon(Identifier.parse("mementoinabyss:echo_reed"))
                .background(Identifier.parse("mementoinabyss:painting/fossil_tree"))
                .layer(2)
                .start("descent")
                .node(
                        "descent",
                        Identifier.parse("mementoinabyss:rope"),
                        130,
                        120,
                        "Descent Point",
                        "Prepare rope and supplies before descending from the entrance.")
                .node(
                        "echo_reed",
                        Identifier.parse("mementoinabyss:echo_reed"),
                        350,
                        100,
                        "Echo Reed",
                        "Record the uses of echo reeds and mark their locations along the route.")
                .node(
                        "caerulite",
                        Identifier.parse("mementoinabyss:caerulite_shard"),
                        220,
                        300,
                        "Caerulite Vein",
                        "Gather caerulite shards and mark a safe route through the vein.")
                .node(
                        "relic",
                        Identifier.parse("mementoinabyss:test_artifact_1"),
                        490,
                        270,
                        "Relic Cache",
                        "Record recovered relics, nearby hazards, and a safe return route.")
                .node(
                        "resonance",
                        Identifier.parse("mementoinabyss:resonance_pressure_fragment"),
                        390,
                        410,
                        "Resonance Fragment",
                        "Keep notes on resonance pressure fragments for a later field guide.")
                .connect("descent", "echo_reed")
                .connect("descent", "caerulite")
                .connect("echo_reed", "relic")
                .connect("caerulite", "relic")
                .connect("relic", "resonance");
    }

    @Override
    public String getName() {
        return "Memento In Abyss guide maps and scene timelines";
    }
}
