package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

public final class MiaProcessorLists {
    public static final ResourceKey<StructureProcessorList> ABYSS_STRONGHOLD_DEGRADATION =
            ResourceKey.create(
                    Registries.PROCESSOR_LIST,
                    MementoInAbyss.asResource("abyss_stronghold_generic_degradation"));

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        context.register(
                ABYSS_STRONGHOLD_DEGRADATION,
                new StructureProcessorList(
                        List.of(
                                new RuleProcessor(
                                        List.of(
                                                new ProcessorRule(
                                                        new RandomBlockMatchTest(
                                                                Blocks.POLISHED_TUFF, 0.25F),
                                                        AlwaysTrueTest.INSTANCE,
                                                        Blocks.TUFF.defaultBlockState()),
                                                new ProcessorRule(
                                                        new RandomBlockMatchTest(
                                                                Blocks.WHITE_CANDLE, 0.05F),
                                                        AlwaysTrueTest.INSTANCE,
                                                        Blocks.AIR.defaultBlockState()))),
                                new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))));
    }

    private MiaProcessorLists() {}
}
