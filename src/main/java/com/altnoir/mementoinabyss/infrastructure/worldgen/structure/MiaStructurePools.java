package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class MiaStructurePools {
    public static final ResourceKey<StructureTemplatePool> STAR_COMPASS_RUINS =
            key("star_compass_ruins/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSS_STRONGHOLD =
            key("abyss_stronghold/portal_center");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_01 =
            key("abyssal_ruins_01/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_02 =
            key("abyssal_ruins_02/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_03 =
            key("abyssal_ruins_03/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_04 =
            key("abyssal_ruins_04/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_05 =
            key("abyssal_ruins_05/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_06 =
            key("abyssal_ruins_06/starts");
    public static final ResourceKey<StructureTemplatePool> CAVE_RAIDER_HUT_START =
            key("cave_raider_hut/start");
    public static final ResourceKey<StructureTemplatePool> CAVE_RAIDER_HUT_BOTTOM =
            key("cave_raider_hut/bottom");
    public static final ResourceKey<StructureTemplatePool> RUINED_CAVE_RAIDER_HUT =
            key("ruined_cave_raider_hut/starts");
    public static final ResourceKey<StructureTemplatePool> FISHERMAN_HUT =
            key("fisherman_hut/starts");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        Holder<StructureTemplatePool> empty =
                context.lookup(Registries.TEMPLATE_POOL).getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> degraded =
                context.lookup(Registries.PROCESSOR_LIST)
                        .getOrThrow(MiaProcessorLists.ABYSS_STRONGHOLD_DEGRADATION);

        context.register(
                STAR_COMPASS_RUINS,
                pool(
                        empty,
                        weighted("star_compass_ruins/jungle_ruins", 3),
                        weighted("star_compass_ruins/jungle_ruins2", 1)));
        context.register(
                ABYSS_STRONGHOLD,
                pool(empty, weighted("abyss_stronghold/portal_center/portal_room", 1)));
        register(
                context,
                empty,
                "abyss_stronghold/portal_center/treasure_room",
                degraded("abyss_stronghold/portal_center/treasure_room", degraded),
                degraded("abyss_stronghold/portal_center/tnt_treasure_room", degraded));
        register(
                context,
                empty,
                "abyss_stronghold/portal_center/hallway",
                degraded("abyss_stronghold/portal_center/hallway", degraded));
        register(
                context,
                empty,
                "abyss_stronghold/main_hall/hall",
                degraded("abyss_stronghold/main_hall/hall", degraded),
                degraded("abyss_stronghold/main_hall/fighting_room", degraded),
                degraded("abyss_stronghold/main_hall/jumping_room", degraded),
                degraded("abyss_stronghold/main_hall/decryption_room", degraded));
        register(
                context,
                empty,
                "abyss_stronghold/main_hall/fighting_room",
                names(
                        "abyss_stronghold/main_hall/fighting_room/",
                        "zombie_spawner",
                        "villager_zombie_spawner",
                        "slime_spawner",
                        "vindicator_spawner",
                        "skeleton_spawner",
                        "witch_spawner",
                        "evoker_spawner",
                        "wither_skeleton_spawner",
                        "carrot_crop",
                        "potato_crop",
                        "wheat_crop",
                        "berries_crop"));
        register(
                context,
                empty,
                "abyss_stronghold/main_hall/decryption_room",
                names(
                        "abyss_stronghold/main_hall/decryption_room/",
                        "copper_bulb_right1",
                        "copper_bulb_right2",
                        "copper_bulb_right3",
                        "copper_bulb_right4",
                        "copper_bulb_left1",
                        "copper_bulb_left2",
                        "copper_bulb_left3",
                        "copper_bulb_left4"));

        register(context, empty, ABYSSAL_RUINS_01, "abyssal_ruins_01");
        register(context, empty, ABYSSAL_RUINS_02, "abyssal_ruins_02");
        register(context, empty, ABYSSAL_RUINS_03, "abyssal_ruins_03");
        register(context, empty, ABYSSAL_RUINS_04, "abyssal_ruins_04");
        register(context, empty, ABYSSAL_RUINS_05, "abyssal_ruins_05");
        register(context, empty, ABYSSAL_RUINS_06, "abyssal_ruins_06");
        register(context, empty, RUINED_CAVE_RAIDER_HUT, "ruined_cave_raider_hut");
        register(context, empty, FISHERMAN_HUT, "fisherman_hut");
        context.register(CAVE_RAIDER_HUT_START, pool(empty, weighted("cave_raider_hut_top", 1)));
        context.register(
                CAVE_RAIDER_HUT_BOTTOM,
                pool(
                        empty,
                        weighted("cave_raider_hut_bottom", 1),
                        Pair.of(StructurePoolElement.empty(), 1)));
    }

    private static void register(
            BootstrapContext<StructureTemplatePool> context,
            Holder<StructureTemplatePool> empty,
            ResourceKey<StructureTemplatePool> poolKey,
            String templateName) {
        context.register(poolKey, pool(empty, weighted(templateName, 1)));
    }

    @SafeVarargs
    private static void register(
            BootstrapContext<StructureTemplatePool> context,
            Holder<StructureTemplatePool> empty,
            String path,
            Pair<
                            Function<
                                    StructureTemplatePool.Projection,
                                    ? extends
                                            net.minecraft.world.level.levelgen.structure.pools
                                                    .StructurePoolElement>,
                            Integer>...
                    elements) {
        context.register(key(path), pool(empty, elements));
    }

    @SafeVarargs
    private static StructureTemplatePool pool(
            Holder<StructureTemplatePool> empty,
            Pair<
                            Function<
                                    StructureTemplatePool.Projection,
                                    ? extends
                                            net.minecraft.world.level.levelgen.structure.pools
                                                    .StructurePoolElement>,
                            Integer>...
                    elements) {
        return new StructureTemplatePool(
                empty, List.of(elements), StructureTemplatePool.Projection.RIGID);
    }

    @SuppressWarnings("unchecked")
    private static Pair<
                    Function<
                            StructureTemplatePool.Projection,
                            ? extends
                                    net.minecraft.world.level.levelgen.structure.pools
                                            .StructurePoolElement>,
                    Integer>
            []
            names(String prefix, String... names) {
        return Arrays.stream(names).map(name -> weighted(prefix + name, 1)).toArray(Pair[]::new);
    }

    private static Pair<
                    Function<
                            StructureTemplatePool.Projection,
                            ? extends
                                    net.minecraft.world.level.levelgen.structure.pools
                                            .StructurePoolElement>,
                    Integer>
            weighted(String path, int weight) {
        return Pair.of(MiaStructurePoolElements.single(path), weight);
    }

    private static Pair<
                    Function<
                            StructureTemplatePool.Projection,
                            ? extends
                                    net.minecraft.world.level.levelgen.structure.pools
                                            .StructurePoolElement>,
                    Integer>
            degraded(String path, Holder<StructureProcessorList> processors) {
        return Pair.of(MiaStructurePoolElements.single(path, processors), 1);
    }

    private static ResourceKey<StructureTemplatePool> key(String path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MementoInAbyss.asResource(path));
    }

    private MiaStructurePools() {}
}
