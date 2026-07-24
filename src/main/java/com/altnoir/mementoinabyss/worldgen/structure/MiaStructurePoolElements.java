package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class MiaStructurePoolElements extends SinglePoolElement {
    private static final Holder<StructureProcessorList> EMPTY =
            Holder.direct(new StructureProcessorList(List.of()));

    private MiaStructurePoolElements(String path, Holder<StructureProcessorList> processors,
                                     StructureTemplatePool.Projection projection) {
        super(Either.left(MementoInAbyss.asResource(path)), processors, projection,
                Optional.<LiquidSettings>empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String path) {
        return projection -> new MiaStructurePoolElements(path, EMPTY, projection);
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(
            String path, Holder<StructureProcessorList> processors) {
        return projection -> new MiaStructurePoolElements(path, processors, projection);
    }
}
