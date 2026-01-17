package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.util.MiaUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class MiaPools {
    public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

    public static ResourceKey<StructureTemplatePool> createKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MiaUtil.miaId(name));
    }

    public static void register(BootstrapContext<StructureTemplatePool> context, String name, StructureTemplatePool pool) {
        context.register(createKey(name), pool);
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> holdergetter = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> holder = holdergetter.getOrThrow(EMPTY);
        context.register(EMPTY, new StructureTemplatePool(holder, ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
        AbyssStrongholdPools.bootstrap(context);
    }
}
