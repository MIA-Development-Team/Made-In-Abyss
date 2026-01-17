package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.worldgen.MiaSructurePoolUitls;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class AbyssStrongholdPools {
    public static final ResourceKey<StructureTemplatePool> START = MiaPools.createKey("abyss_stronghold/portal_center");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> holdergetter1 = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> holder1 = holdergetter1.getOrThrow(MiaPools.EMPTY);
        context.register(
                START,
                new StructureTemplatePool(
                        holder1,
                        ImmutableList.of(
//                                Pair.of(StructurePoolElement.single("abyss_stronghold/portal_center/portal_room_1", holder), 1),
//                                Pair.of(StructurePoolElement.single("abyss_stronghold/portal_center/portal_room_2", holder), 1),
                                Pair.of(MiaSructurePoolUitls.single("abyss_stronghold/portal_center/portal_room"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
        //AncientCityStructurePools.bootstrap(context);
    }
}
