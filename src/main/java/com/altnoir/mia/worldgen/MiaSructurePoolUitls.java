package com.altnoir.mia.worldgen;

import com.altnoir.mia.util.MiaUtil;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MiaSructurePoolUitls extends SinglePoolElement {
    private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final Holder<StructureProcessorList> processors;
    protected final Optional<LiquidSettings> overrideLiquidSettings;

    public MiaSructurePoolUitls(
            Either<ResourceLocation, StructureTemplate> template,
            Holder<StructureProcessorList> processors,
            StructureTemplatePool.Projection projection,
            Optional<LiquidSettings> overrideLiquidSettings
    ) {
        super(template, processors, projection, overrideLiquidSettings);
        this.template = template;
        this.processors = processors;
        this.overrideLiquidSettings = overrideLiquidSettings;
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id) {
        return projection -> new MiaSructurePoolUitls(Either.left(MiaUtil.miaId(id)), EMPTY, projection, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id, Holder<StructureProcessorList> processors) {
        return projection -> new MiaSructurePoolUitls(Either.left(MiaUtil.miaId(id)), processors, projection, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id, LiquidSettings liquidSettings) {
        return projection -> new MiaSructurePoolUitls(Either.left(MiaUtil.miaId(id)), EMPTY, projection, Optional.of(liquidSettings));
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(
            String id, Holder<StructureProcessorList> processors, LiquidSettings liquidSettings
    ) {
        return projection -> new MiaSructurePoolUitls(Either.left(MiaUtil.miaId(id)), processors, projection, Optional.of(liquidSettings));
    }
}
