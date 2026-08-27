package com.altnoir.mia;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import com.altnoir.mia.worldgen.structure.CaveFloorSearch;
import com.altnoir.mia.worldgen.structure.MiaStructureSets;
import com.altnoir.mia.worldgen.structure.MiaStructures;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallCandidate;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanConfig;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanner;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.Tags;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Collectors;


@GameTestHolder(MIA.MOD_ID)
public class MiaGameTests {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void exampleTest(GameTestHelper helper) {
        helper.succeedWhen(() -> {

        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallPlanIsDeterministicSeparatedAndInRange(GameTestHelper helper) {
        AbyssWallPlanConfig config = AbyssWallPlanConfig.DEFAULT;
        AbyssWallPlanner.RadiusPredictor cylinder = (angle, y) -> OptionalDouble.of(200.0);

        List<AbyssWallCandidate> first = AbyssWallPlanner.createPlan(123456789L, config, cylinder);
        List<AbyssWallCandidate> repeated = AbyssWallPlanner.createPlan(123456789L, config, cylinder);
        List<AbyssWallCandidate> otherSeed = AbyssWallPlanner.createPlan(-987654321L, config, cylinder);

        helper.assertValueEqual(first, repeated, "same seed must reproduce the same wall plan");
        helper.assertFalse(first.equals(otherSeed), "different seeds must change the wall plan");
        helper.assertTrue(first.size() >= 16 && first.size() <= 20,
                "default plan must retain approximately 16-20 candidates across its twenty height bands");
        long bufferedMinimumDistance = AbyssWallPlanConfig.DEFAULT.planningAnchorDistance();
        helper.assertValueEqual(AbyssWallPlanConfig.DEFAULT.maxWallAnchorOffset(), 3,
                "the configured embedding value is the maximum radial offset");
        helper.assertValueEqual(bufferedMinimumDistance, 74L,
                "planning distance reserves correction, embedding, and block rounding on both anchors");

        for (int i = 0; i < first.size(); i++) {
            AbyssWallCandidate candidate = first.get(i);
            helper.assertTrue(candidate.y() >= -96 && candidate.y() <= 125,
                    "candidate Y must stay in the allowed windmill range");
            helper.assertTrue(candidate.y() < -3 || candidate.y() > 25,
                    "candidate Y must avoid the horizontal transition shelf from -3 through 25");
            for (int j = 0; j < i; j++) {
                helper.assertTrue(candidate.anchor().distSqr(first.get(j).anchor())
                                >= bufferedMinimumDistance * bufferedMinimumDistance,
                        "predicted anchors must reserve correction space around the final minimum separation");
                helper.assertFalse(candidate.startChunk().equals(first.get(j).startChunk()),
                        "one structure cannot own two candidates in the same start chunk");
            }
        }
        Set<Integer> embedOffsets = first.stream()
                .map(candidate -> AbyssWallPlanner.wallAnchorOffset(123456789L, candidate, config))
                .collect(Collectors.toSet());
        helper.assertTrue(embedOffsets.size() > 1, "deterministic embedding must vary between candidates");
        for (int offset : embedOffsets) {
            helper.assertTrue(offset >= 0 && offset <= 3, "embedding must stay in the inclusive 0-3 range");
        }
        for (AbyssWallCandidate candidate : first) {
            helper.assertValueEqual(
                    AbyssWallPlanner.wallAnchorOffset(123456789L, candidate, config),
                    AbyssWallPlanner.wallAnchorOffset(123456789L, candidate, config),
                    "embedding must be deterministic for a seed and candidate"
            );
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallPlanCodecSerializesTuningKnobs(GameTestHelper helper) {
        JsonObject encoded = AbyssWallPlanConfig.CODEC.encodeStart(JsonOps.INSTANCE, AbyssWallPlanConfig.DEFAULT)
                .getOrThrow()
                .getAsJsonObject();

        helper.assertValueEqual(encoded.get("max_y").getAsInt(), 125,
                "the default data contract must temporarily disable windmills above Y=125");
        helper.assertValueEqual(encoded.get("forbidden_min_y").getAsInt(), -3,
                "the transition shelf lower bound must be serialized");
        helper.assertValueEqual(encoded.get("forbidden_max_y").getAsInt(), 25,
                "the transition shelf upper bound must be serialized");
        helper.assertValueEqual(encoded.get("final_density_inward_search").getAsInt(), 32,
                "the inward final-density search distance must be serialized");
        helper.assertValueEqual(encoded.get("final_density_tolerance").getAsInt(), 3,
                "the outward final-density tolerance must be serialized");
        helper.assertValueEqual(
                AbyssWallPlanConfig.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow(),
                AbyssWallPlanConfig.DEFAULT,
                "the complete wall plan data contract must round-trip"
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallPlanConfigControlsForbiddenYRange(GameTestHelper helper) {
        JsonObject encoded = AbyssWallPlanConfig.CODEC.encodeStart(JsonOps.INSTANCE, AbyssWallPlanConfig.DEFAULT)
                .getOrThrow()
                .getAsJsonObject();
        encoded.addProperty("min_y", -20);
        encoded.addProperty("max_y", 20);
        encoded.addProperty("candidate_count", 4);
        encoded.addProperty("forbidden_min_y", -20);
        encoded.addProperty("forbidden_max_y", 20);
        AbyssWallPlanConfig config = AbyssWallPlanConfig.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        helper.assertValueEqual(
                AbyssWallPlanner.createPlan(42L, config, (angle, y) -> OptionalDouble.of(96.0)),
                List.of(),
                "a data-configured forbidden range covering the full height range must produce no candidates"
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallPlanConfigControlsDensitySearch(GameTestHelper helper) {
        int[] minimumSampledRadius = {Integer.MAX_VALUE};
        IntPredicate wallAtNinetyTwo = radius -> {
            minimumSampledRadius[0] = Math.min(minimumSampledRadius[0], radius);
            return radius >= 92;
        };
        double configuredPrediction = AbyssWallPlanner.preferredRadius(96.0, 8, 3, wallAtNinetyTwo);

        helper.assertValueEqual(configuredPrediction, 92.0,
                "a wall within the configured inward search must be selected");
        helper.assertValueEqual(minimumSampledRadius[0], 88,
                "an inward search of 8 must begin at macroRadius - 8");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallDirectionsSelectStraightAndTiltRotations(GameTestHelper helper) {
        helper.assertValueEqual(AbyssWallPlanner.orientationFor(Math.toRadians(90)),
                new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.STRAIGHT, Rotation.NONE),
                "south wall uses the authored straight orientation");
        helper.assertValueEqual(AbyssWallPlanner.orientationFor(Math.toRadians(45)),
                new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.TILT, Rotation.NONE),
                "south-east wall uses the authored tilt orientation");
        helper.assertValueEqual(AbyssWallPlanner.orientationFor(Math.toRadians(180)),
                new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.STRAIGHT, Rotation.CLOCKWISE_90),
                "west wall rotates the straight template clockwise");
        helper.assertValueEqual(AbyssWallPlanner.orientationFor(Math.toRadians(22.49)),
                new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.STRAIGHT, Rotation.COUNTERCLOCKWISE_90),
                "angle below the sector boundary remains straight");
        helper.assertValueEqual(AbyssWallPlanner.orientationFor(Math.toRadians(22.51)),
                new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.TILT, Rotation.NONE),
                "angle above the sector boundary switches to tilt");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWallRefinementRequiresStableAirToSolidBoundary(GameTestHelper helper) {
        OptionalInt boundary = AbyssWallPlanner.findFirstStableBoundary(180, 230, radius -> radius >= 205);
        OptionalInt missing = AbyssWallPlanner.findFirstStableBoundary(180, 230, radius -> false);
        OptionalInt lowerWall = AbyssWallPlanner.findRefinedBoundary(96.0, 16, radius -> radius >= 94);
        OptionalInt unrelatedOuterWall = AbyssWallPlanner.findRefinedBoundary(96.0, 16, radius -> radius >= 125);
        double inwardPrediction = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> radius >= 90);
        double toleratedPrediction = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> radius >= 99);
        double justOutsideTolerance = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> radius >= 100);
        double distantOuterPrediction = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> radius >= 125);
        double solidAtSearchStart = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> radius >= 64);
        int[] minimumSampledRadius = {Integer.MAX_VALUE};
        double boundedPrediction = AbyssWallPlanner.preferredRadius(96.0, 3, radius -> {
            minimumSampledRadius[0] = Math.min(minimumSampledRadius[0], radius);
            return radius >= 90;
        });

        helper.assertValueEqual(boundary, OptionalInt.of(205), "first stable wall radius");
        helper.assertValueEqual(missing, OptionalInt.empty(), "missing wall must reject the candidate");
        helper.assertValueEqual(lowerWall, OptionalInt.of(94), "nearby lower-layer wall must refine the macro radius");
        helper.assertValueEqual(unrelatedOuterWall, OptionalInt.empty(),
                "a perforated main wall must not fall through to a farther cave boundary");
        helper.assertValueEqual(inwardPrediction, 90.0, "an inward final-density wall must override the macro radius");
        helper.assertValueEqual(toleratedPrediction, 99.0, "the tolerance boundary must remain eligible");
        helper.assertValueEqual(justOutsideTolerance, 96.0,
                "a final-density wall beyond tolerance must fall back to the macro radius");
        helper.assertValueEqual(distantOuterPrediction, 96.0,
                "a distant outer wall must fall back to the macro radius");
        helper.assertTrue(Double.isNaN(solidAtSearchStart),
                "a candidate whose inward search limit is already solid must be rejected");
        helper.assertValueEqual(boundedPrediction, 90.0,
                "a stable wall within the bounded inward search must still be selected");
        helper.assertValueEqual(minimumSampledRadius[0], 64,
                "final density must not be sampled more than 32 blocks inward from the macro radius");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube", timeoutTicks = 200)
    public static void abyssWallPlanFindsTheRealAbyssDensityWall(GameTestHelper helper) {
        long seed = 8675309L;
        var settings = helper.getLevel().registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS)
                .getOrThrow(MiaNoiseGeneratorSettings.THE_ABYSS).value();
        var noises = helper.getLevel().registryAccess().lookupOrThrow(Registries.NOISE);
        RandomState randomState = RandomState.create(settings, noises, seed);
        List<AbyssWallCandidate> candidates = AbyssWallPlanner.planFor(seed, randomState, AbyssWallPlanConfig.DEFAULT);

        helper.assertTrue(candidates.size() >= 16 && candidates.size() <= 20,
                "real Abyss wall plan must yield approximately 16-20 candidates");
        for (AbyssWallCandidate candidate : candidates) {
            helper.assertTrue(candidate.y() < -3 || candidate.y() > 25,
                    "real-density candidates must avoid the horizontal transition shelf");
            if (candidate.y() < -3) {
                helper.assertTrue(candidate.predictedRadius() <= 99.0,
                        "lower-layer prediction must not select a wall beyond macro radius plus tolerance");
            }
        }
        helper.assertValueEqual(candidates, AbyssWallPlanner.planFor(seed, randomState, AbyssWallPlanConfig.DEFAULT),
                "cached real-density plan must be stable");

        long concurrentSeed = -8675309L;
        RandomState concurrentRandomState = RandomState.create(settings, noises, concurrentSeed);
        List<List<AbyssWallCandidate>> concurrentPlans = IntStream.range(0, 16)
                .parallel()
                .mapToObj(ignored -> AbyssWallPlanner.planFor(
                        concurrentSeed, concurrentRandomState, AbyssWallPlanConfig.DEFAULT
                ))
                .toList();
        for (List<AbyssWallCandidate> concurrentPlan : concurrentPlans) {
            helper.assertValueEqual(concurrentPlan, concurrentPlans.getFirst(),
                    "concurrent chunk queries must share one immutable plan");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssWindmillTemplatesHaveOneReplacingAnchor(GameTestHelper helper) {
        assertWindmillTemplate(helper, "abyss_windmill_straight", new Vec3i(27, 26, 36));
        assertWindmillTemplate(helper, "abyss_windmill_tilt", new Vec3i(26, 23, 25));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinTemplatesArePackagedWithExpectedSizes(GameTestHelper helper) {
        assertStructureTemplateSize(helper, "ancient_babylon_compass_ruins", new Vec3i(34, 34, 34));
        assertStructureTemplateSize(helper, "ancient_maya_compass_ruins", new Vec3i(47, 25, 47));
        assertStructureTemplateSize(helper, "ancient_roman_compass_ruins", new Vec3i(36, 12, 35));
        assertStructureTemplateSize(helper, "ancient_trial_compass_ruins", new Vec3i(31, 20, 31));
        assertStructureTemplateSize(helper, "ancient_angkor_compass_ruins", new Vec3i(48, 48, 48));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinTemplatesContainNoStructureBlocks(GameTestHelper helper) {
        assertNoStructureBlocks(helper, "ancient_babylon_compass_ruins");
        assertNoStructureBlocks(helper, "ancient_maya_compass_ruins");
        assertNoStructureBlocks(helper, "ancient_roman_compass_ruins");
        assertNoStructureBlocks(helper, "ancient_trial_compass_ruins");
        assertNoStructureBlocks(helper, "ancient_angkor_compass_ruins");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinStructuresUseTheirTemplateNamesAsRegistryIds(GameTestHelper helper) {
        assertStructureRegistered(helper, "ancient_babylon_compass_ruins");
        assertStructureRegistered(helper, "ancient_maya_compass_ruins");
        assertStructureRegistered(helper, "ancient_roman_compass_ruins");
        assertStructureRegistered(helper, "ancient_trial_compass_ruins");
        assertStructureRegistered(helper, "ancient_angkor_compass_ruins");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinTemplatesContainOneStarCompass(GameTestHelper helper) {
        assertOneStarCompass(helper, "ancient_babylon_compass_ruins");
        assertOneStarCompass(helper, "ancient_maya_compass_ruins");
        assertOneStarCompass(helper, "ancient_roman_compass_ruins");
        assertOneStarCompass(helper, "ancient_trial_compass_ruins");
        assertOneStarCompass(helper, "ancient_angkor_compass_ruins");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinTemplatesOnlyContainIntentionalEntities(GameTestHelper helper) {
        assertOnlyItemFrames(helper, "ancient_babylon_compass_ruins", 0);
        assertOnlyItemFrames(helper, "ancient_maya_compass_ruins", 3);
        assertOnlyItemFrames(helper, "ancient_roman_compass_ruins", 0);
        assertOnlyItemFrames(helper, "ancient_trial_compass_ruins", 0);
        assertOnlyItemFrames(helper, "ancient_angkor_compass_ruins", 0);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void compassRuinWorldgenKeepsBiomeAndPlacementContract(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        assertBiomeTagEquals(helper, MiaTags.Biomes.HAS_ANCIENT_BABYLON_COMPASS_RUINS,
                Set.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE));
        assertBiomeTagEquals(helper, MiaTags.Biomes.HAS_ANCIENT_MAYA_COMPASS_RUINS,
                Set.of(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE));
        assertBiomeTagEquals(helper, MiaTags.Biomes.HAS_ANCIENT_ROMAN_COMPASS_RUINS,
                unionBiomeTags(biomes, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SAVANNA));
        assertBiomeTagEquals(helper, MiaTags.Biomes.HAS_ANCIENT_ANGKOR_COMPASS_RUINS,
                unionBiomeTags(biomes, Tags.Biomes.IS_DESERT));
        assertBiomeTagEquals(helper, MiaTags.Biomes.HAS_ANCIENT_TRIAL_COMPASS_RUINS,
                unionBiomeTags(biomes, BiomeTags.HAS_TRIAL_CHAMBERS));
        helper.assertTrue(
                unionBiomeTags(biomes, MiaTags.Biomes.HAS_ANCIENT_ANGKOR_COMPASS_RUINS).stream()
                        .noneMatch(unionBiomeTags(biomes, Tags.Biomes.IS_BADLANDS)::contains),
                "Angkor compass ruins must exclude badlands"
        );

        assertRandomSpreadSet(
                helper,
                MiaStructureSets.ANCIENT_JUNGLE_COMPASS_RUINS,
                32,
                8,
                70387317,
                Map.of(
                        "mia:ancient_babylon_compass_ruins", 3,
                        "mia:ancient_maya_compass_ruins", 1
                )
        );
        assertRandomSpreadSet(helper, MiaStructureSets.ANCIENT_ROMAN_COMPASS_RUINS,
                32, 8, 70387318, Map.of("mia:ancient_roman_compass_ruins", 1));
        assertRandomSpreadSet(helper, MiaStructureSets.ANCIENT_TRIAL_COMPASS_RUINS,
                34, 12, 94251328, Map.of("mia:ancient_trial_compass_ruins", 1));
        assertRandomSpreadSet(helper, MiaStructureSets.ANCIENT_ANGKOR_COMPASS_RUINS,
                32, 8, 70387319, Map.of("mia:ancient_angkor_compass_ruins", 1));

        JsonObject trial = encodeStructure(helper, MiaStructures.ANCIENT_TRIAL_COMPASS_RUINS);
        helper.assertValueEqual(trial.get("step").getAsString(),
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES.getName(), "trial generation step");
        JsonObject trialHeight = trial.getAsJsonObject("start_height");
        helper.assertValueEqual(trialHeight.getAsJsonObject("min_inclusive").get("absolute").getAsInt(),
                -40, "trial minimum Y");
        helper.assertValueEqual(trialHeight.getAsJsonObject("max_inclusive").get("absolute").getAsInt(),
                -20, "trial maximum Y");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void petrifiedShipTemplatesKeepTheirDirectionalJigsawContract(GameTestHelper helper) {
        assertJigsawTemplate(
                helper,
                "petrified_ship_body_big",
                new Vec3i(8, 6, 17),
                Set.of(
                        new JigsawContract(new BlockPos(0, 1, 7), FrontAndTop.WEST_UP,
                                "mia:petrified_ship/branch", "mia:petrified_ship/branch", "mia:petrified_ship/branch"),
                        new JigsawContract(new BlockPos(3, 1, 0), FrontAndTop.NORTH_UP,
                                "mia:petrified_ship/connect_right", "mia:petrified_ship/connect_left", "mia:petrified_ship/connect_left"),
                        new JigsawContract(new BlockPos(3, 1, 16), FrontAndTop.SOUTH_UP,
                                "mia:petrified_ship/connect_left", "mia:petrified_ship/connect_right", "mia:petrified_ship/connect_right"),
                        new JigsawContract(new BlockPos(7, 1, 10), FrontAndTop.EAST_UP,
                                "mia:petrified_ship/branch", "mia:petrified_ship/branch", "mia:petrified_ship/branch")
                )
        );
        assertJigsawTemplate(
                helper,
                "petrified_ship_body_small",
                new Vec3i(5, 5, 6),
                Set.of(
                        new JigsawContract(new BlockPos(2, 1, 0), FrontAndTop.NORTH_UP,
                                "mia:petrified_ship/connect_right", "mia:petrified_ship/connect_left", "mia:petrified_ship/connect_left"),
                        new JigsawContract(new BlockPos(2, 1, 5), FrontAndTop.SOUTH_UP,
                                "mia:petrified_ship/connect_left", "mia:petrified_ship/connect_right", "mia:petrified_ship/connect_right")
                )
        );
        assertJigsawTemplate(
                helper,
                "petrified_ship_left",
                new Vec3i(14, 23, 27),
                Set.of(new JigsawContract(new BlockPos(6, 1, 0), FrontAndTop.NORTH_UP,
                        "mia:petrified_ship/connect_right", "mia:petrified_ship/connect_left", "minecraft:empty"))
        );
        assertJigsawTemplate(
                helper,
                "petrified_ship_right",
                new Vec3i(12, 16, 17),
                Set.of(
                        new JigsawContract(new BlockPos(6, 1, 16), FrontAndTop.SOUTH_UP,
                                "mia:petrified_ship/connect_left", "mia:petrified_ship/connect_right", "minecraft:empty"),
                        new JigsawContract(new BlockPos(11, 0, 12), FrontAndTop.EAST_UP,
                                "mia:petrified_ship/branch", "mia:petrified_ship/branch", "mia:petrified_ship/branch")
                )
        );
        assertJigsawTemplate(helper, "petrified_ship_part_1", new Vec3i(4, 3, 5),
                Set.of(branchEnd(new BlockPos(3, 0, 2))));
        assertJigsawTemplate(helper, "petrified_ship_part_2", new Vec3i(5, 4, 5),
                Set.of(branchEnd(new BlockPos(4, 1, 2))));
        assertJigsawTemplate(helper, "petrified_ship_part_3", new Vec3i(5, 4, 5),
                Set.of(branchEnd(new BlockPos(4, 1, 2))));
        assertJigsawTemplate(helper, "petrified_ship_part_4", new Vec3i(5, 2, 3),
                Set.of(branchEnd(new BlockPos(4, 1, 1))));
        assertJigsawTemplate(helper, "petrified_ship_part_5", new Vec3i(6, 4, 4),
                Set.of(branchEnd(new BlockPos(5, 1, 1))));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void petrifiedShipPoolsKeepDirectionalTerminationWeights(GameTestHelper helper) {
        assertTemplatePool(helper, "petrified_ship/main", "minecraft:empty", Map.of(
                "mia:petrified_ship_body_small", 1,
                "mia:petrified_ship_body_big", 1
        ));
        assertTemplatePool(helper, "petrified_ship/start", "mia:petrified_ship/main", Map.of(
                "mia:petrified_ship_body_small", 1,
                "mia:petrified_ship_body_big", 1
        ));
        assertTemplatePool(helper, "petrified_ship/connect_left", "mia:petrified_ship/left_or_right", Map.of(
                "mia:petrified_ship_body_small", 1,
                "mia:petrified_ship_body_big", 1,
                "mia:petrified_ship_right", 5,
                "minecraft:empty_pool_element", 1
        ));
        assertTemplatePool(helper, "petrified_ship/connect_right", "mia:petrified_ship/left_or_right", Map.of(
                "mia:petrified_ship_body_small", 1,
                "mia:petrified_ship_body_big", 1,
                "mia:petrified_ship_left", 5,
                "minecraft:empty_pool_element", 1
        ));
        assertTemplatePool(helper, "petrified_ship/left_or_right", "minecraft:empty", Map.of(
                "mia:petrified_ship_left", 1,
                "mia:petrified_ship_right", 1
        ));
        assertTemplatePool(helper, "petrified_ship/branch", "minecraft:empty", Map.of(
                "mia:petrified_ship_part_1", 1,
                "mia:petrified_ship_part_2", 1,
                "mia:petrified_ship_part_3", 1,
                "mia:petrified_ship_part_4", 1,
                "mia:petrified_ship_part_5", 1
        ));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveRaiderHutTemplatesKeepVerticalJigsawContract(GameTestHelper helper) {
        assertJigsawTemplate(
                helper,
                "cave_raider_hut_top",
                new Vec3i(19, 10, 23),
                Set.of(new JigsawContract(
                        new BlockPos(15, 0, 4),
                        FrontAndTop.DOWN_NORTH,
                        "minecraft:empty",
                        "mia:cave_raider_hut/bottom",
                        "mia:cave_raider_hut/bottom"
                ))
        );
        assertJigsawTemplate(
                helper,
                "cave_raider_hut_bottom",
                new Vec3i(15, 5, 15),
                Set.of(new JigsawContract(
                        new BlockPos(14, 4, 0),
                        FrontAndTop.UP_NORTH,
                        "mia:cave_raider_hut/bottom",
                        "minecraft:empty",
                        "minecraft:empty"
                ))
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveRaiderHutBottomIsOptionalAtEqualWeight(GameTestHelper helper) {
        assertTemplatePool(helper, "cave_raider_hut/start", "minecraft:empty", Map.of(
                "mia:cave_raider_hut_top", 1
        ));
        assertTemplatePool(helper, "cave_raider_hut/bottom", "minecraft:empty", Map.of(
                "mia:cave_raider_hut_bottom", 1,
                "minecraft:empty_pool_element", 1
        ));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssSurfaceTemplatesArePackagedWithExpectedContracts(GameTestHelper helper) {
        assertJigsawTemplate(helper, "abyssal_ruins_01", new Vec3i(19, 9, 19), Set.of());
        assertJigsawTemplate(helper, "abyssal_ruins_02", new Vec3i(18, 7, 18), Set.of());
        assertJigsawTemplate(helper, "abyssal_ruins_03", new Vec3i(9, 9, 15), Set.of());
        assertJigsawTemplate(helper, "abyssal_ruins_04", new Vec3i(19, 8, 19), Set.of());
        assertJigsawTemplate(helper, "abyssal_ruins_05", new Vec3i(19, 9, 20), Set.of());
        assertJigsawTemplate(helper, "abyssal_ruins_06", new Vec3i(21, 10, 22), Set.of());
        assertJigsawTemplate(helper, "ruined_cave_raider_hut", new Vec3i(22, 19, 18), Set.of());
        assertJigsawTemplate(helper, "fisherman_hut", new Vec3i(43, 25, 48), Set.of());
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssSurfaceTemplatePoolsUseTheirStructureNames(GameTestHelper helper) {
        assertTemplatePool(helper, "abyssal_ruins_01/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_01", 1));
        assertTemplatePool(helper, "abyssal_ruins_02/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_02", 1));
        assertTemplatePool(helper, "abyssal_ruins_03/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_03", 1));
        assertTemplatePool(helper, "abyssal_ruins_04/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_04", 1));
        assertTemplatePool(helper, "abyssal_ruins_05/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_05", 1));
        assertTemplatePool(helper, "abyssal_ruins_06/starts", "minecraft:empty",
                Map.of("mia:abyssal_ruins_06", 1));
        assertTemplatePool(helper, "ruined_cave_raider_hut/starts", "minecraft:empty",
                Map.of("mia:ruined_cave_raider_hut", 1));
        assertTemplatePool(helper, "fisherman_hut/starts", "minecraft:empty",
                Map.of("mia:fisherman_hut", 1));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void abyssSurfaceWorldgenKeepsApprovedBiomeAndPlacementContract(GameTestHelper helper) {
        TagKey<Biome> abyssalRuinsBiomes = biomeTag("has_abyssal_ruins");
        TagKey<Biome> caveRaiderHutBiomes = biomeTag("has_cave_raider_hut");
        TagKey<Biome> ruinedCaveRaiderHutBiomes = biomeTag("has_ruined_cave_raider_hut");
        TagKey<Biome> fishermanHutBiomes = biomeTag("has_fisherman_hut");
        Set<ResourceKey<Biome>> allAbyssBiomes = Set.of(
                MiaBiomes.THE_ABYSS,
                MiaBiomes.SKYFOG_FOREST,
                MiaBiomes.DENSE_SKYFOG_FOREST,
                MiaBiomes.FOSSILIZED_FOREST,
                MiaBiomes.RICH_FOSSILIZED_FOREST,
                MiaBiomes.UNDER_FOSSILIZED_FOREST,
                MiaBiomes.ABYSS_PLAINS,
                MiaBiomes.PRASIOLITE_CAVES,
                MiaBiomes.ABYSS_LUSH_CAVES,
                MiaBiomes.ABYSS_DRIPSTONE_CAVES,
                MiaBiomes.TEMPTATION_FOREST,
                MiaBiomes.INVERTED_FOREST
        );
        Set<ResourceKey<Biome>> surfaceAbyssBiomes = Set.of(
                MiaBiomes.SKYFOG_FOREST,
                MiaBiomes.DENSE_SKYFOG_FOREST,
                MiaBiomes.FOSSILIZED_FOREST,
                MiaBiomes.RICH_FOSSILIZED_FOREST,
                MiaBiomes.ABYSS_PLAINS
        );

        assertBiomeTagEquals(helper, abyssalRuinsBiomes, allAbyssBiomes);
        assertBiomeTagEquals(helper, caveRaiderHutBiomes, Set.of(MiaBiomes.INVERTED_FOREST));
        assertBiomeTagEquals(helper, ruinedCaveRaiderHutBiomes, Set.of(MiaBiomes.ABYSS_PLAINS));
        assertBiomeTagEquals(helper, fishermanHutBiomes, surfaceAbyssBiomes);

        assertSurfaceJigsawStructure(helper, "abyssal_ruins_01", "has_abyssal_ruins",
                "abyssal_ruins_01/starts");
        assertSurfaceJigsawStructure(helper, "abyssal_ruins_02", "has_abyssal_ruins",
                "abyssal_ruins_02/starts");
        assertSurfaceJigsawStructure(helper, "abyssal_ruins_03", "has_abyssal_ruins",
                "abyssal_ruins_03/starts");
        assertSurfaceJigsawStructure(helper, "abyssal_ruins_04", "has_abyssal_ruins",
                "abyssal_ruins_04/starts");
        assertSurfaceJigsawStructure(helper, "abyssal_ruins_05", "has_abyssal_ruins",
                "abyssal_ruins_05/starts");
        assertSurfaceJigsawStructure(helper, "abyssal_ruins_06", "has_abyssal_ruins",
                "abyssal_ruins_06/starts");
        JsonObject caveRaiderHut = encodeStructure(helper, structureKey("cave_raider_hut"));
        helper.assertValueEqual(caveRaiderHut.get("type").getAsString(),
                "mia:jigsaw", "cave raider hut structure type");
        helper.assertValueEqual(caveRaiderHut.get("biomes").getAsString(),
                "#mia:has_cave_raider_hut", "cave raider hut biome tag");
        helper.assertValueEqual(caveRaiderHut.get("step").getAsString(),
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES.getName(), "cave raider hut generation step");
        helper.assertValueEqual(caveRaiderHut.get("terrain_adaptation").getAsString(),
                "beard_thin", "cave raider hut terrain adaptation");
        helper.assertValueEqual(caveRaiderHut.get("start_pool").getAsString(),
                "mia:cave_raider_hut/start", "cave raider hut start pool");
        helper.assertValueEqual(caveRaiderHut.get("size").getAsInt(), 1, "cave raider hut Jigsaw depth");
        helper.assertValueEqual(caveRaiderHut.getAsJsonObject("start_height").get("absolute").getAsInt(),
                -113, "cave raider hut inverted-forest ceiling");
        helper.assertFalse(caveRaiderHut.has("project_start_to_heightmap"),
                "cave raider hut must not project to the column's highest surface");
        JsonObject caveFloorSearch = caveRaiderHut.getAsJsonObject("cave_floor_search");
        helper.assertValueEqual(caveFloorSearch.getAsJsonObject("min_y").get("above_bottom").getAsInt(),
                16, "cave raider hut search lower bound");
        helper.assertValueEqual(caveFloorSearch.get("clearance").getAsInt(),
                10, "cave raider hut top clearance");
        helper.assertValueEqual(caveFloorSearch.get("horizontal_samples").getAsInt(),
                16, "cave raider hut sampled columns");
        assertSurfaceJigsawStructure(helper, "ruined_cave_raider_hut", "has_ruined_cave_raider_hut",
                "ruined_cave_raider_hut/starts");
        assertSurfaceJigsawStructure(helper, "fisherman_hut", "has_fisherman_hut",
                "fisherman_hut/starts");

        assertRandomSpreadSet(helper, structureSetKey("abyssal_ruins"),
                16, 4, 70387321, Map.of(
                        "mia:abyssal_ruins_01", 1,
                        "mia:abyssal_ruins_02", 1,
                        "mia:abyssal_ruins_03", 1,
                        "mia:abyssal_ruins_04", 1,
                        "mia:abyssal_ruins_05", 1,
                        "mia:abyssal_ruins_06", 1
                ));
        assertRandomSpreadSet(helper, structureSetKey("cave_raider_huts"),
                32, 8, 70387322, Map.of(
                        "mia:cave_raider_hut", 1,
                        "mia:ruined_cave_raider_hut", 1
                ));
        assertRandomSpreadSet(helper, structureSetKey("fisherman_huts"),
                32, 8, 70387323, Map.of("mia:fisherman_hut", 1));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void petrifiedShipWorldgenUsesGreatFaultCaveContract(GameTestHelper helper) {
        TagKey<Biome> petrifiedShipBiomes = TagKey.create(
                Registries.BIOME,
                MiaUtil.miaId("has_petrified_ship")
        );
        ResourceKey<Structure> petrifiedShip = ResourceKey.create(
                Registries.STRUCTURE,
                MiaUtil.miaId("petrified_ship")
        );
        ResourceKey<StructureSet> petrifiedShips = ResourceKey.create(
                Registries.STRUCTURE_SET,
                MiaUtil.miaId("petrified_ships")
        );

        assertBiomeTagEquals(helper, petrifiedShipBiomes,
                Set.of(MiaBiomes.THE_GREAT_FAULT, MiaBiomes.GREAT_FAULT));

        JsonObject structure = encodeStructure(helper, petrifiedShip);
        helper.assertValueEqual(structure.get("type").getAsString(), "mia:jigsaw", "petrified ship type");
        helper.assertValueEqual(structure.get("step").getAsString(),
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES.getName(), "petrified ship generation step");
        helper.assertValueEqual(structure.get("terrain_adaptation").getAsString(),
                "beard_thin", "petrified ship terrain adaptation");
        helper.assertValueEqual(structure.get("start_pool").getAsString(),
                "mia:petrified_ship/start", "petrified ship start pool");
        helper.assertValueEqual(structure.get("size").getAsInt(), 4, "petrified ship Jigsaw depth");
        helper.assertValueEqual(structure.getAsJsonObject("start_height").get("absolute").getAsInt(),
                324, "petrified ship cave scan ceiling");
        helper.assertFalse(structure.has("project_start_to_heightmap"),
                "petrified ship must not project onto the surface heightmap");
        JsonObject caveFloorSearch = structure.getAsJsonObject("cave_floor_search");
        helper.assertValueEqual(caveFloorSearch.getAsJsonObject("min_y").get("above_bottom").getAsInt(),
                16, "petrified ship cave scan lower bound");
        helper.assertValueEqual(caveFloorSearch.get("clearance").getAsInt(),
                24, "petrified ship cave clearance");
        helper.assertValueEqual(caveFloorSearch.get("horizontal_samples").getAsInt(),
                4, "petrified ship cave column samples");
        helper.assertValueEqual(structure.get("max_distance_from_center").getAsInt(),
                128, "petrified ship maximum Jigsaw range");
        helper.assertFalse(structure.get("use_expansion_hack").getAsBoolean(),
                "petrified ship must not use the expansion hack");

        assertRandomSpreadSet(helper, petrifiedShips,
                16, 4, 70387320, Map.of("mia:petrified_ship", 1));
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void miaJigsawCodecKeepsLegacyStructuresWithoutCaveSearch(GameTestHelper helper) {
        JsonObject stronghold = encodeStructure(helper, MiaStructures.ABYSS_STRONGHOLD);

        helper.assertValueEqual(stronghold.get("type").getAsString(),
                "mia:jigsaw", "abyss stronghold structure type");
        helper.assertFalse(stronghold.has("cave_floor_search"),
                "legacy Mia Jigsaw structures must not acquire cave-floor placement");
        helper.assertFalse(stronghold.has("project_start_to_heightmap"),
                "abyss stronghold must retain its original fixed-height placement");
        helper.assertValueEqual(stronghold.getAsJsonObject("start_height").get("absolute").getAsInt(),
                -50, "abyss stronghold fixed start Y");
        helper.assertValueEqual(stronghold.get("size").getAsInt(),
                14, "abyss stronghold Jigsaw depth");
        helper.assertValueEqual(stronghold.get("max_distance_from_center").getAsInt(),
                230, "abyss stronghold maximum Jigsaw range");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveFloorSearchFindsDrySturdyFloorWithClearance(GameTestHelper helper) {
        NoiseColumn column = stoneColumn(0, 80);
        for (int y = 11; y <= 34; y++) {
            column.setBlock(y, Blocks.AIR.defaultBlockState());
        }

        helper.assertValueEqual(
                findCaveFloorStarts(column, 0, 34, 24),
                List.of(11),
                "a sturdy floor below 24 dry air blocks must place the structure one block above the floor"
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveFloorSearchRequiresFullClearance(GameTestHelper helper) {
        NoiseColumn column = stoneColumn(0, 80);
        for (int y = 11; y <= 33; y++) {
            column.setBlock(y, Blocks.AIR.defaultBlockState());
        }

        helper.assertValueEqual(
                findCaveFloorStarts(column, 0, 34, 24),
                List.of(),
                "a cave with only 23 air blocks must be rejected"
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveFloorSearchRejectsWetFloor(GameTestHelper helper) {
        NoiseColumn column = stoneColumn(0, 80);
        column.setBlock(10, Blocks.WATER.defaultBlockState());
        for (int y = 11; y <= 34; y++) {
            column.setBlock(y, Blocks.AIR.defaultBlockState());
        }

        helper.assertValueEqual(
                findCaveFloorStarts(column, 0, 34, 24),
                List.of(),
                "a fluid-filled support block must not count as a cave floor"
        );
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "gametest/amethyst_lamptube")
    public static void caveFloorSearchKeepsOriginBelowConfiguredCeiling(GameTestHelper helper) {
        NoiseColumn column = stoneColumn(300, 80);
        for (int y = 325; y <= 348; y++) {
            column.setBlock(y, Blocks.AIR.defaultBlockState());
        }

        helper.assertValueEqual(
                findCaveFloorStarts(column, 300, 324, 24),
                List.of(),
                "an otherwise valid floor at Y=324 must not produce an origin above Y=324"
        );
        helper.assertValueEqual(
                findCaveFloorStarts(column, 300, 325, 24),
                List.of(325),
                "the same floor must become valid when Y=325 is allowed"
        );
        helper.succeed();
    }

    private static NoiseColumn stoneColumn(int minY, int height) {
        BlockState[] blocks = new BlockState[height];
        Arrays.fill(blocks, Blocks.STONE.defaultBlockState());
        return new NoiseColumn(minY, blocks);
    }

    private static List<Integer> findCaveFloorStarts(
            NoiseColumn column,
            int minY,
            int maxY,
            int clearance
    ) {
        return new CaveFloorSearch(VerticalAnchor.absolute(minY), clearance, 4)
                .findStartYs(column, minY, maxY);
    }

    private static JigsawContract branchEnd(BlockPos pos) {
        return new JigsawContract(
                pos,
                FrontAndTop.EAST_UP,
                "mia:petrified_ship/branch",
                "mia:petrified_ship/branch",
                "minecraft:empty"
        );
    }

    private static void assertJigsawTemplate(
            GameTestHelper helper,
            String path,
            Vec3i expectedSize,
            Set<JigsawContract> expectedJigsaws
    ) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MiaUtil.miaId(path))
                .orElseThrow(() -> new AssertionError("missing structure template mia:" + path));
        helper.assertValueEqual(template.getSize(), expectedSize, path + " template size");
        helper.assertTrue(
                template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.STRUCTURE_BLOCK).isEmpty(),
                path + " must not contain authoring-only structure blocks"
        );

        Set<JigsawContract> actualJigsaws = template
                .filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.JIGSAW)
                .stream()
                .map(info -> {
                    if (info.nbt() == null) {
                        throw new AssertionError(path + " contains a Jigsaw without block-entity NBT at " + info.pos());
                    }
                    return new JigsawContract(
                            info.pos(),
                            info.state().getValue(JigsawBlock.ORIENTATION),
                            info.nbt().getString("name"),
                            info.nbt().getString("target"),
                            info.nbt().getString("pool")
                    );
                })
                .collect(Collectors.toSet());
        helper.assertValueEqual(actualJigsaws, expectedJigsaws, path + " Jigsaw contract");
    }

    private static void assertTemplatePool(
            GameTestHelper helper,
            String path,
            String expectedFallback,
            Map<String, Integer> expectedElements
    ) {
        ResourceKey<StructureTemplatePool> key = ResourceKey.create(Registries.TEMPLATE_POOL, MiaUtil.miaId(path));
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        StructureTemplatePool pool = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
                .getOrThrow(key)
                .value();
        JsonObject encoded = StructureTemplatePool.DIRECT_CODEC.encodeStart(ops, pool)
                .getOrThrow()
                .getAsJsonObject();
        helper.assertValueEqual(encoded.get("fallback").getAsString(), expectedFallback, path + " fallback");

        Map<String, Integer> actualElements = encoded.getAsJsonArray("elements").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .collect(Collectors.toMap(
                        entry -> {
                            JsonObject element = entry.getAsJsonObject("element");
                            return element.has("location")
                                    ? element.get("location").getAsString()
                                    : element.get("element_type").getAsString();
                        },
                        entry -> entry.get("weight").getAsInt()
                ));
        helper.assertValueEqual(actualElements, expectedElements, path + " elements");
    }

    private record JigsawContract(
            BlockPos pos,
            FrontAndTop orientation,
            String name,
            String target,
            String pool
    ) {
    }

    private static void assertStructureTemplateSize(GameTestHelper helper, String path, Vec3i expectedSize) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MiaUtil.miaId(path))
                .orElseThrow(() -> new AssertionError("missing structure template mia:" + path));
        helper.assertValueEqual(template.getSize(), expectedSize, path + " template size");
    }

    private static void assertNoStructureBlocks(GameTestHelper helper, String path) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MiaUtil.miaId(path))
                .orElseThrow(() -> new AssertionError("missing structure template mia:" + path));
        helper.assertTrue(
                template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.STRUCTURE_BLOCK).isEmpty(),
                path + " must not contain authoring-only structure blocks"
        );
    }

    private static void assertStructureRegistered(GameTestHelper helper, String path) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(path));
        helper.assertTrue(
                helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE).get(key).isPresent(),
                "missing structure registry entry mia:" + path
        );
    }

    private static void assertOneStarCompass(GameTestHelper helper, String path) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MiaUtil.miaId(path))
                .orElseThrow(() -> new AssertionError("missing structure template mia:" + path));
        int compassCount = 0;
        for (StructureTemplate.StructureBlockInfo pedestal : template.filterBlocks(
                BlockPos.ZERO,
                new StructurePlaceSettings(),
                MiaBlocks.PEDESTAL.get()
        )) {
            if (pedestal.nbt() == null) {
                continue;
            }
            CompoundTag inputInventory = pedestal.nbt().getCompound("input_inventory");
            ListTag items = inputInventory.getList("Items", Tag.TAG_COMPOUND);
            for (int index = 0; index < items.size(); index++) {
                if ("mia:star_compass".equals(items.getCompound(index).getString("id"))) {
                    compassCount++;
                }
            }
        }
        helper.assertValueEqual(compassCount, 1, path + " star compass count");
    }

    private static void assertOnlyItemFrames(GameTestHelper helper, String path, int expectedItemFrames) {
        String resourcePath = "/data/mia/structure/" + path + ".nbt";
        try (InputStream stream = MiaGameTests.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new AssertionError("missing structure template resource " + resourcePath);
            }
            ListTag entities = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap())
                    .getList("entities", Tag.TAG_COMPOUND);
            helper.assertValueEqual(entities.size(), expectedItemFrames, path + " intentional entity count");
            for (int index = 0; index < entities.size(); index++) {
                helper.assertValueEqual(
                        entities.getCompound(index).getCompound("nbt").getString("id"),
                        "minecraft:item_frame",
                        path + " entity " + index + " type"
                );
            }
        } catch (IOException exception) {
            throw new AssertionError("failed to read structure template resource " + resourcePath, exception);
        }
    }

    private static void assertBiomeTagEquals(
            GameTestHelper helper,
            TagKey<Biome> actualTag,
            Set<ResourceKey<Biome>> expectedBiomes
    ) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        helper.assertValueEqual(
                unionBiomeTags(biomes, actualTag),
                expectedBiomes,
                actualTag.location() + " biome contents"
        );
    }

    @SafeVarargs
    private static Set<ResourceKey<Biome>> unionBiomeTags(
            net.minecraft.core.HolderGetter<Biome> biomes,
            TagKey<Biome>... tags
    ) {
        return Arrays.stream(tags)
                .flatMap(tag -> biomes.getOrThrow(tag).stream())
                .map(holder -> holder.unwrapKey().orElseThrow())
                .collect(Collectors.toSet());
    }

    private static void assertRandomSpreadSet(
            GameTestHelper helper,
            ResourceKey<StructureSet> key,
            int spacing,
            int separation,
            int salt,
            Map<String, Integer> expectedStructures
    ) {
        JsonObject structureSet = encodeStructureSet(helper, key);
        JsonObject placement = structureSet.getAsJsonObject("placement");
        helper.assertValueEqual(placement.get("spacing").getAsInt(), spacing, key.location() + " spacing");
        helper.assertValueEqual(placement.get("separation").getAsInt(), separation, key.location() + " separation");
        helper.assertValueEqual(placement.get("salt").getAsInt(), salt, key.location() + " salt");

        Map<String, Integer> structures = structureSet.getAsJsonArray("structures").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .collect(Collectors.toMap(
                        entry -> entry.get("structure").getAsString(),
                        entry -> entry.get("weight").getAsInt()
                ));
        helper.assertValueEqual(structures, expectedStructures, key.location() + " weighted structures");
    }

    private static void assertSurfaceJigsawStructure(
            GameTestHelper helper,
            String structureName,
            String biomeTag,
            String startPool
    ) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(structureName));
        JsonObject structure = encodeStructure(helper, key);
        helper.assertValueEqual(structure.get("type").getAsString(),
                "minecraft:jigsaw", structureName + " structure type");
        helper.assertValueEqual(structure.get("biomes").getAsString(),
                "#mia:" + biomeTag, structureName + " biome tag");
        helper.assertValueEqual(structure.get("step").getAsString(),
                GenerationStep.Decoration.SURFACE_STRUCTURES.getName(), structureName + " generation step");
        helper.assertValueEqual(structure.get("terrain_adaptation").getAsString(),
                "beard_thin", structureName + " terrain adaptation");
        helper.assertValueEqual(structure.get("start_pool").getAsString(),
                "mia:" + startPool, structureName + " start pool");
        helper.assertValueEqual(structure.get("size").getAsInt(), 1, structureName + " Jigsaw depth");
        helper.assertValueEqual(structure.getAsJsonObject("start_height").get("absolute").getAsInt(),
                0, structureName + " start height");
        helper.assertValueEqual(structure.get("project_start_to_heightmap").getAsString(),
                "WORLD_SURFACE_WG", structureName + " surface heightmap");
        helper.assertTrue(structure.get("use_expansion_hack").getAsBoolean(),
                structureName + " expansion hack");
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registries.BIOME, MiaUtil.miaId(name));
    }

    private static ResourceKey<StructureSet> structureSetKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, MiaUtil.miaId(name));
    }

    private static ResourceKey<Structure> structureKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(name));
    }

    private static JsonObject encodeStructureSet(GameTestHelper helper, ResourceKey<StructureSet> key) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        StructureSet structureSet = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)
                .getOrThrow(key)
                .value();
        return StructureSet.DIRECT_CODEC.encodeStart(ops, structureSet).getOrThrow().getAsJsonObject();
    }

    private static JsonObject encodeStructure(GameTestHelper helper, ResourceKey<Structure> key) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        Structure structure = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(key)
                .value();
        return Structure.DIRECT_CODEC.encodeStart(ops, structure).getOrThrow().getAsJsonObject();
    }

    private static void assertWindmillTemplate(GameTestHelper helper, String path, Vec3i expectedSize) {
        StructureTemplate template = helper.getLevel().getStructureManager().get(MiaUtil.miaId(path))
                .orElseThrow(() -> new AssertionError("missing structure template mia:" + path));
        helper.assertValueEqual(template.getSize(), expectedSize, path + " template size");

        List<StructureTemplate.StructureBlockInfo> anchors = template
                .filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.JIGSAW)
                .stream()
                .filter(info -> info.nbt() != null
                        && "mia:abyss_wall_anchor".equals(info.nbt().getString("name")))
                .toList();
        helper.assertValueEqual(anchors.size(), 1, path + " must contain exactly one wall anchor");
        helper.assertValueEqual(anchors.getFirst().state().getValue(JigsawBlock.ORIENTATION), FrontAndTop.SOUTH_UP,
                path + " anchor orientation");
        helper.assertValueEqual(anchors.getFirst().nbt().getString("final_state"), "mia:abyss_andesite",
                path + " anchor final state");
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(MiaGameTests.class);
    }
}
