package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.worldgen.MiaSructurePoolUtils;
import com.altnoir.mia.worldgen.processor_list.MiaProcessorLists;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.List;

public class AbyssStrongholdPools {
    public static final ResourceKey<StructureTemplatePool> START = MiaPools.createKey("abyss_stronghold/portal_center");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> holdergetter = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> holder = holdergetter.getOrThrow(Pools.EMPTY);
        HolderGetter<StructureProcessorList> processorList = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> polished_tuff = processorList.getOrThrow(MiaProcessorLists.ABYSS_STRONGHOLD_GENERIC_DEGRADATION);
        context.register(
                START,
                new StructureTemplatePool(
                        holder,
                        ImmutableList.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/portal_center/portal_room"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );

        MiaPools.register(
                context,
                "abyss_stronghold/portal_center/treasure_room",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/portal_center/treasure_room", polished_tuff), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/portal_center/tnt_treasure_room", polished_tuff), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        MiaPools.register(
                context,
                "abyss_stronghold/portal_center/hallway",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/portal_center/hallway", polished_tuff), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        MiaPools.register(
                context,
                "abyss_stronghold/main_hall/hall",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/hall", polished_tuff), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room", polished_tuff), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room", polished_tuff), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        MiaPools.register(
                context,
                "abyss_stronghold/main_hall/jumping_room",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/jumping_room", polished_tuff), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        MiaPools.register(
                context,
                "abyss_stronghold/main_hall/fighting_room",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/zombie_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/villager_zombie_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/slime_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/vindicator_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/skeleton_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/witch_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/evoker_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/wither_skeleton_spawner"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/carrot_crop"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/potato_crop"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/wheat_crop"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/fighting_room/berries_crop"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        MiaPools.register(
                context,
                "abyss_stronghold/main_hall/decryption_room",
                new StructureTemplatePool(
                        holder,
                        List.of(
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_right1"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_right2"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_right3"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_right4"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_left1"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_left2"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_left3"), 1),
                                Pair.of(MiaSructurePoolUtils.single("abyss_stronghold/main_hall/decryption_room/copper_bulb_left4"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
    }
}
