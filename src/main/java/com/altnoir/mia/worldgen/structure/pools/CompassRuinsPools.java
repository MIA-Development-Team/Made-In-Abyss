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

public final class CompassRuinsPools {
    public static final ResourceKey<StructureTemplatePool> ANCIENT_BABYLON = createStartKey("ancient_babylon_compass_ruins");
    public static final ResourceKey<StructureTemplatePool> ANCIENT_MAYA = createStartKey("ancient_maya_compass_ruins");
    public static final ResourceKey<StructureTemplatePool> ANCIENT_ROMAN = createStartKey("ancient_roman_compass_ruins");
    public static final ResourceKey<StructureTemplatePool> ANCIENT_TRIAL = createStartKey("ancient_trial_compass_ruins");
    public static final ResourceKey<StructureTemplatePool> ANCIENT_ANGKOR = createStartKey("ancient_angkor_compass_ruins");

    private CompassRuinsPools() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        register(context, empty, ANCIENT_BABYLON, "ancient_babylon_compass_ruins");
        register(context, empty, ANCIENT_MAYA, "ancient_maya_compass_ruins");
        register(context, empty, ANCIENT_ROMAN, "ancient_roman_compass_ruins");
        register(context, empty, ANCIENT_TRIAL, "ancient_trial_compass_ruins");
        register(context, empty, ANCIENT_ANGKOR, "ancient_angkor_compass_ruins");
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
