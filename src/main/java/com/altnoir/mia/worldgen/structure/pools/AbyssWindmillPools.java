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

public final class AbyssWindmillPools {
    public static final ResourceKey<StructureTemplatePool> STRAIGHT = MiaPools.createKey("abyss_windmill/straight");
    public static final ResourceKey<StructureTemplatePool> TILT = MiaPools.createKey("abyss_windmill/tilt");

    private AbyssWindmillPools() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);
        context.register(STRAIGHT, new StructureTemplatePool(
                empty,
                ImmutableList.of(Pair.of(MiaSructurePoolUtils.single("abyss_windmill_straight"), 1)),
                StructureTemplatePool.Projection.RIGID
        ));
        context.register(TILT, new StructureTemplatePool(
                empty,
                ImmutableList.of(Pair.of(MiaSructurePoolUtils.single("abyss_windmill_tilt"), 1)),
                StructureTemplatePool.Projection.RIGID
        ));
    }
}
