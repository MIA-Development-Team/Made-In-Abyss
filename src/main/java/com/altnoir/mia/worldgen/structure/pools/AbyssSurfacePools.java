package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.worldgen.MiaSructurePoolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;

public final class AbyssSurfacePools {
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_01 = createStartKey("abyssal_ruins_01");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_02 = createStartKey("abyssal_ruins_02");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_03 = createStartKey("abyssal_ruins_03");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_04 = createStartKey("abyssal_ruins_04");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_05 = createStartKey("abyssal_ruins_05");
    public static final ResourceKey<StructureTemplatePool> ABYSSAL_RUINS_06 = createStartKey("abyssal_ruins_06");
    public static final ResourceKey<StructureTemplatePool> RUINED_CAVE_RAIDER_HUT =
            createStartKey("ruined_cave_raider_hut");
    public static final ResourceKey<StructureTemplatePool> FISHERMAN_HUT = createStartKey("fisherman_hut");

    private AbyssSurfacePools() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        register(context, empty, ABYSSAL_RUINS_01, "abyssal_ruins_01");
        register(context, empty, ABYSSAL_RUINS_02, "abyssal_ruins_02");
        register(context, empty, ABYSSAL_RUINS_03, "abyssal_ruins_03");
        register(context, empty, ABYSSAL_RUINS_04, "abyssal_ruins_04");
        register(context, empty, ABYSSAL_RUINS_05, "abyssal_ruins_05");
        register(context, empty, ABYSSAL_RUINS_06, "abyssal_ruins_06");
        register(context, empty, RUINED_CAVE_RAIDER_HUT, "ruined_cave_raider_hut");
        register(context, empty, FISHERMAN_HUT, "fisherman_hut");
    }

    private static ResourceKey<StructureTemplatePool> createStartKey(String structureName) {
        return MiaPools.createKey(structureName + "/starts");
    }

    private static void register(
            BootstrapContext<StructureTemplatePool> context,
            Holder<StructureTemplatePool> empty,
            ResourceKey<StructureTemplatePool> key,
            String templateName
    ) {
        context.register(
                key,
                new StructureTemplatePool(
                        empty,
                        List.of(Pair.of(MiaSructurePoolUtils.single(templateName), 1)),
                        StructureTemplatePool.Projection.RIGID
                )
        );
    }
}
