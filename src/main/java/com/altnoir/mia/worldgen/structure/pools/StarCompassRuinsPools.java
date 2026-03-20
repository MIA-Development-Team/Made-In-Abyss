package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.worldgen.MiaSructurePoolUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class StarCompassRuinsPools {
    public static final ResourceKey<StructureTemplatePool> START = MiaPools.createKey("star_compass_ruins/starts");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> holdergetter1 = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> holder1 = holdergetter1.getOrThrow(Pools.EMPTY);
        context.register(
                START,
                new StructureTemplatePool(
                        holder1,
                        ImmutableList.of(
                                Pair.of(MiaSructurePoolUtils.single("star_compass_ruins/jungle_ruins"), 3),
                                Pair.of(MiaSructurePoolUtils.single("star_compass_ruins/jungle_ruins2"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
    }
}
