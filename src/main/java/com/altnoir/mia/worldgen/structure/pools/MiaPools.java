package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class MiaPools {
    public static ResourceKey<StructureTemplatePool> createKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MiaUtil.miaId(name));
    }

    public static void register(BootstrapContext<StructureTemplatePool> context, String name, StructureTemplatePool pool) {
        context.register(createKey(name), pool);
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        StarCompassRuinsPools.bootstrap(context);
        AbyssStrongholdPools.bootstrap(context);
    }
}
