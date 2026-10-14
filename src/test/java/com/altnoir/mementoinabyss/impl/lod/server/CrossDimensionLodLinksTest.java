package com.altnoir.mementoinabyss.impl.lod.server;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalBoundary;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CrossDimensionLodLinksTest {
    private static HolderLookup.Provider vanilla;
    private static DimensionType template;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        vanilla = VanillaRegistries.createLookup();
        template =
                vanilla.lookupOrThrow(Registries.DIMENSION_TYPE)
                        .getOrThrow(
                                ResourceKey.create(
                                        Registries.DIMENSION_TYPE,
                                        Identifier.withDefaultNamespace("overworld")))
                        .value();
    }

    private static DimensionType type(int minY, int height, EnvironmentAttributeMap attributes) {
        return new DimensionType(
                template.hasFixedTime(),
                template.hasSkyLight(),
                template.hasCeiling(),
                template.hasEnderDragonFight(),
                template.coordinateScale(),
                minY,
                height,
                height,
                template.infiniburn(),
                template.ambientLight(),
                template.monsterSettings(),
                template.skybox(),
                template.cardinalLightType(),
                attributes,
                template.timelines(),
                template.defaultClock());
    }

    private static DimensionType type(int minY, int height) {
        return type(minY, height, EnvironmentAttributeMap.builder().build());
    }

    private static RegistryAccess clientRegistries(DimensionType abyss) {
        var types =
                new MappedRegistry<DimensionType>(Registries.DIMENSION_TYPE, Lifecycle.stable());
        types.register(
                ResourceKey.create(
                        Registries.DIMENSION_TYPE,
                        Identifier.fromNamespaceAndPath("mementoinabyss", "the_abyss_type")),
                abyss,
                RegistrationInfo.BUILT_IN);
        types.register(
                ResourceKey.create(Registries.DIMENSION_TYPE, Level.OVERWORLD.identifier()),
                template,
                RegistrationInfo.BUILT_IN);
        types.register(
                ResourceKey.create(
                        Registries.DIMENSION_TYPE,
                        MiaDimensions.GREAT_FAULT_LEVEL
                                .identifier()
                                .withPath(path -> path + "_type")),
                type(-384, 768),
                RegistrationInfo.BUILT_IN);
        return new RegistryAccess.ImmutableRegistryAccess(List.of(types.freeze()));
    }

    @Test
    void implicitOverworldConnectionRequiresEnabledFeatureAndInclusiveCircularRange() {
        var attrs = EnvironmentAttributeMap.builder().build();
        assertTrue(CrossDimensionLodLinks.activeAt(Level.OVERWORLD, attrs, 300, 0, true, 100));
        assertTrue(CrossDimensionLodLinks.activeAt(Level.OVERWORLD, attrs, -180, 240, true, 100));
        assertFalse(CrossDimensionLodLinks.activeAt(Level.OVERWORLD, attrs, 300.01, 0, true, 100));
        assertFalse(CrossDimensionLodLinks.activeAt(Level.OVERWORLD, attrs, 240, 240, true, 100));
        assertFalse(CrossDimensionLodLinks.activeAt(Level.OVERWORLD, attrs, 0, 0, false, 100));
    }

    @Test
    void explicitConnectionsAndOtherDimensionsKeepTheirExistingActivation() {
        var attrs = EnvironmentAttributeMap.builder().build();
        assertTrue(
                CrossDimensionLodLinks.activeAt(
                        MiaDimensions.GREAT_FAULT_LEVEL, attrs, 100000, 100000, false, 100));
        for (var attribute :
                List.of(
                        MiaEnvironmentAttributes.VERTICAL_BELOW,
                        MiaEnvironmentAttributes.VERTICAL_ABOVE)) {
            var explicit =
                    EnvironmentAttributeMap.builder()
                            .set(attribute, new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 0))
                            .build();
            assertTrue(
                    CrossDimensionLodLinks.activeAt(
                            Level.OVERWORLD, explicit, 100000, 100000, false, 100));
        }
    }

    @Test
    void overworldSeesAbyssWithExactTouchingBuildBoundariesOnClient() {
        var target = type(-64, 384);
        var link =
                CrossDimensionLodLinks.forTarget(
                                clientRegistries(type(-256, 640)), Level.OVERWORLD, target)
                        .orElseThrow();
        assertEquals(MiaDimensions.THE_ABYSS_LEVEL, link.source());
        assertEquals(Level.OVERWORLD, link.target());
        assertEquals(-448, link.displayYOffset());
        assertEquals(-64, link.sourceHeight().maxY() + link.displayYOffset());
        assertEquals(-65, link.sourceHeight().maxY() - 1 + link.displayYOffset());
        assertEquals(-704, link.sourceHeight().minY() + link.displayYOffset());
    }

    @Test
    void offsetUsesLoadedDimensionHeightsInsteadOfHardcodedDefaults() {
        var target = type(-128, 512);
        var link =
                CrossDimensionLodLinks.forTarget(
                                clientRegistries(type(-512, 1024)), Level.OVERWORLD, target)
                        .orElseThrow();
        assertEquals(-640, link.displayYOffset());
        assertEquals(target.minY(), link.sourceHeight().maxY() + link.displayYOffset());
    }

    @Test
    void unrelatedDimensionsDoNotAcquireOverworldFallbackAndMissingSourceIsSafe() {
        var registries = clientRegistries(type(-256, 640));
        assertTrue(CrossDimensionLodLinks.forTarget(registries, Level.NETHER, template).isEmpty());
        assertTrue(CrossDimensionLodLinks.forTarget(registries, Level.END, template).isEmpty());
        assertEquals(
                Level.OVERWORLD,
                CrossDimensionLodLinks.forTarget(
                                registries, MiaDimensions.THE_ABYSS_LEVEL, template)
                        .orElseThrow()
                        .source());
        assertTrue(
                CrossDimensionLodLinks.forTarget(RegistryAccess.EMPTY, Level.OVERWORLD, template)
                        .isEmpty());
    }

    @Test
    void existingGreatFaultViewRetainsItsMappingAndHasIndependentLinkIdentity() {
        var registries = clientRegistries(type(-256, 640));
        var attributes =
                EnvironmentAttributeMap.builder()
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_ABOVE,
                                new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 8))
                        .build();
        var upper =
                CrossDimensionLodLinks.forTarget(
                                registries,
                                MiaDimensions.GREAT_FAULT_LEVEL,
                                type(-384, 768, attributes))
                        .orElseThrow();
        var lower =
                CrossDimensionLodLinks.forTarget(registries, Level.OVERWORLD, template)
                        .orElseThrow();
        assertEquals(640, upper.displayYOffset());
        assertEquals(MiaDimensions.THE_ABYSS_LEVEL.identifier(), upper.id());
        assertNotEquals(upper.id(), lower.id());
        assertNotEquals(upper, lower);
        assertEquals(upper.source(), lower.source());
    }

    @Test
    void explicitVerticalRulesTakePrecedenceOverOverworldFallback() {
        var attrs =
                EnvironmentAttributeMap.builder()
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_ABOVE,
                                new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 8))
                        .build();
        var target = type(-64, 384, attrs);
        var link =
                CrossDimensionLodLinks.forTarget(
                                clientRegistries(type(-256, 640)), Level.OVERWORLD, target)
                        .orElseThrow();
        assertEquals(
                target.minY() + target.height(),
                link.sourceHeight().minY() + link.displayYOffset());
    }

    @Test
    void serverStemAndClientSyncedTypeResolveToSameOverworldLink() {
        var source = type(-256, 640);
        var stems = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable());
        var generator =
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                vanilla.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),
                        vanilla.lookupOrThrow(Registries.NOISE_SETTINGS)
                                .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        stems.register(
                MiaDimensions.THE_ABYSS,
                new LevelStem(Holder.direct(source), generator),
                RegistrationInfo.BUILT_IN);
        var server = new RegistryAccess.ImmutableRegistryAccess(List.of(stems.freeze()));
        assertEquals(
                CrossDimensionLodLinks.forTarget(server, Level.OVERWORLD, template),
                CrossDimensionLodLinks.forTarget(
                        clientRegistries(source), Level.OVERWORLD, template));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sourceCaptureUpdatesBothViewersWithoutUpdatingUnrelatedSource() throws Exception {
        var registries = clientRegistries(type(-256, 640));
        var lower =
                CrossDimensionLodLinks.forTarget(registries, Level.OVERWORLD, template)
                        .orElseThrow();
        var attrs =
                EnvironmentAttributeMap.builder()
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_ABOVE,
                                new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 8))
                        .build();
        var upper =
                CrossDimensionLodLinks.forTarget(
                                registries, MiaDimensions.GREAT_FAULT_LEVEL, type(-384, 768, attrs))
                        .orElseThrow();
        var unrelated =
                new CrossDimensionLodLink(
                        MiaDimensions.GREAT_FAULT_LEVEL.identifier(),
                        MiaDimensions.GREAT_FAULT_LEVEL,
                        MiaDimensions.THE_ABYSS_LEVEL,
                        lower.sourceHeight(),
                        0);
        var pos = new ChunkPos(3, -4);
        long key = CrossDimensionLodKey.pack(pos.x(), pos.z());
        // Exercise the actual notification fan-out without starting players or GPU rendering.
        var taskType = Class.forName(MiaLodSampler.class.getName() + "$Task");
        var constructor =
                taskType.getDeclaredConstructor(ServerPlayer.class, CrossDimensionLodLink.class);
        constructor.setAccessible(true);
        var demands = taskType.getDeclaredField("demands");
        demands.setAccessible(true);
        var revisions = taskType.getDeclaredField("pendingRevisions");
        revisions.setAccessible(true);
        var availability = taskType.getDeclaredField("availability");
        availability.setAccessible(true);
        var tasksField = MiaLodSampler.class.getDeclaredField("TASKS");
        tasksField.setAccessible(true);
        var tasks = (Map<UUID, Object>) tasksField.get(null);
        var saved = new HashMap<>(tasks);
        var readers = new java.util.ArrayList<Object>();
        try {
            tasks.clear();
            for (var link : List.of(lower, upper, unrelated)) {
                var task = constructor.newInstance(null, link);
                var demand = new HashMap<Long, Object>();
                demand.put(key, null);
                demands.set(task, demand);
                tasks.put(UUID.randomUUID(), task);
                readers.add(task);
            }
            MiaLodSampler.notifyAvailable(lower, pos);
            MiaLodSampler.notifyReplaced(upper, pos, 100);
            for (int i = 0; i < readers.size(); i++) {
                var task = readers.get(i);
                assertEquals(
                        i < 2 ? 2 : 0, ((MiaLodAvailability) availability.get(task)).version(key));
                assertEquals(i < 2 ? 100L : null, ((Map<Long, Long>) revisions.get(task)).get(key));
            }
        } finally {
            tasks.clear();
            tasks.putAll(saved);
        }
    }

    @Test
    void abyssCanViewOverworldAndGreatFaultSimultaneouslyWithoutCentralRangeLimit() {
        var attrs =
                EnvironmentAttributeMap.builder()
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_BELOW,
                                new VerticalBoundary(MiaDimensions.GREAT_FAULT_LEVEL, 8))
                        .build();
        var abyss = type(-256, 640, attrs);
        var links =
                CrossDimensionLodLinks.forTargets(
                        clientRegistries(abyss), MiaDimensions.THE_ABYSS_LEVEL, abyss);
        assertEquals(2, links.size());
        assertEquals(MiaDimensions.GREAT_FAULT_LEVEL, links.getFirst().source());
        var upper =
                links.stream()
                        .filter(link -> link.source().equals(Level.OVERWORLD))
                        .findFirst()
                        .orElseThrow();
        assertEquals(448, upper.displayYOffset());
        assertEquals(384, upper.sourceHeight().minY() + upper.displayYOffset());
        assertEquals(767, upper.sourceHeight().maxY() - 1 + upper.displayYOffset());
        assertNotEquals(links.getFirst().id(), upper.id());
        assertTrue(
                CrossDimensionLodLinks.activeAt(
                        MiaDimensions.THE_ABYSS_LEVEL, attrs, 1000000, -1000000, false, 240));
        assertEquals(
                256,
                com.altnoir.mementoinabyss.infrastructure.worldgen.dimension
                        .VerticalDimensionCoordinates.sourceY(704, upper.displayYOffset()));
    }

    @Test
    void explicitUpperRuleReplacesOnlyImplicitUpperConnection() {
        var attrs =
                EnvironmentAttributeMap.builder()
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_BELOW,
                                new VerticalBoundary(MiaDimensions.GREAT_FAULT_LEVEL, 8))
                        .set(
                                MiaEnvironmentAttributes.VERTICAL_ABOVE,
                                new VerticalBoundary(MiaDimensions.GREAT_FAULT_LEVEL, 8))
                        .build();
        var abyss = type(-256, 640, attrs);
        var links =
                CrossDimensionLodLinks.forTargets(
                        clientRegistries(abyss), MiaDimensions.THE_ABYSS_LEVEL, abyss);
        assertEquals(2, links.size());
        assertTrue(links.stream().noneMatch(link -> link.source().equals(Level.OVERWORLD)));
        assertNotEquals(links.getFirst().id(), links.getLast().id());
    }
}
