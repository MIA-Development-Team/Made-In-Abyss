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

public final class PetrifiedShipPools {
    public static final ResourceKey<StructureTemplatePool> MAIN = MiaPools.createKey("petrified_ship/main");
    public static final ResourceKey<StructureTemplatePool> BRANCH = MiaPools.createKey("petrified_ship/branch");
    public static final ResourceKey<StructureTemplatePool> START = MiaPools.createKey("petrified_ship/start");
    public static final ResourceKey<StructureTemplatePool> CONNECT_LEFT = MiaPools.createKey("petrified_ship/connect_left");
    public static final ResourceKey<StructureTemplatePool> CONNECT_RIGHT = MiaPools.createKey("petrified_ship/connect_right");
    public static final ResourceKey<StructureTemplatePool> LEFT_OR_RIGHT = MiaPools.createKey("petrified_ship/left_or_right");

    private PetrifiedShipPools() {
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

        context.register(MAIN, pool(empty, List.of(
                piece("petrified_ship_body_small", 1),
                piece("petrified_ship_body_big", 1)
        )));
        context.register(LEFT_OR_RIGHT, pool(empty, List.of(
                piece("petrified_ship_left", 1),
                piece("petrified_ship_right", 1)
        )));

        Holder<StructureTemplatePool> main = pools.getOrThrow(MAIN);
        Holder<StructureTemplatePool> leftOrRight = pools.getOrThrow(LEFT_OR_RIGHT);

        context.register(START, pool(main, List.of(
                piece("petrified_ship_body_small", 1),
                piece("petrified_ship_body_big", 1)
        )));
        context.register(CONNECT_LEFT, pool(leftOrRight, List.of(
                piece("petrified_ship_body_small", 1),
                piece("petrified_ship_body_big", 1),
                piece("petrified_ship_right", 5),
                emptyPiece(1)
        )));
        context.register(CONNECT_RIGHT, pool(leftOrRight, List.of(
                piece("petrified_ship_body_small", 1),
                piece("petrified_ship_body_big", 1),
                piece("petrified_ship_left", 5),
                emptyPiece(1)
        )));
        context.register(BRANCH, pool(empty, List.of(
                piece("petrified_ship_part_1", 1),
                piece("petrified_ship_part_2", 1),
                piece("petrified_ship_part_3", 1),
                piece("petrified_ship_part_4", 1),
                piece("petrified_ship_part_5", 1)
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
