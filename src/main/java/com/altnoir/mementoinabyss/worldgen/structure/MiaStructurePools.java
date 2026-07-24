package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MiaStructurePools {
    public static final ResourceKey<StructureTemplatePool> STAR_COMPASS_RUINS =
            key("star_compass_ruins/starts");
    public static final ResourceKey<StructureTemplatePool> ABYSS_STRONGHOLD =
            key("abyss_stronghold/portal_center");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        Holder<StructureTemplatePool> empty = context.lookup(Registries.TEMPLATE_POOL).getOrThrow(Pools.EMPTY);
        Holder<StructureProcessorList> degraded = context.lookup(Registries.PROCESSOR_LIST)
                .getOrThrow(MiaProcessorLists.ABYSS_STRONGHOLD_DEGRADATION);

        context.register(STAR_COMPASS_RUINS, pool(empty,
                weighted("star_compass_ruins/jungle_ruins", 3),
                weighted("star_compass_ruins/jungle_ruins2", 1)));
        context.register(ABYSS_STRONGHOLD, pool(empty,
                weighted("abyss_stronghold/portal_center/portal_room", 1)));
        register(context, empty, "abyss_stronghold/portal_center/treasure_room",
                degraded("abyss_stronghold/portal_center/treasure_room", degraded),
                degraded("abyss_stronghold/portal_center/tnt_treasure_room", degraded));
        register(context, empty, "abyss_stronghold/portal_center/hallway",
                degraded("abyss_stronghold/portal_center/hallway", degraded));
        register(context, empty, "abyss_stronghold/main_hall/hall",
                degraded("abyss_stronghold/main_hall/hall", degraded),
                degraded("abyss_stronghold/main_hall/fighting_room", degraded),
                degraded("abyss_stronghold/main_hall/jumping_room", degraded),
                degraded("abyss_stronghold/main_hall/decryption_room", degraded));
        register(context, empty, "abyss_stronghold/main_hall/fighting_room",
                names("abyss_stronghold/main_hall/fighting_room/",
                        "zombie_spawner", "villager_zombie_spawner", "slime_spawner", "vindicator_spawner",
                        "skeleton_spawner", "witch_spawner", "evoker_spawner", "wither_skeleton_spawner",
                        "carrot_crop", "potato_crop", "wheat_crop", "berries_crop"));
        register(context, empty, "abyss_stronghold/main_hall/decryption_room",
                names("abyss_stronghold/main_hall/decryption_room/",
                        "copper_bulb_right1", "copper_bulb_right2", "copper_bulb_right3", "copper_bulb_right4",
                        "copper_bulb_left1", "copper_bulb_left2", "copper_bulb_left3", "copper_bulb_left4"));
    }

    @SafeVarargs
    private static void register(BootstrapContext<StructureTemplatePool> context,
                                 Holder<StructureTemplatePool> empty, String path,
                                 Pair<Function<StructureTemplatePool.Projection,
                                         ? extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement>, Integer>... elements) {
        context.register(key(path), pool(empty, elements));
    }

    @SafeVarargs
    private static StructureTemplatePool pool(Holder<StructureTemplatePool> empty,
                                               Pair<Function<StructureTemplatePool.Projection,
                                                       ? extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement>, Integer>... elements) {
        return new StructureTemplatePool(empty, List.of(elements), StructureTemplatePool.Projection.RIGID);
    }

    @SuppressWarnings("unchecked")
    private static Pair<Function<StructureTemplatePool.Projection,
            ? extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement>, Integer>[] names(
            String prefix, String... names) {
        return Arrays.stream(names).map(name -> weighted(prefix + name, 1)).toArray(Pair[]::new);
    }

    private static Pair<Function<StructureTemplatePool.Projection,
            ? extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement>, Integer> weighted(
            String path, int weight) {
        return Pair.of(MiaStructurePoolElements.single(path), weight);
    }

    private static Pair<Function<StructureTemplatePool.Projection,
            ? extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement>, Integer> degraded(
            String path, Holder<StructureProcessorList> processors) {
        return Pair.of(MiaStructurePoolElements.single(path, processors), 1);
    }

    private static ResourceKey<StructureTemplatePool> key(String path) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MementoInAbyss.asResource(path));
    }

    private MiaStructurePools() {
    }
}
