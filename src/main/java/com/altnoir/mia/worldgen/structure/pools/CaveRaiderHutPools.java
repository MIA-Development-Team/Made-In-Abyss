package com.altnoir.mia.worldgen.structure.pools;

import com.altnoir.mia.worldgen.MiaSructurePoolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.function.Function;

public final class CaveRaiderHutPools {
    public static final ResourceKey<StructureTemplatePool> START =
            MiaPools.createKey("cave_raider_hut/start");
    public static final ResourceKey<StructureTemplatePool> BOTTOM =
            MiaPools.createKey("cave_raider_hut/bottom");

    private CaveRaiderHutPools() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        context.register(START, pool(empty, List.of(
                piece("cave_raider_hut_top", 1)
        )));
        context.register(BOTTOM, pool(empty, List.of(
                piece("cave_raider_hut_bottom", 1),
                emptyPiece(1)
        )));
    }

    private static Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> piece(
            String template,
            int weight
    ) {
        return Pair.of(MiaSructurePoolUtils.single(template), weight);
    }

    private static Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> emptyPiece(
            int weight
    ) {
        return Pair.of(StructurePoolElement.empty(), weight);
    }

    private static StructureTemplatePool pool(
            Holder<StructureTemplatePool> fallback,
            List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> pieces
    ) {
        return new StructureTemplatePool(fallback, pieces, StructureTemplatePool.Projection.RIGID);
    }
}
