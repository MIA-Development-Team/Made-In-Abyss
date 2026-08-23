package com.altnoir.mia;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallCandidate;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanConfig;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
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

        helper.assertValueEqual(boundary, OptionalInt.of(205), "first stable wall radius");
        helper.assertValueEqual(missing, OptionalInt.empty(), "missing wall must reject the candidate");
        helper.assertValueEqual(lowerWall, OptionalInt.of(94), "nearby lower-layer wall must refine the macro radius");
        helper.assertValueEqual(unrelatedOuterWall, OptionalInt.empty(),
                "a perforated main wall must not fall through to a farther cave boundary");
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
            helper.assertTrue(candidate.y() <= 125, "real-density candidates above Y=125 must be rejected");
            helper.assertTrue(candidate.y() < -3 || candidate.y() > 25,
                    "real-density candidates must avoid the horizontal transition shelf");
            if (candidate.y() < -3) {
                helper.assertValueEqual(candidate.predictedRadius(), 96.0,
                        "lower-layer candidates must target the macro wall instead of a farther final-density boundary");
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
